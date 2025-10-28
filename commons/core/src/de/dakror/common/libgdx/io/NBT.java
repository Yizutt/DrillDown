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

import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.badlogic.gdx.utils.Pools;

import net.jpountz.lz4.LZ4FrameInputStream;
import net.jpountz.lz4.LZ4FrameOutputStream;
import net.jpountz.lz4.LZ4FrameOutputStream.BLOCKSIZE;
import net.jpountz.lz4.LZ4FrameOutputStream.FLG;

/**
 * @author Maximilian Stark | Dakror
 */
public class NBT {
    public static class NBTException extends Exception {

        private static final long serialVersionUID = 1L;

        public NBTException() {
            super();
        }

        public NBTException(String message, Throwable cause) {
            super(message, cause);
        }

        public NBTException(String message) {
            super(message);
        }

        public NBTException(Throwable cause) {
            super(cause);
        }
    }

    static final TagType[] reverseTags = new TagType[256];

    public enum CompressionType {
        Uncompressed,
        Fast,
        Small
    }

    public enum TagType {
        End(0, EndTag.class, 0),
        Byte(1, ByteTag.class, 1),
        Short(2, ShortTag.class, 2),
        Int(3, IntTag.class, 4),
        Long(4, LongTag.class, 8),
        Float(5, FloatTag.class, 4),
        Double(6, DoubleTag.class, 8),
        ByteArray(7, ByteArrayTag.class, 1),
        String(8, StringTag.class, 1),
        List(9, ListTag.class, 0),
        Compound(10, CompoundTag.class, 0),
        IntArray(11, IntArrayTag.class, 4),
        LongArray(12, LongArrayTag.class, 8),
        ShortArray(13, ShortArrayTag.class, 2),
        FloatArray(14, FloatArrayTag.class, 4);

        public byte value;
        public Class<? extends Tag> clazz;
        public final int width;

        TagType(int val, Class<? extends Tag> clazz, int width) {
            value = (byte) val;
            this.clazz = clazz;
            this.width = width;

            reverseTags[value] = this;
        }
    }

    public static abstract class Tag implements Poolable {
        static int idCounter = 0;
        public final int id;
        public final TagType type;
        public String name;
        public CollectionTag parent;

        public Tag(TagType type) {
            this.type = type;
            this.id = idCounter++;
        }

        @Override
        public void reset() {
            name = null;
        }

        public void free() {
            Pools.free(this);
        }

        @Override
        public final String toString() {
            return toString("");
        }

        protected String toString(String pad) {
            return pad + type.name() + (name != null ? "(\"" + name + "\")" : "") + ": ";
        }

        @Override
        public boolean equals(Object obj) {
            if (getClass().isInstance(obj)) {
                return type == ((Tag) obj).type
                        && (name == null ? ((Tag) obj).name == null : name.equals(((Tag) obj).name))
                        && dataEquals((Tag) obj);
            }
            return false;
        }

        public void checkName(String name) throws NBTException {
            if (!name.equals(this.name))
                throw new RuntimeException("Invalid Tag name, wanted \"" + name + "\", got \"" + this.name + "\"");
        }

        protected abstract boolean dataEquals(Tag o);

        public abstract Object data();

        @Override
        public int hashCode() {
            return Objects.hash(parent != null ? parent.id : 0, id);
        }
    }

    public static class EndTag extends Tag {
        public EndTag() {
            super(TagType.End);
        }

        @Override
        protected boolean dataEquals(Tag o) {
            return true;
        }

        @Override
        public Object data() {
            return null;
        }
    }

    public static class ByteTag extends Tag {
        public byte data;

        public ByteTag() {
            super(TagType.Byte);
        }

        public ByteTag(String name, byte data) {
            super(TagType.Byte);
            this.data = data;
            this.name = name;
        }

        @Override
        public void reset() {
            super.reset();
            data = 0;
        }

        @Override
        protected String toString(String pad) {
            return super.toString(pad) + data + "\r\n";
        }

        @Override
        protected boolean dataEquals(Tag o) {
            return data == ((ByteTag) o).data;
        }

        @Override
        public Object data() {
            return data;
        }
    }

    public static class ShortTag extends Tag {
        public short data;

        public ShortTag() {
            super(TagType.Short);
        }

        public ShortTag(String name, short data) {
            super(TagType.Short);
            this.data = data;
            this.name = name;
        }

        @Override
        public void reset() {
            super.reset();
            data = 0;
        }

        @Override
        protected String toString(String pad) {
            return super.toString(pad) + data + "\r\n";
        }

        @Override
        protected boolean dataEquals(Tag o) {
            return data == ((ShortTag) o).data;
        }

        @Override
        public Object data() {
            return data;
        }
    }

    public static class IntTag extends Tag {
        public int data;

        public IntTag() {
            super(TagType.Int);
        }

        public IntTag(String name, int data) {
            super(TagType.Int);
            this.data = data;
            this.name = name;
        }

        @Override
        public void reset() {
            super.reset();
            data = 0;
        }

        @Override
        protected String toString(String pad) {
            return super.toString(pad) + data + "\r\n";
        }

        @Override
        protected boolean dataEquals(Tag o) {
            return data == ((IntTag) o).data;
        }

        @Override
        public Object data() {
            return data;
        }
    }

    public static class LongTag extends Tag {
        public long data;

        public LongTag() {
            super(TagType.Long);
        }

        public LongTag(String name, long data) {
            super(TagType.Long);
            this.data = data;
            this.name = name;
        }

        @Override
        public void reset() {
            super.reset();
            data = 0;
        }

        @Override
        protected String toString(String pad) {
            return super.toString(pad) + data + "\r\n";
        }

        @Override
        protected boolean dataEquals(Tag o) {
            return data == ((LongTag) o).data;
        }

        @Override
        public Object data() {
            return data;
        }
    }

    public static class FloatTag extends Tag {
        public float data;

        public FloatTag() {
            super(TagType.Float);
        }

        public FloatTag(String name, float data) {
            super(TagType.Float);
            this.data = data;
            this.name = name;
        }

        @Override
        public void reset() {
            super.reset();
            data = 0;
        }

