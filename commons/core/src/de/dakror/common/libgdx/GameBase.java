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

import static com.badlogic.gdx.graphics.GL20.*;

import java.util.Stack;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.WindowedMean;

import de.dakror.common.libgdx.ui.Scene;

/**
 * @author Maximilian Stark | Dakror
 */
public abstract class GameBase extends ApplicationAdapter {
    public static enum WindowMode {
        Fullscreen, Borderless;
    }

    protected final Stack<Scene> sceneStack = new Stack<>();
    protected InputMultiplexer input;

    public PlatformInterface pi;

    int w, h;

    protected final WindowedMean updateTimeWindow = new WindowedMean(10);
    protected final WindowedMean frameTimeWindow = new WindowedMean(10);
    long lastUpdateTime;
    float updateTime;
    long lastFrameTime;
    float frameTime;

    WindowMode mode;

    SpriteBatch mouseBatch;
    public TextureRegion mouseTex;

    protected float updateRate = 1 / 60f;

    double currentTime;
    public boolean desktop;

    public GameBase(WindowMode mode, boolean desktop, PlatformInterface pi) {
        this.pi = pi;
        this.desktop = desktop;
        this.mode = mode;
    }

    protected void setWindowMode(WindowMode mode) {
        if (Gdx.app.getType() != ApplicationType.Desktop) return;

        Gdx.input.setCursorCatched(false);
        if (mode == WindowMode.Borderless) {
            Gdx.graphics.setUndecorated(true);
            Gdx.graphics.setWindowedMode(Gdx.graphics.getDisplayMode().width, Gdx.graphics.getDisplayMode().height);
            Gdx.graphics.setResizable(true);
        } else if (mode == WindowMode.Fullscreen) {
            if (!Gdx.graphics.isFullscreen())
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        } /* else {
          // cant re-decorate right now, idk
             Gdx.graphics.setUndecorated(false);
             Gdx.graphics.setWindowedMode(Gdx.graphics.getDisplayMode().width, Gdx.graphics.getDisplayMode().height);
             Gdx.graphics.setResizable(true);
             Gdx.graphics.setUndecorated(false);
          }*/

        this.mode = mode;
    }

    @Override
    public void create() {
        input = new InputMultiplexer();

        if (Gdx.app.getType() == ApplicationType.Desktop) {
            input.addProcessor(new InputAdapter() {
                @Override
                public boolean keyDown(int keycode) {
                    if (keycode == Keys.F11) {
                        if (mode == null) mode = WindowMode.Borderless;
                        setWindowMode(WindowMode.values()[(mode.ordinal() + 1) % WindowMode.values().length]);

                        return true;
                    }
                    return false;
                }
            });
            setWindowMode(mode);
        }

        Gdx.input.setInputProcessor(input);
        currentTime = System.nanoTime() / 1_000_000_000.0;

        mouseBatch = new SpriteBatch(1);
    }

    public Scene getScene() {
        synchronized (sceneStack) {
            if (sceneStack.isEmpty()) return null;
            Scene scene = sceneStack.peek();
            return scene;
        }
    }

    public void addScene(Scene scene) {
        synchronized (sceneStack) {
            if (scene.getInput() != null)
                input.addProcessor(0, scene.getInput());
            sceneStack.push(scene);
            scene.show();
            scene.resize(w, h);
        }
    }

    public void addSceneBelow(Scene scene) {
        synchronized (sceneStack) {
            if (scene.getInput() != null)
                input.addProcessor(1, scene.getInput());
            sceneStack.add(Math.max(0, sceneStack.size() - 1), scene);
            scene.show();
            scene.resize(w, h);
        }
    }

    public boolean dropScene(Scene scene) {
        synchronized (sceneStack) {
            scene.hide();
            boolean res = sceneStack.remove(scene);
            if (scene.getInput() != null)
                input.removeProcessor(scene.getInput());
            return res;
        }
    }

    public boolean hasScene(Scene scene) {
        synchronized (sceneStack) {
            boolean has = sceneStack.contains(scene);
            return has;
        }
    }

    public Scene dropScene() {
        synchronized (sceneStack) {
            sceneStack.peek().hide();
            input.removeProcessor(0);
            Scene scene = sceneStack.pop();
            return scene;
        }
    }

    @Override
    public void resize(int width, int height) {
        w = width;
        h = height;
        synchronized (sceneStack) {
            for (Scene scene : sceneStack)
                scene.resize(width, height);
        }
    }

    @Override
    public void pause() {
        synchronized (sceneStack) {
            for (Scene scene : sceneStack)
                scene.pause();
        }
    }

    @Override
    public void resume() {
        currentTime = System.nanoTime() / 1_000_000_000.0;
        synchronized (sceneStack) {
            for (Scene scene : sceneStack)
                scene.resume();
        }
    }

    public void update() {
        double newTime = System.nanoTime() / 1_000_000_000.0;
        double deltaTime = Math.min(newTime - currentTime, 1); // limit to 1 second of catch up time
        currentTime = newTime;

        //        float deltaTime = (float) Math.min(frameTime, updateRate);

        long t = System.nanoTime();
        synchronized (sceneStack) {
            for (int i = sceneStack.size() - 1; i > -1; i--) {
                Scene s = null;
                try {
                    s = sceneStack.get(i);
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
                if (s != null)
                    s.update(deltaTime);
            }
        }
        long realDelta = System.nanoTime() - t;

        updateTimeWindow.addValue(realDelta);

        //
        // filler framerate system thingy
        //
        /*double newTime = System.nanoTime() / 1_000_000_000.0;
        double frameTime = newTime - currentTime;
        currentTime = newTime;
        
        while (frameTime > 0.0) {
            float deltaTime = (float) Math.min(frameTime, updateRate);
        
            long t = System.nanoTime();
            synchronized (sceneStack) {
                try {
                    for (int i = sceneStack.size() - 1; i > -1; i--)
                        sceneStack.get(i).update(deltaTime);
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
            long realDelta = System.nanoTime() - t;
        
            updateTimeWindow.addValue(realDelta);
        
            // ayaya
            frameTime -= Math.max(realDelta, deltaTime);
        }*/
    }

    @Override
    public void render() {
        try {
            synchronized (sceneStack) {
                update();

                long t = System.nanoTime();

                Gdx.gl.glClear(GL_COLOR_BUFFER_BIT);
                for (Scene scene : sceneStack)
                    scene.draw();

                if (desktop) {
                    mouseBatch.begin();
                    mouseBatch.draw(mouseTex, Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY() - 24, 24, 24);
                    mouseBatch.end();
                }
                frameTimeWindow.addValue(System.nanoTime() - t);
            }
        } catch (Exception e) {
            pi.message(PlatformInterface.MSG_EXCEPTION, e);
            if (e instanceof RuntimeException) throw e;
        }
    }

    @Override
    public void dispose() {
        synchronized (sceneStack) {
            for (Scene scene : sceneStack)
                scene.dispose();
        }
    }

    public float getUpdateTime() {
        if (System.currentTimeMillis() - lastUpdateTime > 1000) {
            updateTime = updateTimeWindow.getMean() / 1_000_000f;
            lastUpdateTime = System.currentTimeMillis();
        }
        return updateTime;
    }

    public float getFrameTime() {
        if (System.currentTimeMillis() - lastFrameTime > 1000) {
            frameTime = frameTimeWindow.getMean() / 1_000_000f;
            lastFrameTime = System.currentTimeMillis();
        }
        return frameTime;
    }
}
