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

package de.dakror.common.libgdx.audio;

import com.badlogic.gdx.audio.Sound;

/**
 * @author Maximilian Stark | Dakror
 */
public class AmbientSound {
    Sound sound;
    long soundId;

    double nominalVolume;
    double oldVolume;
    double volume;

    double progress;
    double interpolationTime;

    public AmbientSound(Sound sound, float interpolationTime) {
        this.sound = sound;
        this.interpolationTime = interpolationTime;
        soundId = -1;
    }

    public void setVolume(float volume) {
        if (volume != nominalVolume) {
            oldVolume = this.volume;
            nominalVolume = volume;
            progress = 0;
        }
    }

    public void update(double deltaTime) {
        if (volume != nominalVolume || soundId == -1) {
            if (progress >= interpolationTime) {
                volume = nominalVolume;
                progress = 0;
            } else {
                volume = oldVolume + (nominalVolume - oldVolume) * (progress / interpolationTime);
                progress += deltaTime;
            }

            if (soundId == -1) {
                soundId = sound.loop((float) volume);
            } else {
                sound.setVolume(soundId, (float) volume);
            }
        }
    }

    public void stop() {
        sound.stop();
        soundId = -1;
    }
}
