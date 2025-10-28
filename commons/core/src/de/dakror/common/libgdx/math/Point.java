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

package de.dakror.common.libgdx.math;

/**
 * 
 * @author Maximilian Stark | Dakror
 */
public class Point implements Comparable<Point> {

    private final Double x;
    private final Double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Double getX() {
        return x;
    }

    public Double getY() {
        return y;
    }

    @Override
    public String toString() {
        return "(" + x + " " + y + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Point) {
            if (x.equals(((Point) obj).getX()) && y.equals(((Point) obj).getY())) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        // http://stackoverflow.com/questions/22826326/good-hashcode-function-for-2d-coordinates
        // http://www.cs.upc.edu/~alvarez/calculabilitat/enumerabilitat.pdf
        int tmp = (int) (y + ((x + 1) / 2));
        return Math.abs((int) (x + (tmp * tmp)));
    }

    @Override
    public int compareTo(Point o) {
        if (x.equals(o.x)) {
            return y.compareTo(o.y);
        }
        return x.compareTo(o.x);
    }
}
