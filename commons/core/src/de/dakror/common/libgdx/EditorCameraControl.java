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

package de.dakror.common.libgdx;

import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * @author Maximilian Stark | Dakror
 */
public abstract class EditorCameraControl extends InputAdapter {
    protected Viewport viewport;
    protected OrthographicCamera cam;
    protected GestureDetector gd;

    public InputMultiplexer input;
    public float minZoom = 0.1f;
    public float maxZoom = 10f;
    public boolean elementPlaceable;

    protected boolean canPan = true;
    protected float dragX = -1, dragY = -1;
    protected boolean zoomed;
    protected boolean p0Down = false;
    protected boolean p1Down = false;
    protected float initialZoom;

    protected boolean cameraChanged;

    protected final Vector2 activeElementPos = new Vector2();

    protected final Vector2 dragStart = new Vector2();
    protected final Vector3 dragStartCamPos = new Vector3();
    protected final Vector2 tmp = new Vector2();

    protected int tileSize;

    final static int scale = 10000;

    public EditorCameraControl(int tileSize, Viewport viewport) {
        this.tileSize = tileSize;
        this.viewport = viewport;
        this.cam = (OrthographicCamera) viewport.getCamera();
        gd = new GestureDetector(this);
        input = new InputMultiplexer(this, gd);
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        initialZoom = cam.zoom;

        viewport.unproject(tmp.set(x, y));
        int tileX = (int) (tmp.x / tileSize);
        int tileY = (int) (tmp.y / tileSize);

        if (isActiveElementEnabled() && isWithinActiveElement((int) tmp.x, (int) tmp.y, tileX, tileY)) {
            canPan = false;
            dragX = x;
            dragY = y;
            setParamRawPosition(activeElementPos, (int) tmp.x, (int) tmp.y, tileX, tileY);
            return true;
        }

        return false;
    }

    protected abstract boolean isWithinActiveElement(int x, int y, int tileX, int tileY);

    protected abstract void setParamRawPosition(Vector2 rawPosition, int x, int y, int tileX, int tileY);

    @Override
    public boolean tap(float x, float y, int count, int button) {
        viewport.unproject(tmp.set(x, y));
        int tileX = (int) (tmp.x / tileSize);
        int tileY = (int) (tmp.y / tileSize);

        updateActiveElementPlaceable();
        if (elementPlaceable && isWithinActiveElement((int) tmp.x, (int) tmp.y, tileX, tileY) && isActiveElementEnabled()) {
            placeActiveElement();

            // for a next placement
            updateActiveElementPlaceable();
            return true;
        } else if (handleInitialPlacement(tileX, tileY)) {
            setParamRawPosition(activeElementPos, (int) tmp.x, (int) tmp.y, tileX, tileY);
            updateActiveElementPlaceable();
            return true;
        } else return handleTap((int) tmp.x, (int) tmp.y, tileX, tileY);
    }

    protected abstract void placeActiveElement();

    protected boolean handleTap(int x, int y, int tileX, int tileY) {
        return false;
    }

    protected abstract boolean handleInitialPlacement(int tileX, int tileY);

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        if (dragX > -1) {
            float ratio = viewport.getWorldWidth() / viewport.getScreenWidth() * cam.zoom;

            float dX = (dragX - x) * ratio;
            float dY = (y - dragY) * ratio;

            if (canPan) {
                cam.translate(dX, dY);

                if (cam.zoom > minZoom * 2) {
                    cam.position.x = Math.round(cam.position.x);
                    cam.position.y = Math.round(cam.position.y);
                } else {
                    cam.position.x = cam.position.x;
                    cam.position.y = cam.position.y;
                }

                cameraChanged = true;
            } else if (isActiveElementEnabled()) {
                activeElementPos.sub(dX, dY);
                setActiveElementPosition(activeElementPos);
                updateActiveElementPlaceable();
            }
        }
        dragX = x;
        dragY = y;
        return false;
    }

    public void updateActiveElementPlaceable() {
        elementPlaceable = checkActiveElementPlaceable();
    }

    protected abstract void setActiveElementPosition(Vector2 rawPosition);

    protected abstract boolean isActiveElementEnabled();

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        dragX = -1;
        dragY = -1;
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if ((pointer == 1 || button == 1) && !zoomed && !p0Down && !p1Down) {
            // idek
        } else {
            canPan = true;
            activeElementPos.setZero();
        }
        zoomed = false;

        if (pointer == 0) p0Down = false;
        if (pointer == 1) p1Down = false;

        dragX = -1;
        dragY = -1;

        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        clampZoom(initialZoom * initialDistance / distance);

        zoomed = true;
        p0Down = true;
        p1Down = true;
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        clampZoom(cam.zoom + amount / 10f);
        return true;
    }

    protected boolean clampZoom(float zoom) {
        float clamped = MathUtils.clamp(zoom, minZoom, maxZoom);
        cam.zoom = Math.round(clamped * scale) / (float) scale;
        cameraChanged = true;
        return zoom == clamped;
    }

    public abstract void clampCam(OrthographicCamera cam);

    public boolean isDragging() {
        return dragX > -1;
    }

    public void update() {
        if (cameraChanged) {
            clampCam(cam);

            if (isActiveElementEnabled()) updateActiveElementPlaceable();

            cameraChanged = false;
        }
    }

    protected abstract boolean checkActiveElementPlaceable();
}