        @Override
        protected String toString(String pad) {
            return super.toString(pad) + data + "\r\n";
        }

        @Override
        protected boolean dataEquals(Tag o) {
            return data == ((FloatTag) o).data;
        }

        @Override
        public Object data() {
            return data;
        }
    }

    public static class DoubleTag extends Tag {
        public double data;

        public DoubleTag() {
            super(TagType.Double);
        }

        public DoubleTag(String name, double data) {
            super(TagType.Double);
            this.data = data;
            this.name = name;
        }

        @Override
        public void reset() {
            super.reset();
            data = 0;
        }

        @Override
        protected String toString(String pad) {
            return super.toString(pad) + data + "\r\n";
        }

        @Override
        protected boolean dataEquals(Tag o) {
            return data == ((DoubleTag) o).data;
        }

        @Override
        public Object data() {
            return data;
        }
    }

    public static class ByteArrayTag extends Tag {
        public byte[] data;

        public ByteArrayTag() {
            super(TagType.ByteArray);
        }

        public ByteArrayTag(String name, byte[] data) {
            super(TagType.ByteArray);
            this.data = data;
            this.name = name;
        }

        @Override
        public void reset() {
            super.reset();
            data = null;
        }

        @Override
        protected String toString(String pad) {
            return super.toString(pad) + Arrays.toString(data) + "\r\n";
        }

        @Override
        protected boolean dataEquals(Tag o) {
            return Arrays.equals(data, ((ByteArrayTag) o).data);
        }

        @Override
        public Object data() {
            return data;
        }
    }

    public static class StringTag extends Tag {
        public String data;

        public StringTag() {
            super(TagType.String);
        }

        public StringTag(String name, String data) {
            super(TagType.String);
            this.data = data;
            this.name = name;
        }

        @Override
        public void reset() {
            super.reset();
            data = null;
        }

        @Override
        protected String toString(String pad) {
            return super.toString(pad) + "\"" + data + "\"\r\n";
        }

        @Override
        protected boolean dataEquals(Tag o) {
            return data.equals(((StringTag) o).data);
        }

        @Override
        public Object data() {
            return data;
        }
    }

    public static abstract class CollectionTag extends Tag {
        public CollectionTag(TagType type) {
            super(type);
        }

        @Override
        public void reset() {
            super.reset();
            parent = null;
        }

        public abstract void add(Tag tag);

        public abstract boolean remove(Tag tag);

        protected abstract Iterable<Tag> getData();

        protected void getChildren(Filter[] query, int index, Set<Tag> targets) {
            Filter f = query[index];

            for (Tag t : getData()) {
                if (f.matches(t)) {
                    if (index == query.length - 1) {
                        targets.add(t);
                    }

                    if (t instanceof CollectionTag) {
                        if (index < query.length - 1) {
                            ((CollectionTag) t).getChildren(query, index + 1, targets);
                        }

                        ((CollectionTag) t).getChildren(query, index, targets);
                    }
                } else if (t instanceof CollectionTag) {
                    ((CollectionTag) t).getChildren(query, index, targets);
                }
            }
        }

        /**
         * Query syntax:
         * tag name to match type
         * #name to match compound name
         * [] to match index in list
         * > (no space) to enforce direct parentship between to filters, otherwise all elements in between possible
         * , to specify multiple filters on a single object
         * 
         * 
         * @param queryString
         * @return
         */
        public Set<Tag> query(String queryString) {
            String[] parts = queryString.split(" ");
            Set<Tag> results = new HashSet<>();

            // parse query
            Filter[] query = new Filter[parts.length];

            int index = 0;
            for (String s : parts) {
                query[index++] = parseFilter(s);
            }

            getChildren(query, 0, results);

            return results;
        }
    }

    public static class ListTag extends CollectionTag {
        public TagType elementType;
        public final Array<Tag> data;

        public ListTag() {
            super(TagType.List);
            data = new Array<>();
        }

        public ListTag(String name, TagType type) {
            super(TagType.List);
            data = new Array<>();
            this.elementType = type;
            this.name = name;
        }

        @Override
        public void reset() {
            super.reset();
            data.clear();
            elementType = null;
        }

        @Override
        public void free() {
            for (Tag t : data)
                t.free();
            super.free();
        }

        @Override
        protected String toString(String pad) {
            StringBuilder sb = new StringBuilder();
            sb.append(super.toString(pad));
            sb.append(data.size);
            sb.append(" entries of type ");
            sb.append(elementType);
            sb.append("\r\n");
            sb.append(pad);
            sb.append("{\r\n");
            for (Tag t : data) {
                sb.append(t.toString(pad + "  "));
            }
            sb.append(pad);
            sb.append("}\r\n");
            return sb.toString();
        }

        @Override
        protected boolean dataEquals(Tag o) {
            return data.equals(((ListTag) o).data);
        }

        @Override
        public void add(Tag tag) {
            if (tag.type != elementType)
                throw new RuntimeException("Incompatible Tag Types in List Tag, wanted \"" + elementType + "\", got \"" + tag.type + "\"");
            tag.parent = this;
            data.add(tag);
        }

        @Override
        protected Iterable<Tag> getData() {
            return data;
        }

        @Override
        public boolean remove(Tag tag) {
            for (Iterator<Tag> iter = data.iterator(); iter.hasNext();) {
                if (iter.next().equals(tag)) {
                    iter.remove();
                    return true;
                }
            }

            return false;
        }

        @Override
        public Object data() {
            return data;
        }
    }

    public static class CompoundTag extends CollectionTag {
        public final ObjectMap<String, Tag> data;

        public CompoundTag() {
            super(TagType.Compound);
            data = new ObjectMap<>();
        }

        public CompoundTag(String name) {
            super(TagType.Compound);
            data = new ObjectMap<>();
            this.name = name;
        }

        @Override
        public void reset() {
            super.reset();
            data.clear();
        }

        @Override
        public void free() {
            for (Tag t : data.values())
                t.free();
            super.free();
        }

