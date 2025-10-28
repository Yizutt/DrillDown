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

package de.dakror.common.libgdx.io;

import net.jpountz.lz4.LZ4Factory;
import net.jpountz.xxhash.XXHashFactory;

/**
 * @author Maximilian Stark | Dakror
 */
public class IOUtils {
    public static byte[] twiddleBoolArray(boolean[] array) {
        int len = (int) Math.ceil(array.length / 8f);
        byte[] data = new byte[len];
        for (int i = 0; i < array.length; i++)
            if (array[i]) data[i / 8] |= 1 << i % 8;
        return data;
    }

    public static boolean[] getBoolArray(byte[] data) {
        boolean[] bools = new boolean[data.length * 8];
        for (int i = 0; i < bools.length; i++)
            bools[i] = (data[i / 8] & (1 << (i % 8))) != 0;
        return bools;
    }

    public static LZ4Factory getLZ4() {
        try {
            LZ4Factory nativ = LZ4Factory.nativeInstance();
            if (nativ != null) return nativ;
        } catch (Throwable e) {
            // empty catch error handling to fallthrough
        }

        // no jni in ios (for now)
        // android does not support all of sun.misc.Unsafe
        return LZ4Factory.safeInstance();
    }

    public static XXHashFactory getXXHash() {
        try {
            XXHashFactory nativ = XXHashFactory.nativeInstance();
            if (nativ != null) return nativ;
        } catch (Throwable e) {
            // empty catch error handling to fallthrough
        }

        // no jni in ios (for now)
        // android does not support all of sun.misc.Unsafe
        return XXHashFactory.safeInstance();
    }
}
