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

package de.dakror.common.libgdx.io;

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.badlogic.gdx.utils.Pools;

/**
 * @author Maximilian Stark | Dakror
 */
public class Pack implements Poolable {
    private static final Pool<Pack> POOL = Pools.get(Pack.class);

    public static Pack get() {
        return POOL.obtain();
    }

    protected final ObjectMap<String, Object> map;

    public Pack() {
        map = new ObjectMap<>();
    }

    public void free() {
        POOL.free(this);
    }

    @Override
    public void reset() {
        map.clear();
    }

    public boolean has(String key) {
        return map.containsKey(key);
    }

    public boolean remove(String key) {
        return map.remove(key) != null;
    }

    public Pack put(String key, Object value) {
        map.put(key, value);
        return this;
    }

    public Object get(String key) {
        return map.get(key);
    }

    public int getInt(String key) {
        return (Integer) get(key);
    }

    public long getLong(String key) {
        return (Long) get(key);
    }

    public double getDouble(String key) {
        return (Double) get(key);
    }

    public float getFloat(String key) {
        return (float) get(key);
    }

    public String getString(String key) {
        return (String) get(key);
    }

    public byte[] getBytes(String key) {
        return (byte[]) get(key);
    }

    public String[] keys() {
        return map.keys().toArray().toArray(String.class);
    }

    @Override
    public String toString() {
        return map.toString();
    }
}
