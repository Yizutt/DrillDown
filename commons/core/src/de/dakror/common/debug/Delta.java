/*******************************************************************************
 * Copyright 2019 Maximilian Stark | Dakror <mail@dakror.de>
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

package de.dakror.common.debug;

/**
 * @author Maximilian Stark | Dakror
 */
public class Delta {
    static long time;

    public static void t(String msg) {
        if (time > 0) {
            long delta = System.nanoTime() - time;
            System.out.printf("%s, %,12d\n", msg != null ? msg : "", delta);
        }

        time = System.nanoTime();
    }

    public static void r() {
        time = System.nanoTime();
    }

    public static void t() {
        t(null);
    }
}
