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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.badlogic.gdx.files.FileHandleStream;
import com.badlogic.gdx.utils.StreamUtils.OptimizedByteArrayOutputStream;

/**
 * @author Maximilian Stark | Dakror
 */
public class ByteArrayFileHandle extends FileHandleStream {
    OptimizedByteArrayOutputStream baos;
    ByteArrayInputStream bais;

    public ByteArrayFileHandle() {
        super("");

        baos = new OptimizedByteArrayOutputStream(1024);
    }

    public ByteArrayFileHandle(byte[] in) {
        super("");

        bais = new ByteArrayInputStream(in);
    }

    @Override
    public OutputStream write(boolean append) {
        if (!append)
            baos.reset();
        return baos;
    }

    @Override
    public InputStream read() {
        return bais;
    }

    public byte[] getBytes() {
        return baos.toByteArray();
    }
}
