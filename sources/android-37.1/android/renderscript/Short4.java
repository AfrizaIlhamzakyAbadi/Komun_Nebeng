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
 * Vector version of the basic short type.
 * Provides four short fields packed.
 *
 * @deprecated Renderscript has been deprecated in API level 31. Please refer to the <a
 * href="https://developer.android.com/guide/topics/renderscript/migration-guide">migration
 * guide</a> for the proposed alternatives.
 */
@Deprecated
public class Short4 {
    public short x;
    public short y;
    public short z;
    public short w;

    public Short4() {
    }

    @Hide
    public Short4(short i) {
        this.x = this.y = this.z = this.w = i;
    }

    public Short4(short x, short y, short z, short w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    @Hide
    public Short4(Short4 source) {
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
    public void add(Short4 a) {
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
    public static Short4 add(Short4 a, Short4 b) {
        Short4 result = new Short4();
        result.x = (short)(a.x + b.x);
        result.y = (short)(a.y + b.y);
        result.z = (short)(a.z + b.z);
        result.w = (short)(a.w + b.w);

        return result;
    }

    /**
     * Vector add
     *
     * @param value
     */
    @Hide
    public void add(short value) {
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
    public static Short4 add(Short4 a, short b) {
        Short4 result = new Short4();
        result.x = (short)(a.x + b);
        result.y = (short)(a.y + b);
        result.z = (short)(a.z + b);
        result.w = (short)(a.w + b);

        return result;
    }

    /**
     * Vector subtraction
     *
     * @param a
     */
    @Hide
    public void sub(Short4 a) {
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
    public static Short4 sub(Short4 a, Short4 b) {
        Short4 result = new Short4();
        result.x = (short)(a.x - b.x);
        result.y = (short)(a.y - b.y);
        result.z = (short)(a.z - b.z);
        result.w = (short)(a.w - b.w);

        return result;
    }

    /**
     * Vector subtraction
     *
     * @param value
     */
    @Hide
    public void sub(short value) {
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
    public static Short4 sub(Short4 a, short b) {
        Short4 result = new Short4();
        result.x = (short)(a.x - b);
        result.y = (short)(a.y - b);
        result.z = (short)(a.z - b);
        result.w = (short)(a.w - b);

        return result;
    }

    /**
     * Vector multiplication
     *
     * @param a
     */
    @Hide
    public void mul(Short4 a) {
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
    public static Short4 mul(Short4 a, Short4 b) {
        Short4 result = new Short4();
        result.x = (short)(a.x * b.x);
        result.y = (short)(a.y * b.y);
        result.z = (short)(a.z * b.z);
        result.w = (short)(a.w * b.w);

        return result;
    }

    /**
     * Vector multiplication
     *
     * @param value
     */
    @Hide
    public void mul(short value) {
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
    public static Short4 mul(Short4 a, short b) {
        Short4 result = new Short4();
        result.x = (short)(a.x * b);
        result.y = (short)(a.y * b);
        result.z = (short)(a.z * b);
        result.w = (short)(a.w * b);

        return result;
    }

    /**
     * Vector division
     *
     * @param a
     */
    @Hide
    public void div(Short4 a) {
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
    public static Short4 div(Short4 a, Short4 b) {
        Short4 result = new Short4();
        result.x = (short)(a.x / b.x);
        result.y = (short)(a.y / b.y);
        result.z = (short)(a.z / b.z);
        result.w = (short)(a.w / b.w);

        return result;
    }

    /**
     * Vector division
     *
     * @param value
     */
    @Hide
    public void div(short value) {
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
    public static Short4 div(Short4 a, short b) {
        Short4 result = new Short4();
        result.x = (short)(a.x / b);
        result.y = (short)(a.y / b);
        result.z = (short)(a.z / b);
        result.w = (short)(a.w / b);

        return result;
    }

    /**
     * Vector Modulo
     *
     * @param a
     */
    @Hide
    public void mod(Short4 a) {
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
    public static Short4 mod(Short4 a, Short4 b) {
        Short4 result = new Short4();
        result.x = (short)(a.x % b.x);
        result.y = (short)(a.y % b.y);
        result.z = (short)(a.z % b.z);
        result.w = (short)(a.w % b.w);

        return result;
    }

    /**
     * Vector Modulo
     *
     * @param value
     */
    @Hide
    public void mod(short value) {
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
    public static Short4 mod(Short4 a, short b) {
        Short4 result = new Short4();
        result.x = (short)(a.x % b);
        result.y = (short)(a.y % b);
        result.z = (short)(a.z % b);
        result.w = (short)(a.w % b);

        return result;
    }

    /**
     * get vector length
     *
     * @return
     */
    @Hide
    public short length() {
        return 4;
    }

    /**
     * set vector negate
     */
    @Hide
    public void negate() {
        this.x = (short)(-x);
        this.y = (short)(-y);
        this.z = (short)(-z);
        this.w = (short)(-w);
    }

    /**
     * Vector dot Product
     *
     * @param a
     * @return
     */
    @Hide
    public short dotProduct(Short4 a) {
        return (short)((x * a.x) + (y * a.y) + (z * a.z) + (w * a.w));
    }

    /**
     * Vector dot Product
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static short dotProduct(Short4 a, Short4 b) {
        return (short)((b.x * a.x) + (b.y * a.y) + (b.z * a.z) + (b.w * a.w));
    }

    /**
     * Vector add Multiple
     *
     * @param a
     * @param factor
     */
    @Hide
    public void addMultiple(Short4 a, short factor) {
        x += a.x * factor;
        y += a.y * factor;
        z += a.z * factor;
        w += a.w * factor;
    }

    /**
     * set vector value by Short4
     *
     * @param a
     */
    @Hide
    public void set(Short4 a) {
        this.x = a.x;
        this.y = a.y;
        this.z = a.z;
        this.w = a.w;
    }

    /**
     * set the vector field value by Short
     *
     * @param a
     * @param b
     * @param c
     * @param d
     */
    @Hide
    public void setValues(short a, short b, short c, short d) {
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
    public short elementSum() {
        return (short)(x + y + z + w);
    }

    /**
     * get the vector field value by index
     *
     * @param i
     * @return
     */
    @Hide
    public short get(int i) {
        switch (i) {
        case 0:
            return (short)(x);
        case 1:
            return (short)(y);
        case 2:
            return (short)(z);
        case 3:
            return (short)(w);
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
    public void setAt(int i, short value) {
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
    public void addAt(int i, short value) {
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
     * copy the vector to short array
     *
     * @param data
     * @param offset
     */
    @Hide
    public void copyTo(short[] data, int offset) {
        data[offset] = (short)(x);
        data[offset + 1] = (short)(y);
        data[offset + 2] = (short)(z);
        data[offset + 3] = (short)(w);
    }
}