        @Override
        protected String toString(String pad) {
            StringBuilder sb = new StringBuilder();
            sb.append(super.toString(pad));
            sb.append(data.size);
            sb.append(" entries\r\n");
            sb.append(pad);
            sb.append("{\r\n");
            for (Tag t : data.values()) {
                sb.append(t.toString(pad + "  "));
            }
            sb.append(pad);
            sb.append("}\r\n");
            return sb.toString();
        }

        @Override
        protected boolean dataEquals(Tag o) {
            return data.equals(((CompoundTag) o).data);
        }

        @Override
        public void add(Tag tag) {
            if (tag.name == null)
                throw new RuntimeException("Compound Tag expects named tags, got no name");
            tag.parent = this;
            data.put(tag.name, tag);
        }

        @Override
        public boolean remove(Tag tag) {
            return data.remove(tag.name) != null;
        }

        public boolean has(String name) {
            return data.containsKey(name);
        }

        /////////////////////////////////////////

        public byte Byte(String name) throws NBTException {
            return ((ByteTag) getWithException(name, TagType.Byte)).data;
        }

        public byte Byte(String name, byte defaultValue) {
            ByteTag tag = ((ByteTag) get(name, TagType.Byte));
            if (tag == null) return defaultValue;
            return tag.data;
        }

        public short Short(String name) throws NBTException {
            return ((ShortTag) getWithException(name, TagType.Short)).data;
        }

        public short Short(String name, short defaultValue) {
            ShortTag tag = ((ShortTag) get(name, TagType.Short));
            if (tag == null) return defaultValue;
            return tag.data;
        }

        public int Int(String name) throws NBTException {
            return ((IntTag) getWithException(name, TagType.Int)).data;
        }

        public int Int(String name, int defaultValue) {
            IntTag tag = ((IntTag) get(name, TagType.Int));
            if (tag == null) return defaultValue;
            return tag.data;
        }

        public long Long(String name) throws NBTException {
            return ((LongTag) getWithException(name, TagType.Long)).data;
        }

        public long Long(String name, long defaultValue) {
            LongTag tag = ((LongTag) get(name, TagType.Long));
            if (tag == null) return defaultValue;
            return tag.data;
        }

        public float Float(String name) throws NBTException {
            return ((FloatTag) getWithException(name, TagType.Float)).data;
        }

        public float Float(String name, float defaultValue) {
            FloatTag tag = ((FloatTag) get(name, TagType.Float));
            if (tag == null) return defaultValue;
            return tag.data;
        }

        public double Double(String name) throws NBTException {
            return ((DoubleTag) getWithException(name, TagType.Double)).data;
        }

        public double Double(String name, double defaultValue) {
            DoubleTag tag = ((DoubleTag) get(name, TagType.Double));
            if (tag == null) return defaultValue;
            return tag.data;
        }

        public byte[] ByteArray(String name) throws NBTException {
            return ((ByteArrayTag) getWithException(name, TagType.ByteArray)).data;
        }

        public byte[] ByteArray(String name, byte[] defaultValue) {
            ByteArrayTag tag = ((ByteArrayTag) get(name, TagType.ByteArray));
            if (tag == null) return defaultValue;
            return tag.data;
        }

        public String String(String name) throws NBTException {
            return ((StringTag) getWithException(name, TagType.String)).data;
        }

        public String String(String name, String defaultValue) {
            StringTag tag = ((StringTag) get(name, TagType.String));
            if (tag == null) return defaultValue;
            return tag.data;
        }

        public ListTag List(String name) throws NBTException {
            return (ListTag) getWithException(name, TagType.List);
        }

        public ListTag List(String name, TagType elementType) throws NBTException {
            ListTag tag = (ListTag) getWithException(name, TagType.List);
            if (tag.elementType != elementType)
                throw new NBTException("Invalid element tag type! Expected \"" + type + "\", got \"" + tag.type + "\"");
            return tag;
        }

        public CompoundTag Compound(String name) throws NBTException {
            return (CompoundTag) getWithException(name, TagType.Compound);
        }

        public CompoundTag CompoundOpt(String name) {
            CompoundTag tag = (CompoundTag) get(name, TagType.Compound);
            return tag;
        }

        public int[] IntArray(String name) throws NBTException {
            return ((IntArrayTag) getWithException(name, TagType.IntArray)).data;
        }

        public int[] IntArray(String name, int[] defaultValue) {
            IntArrayTag tag = ((IntArrayTag) get(name, TagType.IntArray));
            if (tag == null) return defaultValue;
            return tag.data;
        }

        public long[] LongArray(String name) throws NBTException {
            return ((LongArrayTag) getWithException(name, TagType.LongArray)).data;
        }

        public long[] LongArray(String name, long[] defaultValue) {
            LongArrayTag tag = ((LongArrayTag) get(name, TagType.LongArray));
            if (tag == null) return defaultValue;
            return tag.data;
        }

        public short[] ShortArray(String name) throws NBTException {
            return ((ShortArrayTag) getWithException(name, TagType.ShortArray)).data;
        }

        public short[] ShortArray(String name, short[] defaultValue) {
            ShortArrayTag tag = ((ShortArrayTag) get(name, TagType.ShortArray));
            if (tag == null) return defaultValue;
            return tag.data;
        }

        public float[] FloatArray(String name) throws NBTException {
            return ((FloatArrayTag) getWithException(name, TagType.FloatArray)).data;
        }

        public float[] FloatArray(String name, float[] defaultValue) {
            FloatArrayTag tag = ((FloatArrayTag) get(name, TagType.FloatArray));
            if (tag == null) return defaultValue;
            return tag.data;
        }

        /////////////////////////////////////////

        public Tag get(String name) {
            return data.get(name);
        }

        public Tag getWithException(String name) throws NBTException {
            Tag t = data.get(name);
            if (t == null) throw new NBTException("No tag found with name \"" + name + "\"");
            return t;
        }

        public Tag get(String name, TagType type) {
            Tag tag = data.get(name);
            if (tag == null) return null;
            if (tag.type != type) return null;
            return tag;
        }

        public Tag getWithException(String name, TagType type) throws NBTException {
            Tag tag = data.get(name);
            if (tag == null) throw new NBTException("No tag found with name \"" + name + "\"");
            if (tag.type != type)
                throw new NBTException("Invalid tag type! Expected \"" + type + "\", got \"" + tag.type + "\"");
            return tag;
        }

