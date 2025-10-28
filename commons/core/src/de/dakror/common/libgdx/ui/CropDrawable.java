/*******************************************************************************
 * Copyright 2018 Maximilian Stark | Dakror <mail@dakror.de>
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

package de.dakror.common.libgdx.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * @author Maximilian Stark | Dakror
 */
public class CropDrawable extends TextureRegionDrawable {
    float heightRatio, widthRatio;

    public CropDrawable() {}

    public CropDrawable(TextureRegion region) {
        super(region);
    }

    public CropDrawable(TextureRegionDrawable drawable) {
        super(drawable);
    }

    public void setWidthRatio(float width) {
        this.widthRatio = width;
    }

    public void setHeightRatio(float height) {
        this.heightRatio = height;
    }

    @Override
    public void setRegion(TextureRegion region) {
        super.setRegion(region);
        widthRatio = 1;
        heightRatio = 1;
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height) {
        TextureRegion tr = getRegion();

        batch.draw(tr.getTexture(), x, y, widthRatio * width, heightRatio * height, tr.getU(), tr.getV2(), tr.getU2(), tr.getV() + (tr.getV2() - tr.getV()) * (1 - heightRatio));
    }
}
