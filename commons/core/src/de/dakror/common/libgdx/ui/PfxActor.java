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
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * @author Maximilian Stark | Dakror
 */
public class PfxActor extends Actor {
    protected ParticleEffect pe;

    protected boolean started;

    public PfxActor(ParticleEffect e) {
        pe = e;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (started) pe.draw(batch);
    }

    @Override
    public void act(float delta) {
        if (started) pe.update(delta);
    }

    public void reset() {
        pe.reset();
        started = false;
    }

    public void start() {
        started = true;
    }

    public boolean isComplete() {
        return pe.isComplete();
    }
}