        @Override
        protected Iterable<Tag> getData() {
            return data.values();
        }

        @Override
        public Object data() {
            return data;
        }
    }

    public static class IntArrayTag extends Tag {
        public int[] data;

        public IntArrayTag() {
            super(TagType.IntArray);
        }

        public IntArrayTag(String name, int[] data) {
            super(TagType.IntArray);
            this.data = data;
            this.name = name;
        }

        @Override
        public void reset() {
            super.reset();
            data = null;
        }

        @Override
        protected String toString(String pad) {
            return super.toString(pad) + Arrays.toString(data) + "\r\n";
        }

        @Override
        protected boolean dataEquals(Tag o) {
            return Arrays.equals(data, ((IntArrayTag) o).data);
        }

        @Override
        public Object data() {
            return data;
        }
    }

    public static class LongArrayTag extends Tag {
        public long[] data;

        public LongArrayTag() {
            super(TagType.LongArray);
        }

        public LongArrayTag(String name, long[] data) {
            super(TagType.LongArray);
            this.data = data;
            this.name = name;
        }

        @Override
        public void reset() {
            super.reset();
            data = null;
        }

        @Override
        protected String toString(String pad) {
            return super.toString(pad) + Arrays.toString(data) + "\r\n";
        }

        @Override
        protected boolean dataEquals(Tag o) {
            return Arrays.equals(data, ((LongArrayTag) o).data);
        }

        @Override
        public Object data() {
            return data;
        }
    }

    public static class ShortArrayTag extends Tag {
        public short[] data;

        public ShortArrayTag() {
            super(TagType.ShortArray);
        }

        public ShortArrayTag(String name, short[] data) {
            super(TagType.ShortArray);
            this.data = data;
            this.name = name;
        }

        @Override
        public void reset() {
            super.reset();
            data = null;
        }

        @Override
        protected String toString(String pad) {
            return super.toString(pad) + Arrays.toString(data) + "\r\n";
        }

        @Override
        protected boolean dataEquals(Tag o) {
            return Arrays.equals(data, ((ShortArrayTag) o).data);
        }

        @Override
        public Object data() {
            return data;
        }
    }

    public static class FloatArrayTag extends Tag {
        public float[] data;

        public FloatArrayTag() {
            super(TagType.FloatArray);
        }

        public FloatArrayTag(String name, float[] data) {
            super(TagType.FloatArray);
            this.data = data;
            this.name = name;
        }

        @Override
        public void reset() {
            super.reset();
            data = null;
        }

        @Override
        protected String toString(String pad) {
            return super.toString(pad) + Arrays.toString(data) + "\r\n";
        }

        @Override
        protected boolean dataEquals(Tag o) {
            return Arrays.equals(data, ((FloatArrayTag) o).data);
        }

        @Override
        public Object data() {
            return data;
        }
    }

    //////////////////////////////////////////
    //////////////////////////////////////////

    public static interface Filter {
        static final Pattern indexPattern = Pattern.compile("^\\[(\\d+)\\]$");

        boolean matches(Tag t);
    }

    public static class NameFilter implements Filter {
        private String name;

        public NameFilter(String name) {
            this.name = name;
        }

        @Override
        public boolean matches(Tag t) {
            return t.name != null && t.name.equals(this.name);
        }

        @Override
        public String toString() {
            return "#" + name;
        }
    }

    public static class TypeFilter implements Filter {
        private TagType type;

        public TypeFilter(TagType type) {
            this.type = type;
        }

        @Override
        public boolean matches(Tag t) {
            return t.type.equals(type);
        }

        @Override
        public String toString() {
            return type.name();
        }
    }

    public static class IndexFilter implements Filter {
        private int index;

        public IndexFilter(int index) {
            this.index = index;
        }

        @Override
        public boolean matches(Tag t) {
            return t.parent != null && t.parent.type == TagType.List && ((ListTag) t.parent).data.indexOf(t, false) == index;
        }

        @Override
        public String toString() {
            return "[" + index + "]";
        }
    }

    public static class AnyFilter implements Filter {
        @Override
        public boolean matches(Tag t) {
            return true;
        }

        @Override
        public String toString() {
            return "*";
        }
    }

    public static class ParentFilter implements Filter {
        private Filter parent, child;

        public ParentFilter(Filter parent, Filter child) {
            this.parent = parent;
            this.child = child;
        }

        @Override
        public boolean matches(Tag t) {
            return t.parent != null && this.parent.matches(t.parent) && this.child.matches(t);
        }

        @Override
        public String toString() {
            return "[" + parent + ">" + child + "]";
        }
    }

    public static class CompoundFilter implements Filter {
        private Filter[] filters;

        public CompoundFilter(Filter... filters) {
            this.filters = filters;
        }

        @Override
        public boolean matches(Tag t) {
            for (Filter f : filters)
                if (!f.matches(t)) return false;
            return true;
        }

        @Override
        public String toString() {
            String s = "{";
            for (Filter f : filters)
                s += f;
            return s + "}";
        }
    }

    //////////////////////////////////////////
    //////////////////////////////////////////
    //////////////////////////////////////////
    //////////////////////////////////////////

    public static class Builder {
        protected CompoundTag root;

        protected CollectionTag current;

        public Builder(String name) {
            root = new CompoundTag(name);
            current = root;
        }

        public Builder Byte(String name, byte value) {
            current.add(new ByteTag(name, value));
            return this;
        }

        public Builder Byte(byte value) {
            current.add(new ByteTag(null, value));
            return this;
        }

        public Builder Short(String name, short value) {
            current.add(new ShortTag(name, value));
            return this;
        }

        public Builder Short(short value) {
            current.add(new ShortTag(null, value));
            return this;
        }

        public Builder Int(String name, int value) {
            current.add(new IntTag(name, value));
            return this;
        }

        public Builder Int(int value) {
            current.add(new IntTag(null, value));
            return this;
        }

        public Builder Long(String name, long value) {
            current.add(new LongTag(name, value));
            return this;
        }

        public Builder Long(long value) {
            current.add(new LongTag(null, value));
            return this;
        }

