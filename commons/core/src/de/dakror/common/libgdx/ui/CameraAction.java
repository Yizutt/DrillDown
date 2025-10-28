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

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;

/**
 * @author Maximilian Stark | Dakror
 */
public class CameraAction extends TemporalAction {
    Vector3 from, to;
    float zoomFrom, zoomTo;
    OrthographicCamera cam;

    boolean done;

    public CameraAction(float duration, Vector3 target, float zoom, OrthographicCamera cam) {
        this(duration, target, zoom, cam, Interpolation.fade);
    }

    public CameraAction(float duration, Vector3 target, float zoom, OrthographicCamera cam, Interpolation interpolation) {
        super(duration, interpolation);
        this.from = new Vector3(cam.position);
        this.to = target;
        this.zoomFrom = cam.zoom;
        this.zoomTo = zoom;
        this.cam = cam;
    }

    @Override
    protected void update(float percent) {
        cam.position.set(from.x + (to.x - from.x) * percent, from.y + (to.y - from.y) * percent, from.z + (to.z - from.z) * percent);
        cam.zoom = zoomFrom + (zoomTo - zoomFrom) * percent;
        cam.update();
    }

    @Override
    protected void end() {
        done = true;
    }

    public boolean isDone() {
        return done;
    }
}
