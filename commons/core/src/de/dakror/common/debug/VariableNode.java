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

package de.dakror.common.debug;

/**
 * @author Maximilian Stark | Dakror
 */
public class VariableNode {
    boolean couldHaveChildren;
    Object realObject;
    String name;
    boolean empty;

    public VariableNode(String name, Object o) {
        this.name = name;
        realObject = o;
        couldHaveChildren = realObject != null && (realObject.getClass().isArray() || !TreeDebugger.isWrapperType(realObject.getClass()));
    }

    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }

    public Object getRealObject() {
        return realObject;
    }

    public boolean couldHaveChildren() {
        return couldHaveChildren;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        if (realObject == null) return name + " = null";
        return name + " = " + realObject.toString();
    }
}
