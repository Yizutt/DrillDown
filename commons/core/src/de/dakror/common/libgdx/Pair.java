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

package de.dakror.common.libgdx;

import java.util.Objects;

import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * @author Maximilian Stark | Dakror
 */
public class Pair<K, V> implements Poolable {
    protected K key;
    protected V val;

    public Pair() {}

    public Pair(K key, V val) {
        this.key = key;
        this.val = val;
    }

    public Pair<K, V> set(K key, V val) {
        this.key = key;
        this.val = val;
        return this;
    }

    public K getKey() {
        return key;
    }

    public V getVal() {
        return val;
    }

    @Override
    public void reset() {
        key = null;
        val = null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Pair) {
            return (key == ((Pair<?, ?>) obj).key || key.equals(((Pair<?, ?>) obj).key))
                    && (val == ((Pair<?, ?>) obj).val || val.equals(((Pair<?, ?>) obj).val));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, val);
    }
}
