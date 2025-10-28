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

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;

import de.dakror.common.libgdx.ChangeNotifier.Event.Type;

/**
 * @author Maximilian Stark | Dakror
 */
public class ChangeNotifier<T> {
    public static class Event<T> implements Poolable {
        public enum Type {
            ADD,
            REMOVE,
            CHANGE,
            RESET,
            BULK_ADD,
            BULD_REMOVE,
            BULK_CHANGE,
        }

        protected Type type;
        protected T data;

        protected Event() {}

        public void set(Type type, T data) {
            this.type = type;
            this.data = data;
        }

        public T getData() {
            return data;
        }

        public Type getType() {
            return type;
        }

        @Override
        public void reset() {
            type = null;
            data = null;
        }
    }

    private final Pool<Event<T>> pool = new Pool<Event<T>>() {
        @Override
        protected Event<T> newObject() {
            return new Event<>();
        }
    };

    public interface Listener<T> {
        void onChangeEvent(Event<T> event);
    }

    protected Array<Listener<T>> listeners;

    protected final Object lock = new Object();

    public ChangeNotifier() {
        listeners = new Array<>();
    }

    public void notify(Type type, T data) {
        synchronized (lock) {
            Event<T> e = pool.obtain();
            e.set(type, data);

            for (Listener<T> l : listeners) {
                l.onChangeEvent(e);
            }

            pool.free(e);
        }
    }

    public void addListener(Listener<T> listener) {
        listeners.add(listener);
    }

    public boolean removeListener(Listener<T> listener) {
        return removeListener(listener, true);
    }

    public boolean removeListener(Listener<T> listener, boolean identity) {
        synchronized (lock) {
            return listeners.removeValue(listener, identity);
        }
    }
}
