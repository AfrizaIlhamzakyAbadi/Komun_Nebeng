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
 * Vector version of the basic long type.
 * Provides three long fields packed.
 *
 * @deprecated Renderscript has been deprecated in API level 31. Please refer to the <a
 * href="https://developer.android.com/guide/topics/renderscript/migration-guide">migration
 * guide</a> for the proposed alternatives.
 */
@Deprecated
public class Long3 {
    public long x;
    public long y;
    public long z;

    public Long3() {
    }

    @Hide
    public Long3(long i) {
        this.x = this.y = this.z = i;
    }

    public Long3(long x, long y, long z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Hide
    public Long3(Long3 source) {
        this.x = source.x;
        this.y = source.y;
        this.z = source.z;
    }

    /**
     * Vector add
     *
     * @param a
     */
    @Hide
    public void add(Long3 a) {
        this.x += a.x;
        this.y += a.y;
        this.z += a.z;
    }

    /**
     * Vector add
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Long3 add(Long3 a, Long3 b) {
        Long3 result = new Long3();
        result.x = a.x + b.x;
        result.y = a.y + b.y;
        result.z = a.z + b.z;

        return result;
    }

    /**
     * Vector add
     *
     * @param value
     */
    @Hide
    public void add(long value) {
        x += value;
        y += value;
        z += value;
    }

    /**
     * Vector add
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Long3 add(Long3 a, long b) {
        Long3 result = new Long3();
        result.x = a.x + b;
        result.y = a.y + b;
        result.z = a.z + b;

        return result;
    }

    /**
     * Vector subtraction
     *
     * @param a
     */
    @Hide
    public void sub(Long3 a) {
        this.x -= a.x;
        this.y -= a.y;
        this.z -= a.z;
    }

    /**
     * Vector subtraction
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Long3 sub(Long3 a, Long3 b) {
        Long3 result = new Long3();
        result.x = a.x - b.x;
        result.y = a.y - b.y;
        result.z = a.z - b.z;

        return result;
    }

    /**
     * Vector subtraction
     *
     * @param value
     */
    @Hide
    public void sub(long value) {
        x -= value;
        y -= value;
        z -= value;
    }

    /**
     * Vector subtraction
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Long3 sub(Long3 a, long b) {
        Long3 result = new Long3();
        result.x = a.x - b;
        result.y = a.y - b;
        result.z = a.z - b;

        return result;
    }

    /**
     * Vector multiplication
     *
     * @param a
     */
    @Hide
    public void mul(Long3 a) {
        this.x *= a.x;
        this.y *= a.y;
        this.z *= a.z;
    }

    /**
     * Vector multiplication
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Long3 mul(Long3 a, Long3 b) {
        Long3 result = new Long3();
        result.x = a.x * b.x;
        result.y = a.y * b.y;
        result.z = a.z * b.z;

        return result;
    }

    /**
     * Vector multiplication
     *
     * @param value
     */
    @Hide
    public void mul(long value) {
        x *= value;
        y *= value;
        z *= value;
    }

    /**
     * Vector multiplication
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Long3 mul(Long3 a, long b) {
        Long3 result = new Long3();
        result.x = a.x * b;
        result.y = a.y * b;
        result.z = a.z * b;

        return result;
    }

    /**
     * Vector division
     *
     * @param a
     */
    @Hide
    public void div(Long3 a) {
        this.x /= a.x;
        this.y /= a.y;
        this.z /= a.z;
    }

    /**
     * Vector division
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Long3 div(Long3 a, Long3 b) {
        Long3 result = new Long3();
        result.x = a.x / b.x;
        result.y = a.y / b.y;
        result.z = a.z / b.z;

        return result;
    }

    /**
     * Vector division
     *
     * @param value
     */
    @Hide
    public void div(long value) {
        x /= value;
        y /= value;
        z /= value;
    }

    /**
     * Vector division
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Long3 div(Long3 a, long b) {
        Long3 result = new Long3();
        result.x = a.x / b;
        result.y = a.y / b;
        result.z = a.z / b;

        return result;
    }

    /**
     * Vector Modulo
     *
     * @param a
     */
    @Hide
    public void mod(Long3 a) {
        this.x %= a.x;
        this.y %= a.y;
        this.z %= a.z;
    }

    /**
     * Vector Modulo
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Long3 mod(Long3 a, Long3 b) {
        Long3 result = new Long3();
        result.x = a.x % b.x;
        result.y = a.y % b.y;
        result.z = a.z % b.z;

        return result;
    }

    /**
     * Vector Modulo
     *
     * @param value
     */
    @Hide
    public void mod(long value) {
        x %= value;
        y %= value;
        z %= value;
    }

    /**
     * Vector Modulo
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Long3 mod(Long3 a, long b) {
        Long3 result = new Long3();
        result.x = a.x % b;
        result.y = a.y % b;
        result.z = a.z % b;

        return result;
    }

    /**
     * get vector length
     *
     * @return
     */
    @Hide
    public long length() {
        return 3;
    }

    /**
     * set vector negate
     */
    @Hide
    public void negate() {
        this.x = -x;
        this.y = -y;
        this.z = -z;
    }

    /**
     * Vector dot Product
     *
     * @param a
     * @return
     */
    @Hide
    public long dotProduct(Long3 a) {
        return (long)((x * a.x) + (y * a.y) + (z * a.z));
    }

    /**
     * Vector dot Product
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static long dotProduct(Long3 a, Long3 b) {
        return (long)((b.x * a.x) + (b.y * a.y) + (b.z * a.z));
    }

    /**
     * Vector add Multiple
     *
     * @param a
     * @param factor
     */
    @Hide
    public void addMultiple(Long3 a, long factor) {
        x += a.x * factor;
        y += a.y * factor;
        z += a.z * factor;
    }

    /**
     * set vector value by Long3
     *
     * @param a
     */
    @Hide
    public void set(Long3 a) {
        this.x = a.x;
        this.y = a.y;
        this.z = a.z;
    }

    /**
     * set the vector field value by Long
     *
     * @param a
     * @param b
     * @param c
     */
    @Hide
    public void setValues(long a, long b, long c) {
        this.x = a;
        this.y = b;
        this.z = c;
    }

    /**
     * return the element sum of vector
     *
     * @return
     */
    @Hide
    public long elementSum() {
        return (long)(x + y + z);
    }

    /**
     * get the vector field value by index
     *
     * @param i
     * @return
     */
    @Hide
    public long get(int i) {
        switch (i) {
        case 0:
            return (long)(x);
        case 1:
            return (long)(y);
        case 2:
            return (long)(z);
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
    public void setAt(int i, long value) {
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
    public void addAt(int i, long value) {
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
        default:
            throw new IndexOutOfBoundsException("Index: i");
        }
    }

    /**
     * copy the vector to long array
     *
     * @param data
     * @param offset
     */
    @Hide
    public void copyTo(long[] data, int offset) {
        data[offset] = (long)(x);
        data[offset + 1] = (long)(y);
        data[offset + 2] = (long)(z);
    }
}