        public Builder Float(String name, float value) {
            current.add(new FloatTag(name, value));
            return this;
        }

        public Builder Float(float value) {
            current.add(new FloatTag(null, value));
            return this;
        }

        public Builder Double(String name, double value) {
            current.add(new DoubleTag(name, value));
            return this;
        }

        public Builder Double(double value) {
            current.add(new DoubleTag(null, value));
            return this;
        }

        public Builder ByteArray(String name, byte[] value) {
            current.add(new ByteArrayTag(name, value));
            return this;
        }

        public Builder ByteArray(byte[] value) {
            current.add(new ByteArrayTag(null, value));
            return this;
        }

        public Builder String(String name, String value) {
            current.add(new StringTag(name, value));
            return this;
        }

        public Builder String(String value) {
            current.add(new StringTag(null, value));
            return this;
        }

        public Builder List(String name, TagType type) {
            CollectionTag prev = current;
            current = new ListTag(name, type);
            current.parent = prev;
            prev.add(current);
            return this;
        }

        public Builder List(TagType type) {
            CollectionTag prev = current;
            current = new ListTag(null, type);
            current.parent = prev;
            prev.add(current);
            return this;
        }

        public Builder Compound(String name) {
            CollectionTag prev = current;
            current = new CompoundTag(name);
            current.parent = prev;
            prev.add(current);
            return this;
        }

        public Builder Compound() {
            CollectionTag prev = current;
            current = new CompoundTag();
            current.parent = prev;
            prev.add(current);
            return this;
        }

        public Builder IntArray(String name, int[] value) {
            current.add(new IntArrayTag(name, value));
            return this;
        }

        public Builder IntArray(int[] value) {
            current.add(new IntArrayTag(null, value));
            return this;
        }

        public Builder LongArray(String name, long[] value) {
            current.add(new LongArrayTag(name, value));
            return this;
        }

        public Builder LongArray(long[] value) {
            current.add(new LongArrayTag(null, value));
            return this;
        }

        public Builder ShortArray(String name, short[] value) {
            current.add(new ShortArrayTag(name, value));
            return this;
        }

        public Builder ShortArray(short[] value) {
            current.add(new ShortArrayTag(null, value));
            return this;
        }

        public Builder FloatArray(String name, float[] value) {
            current.add(new FloatArrayTag(name, value));
            return this;
        }

        public Builder FloatArray(float[] value) {
            current.add(new FloatArrayTag(null, value));
            return this;
        }

        public Builder add(Tag tag) {
            current.add(tag);
            return this;
        }

        public Builder End() {
            current = current.parent;
            return this;
        }

        public CompoundTag Get() {
            while (current != null)
                End();
            return root;
        }
    }

    //////////////////////////////////////////
    //////////////////////////////////////////

    protected static final NBT nbt = new NBT();

    protected DataInput input;
    protected DataOutput output;

    protected NBT() {}

    protected String readName() throws IOException {
        StringTag tag = readTag(TagType.String);
        return tag.data;
    }

    @SuppressWarnings("unchecked")
    protected <T extends Tag> T readTag(TagType type) throws IOException {
        return (T) readPayload(type, type.clazz);
    }

    @SuppressWarnings("unchecked")
    protected <T extends Tag> T readPayload(TagType type, Class<T> expected) throws IOException {
        Tag tag = null;
        switch (type) {
            case End:
                tag = Pools.obtain(EndTag.class);
                break;
            case Byte:
                tag = Pools.obtain(ByteTag.class);
                ((ByteTag) tag).data = input.readByte();
                break;
            case Short:
                tag = Pools.obtain(ShortTag.class);
                ((ShortTag) tag).data = input.readShort();
                break;
            case Int:
                tag = Pools.obtain(IntTag.class);
                ((IntTag) tag).data = input.readInt();
                break;
            case Long:
                tag = Pools.obtain(LongTag.class);
                ((LongTag) tag).data = input.readLong();
                break;
            case Float:
                tag = Pools.obtain(FloatTag.class);
                ((FloatTag) tag).data = input.readFloat();
                break;
            case Double:
                tag = Pools.obtain(DoubleTag.class);
                ((DoubleTag) tag).data = input.readDouble();
                break;
            case ByteArray:
                tag = Pools.obtain(ByteArrayTag.class);
                IntTag length7 = readTag(TagType.Int);
                byte[] data7 = new byte[length7.data];
                input.readFully(data7);
                ((ByteArrayTag) tag).data = data7;
                break;
            case String:
                tag = Pools.obtain(StringTag.class);
                ShortTag length8 = readTag(TagType.Short);
                byte[] data8 = new byte[length8.data];
                input.readFully(data8);
                ((StringTag) tag).data = new String(data8, "UTF-8");
                break;
            case List:
                tag = Pools.obtain(ListTag.class);
                ByteTag type9 = readTag(TagType.Byte);
                IntTag length9 = readTag(TagType.Int);
                TagType tagtype9 = TagType.values()[type9.data];
                ((ListTag) tag).elementType = tagtype9;
                for (int i = 0; i < length9.data; i++)
                    ((ListTag) tag).add(readTag(tagtype9));
                break;
            case Compound:
                tag = Pools.obtain(CompoundTag.class);

                while (true) {
                    Tag t = readTag(true, null);

                    if (t instanceof EndTag) {
                        break;
                    } else((CompoundTag) tag).add(t);
                }
                break;
            case IntArray:
                tag = Pools.obtain(IntArrayTag.class);
                IntTag length11 = readTag(TagType.Int);
                int[] data11 = new int[length11.data];
                for (int i = 0; i < data11.length; i++) {
                    data11[i] = input.readInt();
                }
                ((IntArrayTag) tag).data = data11;
                break;
            case LongArray:
                tag = Pools.obtain(LongArrayTag.class);
                IntTag length12 = readTag(TagType.Int);
                long[] data12 = new long[length12.data];
                for (int i = 0; i < data12.length; i++) {
                    data12[i] = input.readLong();
                }
                ((LongArrayTag) tag).data = data12;
                break;
            case ShortArray:
                tag = Pools.obtain(ShortArrayTag.class);
                IntTag length13 = readTag(TagType.Int);
                short[] data13 = new short[length13.data];
                for (int i = 0; i < data13.length; i++) {
                    data13[i] = input.readShort();
                }
                ((ShortArrayTag) tag).data = data13;
                break;
            case FloatArray:
                tag = Pools.obtain(FloatArrayTag.class);
                IntTag length14 = readTag(TagType.Int);
                float[] data14 = new float[length14.data];
                for (int i = 0; i < data14.length; i++) {
                    data14[i] = input.readFloat();
                }
                ((FloatArrayTag) tag).data = data14;
                break;
            default:
                throw new IOException("Unknown Tag Type: " + type);
        }

        if (expected != null && !expected.isInstance(tag))
            throw new IOException("Invalid Tag Type! Expected \"" + expected.getSimpleName() + "\", got \"" + tag.getClass().getSimpleName() + "\"");

        return (T) tag;
    }

