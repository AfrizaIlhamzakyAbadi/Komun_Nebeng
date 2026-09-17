/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.renderscript;

import android.annotation.Hide;

/**
 * Vector version of the basic double type.
 * Provides four double fields packed.
 *
 * @deprecated Renderscript has been deprecated in API level 31. Please refer to the <a
 * href="https://developer.android.com/guide/topics/renderscript/migration-guide">migration
 * guide</a> for the proposed alternatives.
 */
@Deprecated
public class Double4 {
    public double x;
    public double y;
    public double z;
    public double w;

    public Double4() {
    }

    @Hide
    public Double4(Double4 data) {
        this.x = data.x;
        this.y = data.y;
        this.z = data.z;
        this.w = data.w;
    }

    public Double4(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    /**
     * Vector add
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Double4 add(Double4 a, Double4 b) {
        Double4 res = new Double4();
        res.x = a.x + b.x;
        res.y = a.y + b.y;
        res.z = a.z + b.z;
        res.w = a.w + b.w;

        return res;
    }

    /**
     * Vector add
     *
     * @param value
     */
    @Hide
    public void add(Double4 value) {
        x += value.x;
        y += value.y;
        z += value.z;
        w += value.w;
    }

    /**
     * Vector add
     *
     * @param value
     */
    @Hide
    public void add(double value) {
        x += value;
        y += value;
        z += value;
        w += value;
    }

    /**
     * Vector add
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Double4 add(Double4 a, double b) {
        Double4 res = new Double4();
        res.x = a.x + b;
        res.y = a.y + b;
        res.z = a.z + b;
        res.w = a.w + b;

        return res;
    }

    /**
     * Vector subtraction
     *
     * @param value
     */
    @Hide
    public void sub(Double4 value) {
        x -= value.x;
        y -= value.y;
        z -= value.z;
        w -= value.w;
    }

    /**
     * Vector subtraction
     *
     * @param value
     */
    @Hide
    public void sub(double value) {
        x -= value;
        y -= value;
        z -= value;
        w -= value;
    }

    /**
     * Vector subtraction
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Double4 sub(Double4 a, double b) {
        Double4 res = new Double4();
        res.x = a.x - b;
        res.y = a.y - b;
        res.z = a.z - b;
        res.w = a.w - b;

        return res;
    }

    /**
     * Vector subtraction
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Double4 sub(Double4 a, Double4 b) {
        Double4 res = new Double4();
        res.x = a.x - b.x;
        res.y = a.y - b.y;
        res.z = a.z - b.z;
        res.w = a.w - b.w;

        return res;
    }

    /**
     * Vector multiplication
     *
     * @param value
     */
    @Hide
    public void mul(Double4 value) {
        x *= value.x;
        y *= value.y;
        z *= value.z;
        w *= value.w;
    }

    /**
     * Vector multiplication
     *
     * @param value
     */
    @Hide
    public void mul(double value) {
        x *= value;
        y *= value;
        z *= value;
        w *= value;
    }

    /**
     * Vector multiplication
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Double4 mul(Double4 a, Double4 b) {
        Double4 res = new Double4();
        res.x = a.x * b.x;
        res.y = a.y * b.y;
        res.z = a.z * b.z;
        res.w = a.w * b.w;

        return res;
    }

    /**
     * Vector multiplication
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Double4 mul(Double4 a, double b) {
        Double4 res = new Double4();
        res.x = a.x * b;
        res.y = a.y * b;
        res.z = a.z * b;
        res.w = a.w * b;

        return res;
    }

    /**
     * Vector division
     *
     * @param value
     */
    @Hide
    public void div(Double4 value) {
        x /= value.x;
        y /= value.y;
        z /= value.z;
        w /= value.w;
    }

    /**
     * Vector division
     *
     * @param value
     */
    @Hide
    public void div(double value) {
        x /= value;
        y /= value;
        z /= value;
        w /= value;
    }

    /**
     * Vector division
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Double4 div(Double4 a, double b) {
        Double4 res = new Double4();
        res.x = a.x / b;
        res.y = a.y / b;
        res.z = a.z / b;
        res.w = a.w / b;

        return res;
    }

    /**
     * Vector division
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Double4 div(Double4 a, Double4 b) {
        Double4 res = new Double4();
        res.x = a.x / b.x;
        res.y = a.y / b.y;
        res.z = a.z / b.z;
        res.w = a.w / b.w;

        return res;
    }

    /**
     * Vector dot Product
     *
     * @param a
     * @return
     */
    @Hide
    public double dotProduct(Double4 a) {
        return (x * a.x) + (y * a.y) + (z * a.z) + (w * a.w);
    }

    /**
     * Vector dot Product
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static double dotProduct(Double4 a, Double4 b) {
        return (b.x * a.x) + (b.y * a.y) + (b.z * a.z) + (b.w * a.w);
    }

    /**
     * Vector add Multiple
     *
     * @param a
     * @param factor
     */
    @Hide
    public void addMultiple(Double4 a, double factor) {
        x += a.x * factor;
        y += a.y * factor;
        z += a.z * factor;
        w += a.w * factor;
    }

    /**
     * Set vector value by double4
     *
     * @param a
     */
    @Hide
    public void set(Double4 a) {
        this.x = a.x;
        this.y = a.y;
        this.z = a.z;
        this.w = a.w;
    }

    /**
     * Set vector negate
     */
    @Hide
    public void negate() {
        x = -x;
        y = -y;
        z = -z;
        w = -w;
    }

    /**
     * Get vector length
     *
     * @return
     */
    @Hide
    public int length() {
        return 4;
    }

    /**
     * Return the element sum of vector
     *
     * @return
     */
    @Hide
    public double elementSum() {
        return x + y + z + w;
    }

    /**
     * Get the vector field value by index
     *
     * @param i
     * @return
     */
    @Hide
    public double get(int i) {
        switch (i) {
        case 0:
            return x;
        case 1:
            return y;
        case 2:
            return z;
        case 3:
            return w;
        default:
            throw new IndexOutOfBoundsException("Index: i");
        }
    }

    /**
     * Set the vector field value by index
     *
     * @param i
     * @param value
     */
    @Hide
    public void setAt(int i, double value) {
        switch (i) {
        case 0:
            x = value;
            return;
        case 1:
            y = value;
            return;
        case 2:
            z = value;
            return;
        case 3:
            w = value;
            return;
        default:
            throw new IndexOutOfBoundsException("Index: i");
        }
    }

    /**
     * Add the vector field value by index
     *
     * @param i
     * @param value
     */
    @Hide
    public void addAt(int i, double value) {
        switch (i) {
        case 0:
            x += value;
            return;
        case 1:
            y += value;
            return;
        case 2:
            z += value;
            return;
        case 3:
            w += value;
            return;
        default:
            throw new IndexOutOfBoundsException("Index: i");
        }
    }

    /**
     * Set the vector field value
     *
     * @param x
     * @param y
     * @param z
     * @param w
     */
    @Hide
    public void setValues(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    /**
     * Copy the vector to double array
     *
     * @param data
     * @param offset
     */
    @Hide
    public void copyTo(double[] data, int offset) {
        data[offset] = x;
        data[offset + 1] = y;
        data[offset + 2] = z;
        data[offset + 3] = w;
    }
}
