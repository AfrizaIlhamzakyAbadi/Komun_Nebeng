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
 * Vector version of the basic int type.
 * Provides four int fields packed.
 *
 * @deprecated Renderscript has been deprecated in API level 31. Please refer to the <a
 * href="https://developer.android.com/guide/topics/renderscript/migration-guide">migration
 * guide</a> for the proposed alternatives.
 */
@Deprecated
public class Int4 {
    public int x;
    public int y;
    public int z;
    public int w;

    public Int4() {
    }

    @Hide
    public Int4(int i) {
        this.x = this.y = this.z = this.w = i;
    }

    public Int4(int x, int y, int z, int w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    @Hide
    public Int4(Int4 source) {
        this.x = source.x;
        this.y = source.y;
        this.z = source.z;
        this.w = source.w;
    }

    /**
     * Vector add
     *
     * @param a
     */
    @Hide
    public void add(Int4 a) {
        this.x += a.x;
        this.y += a.y;
        this.z += a.z;
        this.w += a.w;
    }

    /**
     * Vector add
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Int4 add(Int4 a, Int4 b) {
        Int4 result = new Int4();
        result.x = a.x + b.x;
        result.y = a.y + b.y;
        result.z = a.z + b.z;
        result.w = a.w + b.w;

        return result;
    }

    /**
     * Vector add
     *
     * @param value
     */
    @Hide
    public void add(int value) {
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
    public static Int4 add(Int4 a, int b) {
        Int4 result = new Int4();
        result.x = a.x + b;
        result.y = a.y + b;
        result.z = a.z + b;
        result.w = a.w + b;

        return result;
    }

    /**
     * Vector subtraction
     *
     * @param a
     */
    @Hide
    public void sub(Int4 a) {
        this.x -= a.x;
        this.y -= a.y;
        this.z -= a.z;
        this.w -= a.w;
    }

    /**
     * Vector subtraction
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Int4 sub(Int4 a, Int4 b) {
        Int4 result = new Int4();
        result.x = a.x - b.x;
        result.y = a.y - b.y;
        result.z = a.z - b.z;
        result.w = a.w - b.w;

        return result;
    }

    /**
     * Vector subtraction
     *
     * @param value
     */
    @Hide
    public void sub(int value) {
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
    public static Int4 sub(Int4 a, int b) {
        Int4 result = new Int4();
        result.x = a.x - b;
        result.y = a.y - b;
        result.z = a.z - b;
        result.w = a.w - b;

        return result;
    }

    /**
     * Vector multiplication
     *
     * @param a
     */
    @Hide
    public void mul(Int4 a) {
        this.x *= a.x;
        this.y *= a.y;
        this.z *= a.z;
        this.w *= a.w;
    }

    /**
     * Vector multiplication
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Int4 mul(Int4 a, Int4 b) {
        Int4 result = new Int4();
        result.x = a.x * b.x;
        result.y = a.y * b.y;
        result.z = a.z * b.z;
        result.w = a.w * b.w;

        return result;
    }

    /**
     * Vector multiplication
     *
     * @param value
     */
    @Hide
    public void mul(int value) {
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
    public static Int4 mul(Int4 a, int b) {
        Int4 result = new Int4();
        result.x = a.x * b;
        result.y = a.y * b;
        result.z = a.z * b;
        result.w = a.w * b;

        return result;
    }

    /**
     * Vector division
     *
     * @param a
     */
    @Hide
    public void div(Int4 a) {
        this.x /= a.x;
        this.y /= a.y;
        this.z /= a.z;
        this.w /= a.w;
    }

    /**
     * Vector division
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Int4 div(Int4 a, Int4 b) {
        Int4 result = new Int4();
        result.x = a.x / b.x;
        result.y = a.y / b.y;
        result.z = a.z / b.z;
        result.w = a.w / b.w;

        return result;
    }

    /**
     * Vector division
     *
     * @param value
     */
    @Hide
    public void div(int value) {
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
    public static Int4 div(Int4 a, int b) {
        Int4 result = new Int4();
        result.x = a.x / b;
        result.y = a.y / b;
        result.z = a.z / b;
        result.w = a.w / b;

        return result;
    }

    /**
     * Vector Modulo
     *
     * @param a
     */
    @Hide
    public void mod(Int4 a) {
        this.x %= a.x;
        this.y %= a.y;
        this.z %= a.z;
        this.w %= a.w;
    }

    /**
     * Vector Modulo
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Int4 mod(Int4 a, Int4 b) {
        Int4 result = new Int4();
        result.x = a.x % b.x;
        result.y = a.y % b.y;
        result.z = a.z % b.z;
        result.w = a.w % b.w;

        return result;
    }

    /**
     * Vector Modulo
     *
     * @param value
     */
    @Hide
    public void mod(int value) {
        x %= value;
        y %= value;
        z %= value;
        w %= value;
    }

    /**
     * Vector Modulo
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Int4 mod(Int4 a, int b) {
        Int4 result = new Int4();
        result.x = a.x % b;
        result.y = a.y % b;
        result.z = a.z % b;
        result.w = a.w % b;

        return result;
    }

    /**
     * get vector length
     *
     * @return
     */
    @Hide
    public int length() {
        return 4;
    }

    /**
     * set vector negate
     */
    @Hide
    public void negate() {
        this.x = -x;
        this.y = -y;
        this.z = -z;
        this.w = -w;
    }

    /**
     * Vector dot Product
     *
     * @param a
     * @return
     */
    @Hide
    public int dotProduct(Int4 a) {
        return (int)((x * a.x) + (y * a.y) + (z * a.z) + (w * a.w));
    }

    /**
     * Vector dot Product
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static int dotProduct(Int4 a, Int4 b) {
        return (int)((b.x * a.x) + (b.y * a.y) + (b.z * a.z) + (b.w * a.w));
    }

    /**
     * Vector add Multiple
     *
     * @param a
     * @param factor
     */
    @Hide
    public void addMultiple(Int4 a, int factor) {
        x += a.x * factor;
        y += a.y * factor;
        z += a.z * factor;
        w += a.w * factor;
    }

    /**
     * set vector value by Int4
     *
     * @param a
     */
    @Hide
    public void set(Int4 a) {
        this.x = a.x;
        this.y = a.y;
        this.z = a.z;
        this.w = a.w;
    }

    /**
     * set the vector field value by Int
     *
     * @param a
     * @param b
     * @param c
     * @param d
     */
    @Hide
    public void setValues(int a, int b, int c, int d) {
        this.x = a;
        this.y = b;
        this.z = c;
        this.w = d;
    }

    /**
     * return the element sum of vector
     *
     * @return
     */
    @Hide
    public int elementSum() {
        return (int)(x + y + z + w);
    }

    /**
     * get the vector field value by index
     *
     * @param i
     * @return
     */
    @Hide
    public int get(int i) {
        switch (i) {
        case 0:
            return (int)(x);
        case 1:
            return (int)(y);
        case 2:
            return (int)(z);
        case 3:
            return (int)(w);
        default:
            throw new IndexOutOfBoundsException("Index: i");
        }
    }

    /**
     * set the vector field value by index
     *
     * @param i
     * @param value
     */
    @Hide
    public void setAt(int i, int value) {
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
     * add the vector field value by index
     *
     * @param i
     * @param value
     */
    @Hide
    public void addAt(int i, int value) {
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
     * copy the vector to int array
     *
     * @param data
     * @param offset
     */
    @Hide
    public void copyTo(int[] data, int offset) {
        data[offset] = (int)(x);
        data[offset + 1] = (int)(y);
        data[offset + 2] = (int)(z);
        data[offset + 3] = (int)(w);
    }
}
