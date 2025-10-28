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

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;

/**
 * @author Maximilian Stark | Dakror
 */
public interface SpriteRenderer extends Disposable {
    void add(TextureRegion region, float x, float y, float z, float width, float height);

    void add(TextureRegion region, float x, float y, float z, float originX, float originY, float width, float height,
            float scaleX, float scaleY, float rotation);

    void add(float x, float y, float z, float width, float height, float u, float v, float u2, float v2);

    void add(float x, float y, float z, float originX, float originY, float width, float height, float scaleX,
            float scaleY, float rotation, float srcX, float srcY, float srcWidth, float srcHeight, boolean flipX, boolean flipY);

    void flush();
    
    ShaderProgram getShader();
}
