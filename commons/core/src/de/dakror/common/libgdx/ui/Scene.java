package de.dakror.common.libgdx.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Stage;

public abstract class Scene {
    public Stage stage;

    protected float alpha = 1;
    protected boolean fadeIn, fadeOut;

    public abstract void init();

    public InputProcessor getInput() {
        return stage;
    }

    public void update(double deltaTime) {
        if (fadeIn) {
            if (alpha == 1) fadeIn = false;
            alpha = (float) Math.min(1, alpha + deltaTime * 4); // fadeIn time: 0.25s
        }
        if (fadeOut) {
            alpha = (float) Math.max(0, alpha - deltaTime * 4); // fadeIn time: 0.25s
        }
        if (stage != null)
            stage.act((float) deltaTime);
    }

    public void draw() {
        if (stage != null) {
            if (alpha == 1) stage.draw();
            else if (alpha != 0) {
                Camera camera = stage.getViewport().getCamera();
                camera.update();

                if (!stage.getRoot().isVisible()) return;

                Batch batch = stage.getBatch();
                if (batch != null) {
                    batch.setProjectionMatrix(camera.combined);
                    batch.begin();
                    stage.getRoot().draw(batch, alpha);

                    batch.end();
                }

                // if alpha != 1 we can't draw debug, thx libgdx private methods
            }
        }
    }

    public void show() {
        fadeIn = true;
        fadeOut = false;
        alpha = 0;
    }

    public void hide() {
    }

    public void pause() {
    }

    public void resume() {
    }

    public void dispose() {
        stage.dispose();
    }

    public void resize(final int width, final int height) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                if (stage != null)
                    stage.getViewport().update(width, height, true);
            }
        });
    }
}
