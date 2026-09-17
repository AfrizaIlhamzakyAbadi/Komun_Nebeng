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
 * Class for exposing the native RenderScript Short2 type back to the Android system.
 *
 * Vector version of the basic short type.
 * Provides two short fields packed.
 *
 * @deprecated Renderscript has been deprecated in API level 31. Please refer to the <a
 * href="https://developer.android.com/guide/topics/renderscript/migration-guide">migration
 * guide</a> for the proposed alternatives.
 */
@Deprecated
public class Short2 {
    public short x;
    public short y;

    public Short2() {
    }

    @Hide
    public Short2(short i) {
        this.x = this.y = i;
    }

    public Short2(short x, short y) {
        this.x = x;
        this.y = y;
    }

    @Hide
    public Short2(Short2 source) {
        this.x = source.x;
        this.y = source.y;
    }

    /**
     * Vector add
     *
     * @param a
     */
    @Hide
    public void add(Short2 a) {
        this.x += a.x;
        this.y += a.y;
    }

    /**
     * Vector add
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Short2 add(Short2 a, Short2 b) {
        Short2 result = new Short2();
        result.x = (short)(a.x + b.x);
        result.y = (short)(a.y + b.y);

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
    }

    /**
     * Vector add
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Short2 add(Short2 a, short b) {
        Short2 result = new Short2();
        result.x = (short)(a.x + b);
        result.y = (short)(a.y + b);

        return result;
    }

    /**
     * Vector subtraction
     *
     * @param a
     */
    @Hide
    public void sub(Short2 a) {
        this.x -= a.x;
        this.y -= a.y;
    }

    /**
     * Vector subtraction
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Short2 sub(Short2 a, Short2 b) {
        Short2 result = new Short2();
        result.x = (short)(a.x - b.x);
        result.y = (short)(a.y - b.y);

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
    }

    /**
     * Vector subtraction
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Short2 sub(Short2 a, short b) {
        Short2 result = new Short2();
        result.x = (short)(a.x - b);
        result.y = (short)(a.y - b);

        return result;
    }

    /**
     * Vector multiplication
     *
     * @param a
     */
    @Hide
    public void mul(Short2 a) {
        this.x *= a.x;
        this.y *= a.y;
    }

    /**
     * Vector multiplication
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Short2 mul(Short2 a, Short2 b) {
        Short2 result = new Short2();
        result.x = (short)(a.x * b.x);
        result.y = (short)(a.y * b.y);

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
    }

    /**
     * Vector multiplication
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Short2 mul(Short2 a, short b) {
        Short2 result = new Short2();
        result.x = (short)(a.x * b);
        result.y = (short)(a.y * b);

        return result;
    }

    /**
     * Vector division
     *
     * @param a
     */
    @Hide
    public void div(Short2 a) {
        this.x /= a.x;
        this.y /= a.y;
    }

    /**
     * Vector division
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Short2 div(Short2 a, Short2 b) {
        Short2 result = new Short2();
        result.x = (short)(a.x / b.x);
        result.y = (short)(a.y / b.y);

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
    }

    /**
     * Vector division
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Short2 div(Short2 a, short b) {
        Short2 result = new Short2();
        result.x = (short)(a.x / b);
        result.y = (short)(a.y / b);

        return result;
    }

    /**
     * Vector Modulo
     *
     * @param a
     */
    @Hide
    public void mod(Short2 a) {
        this.x %= a.x;
        this.y %= a.y;
    }

    /**
     * Vector Modulo
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Short2 mod(Short2 a, Short2 b) {
        Short2 result = new Short2();
        result.x = (short)(a.x % b.x);
        result.y = (short)(a.y % b.y);

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
    }

    /**
     * Vector Modulo
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Short2 mod(Short2 a, short b) {
        Short2 result = new Short2();
        result.x = (short)(a.x % b);
        result.y = (short)(a.y % b);

        return result;
    }

    /**
     * get vector length
     *
     * @return
     */
    @Hide
    public short length() {
        return 2;
    }

    /**
     * set vector negate
     */
    @Hide
    public void negate() {
        this.x = (short)(-x);
        this.y = (short)(-y);
    }

    /**
     * Vector dot Product
     *
     * @param a
     * @return
     */
    @Hide
    public short dotProduct(Short2 a) {
        return (short)((x * a.x) + (y * a.y));
    }

    /**
     * Vector dot Product
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static short dotProduct(Short2 a, Short2 b) {
        return (short)((b.x * a.x) + (b.y * a.y));
    }

    /**
     * Vector add Multiple
     *
     * @param a
     * @param factor
     */
    @Hide
    public void addMultiple(Short2 a, short factor) {
        x += a.x * factor;
        y += a.y * factor;
    }

    /**
     * set vector value by Short2
     *
     * @param a
     */
    @Hide
    public void set(Short2 a) {
        this.x = a.x;
        this.y = a.y;
    }

    /**
     * set the vector field value by Short
     *
     * @param a
     * @param b
     */
    @Hide
    public void setValues(short a, short b) {
        this.x = a;
        this.y = b;
    }

    /**
     * return the element sum of vector
     *
     * @return
     */
    @Hide
    public short elementSum() {
        return (short)(x + y);
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
    }
}
