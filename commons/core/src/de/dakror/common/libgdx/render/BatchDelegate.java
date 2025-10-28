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

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * @author Maximilian Stark | Dakror
 */
public class BatchDelegate implements SpriteRenderer {
    private Batch batch;
    private Texture atlas;

    public BatchDelegate(Texture atlas, Batch batch) {
        this.atlas = atlas;
        this.batch = batch;
    }

    @Override
    public void add(TextureRegion region, float x, float y, float z, float width, float height) {
        batch.draw(region, x, y, width, height);
    }

    @Override
    public void add(float x, float y, float z, float originX, float originY, float width, float height,
            float scaleX, float scaleY, float rotation, float srcX, float srcY, float srcWidth, float srcHeight,
            boolean flipX, boolean flipY) {
        batch.draw(atlas, x, y, originX, originY, width, height, scaleX, scaleY, rotation, (int) srcX, (int) srcY, (int) srcWidth, (int) srcHeight, flipX, flipY);
    }

    @Override
    public void add(TextureRegion region, float x, float y, float z, float originX, float originY,
            float width, float height, float scaleX, float scaleY, float rotation) {
        batch.draw(region, x, y, originX, originY, width, height, scaleX, scaleY, rotation);
    }

    @Override
    public void add(float x, float y, float z, float width, float height, float u, float v, float u2, float v2) {
        batch.draw(atlas, x, y, width, height, u, v, u2, v2);
    }

    @Override
    public void flush() {
        batch.flush();
    }

    public Batch getBatch() {
        return batch;
    }

    @Override
    public ShaderProgram getShader() {
        return batch.getShader();
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}