    protected <T extends Tag> T readTag(boolean named, Class<T> expected) throws IOException {
        byte type = input.readByte();

        String name = null;
        if (named && type != 0) name = readName();

        T tag = readPayload(reverseTags[type], expected);

        tag.name = name;
        return tag;
    }

    public static class LazyTagContainer {
        public static class RawTag implements Poolable {
            int position;
            TagType type;
            int size;
            LazyTagContainer container;

            @Override
            public void reset() {
                position = 0;
                size = 0;
                type = null;
                container = null;
            }
        }

        public static class RawCompoundTag extends RawTag {
            ObjectMap<String, RawTag> children = new ObjectMap<>();

            @Override
            public void reset() {
                children.clear();
            }
        }

        public static class RawListTag extends RawTag {
            /**
             * offsets for basic types, tags for compound & list
             */
            Object[] children;
            TagType childrenType;

            @Override
            public void reset() {
                children = null;
                childrenType = null;
            }
        }

        ByteBuffer buf;

        public final RawCompoundTag root;
        public final String rootName;

        public LazyTagContainer(byte[] data) throws IOException {
            buf = ByteBuffer.wrap(data);

            buf.position(0);
            TagType type = reverseTags[buf.get()];
            if (type == null || type != TagType.Compound) throw new IOException("Unknown Tag Type: " + type);

            short len = buf.getShort();
            byte[] str = new byte[len];
            buf.get(str);
            rootName = new String(str, "UTF-8");
            root = readCompound();
        }

        private RawCompoundTag readCompound() throws IOException {
            RawCompoundTag compound = Pools.obtain(RawCompoundTag.class);
            compound.position = buf.position();
            compound.container = this;
            while (buf.position() < buf.capacity()) {
                short len = buf.getShort();
                byte[] str = new byte[len];
                buf.get(str);
                String name = new String(str, "UTF-8");
                TagType type = reverseTags[buf.get()];
                if (type == null) throw new IOException("Unknown Tag Type: " + type);

                switch (type) {
                    case Byte:
                    case Short:
                    case Int:
                    case Long:
                    case Float:
                    case Double: {
                        RawTag tag = Pools.obtain(RawTag.class);
                        tag.container = this;
                        tag.position = buf.position();
                        tag.type = type;
                        buf.position(buf.position() + type.width);
                        root.children.put(name, tag);
                        break;
                    }
                    case String: {
                        RawTag tag = Pools.obtain(RawTag.class);
                        tag.container = this;
                        tag.position = buf.position();
                        tag.type = type;
                        tag.size = buf.getShort();
                        root.children.put(name, tag);
                        buf.position(buf.position() + tag.size);
                        break;
                    }
                    case ByteArray:
                    case ShortArray:
                    case IntArray:
                    case LongArray:
                    case FloatArray: {
                        RawTag tag = Pools.obtain(RawTag.class);
                        tag.container = this;
                        tag.position = buf.position();
                        tag.type = type;
                        tag.size = buf.getInt();
                        buf.position(buf.position() + tag.size * type.width);
                        break;
                    }
                    case Compound:
                        root.children.put(name, readCompound());
                        break;
                    case List: {
                        root.children.put(name, readList());
                        break;
                    }
                    case End:
                    default:
                        break;
                }

                // end tag
                if (buf.get(buf.position()) == 0) break;
            }

            return compound;
        }

        private RawListTag readList() throws IOException {
            RawListTag tag = Pools.obtain(RawListTag.class);
            tag.container = this;
            tag.childrenType = reverseTags[buf.get()];

            int pos = buf.position();
            switch (tag.childrenType) {
                case Byte:
                case Short:
                case Int:
                case Float:
                case Long:
                case Double:
                    buf.position(pos + tag.children.length * tag.childrenType.width);
                    break;
                case ByteArray:
                case ShortArray:
                case IntArray:
                case FloatArray:
                case LongArray:
                    tag.children = new Object[buf.getInt()];
                    for (int i = 0; i < tag.children.length; i++) {
                        int elems = buf.getInt();
                        pos += elems * tag.childrenType.width;
                        buf.position(pos);
                        tag.size = elems;
                        tag.children[i] = pos;
                    }
                    break;
                case String:
                    tag.children = new Object[buf.getInt()];
                    for (int i = 0; i < tag.children.length; i++) {
                        int elems = buf.getShort();
                        pos += elems;
                        buf.position(pos);
                        tag.children[i] = pos;
                    }
                    break;
                case Compound: {
                    tag.children = new Object[buf.getInt()];
                    for (int i = 0; i < tag.children.length; i++) {
                        tag.children[i] = readCompound();
                    }
                    break;
                }
                case List: {
                    tag.children = new Object[buf.getInt()];
                    for (int i = 0; i < tag.children.length; i++) {
                        tag.children[i] = readList();
                    }
                    break;
                }
                case End:
                default:
                    break;
            }

            return tag;
        }

    }

