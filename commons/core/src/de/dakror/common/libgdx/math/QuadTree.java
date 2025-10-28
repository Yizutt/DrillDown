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

package de.dakror.common.libgdx.math;

import com.badlogic.gdx.utils.Array;

import de.dakror.common.libgdx.math.QuadTree.Region;

/**
 * taken from https://github.com/alwex/QuadTree
 * 
 * @author Maximilian Stark | Dakror
 */
public class QuadTree<T extends Region> {
    public static final int REGION_SELF = -1;
    public static final int REGION_NW = 0;
    public static final int REGION_NE = 1;
    public static final int REGION_SW = 2;
    public static final int REGION_SE = 3;

    public static int maxNodes = 5;
    public static int maxLevel = 10;

    public interface Region {
        int getX();

        int getY();

        int getWidth();

        int getHeight();
    }

    public static class RectRegion implements Region {
        int x, y, w, h;

        public RectRegion(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        @Override
        public int getX() {
            return x;
        }

        @Override
        public int getY() {
            return y;
        }

        @Override
        public int getWidth() {
            return w;
        }

        @Override
        public int getHeight() {
            return h;
        }
    }

    public static boolean contains(Region r, Region other) {
        return r.getWidth() > 0 && r.getHeight() > 0 && other.getWidth() > 0 && other.getHeight() > 0
                && other.getX() >= r.getX() && other.getX() + other.getWidth() <= r.getX() + r.getWidth()
                && other.getY() >= r.getY() && other.getY() + other.getHeight() <= r.getY() + r.getHeight();
    }

    Array<T> nodes;
    QuadTree<T>[] regions;

    RectRegion zone;
    int level;

    final Array<T> temp = new Array<>();

    public QuadTree(RectRegion zone, int level) {
        nodes = new Array<>();
        this.zone = zone;
        this.level = level;
    }

    @SuppressWarnings("unchecked")
    private int findRegion(Region r, boolean split) {
        int region = REGION_SELF;
        if (nodes.size >= maxNodes && level < maxLevel) {
            if (regions == null && split) {
                //split 
                regions = new QuadTree[4];
                int w = zone.w / 2;
                int h = zone.h / 2;
                int newLevel = level + 1;

                regions[REGION_NW] = new QuadTree<>(new RectRegion(zone.x, zone.y + zone.h / 2, w, h), newLevel);
                regions[REGION_NE] = new QuadTree<>(new RectRegion(zone.x + zone.w / 2, zone.y + zone.h / 2, w, h), newLevel);
                regions[REGION_SW] = new QuadTree<>(new RectRegion(zone.x, zone.y, w, h), newLevel);
                regions[REGION_SE] = new QuadTree<>(new RectRegion(zone.x + zone.w / 2, zone.y, w, h), newLevel);
            }

            if (regions != null) {
                for (int i = 0; i < 4; i++) {
                    if (contains(regions[i].zone, r)) {
                        region = i;
                        break;
                    }
                }
            }
        }

        return region;

    }

    public void insert(T element) {
        int region = this.findRegion(element, true);
        if (region == REGION_SELF || this.level == maxLevel) {
            nodes.add(element);
            return;
        } else {
            regions[region].insert(element);
        }

        if (nodes.size >= maxNodes && this.level < maxLevel) {
            // redispatch the elements
            temp.clear();
            temp.addAll(nodes);
            nodes.clear();
            for (T t : temp)
                insert(t);
        }
    }

    public Array<T> getElements(Array<T> list, RectRegion r) {
        int region = this.findRegion(r, false);

        list.addAll(nodes);

        if (region != REGION_SELF) {
            regions[region].getElements(list, r);
        } else {
            getAllElements(list, true);
        }

        return list;
    }

    public Array<T> getAllElements(Array<T> list, boolean firstCall) {
        if (regions != null) {
            regions[REGION_NW].getAllElements(list, false);
            regions[REGION_NE].getAllElements(list, false);
            regions[REGION_SW].getAllElements(list, false);
            regions[REGION_SE].getAllElements(list, false);
        }

        if (!firstCall) {
            list.addAll(nodes);
        }

        return list;
    }

    // TODO: Save & Load
}
