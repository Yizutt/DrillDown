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

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;

/**
 * @author Maximilian Stark | Dakror
 */
public class MeshBuilderDelegate implements SpriteRenderer {
    private static final VertexInfo vertTmp1 = new VertexInfo();
    private static final VertexInfo vertTmp2 = new VertexInfo();
    private static final VertexInfo vertTmp3 = new VertexInfo();
    private static final VertexInfo vertTmp4 = new VertexInfo();
    private static final MeshBuilder builder = new MeshBuilder();

    private Mesh mesh;
    private VertexAttributes attributes;
    private Texture texture;

    public MeshBuilderDelegate(VertexAttributes attributes, Texture texture) {
        this.attributes = attributes;
        this.texture = texture;
    }

    public Mesh getMesh() {
        return mesh;
    }

    public void begin() {
        if (mesh != null) {
            mesh.dispose();
            mesh = null;
        }
        builder.begin(attributes, GL20.GL_TRIANGLES);
    }

    public void end() {
        mesh = builder.end();
    }

    private VertexInfo set(VertexInfo i, float x, float y, float z, float u, float v) {
        return i.set(null, null, null, null).setPos(x, y, z).setUV(u, v);
    }

    @Override
    public void add(TextureRegion region, float x, float y, float z, float width, float height) {
        final float fx2 = x + width;
        final float fy2 = y + height;
        final float u = region.getU();
        final float v = region.getV2();
        final float u2 = region.getU2();
        final float v2 = region.getV();

        builder.rect(set(vertTmp1, x, y, z, u, v), set(vertTmp2, x, fy2, z, u, v2), set(vertTmp3, fx2, fy2, z, u2, v2), set(vertTmp4, fx2, y, z, u2, v));
    }

    @Override
    public void add(TextureRegion region, float x, float y, float z, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation) {
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

        builder.rect(set(vertTmp1, x1, y1, z, u, v), set(vertTmp2, x2, y2, z, u, v2), set(vertTmp3, x3, y3, z, u2, v2), set(vertTmp4, x4, y4, z, u2, v));
    }

    @Override
    public void add(float x, float y, float z, float width, float height, float u, float v, float u2, float v2) {
        final float fx2 = x + width;
        final float fy2 = y + height;
        builder.rect(set(vertTmp1, x, y, z, u, v), set(vertTmp2, x, fy2, z, u, v2), set(vertTmp3, fx2, fy2, z, u2, v2), set(vertTmp4, fx2, y, z, u2, v));
    }

    @Override
    public void add(float x, float y, float z, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation, float srcX, float srcY, float srcWidth, float srcHeight, boolean flipX, boolean flipY) {
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

        builder.rect(set(vertTmp1, x1, y1, z, u, v), set(vertTmp2, x2, y2, z, u, v2), set(vertTmp3, x3, y3, z, u2, v2), set(vertTmp4, x4, y4, z, u2, v));
    }

    @Override
    public void flush() {}

    @Override
    public ShaderProgram getShader() {
        return null;
    }

    @Override
    public void dispose() {
        if (mesh != null) {
            mesh.dispose();
            mesh = null;
        }
    }
}