    protected CompoundTag readFile(InputStream is, CompressionType compression) throws IOException {
        Tag.idCounter = 0;
        InputStream stream = is;
        is.mark(Integer.MAX_VALUE);
        try {
            if (compression == CompressionType.Fast) {
                if (Gdx.app == null || Gdx.app.getType() == ApplicationType.Desktop)
                    stream = new LZ4FrameInputStream(is);
                else
                    stream = new LZ4FrameInputStream(is, IOUtils.getLZ4().safeDecompressor(),
                            IOUtils.getXXHash().hash32());
            } else if (compression == CompressionType.Small) {
                stream = new GZIPInputStream(is);
            }

            input = new DataInputStream(stream);
            CompoundTag t = readTag(true, CompoundTag.class);
            return t;
        } catch (IOException e) {
            e.printStackTrace();
            stream = is;
            is.reset();

            // try gzip, maybe file format is old
            if (compression == CompressionType.Fast) {
                stream = new GZIPInputStream(is);
            }

            input = new DataInputStream(stream);
            CompoundTag t = readTag(true, CompoundTag.class);
            return t;
        } finally {
            stream.close();
            Tag.idCounter = 0;
        }
    }

    //////////////////////////////////////////
    //////////////////////////////////////////

    protected void writeTag(Tag tag, boolean named) throws IOException {
        if (named) {
            output.writeByte(tag.type.value);
            if (tag.name != null) {
                output.writeShort(tag.name.length());
                output.write(tag.name.getBytes("UTF-8"));
            }
        }
        switch (tag.type) {
            case End:
                output.writeByte(0);
                break;
            case Byte:
                output.writeByte(((ByteTag) tag).data);
                break;
            case Short:
                output.writeShort(((ShortTag) tag).data);
                break;
            case Int:
                output.writeInt(((IntTag) tag).data);
                break;
            case Long:
                output.writeLong(((LongTag) tag).data);
                break;
            case Float:
                output.writeFloat(((FloatTag) tag).data);
                break;
            case Double:
                output.writeDouble(((DoubleTag) tag).data);
                break;
            case ByteArray:
                output.writeInt(((ByteArrayTag) tag).data.length);
                output.write(((ByteArrayTag) tag).data);
                break;
            case String:
                byte[] bytes = ((StringTag) tag).data.getBytes();
                output.writeShort(bytes.length);
                output.write(bytes);
                break;
            case List:
                ListTag lt = (ListTag) tag;
                output.writeByte(lt.elementType.value);
                output.writeInt(lt.data.size);
                for (Tag t : lt.data)
                    writeTag(t, false);
                break;
            case Compound:
                for (Tag t : ((CompoundTag) tag).data.values())
                    writeTag(t, true);

                // TAG_End
                output.writeByte(0);
                break;
            case IntArray:
                output.writeInt(((IntArrayTag) tag).data.length);
                for (int i : ((IntArrayTag) tag).data)
                    output.writeInt(i);
                break;
            case LongArray:
                output.writeInt(((LongArrayTag) tag).data.length);
                for (long i : ((LongArrayTag) tag).data)
                    output.writeLong(i);
                break;
            case ShortArray:
                output.writeInt(((ShortArrayTag) tag).data.length);
                for (short i : ((ShortArrayTag) tag).data)
                    output.writeShort(i);
                break;
            case FloatArray:
                output.writeInt(((FloatArrayTag) tag).data.length);
                for (float i : ((FloatArrayTag) tag).data)
                    output.writeFloat(i);
                break;
            default:
                throw new IOException("Unknown Tag Type: " + tag.type);
        }
    }

    protected void writeFile(OutputStream os, CompoundTag data, CompressionType compression) throws IOException {
        Tag.idCounter = 0;
        if (compression == CompressionType.Fast) {
            if (Gdx.app == null || Gdx.app.getType() == ApplicationType.Desktop)
                os = new LZ4FrameOutputStream(os);
            else
                os = new LZ4FrameOutputStream(os, BLOCKSIZE.SIZE_4MB, -1L, IOUtils.getLZ4().fastCompressor(),
                        IOUtils.getXXHash().hash32(), FLG.Bits.BLOCK_INDEPENDENCE);
        } else if (compression == CompressionType.Small) {
            os = new GZIPOutputStream(os);
        }

        output = new DataOutputStream(os);
        writeTag(data, true);
        output = null;
        os.flush();
        os.close();
        Tag.idCounter = 0;
    }

    //////////////////////////////////////////
    //////////////////////////////////////////

    public static CompoundTag read(InputStream is, CompressionType compression) throws IOException {
        return nbt.readFile(is, compression);
    }

    private static final Pattern textRegex = Pattern.compile("(?:([a-zA-Z]+)(?:\\(\"(\\w+)\"\\))?: ?(?:\\d+ entries of type ([a-zA-Z]+)|(.+)))|\\{|\\}");

