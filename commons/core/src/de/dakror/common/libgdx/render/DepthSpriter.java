/*******************************************************************************
 * Copyright 2017 Maximilian Stark | Dakror <mail@dakror.de>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package de.dakror.common.libgdx.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;

/**
 * @author Maximilian Stark | Dakror
 */
public class DepthSpriter implements SpriteRenderer {
    private final float[] vertices;
    private int idx;
    protected Mesh staticMesh, dynMesh;
    protected ShaderProgram shader;
    protected Texture texture;

    protected int staticCount;

    protected boolean dynamic;
    protected boolean drawing;
    protected boolean caching;

    protected Matrix4 projectionMatrix;

    public static final int VERTEX_SIZE = 5;

    public DepthSpriter(Texture atlas, int size) {
        texture = atlas;
        staticMesh = new Mesh(false, size * 4, size * 6,
                new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
                new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));

        dynMesh = new Mesh(false, size * 4, size * 6,
                new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
                new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));

        vertices = new float[size * 4 * VERTEX_SIZE];

        // create index array, ignoring the possible overflow (stackoverflow told me so) for > 32767 vertices
        int length = size * 6;
        short[] indices = new short[length];
        short j = 0;
        for (int i = 0; i < length; i += 6, j += 4) {
            indices[i + 0] = j;
            indices[i + 1] = (short) (j + 1);
            indices[i + 2] = (short) (j + 2);
            indices[i + 3] = (short) (j + 2);
            indices[i + 4] = (short) (j + 3);
            indices[i + 5] = j;
        }

        staticMesh.setIndices(indices);
        dynMesh.setIndices(indices);

        shader = new ShaderProgram(Gdx.files.internal("glsl/base.vs"), Gdx.files.internal("glsl/base.fs"));
        if (shader.isCompiled() == false)
            throw new IllegalArgumentException("Error compiling shader: " + shader.getLog());
    }

    @Override
    public ShaderProgram getShader() {
        return shader;
    }

    public int getIdx() {
        return idx;
    }

    public int inc() {
        return idx++;
    }

    public float[] getVertices() {
        return vertices;
    }

    public int getVertexCount() {
        return vertices.length;
    }

    @Override
    public void add(TextureRegion region, float x, float y, float z, float width, float height) {
        if (idx + 4 * VERTEX_SIZE >= vertices.length)
            flush();
        final float fx2 = x + width;
        final float fy2 = y + height;
        final float u = region.getU();
        final float v = region.getV2();
        final float u2 = region.getU2();
        final float v2 = region.getV();

        vertices[idx++] = x;
        vertices[idx++] = y;
        vertices[idx++] = z;
        vertices[idx++] = u;
        vertices[idx++] = v;

        vertices[idx++] = x;
        vertices[idx++] = fy2;
        vertices[idx++] = z;
        vertices[idx++] = u;
        vertices[idx++] = v2;

        vertices[idx++] = fx2;
        vertices[idx++] = fy2;
        vertices[idx++] = z;
        vertices[idx++] = u2;
        vertices[idx++] = v2;

        vertices[idx++] = fx2;
        vertices[idx++] = y;
        vertices[idx++] = z;
        vertices[idx++] = u2;
        vertices[idx++] = v;
    }

    @Override
    public void add(TextureRegion region, float x, float y, float z, float originX, float originY, float width, float height,
            float scaleX, float scaleY, float rotation) {
        if (idx + 4 * VERTEX_SIZE >= vertices.length)
            flush();
        // bottom left and top right corner points relative to origin
        final float worldOriginX = x + originX;
        final float worldOriginY = y + originY;
        float fx = -originX;
        float fy = -originY;
        float fx2 = width - originX;
        float fy2 = height - originY;

        // scale
        if (scaleX != 1 || scaleY != 1) {
            fx *= scaleX;
            fy *= scaleY;
            fx2 *= scaleX;
            fy2 *= scaleY;
        }

        // construct corner points, start from top left and go counter clockwise
        final float p1x = fx;
        final float p1y = fy;
        final float p2x = fx;
        final float p2y = fy2;
        final float p3x = fx2;
        final float p3y = fy2;
        final float p4x = fx2;
        final float p4y = fy;

        float x1;
        float y1;
        float x2;
        float y2;
        float x3;
        float y3;
        float x4;
        float y4;

        // rotate
        if (rotation != 0) {
            final float cos = MathUtils.cosDeg(rotation);
            final float sin = MathUtils.sinDeg(rotation);

            x1 = cos * p1x - sin * p1y;
            y1 = sin * p1x + cos * p1y;

            x2 = cos * p2x - sin * p2y;
            y2 = sin * p2x + cos * p2y;

            x3 = cos * p3x - sin * p3y;
            y3 = sin * p3x + cos * p3y;

            x4 = x1 + (x3 - x2);
            y4 = y3 - (y2 - y1);
        } else {
            x1 = p1x;
            y1 = p1y;

            x2 = p2x;
            y2 = p2y;

            x3 = p3x;
            y3 = p3y;

            x4 = p4x;
            y4 = p4y;
        }

        x1 += worldOriginX;
        y1 += worldOriginY;
        x2 += worldOriginX;
        y2 += worldOriginY;
        x3 += worldOriginX;
        y3 += worldOriginY;
        x4 += worldOriginX;
        y4 += worldOriginY;

        final float u = region.getU();
        final float v = region.getV2();
        final float u2 = region.getU2();
        final float v2 = region.getV();

        vertices[idx++] = x1;
        vertices[idx++] = y1;
        vertices[idx++] = z;
        vertices[idx++] = u;
        vertices[idx++] = v;

        vertices[idx++] = x2;
        vertices[idx++] = y2;
        vertices[idx++] = z;
        vertices[idx++] = u;
        vertices[idx++] = v2;

        vertices[idx++] = x3;
        vertices[idx++] = y3;
        vertices[idx++] = z;
        vertices[idx++] = u2;
        vertices[idx++] = v2;

        vertices[idx++] = x4;
        vertices[idx++] = y4;
        vertices[idx++] = z;
        vertices[idx++] = u2;
        vertices[idx++] = v;
    }

    @Override
    public void add(float x, float y, float z, float originX, float originY, float width, float height, float scaleX,
            float scaleY, float rotation, float srcX, float srcY, float srcWidth, float srcHeight, boolean flipX, boolean flipY) {
        if (idx + 4 * VERTEX_SIZE >= vertices.length)
            flush();
        // bottom left and top right corner points relative to origin
        final float worldOriginX = x + originX;
        final float worldOriginY = y + originY;
        float fx = -originX;
        float fy = -originY;
        float fx2 = width - originX;
        float fy2 = height - originY;

        // scale
        if (scaleX != 1 || scaleY != 1) {
            fx *= scaleX;
            fy *= scaleY;
            fx2 *= scaleX;
            fy2 *= scaleY;
        }

        // construct corner points, start from top left and go counter clockwise
        final float p1x = fx;
        final float p1y = fy;
        final float p2x = fx;
        final float p2y = fy2;
        final float p3x = fx2;
        final float p3y = fy2;
        final float p4x = fx2;
        final float p4y = fy;

        float x1;
        float y1;
        float x2;
        float y2;
        float x3;
        float y3;
        float x4;
        float y4;

        // rotate
        if (rotation != 0) {
            final float cos = MathUtils.cosDeg(rotation);
            final float sin = MathUtils.sinDeg(rotation);

            x1 = cos * p1x - sin * p1y;
            y1 = sin * p1x + cos * p1y;

            x2 = cos * p2x - sin * p2y;
            y2 = sin * p2x + cos * p2y;

            x3 = cos * p3x - sin * p3y;
            y3 = sin * p3x + cos * p3y;

            x4 = x1 + (x3 - x2);
            y4 = y3 - (y2 - y1);
        } else {
            x1 = p1x;
            y1 = p1y;

            x2 = p2x;
            y2 = p2y;

            x3 = p3x;
            y3 = p3y;

            x4 = p4x;
            y4 = p4y;
        }

        x1 += worldOriginX;
        y1 += worldOriginY;
        x2 += worldOriginX;
        y2 += worldOriginY;
        x3 += worldOriginX;
        y3 += worldOriginY;
        x4 += worldOriginX;
        y4 += worldOriginY;

        float invTexWidth = 1.0f / texture.getWidth();
        float invTexHeight = 1.0f / texture.getHeight();
        float u = srcX * invTexWidth;
        float v = (srcY + srcHeight) * invTexHeight;
        float u2 = (srcX + srcWidth) * invTexWidth;
        float v2 = srcY * invTexHeight;

        if (flipX) {
            float tmp = u;
            u = u2;
            u2 = tmp;
        }

        if (flipY) {
            float tmp = v;
            v = v2;
            v2 = tmp;
        }

        vertices[idx++] = x1;
        vertices[idx++] = y1;
        vertices[idx++] = z;
        vertices[idx++] = u;
        vertices[idx++] = v;

        vertices[idx++] = x2;
        vertices[idx++] = y2;
        vertices[idx++] = z;
        vertices[idx++] = u;
        vertices[idx++] = v2;

        vertices[idx++] = x3;
        vertices[idx++] = y3;
        vertices[idx++] = z;
        vertices[idx++] = u2;
        vertices[idx++] = v2;

        vertices[idx++] = x4;
        vertices[idx++] = y4;
        vertices[idx++] = z;
        vertices[idx++] = u2;
        vertices[idx++] = v;
    }

    @Override
    public void add(float x, float y, float z, float width, float height, float u, float v, float u2, float v2) {
        if (idx + 4 * VERTEX_SIZE >= vertices.length)
            flush();
        final float fx2 = x + width;
        final float fy2 = y + height;
        vertices[idx++] = x;
        vertices[idx++] = y;
        vertices[idx++] = z;
        vertices[idx++] = u;
        vertices[idx++] = v;

        vertices[idx++] = x;
        vertices[idx++] = fy2;
        vertices[idx++] = z;
        vertices[idx++] = u;
        vertices[idx++] = v2;

        vertices[idx++] = fx2;
        vertices[idx++] = fy2;
        vertices[idx++] = z;
        vertices[idx++] = u2;
        vertices[idx++] = v2;

        vertices[idx++] = fx2;
        vertices[idx++] = y;
        vertices[idx++] = z;
        vertices[idx++] = u2;
        vertices[idx++] = v;
    }

    public void beginCache() {
        if (caching)
            throw new IllegalStateException("endCache must be called before beginCache.");
        idx = 0;
        caching = true;
    }

    public void endCache() {
        caching = false;

        int spritesInBatch = idx / (4 * VERTEX_SIZE);
        int count = spritesInBatch * 6;

        staticMesh.setVertices(vertices, 0, idx);
        staticMesh.getIndicesBuffer().position(0);
        staticMesh.getIndicesBuffer().limit(count);

        staticCount = count;
    }

    public void clear() {
        idx = 0;
    }

    public void setProjectionMatrix(Matrix4 proj) {
        projectionMatrix = proj;
    }

    public void begin(boolean dynamic) {
        if (drawing)
            throw new IllegalStateException("end must be called before begin.");

        this.dynamic = dynamic;

        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

        shader.begin();
        shader.setUniformMatrix("u_projectionViewMatrix", projectionMatrix);
        shader.setUniformi("u_texture", 0);

        if (dynamic)
            idx = 0;

        texture.bind();

        drawing = true;
    }

    @Override
    public void flush() {
        boolean caching = this.caching;
        if (caching) {
            endCache();
            begin(false);
        }
        draw();
        clear();
        if (caching) {
            end();
            beginCache();
        }
    }

    public void draw() {
        if (!drawing)
            throw new IllegalStateException("begin must be called before draw.");
        if (dynamic) {
            int spritesInBatch = idx / (4 * VERTEX_SIZE);
            int count = spritesInBatch * 6;

            dynMesh.setVertices(vertices, 0, idx);
            dynMesh.getIndicesBuffer().position(0);
            dynMesh.getIndicesBuffer().limit(count);

            dynMesh.render(shader, GL20.GL_TRIANGLES, 0, count);
        } else {
            staticMesh.render(shader, GL20.GL_TRIANGLES, 0, staticCount);
        }
    }

    public void end() {
        if (!drawing)
            throw new IllegalStateException("begin must be called before end.");

        drawing = false;
        shader.end();

        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
    }

    @Override
    public void dispose() {
        staticMesh.dispose();
        shader.dispose();
    }
}
