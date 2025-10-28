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

package de.dakror.common.libgdx.ui;

import com.badlogic.gdx.graphics.Color;

/**
 * @author Maximilian Stark | Dakror
 */
public class ColorUtil {
    public static Color HSLtoRGB(float h, float s, float l, float alpha) {
        if (s < 0.0f || s > 100.0f) {
            String message = "Color parameter outside of expected range - Saturation";
            throw new IllegalArgumentException(message);
        }

        if (l < 0.0f || l > 100.0f) {
            String message = "Color parameter outside of expected range - Luminance";
            throw new IllegalArgumentException(message);
        }

        if (alpha < 0.0f || alpha > 1.0f) {
            String message = "Color parameter outside of expected range - Alpha";
            throw new IllegalArgumentException(message);
        }

        //  Formula needs all values between 0 - 1.

        h = h % 360.0f;
        h /= 360f;
        s /= 100f;
        l /= 100f;

        float q = 0;

        if (l < 0.5)
            q = l * (1 + s);
        else q = (l + s) - (s * l);

        float p = 2 * l - q;

        float r = Math.max(0, HueToRGB(p, q, h + (1.0f / 3.0f)));
        float g = Math.max(0, HueToRGB(p, q, h));
        float b = Math.max(0, HueToRGB(p, q, h - (1.0f / 3.0f)));

        r = Math.min(r, 1.0f);
        g = Math.min(g, 1.0f);
        b = Math.min(b, 1.0f);

        return new Color(r, g, b, alpha);
    }

    private static float HueToRGB(float p, float q, float h) {
        if (h < 0) h += 1;

        if (h > 1) h -= 1;

        if (6 * h < 1) {
            return p + ((q - p) * 6 * h);
        }

        if (2 * h < 1) {
            return q;
        }

        if (3 * h < 2) {
            return p + ((q - p) * 6 * ((2.0f / 3.0f) - h));
        }

        return p;
    }

    /*******************************************************************************
     * Copyright 2014 Pawel Pastuszak
     *
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     *
     * http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     ******************************************************************************/
    public static Color HSVtoRGB(float h, float s, float v) {
        int r, g, b;
        int i;
        float f, p, q, t;
        h = (float) Math.max(0.0, Math.min(360.0, h));
        s = (float) Math.max(0.0, Math.min(100.0, s));
        v = (float) Math.max(0.0, Math.min(100.0, v));
        s /= 100;
        v /= 100;

        h /= 60;
        i = (int) Math.floor(h);
        f = h - i;
        p = v * (1 - s);
        q = v * (1 - s * f);
        t = v * (1 - s * (1 - f));
        switch (i) {
            case 0:
                r = Math.round(255 * v);
                g = Math.round(255 * t);
                b = Math.round(255 * p);
                break;
            case 1:
                r = Math.round(255 * q);
                g = Math.round(255 * v);
                b = Math.round(255 * p);
                break;
            case 2:
                r = Math.round(255 * p);
                g = Math.round(255 * v);
                b = Math.round(255 * t);
                break;
            case 3:
                r = Math.round(255 * p);
                g = Math.round(255 * q);
                b = Math.round(255 * v);
                break;
            case 4:
                r = Math.round(255 * t);
                g = Math.round(255 * p);
                b = Math.round(255 * v);
                break;
            default:
                r = Math.round(255 * v);
                g = Math.round(255 * p);
                b = Math.round(255 * q);
        }

        return new Color(r / 255.0f, g / 255.0f, b / 255.0f, 1);
    }
}