    public static CompoundTag readText(InputStream is) throws IOException {
        CompoundTag root = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String s = null;
        Matcher m = textRegex.matcher("");
        int line = 0;

        Tag lastTag = null;
        CollectionTag currentParent = null;
        ArrayDeque<CollectionTag> stack = new ArrayDeque<>();
        while ((s = br.readLine()) != null) {
            s = s.trim();
            if (!m.reset(s).find()) {
                throw new IOException("Invalid file format. Could not parse line " + line);
            }

            if (root == null) {
                if (m.group(1).equals("Compound")) {
                    root = new CompoundTag(m.group(2));
                    lastTag = root;
                } else {
                    throw new IOException("Invalid root tag. Could not parse line " + line);
                }
            } else {
                if (m.group().equals("{")) {
                    if (currentParent != null) stack.push(currentParent);
                    if (!(lastTag instanceof CollectionTag))
                        throw new IOException("Invalid parent tag. Could not parse line " + line);
                    currentParent = (CollectionTag) lastTag;
                } else if (m.group().equals("}")) {
                    currentParent = stack.size() > 0 ? stack.pop() : null;
                } else {

                    String t = m.group(1);
                    String n = m.group(2);
                    String v = m.group(4);

                    try {
                        if (t.equals("Byte")) {
                            lastTag = new ByteTag(n, Byte.parseByte(v));
                        } else if (t.equals("Short")) {
                            lastTag = new ShortTag(n, Short.parseShort(v));
                        } else if (t.equals("Int")) {
                            lastTag = new IntTag(n, Integer.parseInt(v));
                        } else if (t.equals("Long")) {
                            lastTag = new LongTag(n, Long.parseLong(v));
                        } else if (t.equals("Float")) {
                            lastTag = new FloatTag(n, Float.parseFloat(v));
                        } else if (t.equals("Double")) {
                            lastTag = new DoubleTag(n, Double.parseDouble(v));
                        } else if (t.equals("ByteArray")) {
                            byte[] b = {};
                            if (!v.equals("[]")) {
                                String[] nums = v.substring(1, v.length() - 1).split(", ");
                                b = new byte[nums.length];
                                for (int i = 0; i < b.length; i++)
                                    b[i] = Byte.parseByte(nums[i]);
                            }
                            lastTag = new ByteArrayTag(n, b);
                        } else if (t.equals("String")) {
                            lastTag = new StringTag(n, v.substring(1, v.length() - 1));
                        } else if (t.equals("List")) {
                            TagType type = null;
                            String p = m.group(3);

                            if (p.equals("Byte")) type = TagType.Byte;
                            else if (p.equals("Short")) type = TagType.Short;
                            else if (p.equals("Int")) type = TagType.Int;
                            else if (p.equals("Long")) type = TagType.Long;
                            else if (p.equals("Float")) type = TagType.Float;
                            else if (p.equals("Double")) type = TagType.Double;
                            else if (p.equals("ByteArray")) type = TagType.ByteArray;
                            else if (p.equals("String")) type = TagType.String;
                            else if (p.equals("List")) type = TagType.List;
                            else if (p.equals("Compound")) type = TagType.Compound;
                            else if (p.equals("IntArray")) type = TagType.IntArray;
                            else if (p.equals("LongArray")) type = TagType.LongArray;
                            else if (p.equals("ShortArray")) type = TagType.ShortArray;
                            else if (p.equals("FloatArray")) type = TagType.FloatArray;
                            else
                                throw new IOException("Unkown tag type. Could not parse line " + line);

                            lastTag = new ListTag(n, type);
                        } else if (t.equals("Compound")) {
                            lastTag = new CompoundTag(n);
                        } else if (t.equals("IntArray")) {
                            int[] b = {};
                            if (!v.equals("[]")) {
                                String[] nums = v.substring(1, v.length() - 1).split(", ");
                                b = new int[nums.length];
                                for (int i = 0; i < b.length; i++)
                                    b[i] = Integer.parseInt(nums[i]);
                            }
                            lastTag = new IntArrayTag(n, b);
                        } else if (t.equals("LongArray")) {
                            long[] b = {};
                            if (!v.equals("[]")) {
                                String[] nums = v.substring(1, v.length() - 1).split(", ");
                                b = new long[nums.length];
                                for (int i = 0; i < b.length; i++)
                                    b[i] = Long.parseLong(nums[i]);
                            }
                            lastTag = new LongArrayTag(n, b);
                        } else if (t.equals("ShortArray")) {
                            short[] b = {};
                            if (!v.equals("[]")) {
                                String[] nums = v.substring(1, v.length() - 1).split(", ");
                                b = new short[nums.length];
                                for (int i = 0; i < b.length; i++)
                                    b[i] = Short.parseShort(nums[i]);
                            }
                            lastTag = new ShortArrayTag(n, b);
                        } else if (t.equals("FloatArray")) {
                            float[] b = {};
                            if (!v.equals("[]")) {
                                String[] nums = v.substring(1, v.length() - 1).split(", ");
                                b = new float[nums.length];
                                for (int i = 0; i < b.length; i++)
                                    b[i] = Float.parseFloat(nums[i]);
                            }
                            lastTag = new FloatArrayTag(n, b);
                        } else
                            throw new IOException("Unkown tag type. Could not parse line " + line);

                        currentParent.add(lastTag);
                    } catch (RuntimeException e) {
                        throw new IOException("Could not parse line " + line, e);
                    }
                }
            }

            line++;
        }

        return root;

    }

    public static void write(OutputStream os, CompoundTag data, CompressionType compression) throws IOException {
        nbt.writeFile(os, data, compression);
    }

    //////////////////////////////////////////
    //////////////////////////////////////////

    public static Filter parseFilter(String s) {
        if (s.contains(",")) {
            String[] parts = s.split(",");
            Filter[] fs = new Filter[parts.length];
            for (int i = 0; i < fs.length; i++)
                fs[i] = parseFilter(parts[i]);

            return new CompoundFilter(fs);
        } else if (s.contains(">")) {
            int index = s.lastIndexOf(">");
            return new ParentFilter(parseFilter(s.substring(0, index)), parseFilter(s.substring(index + 1)));
        } else if (s.startsWith("#")) {
            return new NameFilter(s.substring(1));
        } else if (s.equals("*") || s.length() == 0) {
            return new AnyFilter();
        } else {
            Matcher m = Filter.indexPattern.matcher(s);
            if (m.matches()) {
                return new IndexFilter(Integer.parseInt(m.group(1)));
            } else {
                switch (s.toLowerCase()) {
                    case "end":
                        return new TypeFilter(TagType.End);
                    case "byte":
                        return new TypeFilter(TagType.Byte);
                    case "short":
                        return new TypeFilter(TagType.Short);
                    case "int":
                        return new TypeFilter(TagType.Int);
                    case "long":
                        return new TypeFilter(TagType.Long);
                    case "float":
                        return new TypeFilter(TagType.Float);
                    case "double":
                        return new TypeFilter(TagType.Double);
                    case "bytearray":
                        return new TypeFilter(TagType.ByteArray);
                    case "string":
                        return new TypeFilter(TagType.String);
                    case "list":
                        return new TypeFilter(TagType.List);
                    case "compound":
                        return new TypeFilter(TagType.Compound);
                    case "intarray":
                        return new TypeFilter(TagType.IntArray);
                    case "longarray":
                        return new TypeFilter(TagType.LongArray);
                    case "shortarray":
                        return new TypeFilter(TagType.ShortArray);
                    case "floatarray":
                        return new TypeFilter(TagType.FloatArray);
                    default:
                        throw new IllegalArgumentException("Invalid type filter name: " + s);
                }
            }
        }
    }
}
