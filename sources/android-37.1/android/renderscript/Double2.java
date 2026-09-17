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
 * Provides two double fields packed.
 *
 * @deprecated Renderscript has been deprecated in API level 31. Please refer to the <a
 * href="https://developer.android.com/guide/topics/renderscript/migration-guide">migration
 * guide</a> for the proposed alternatives.
 */
@Deprecated
public class Double2 {
    public double x;
    public double y;

    public Double2() {
    }

    @Hide
    public Double2(Double2 data) {
        this.x = data.x;
        this.y = data.y;
    }

    public Double2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Vector add
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Double2 add(Double2 a, Double2 b) {
        Double2 res = new Double2();
        res.x = a.x + b.x;
        res.y = a.y + b.y;

        return res;
    }

    /**
     * Vector add
     *
     * @param value
     */
    @Hide
    public void add(Double2 value) {
        x += value.x;
        y += value.y;
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
    }

    /**
     * Vector add
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Double2 add(Double2 a, double b) {
        Double2 res = new Double2();
        res.x = a.x + b;
        res.y = a.y + b;

        return res;
    }

    /**
     * Vector subtraction
     *
     * @param value
     */
    @Hide
    public void sub(Double2 value) {
        x -= value.x;
        y -= value.y;
    }

    /**
     * Vector subtraction
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Double2 sub(Double2 a, Double2 b) {
        Double2 res = new Double2();
        res.x = a.x - b.x;
        res.y = a.y - b.y;

        return res;
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
    }

    /**
     * Vector subtraction
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Double2 sub(Double2 a, double b) {
        Double2 res = new Double2();
        res.x = a.x - b;
        res.y = a.y - b;

        return res;
    }

    /**
     * Vector multiplication
     *
     * @param value
     */
    @Hide
    public void mul(Double2 value) {
        x *= value.x;
        y *= value.y;
    }

    /**
     * Vector multiplication
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Double2 mul(Double2 a, Double2 b) {
        Double2 res = new Double2();
        res.x = a.x * b.x;
        res.y = a.y * b.y;

        return res;
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
    }

    /**
     * Vector multiplication
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Double2 mul(Double2 a, double b) {
        Double2 res = new Double2();
        res.x = a.x * b;
        res.y = a.y * b;

        return res;
    }

    /**
     * Vector division
     *
     * @param value
     */
    @Hide
    public void div(Double2 value) {
        x /= value.x;
        y /= value.y;
    }

    /**
     * Vector division
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Double2 div(Double2 a, Double2 b) {
        Double2 res = new Double2();
        res.x = a.x / b.x;
        res.y = a.y / b.y;

        return res;
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
    }

    /**
     * Vector division
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Double2 div(Double2 a, double b) {
        Double2 res = new Double2();
        res.x = a.x / b;
        res.y = a.y / b;

        return res;
    }

    /**
     * Vector dot Product
     *
     * @param a
     * @return
     */
    @Hide
    public double dotProduct(Double2 a) {
        return (x * a.x) + (y * a.y);
    }

    /**
     * Vector dot Product
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Double dotProduct(Double2 a, Double2 b) {
        return (b.x * a.x) + (b.y * a.y);
    }

    /**
     * Vector add Multiple
     *
     * @param a
     * @param factor
     */
    @Hide
    public void addMultiple(Double2 a, double factor) {
        x += a.x * factor;
        y += a.y * factor;
    }

    /**
     * Set vector value by double2
     *
     * @param a
     */
    @Hide
    public void set(Double2 a) {
        this.x = a.x;
        this.y = a.y;
    }

    /**
     * Set vector negate
     */
    @Hide
    public void negate() {
        x = -x;
        y = -y;
    }

    /**
     * Get vector length
     *
     * @return
     */
    @Hide
    public int length() {
        return 2;
    }

    /**
     * Return the element sum of vector
     *
     * @return
     */
    @Hide
    public double elementSum() {
        return x + y;
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
        default:
            throw new IndexOutOfBoundsException("Index: i");
        }
    }

    /**
     * Set the vector field value
     *
     * @param x
     * @param y
     */
    @Hide
    public void setValues(double x, double y) {
        this.x = x;
        this.y = y;
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
    }
}
