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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author Maximilian Stark | Dakror
 */
public class TreeDebugger {
    public static String getChildrenRecursively(Object object, int maxDepth, Class<?>... exceptions) {
        if (maxDepth <= 0) return "{}";
        VariableNode[] nodes = getChildren(object);
        StringBuilder sb = new StringBuilder();
        sb.append("{\"type\":\"");
        sb.append(object.getClass().getName());
        sb.append("\"");
        if (nodes.length > 0) {
            sb.append(",\"children\": {");
            outer: for (int i = 0; i < Math.min(20, nodes.length); i++) {
                VariableNode v = nodes[i];
                if (v.name.contains("reflection")) continue;
                if (v.realObject != null) {
                    if (v.realObject.getClass().isEnum()) continue;
                    for (Class<?> c : exceptions) {
                        if (c.isAssignableFrom(v.realObject.getClass()) || v.realObject.getClass().getClass().isAssignableFrom(c)) continue outer;
                    }
                }

                sb.append("\"");
                sb.append(v.name);
                sb.append("\":");

                if (v.couldHaveChildren)
                    sb.append(getChildrenRecursively(v.realObject, maxDepth - 1, exceptions));
                else if (v.realObject != null) {
                    if (v.realObject instanceof String)
                        sb.append("\"");
                    sb.append(v.realObject);
                    if (v.realObject instanceof String)
                        sb.append("\"");
                } else {
                    sb.append("null");
                }
                if (i < nodes.length - 1) sb.append(",");
            }
            sb.append("}");
        }
        sb.append("}");
        return sb.toString();
    }

    public static VariableNode[] getChildren(Object object) {
        ArrayList<VariableNode> nodes = new ArrayList<>();

        if (object.getClass().isArray()) {
            int length = Array.getLength(object);

            // TODO: for big arrays create fake groups [0-99],...

            for (int i = 0; i < length; i++) {
                nodes.add(new VariableNode("[" + i + "]", Array.get(object, i)));
            }
        } else {
            Field[] fields = getAllFields(object.getClass());

            for (Field f : fields) {
                try {
                    f.setAccessible(true);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }

                try {
                    nodes.add(new VariableNode(f.getName(), f.get(object)));
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        return nodes.toArray(new VariableNode[] {});
    }

    private static Field[] getAllFields(Class<?> clazz) {
        ArrayList<Field> fields = new ArrayList<>();

        Class<?> c = clazz;
        while (c != null) {
            Collections.addAll(fields, c.getDeclaredFields());
            c = c.getSuperclass();
        }

        Collections.sort(fields, new Comparator<Field>() {
            @Override
            public int compare(Field o1, Field o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return fields.toArray(new Field[] {});
    }

    private static final Set<Class<?>> WRAPPER_TYPES = getWrapperTypes();

    public static boolean isWrapperType(Class<?> clazz) {
        return WRAPPER_TYPES.contains(clazz);
    }

    private static Set<Class<?>> getWrapperTypes() {
        Set<Class<?>> ret = new HashSet<>();
        ret.add(Boolean.class);
        ret.add(Character.class);
        ret.add(Byte.class);
        ret.add(Short.class);
        ret.add(Integer.class);
        ret.add(Long.class);
        ret.add(Float.class);
        ret.add(Double.class);
        ret.add(Void.class);

        ret.add(String.class);
        return ret;
    }
}
