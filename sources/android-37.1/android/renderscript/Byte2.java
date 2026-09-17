/*
 * Copyright (C) 2009 The Android Open Source Project
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
 * Class for exposing the native RenderScript byte2 type back to the Android system.
 *
 * @deprecated Renderscript has been deprecated in API level 31. Please refer to the <a
 * href="https://developer.android.com/guide/topics/renderscript/migration-guide">migration
 * guide</a> for the proposed alternatives.
 **/
@Deprecated
public class Byte2 {
    public byte x;
    public byte y;

    public Byte2() {
    }

    public Byte2(byte initX, byte initY) {
        x = initX;
        y = initY;
    }

    @Hide
    public Byte2(Byte2 source) {
        this.x = source.x;
        this.y = source.y;
    }

    /**
     * Vector add
     *
     * @param a
     */
    @Hide
    public void add(Byte2 a) {
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
    public static Byte2 add(Byte2 a, Byte2 b) {
        Byte2 result = new Byte2();
        result.x = (byte)(a.x + b.x);
        result.y = (byte)(a.y + b.y);

        return result;
    }

    /**
     * Vector add
     *
     * @param value
     */
    @Hide
    public void add(byte value) {
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
    public static Byte2 add(Byte2 a, byte b) {
        Byte2 result = new Byte2();
        result.x = (byte)(a.x + b);
        result.y = (byte)(a.y + b);

        return result;
    }

    /**
     * Vector subtraction
     *
     * @param a
     */
    @Hide
    public void sub(Byte2 a) {
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
    public static Byte2 sub(Byte2 a, Byte2 b) {
        Byte2 result = new Byte2();
        result.x = (byte)(a.x - b.x);
        result.y = (byte)(a.y - b.y);

        return result;
    }

    /**
     * Vector subtraction
     *
     * @param value
     */
    @Hide
    public void sub(byte value) {
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
    public static Byte2 sub(Byte2 a, byte b) {
        Byte2 result = new Byte2();
        result.x = (byte)(a.x - b);
        result.y = (byte)(a.y - b);

        return result;
    }

    /**
     * Vector multiplication
     *
     * @param a
     */
    @Hide
    public void mul(Byte2 a) {
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
    public static Byte2 mul(Byte2 a, Byte2 b) {
        Byte2 result = new Byte2();
        result.x = (byte)(a.x * b.x);
        result.y = (byte)(a.y * b.y);

        return result;
    }

    /**
     * Vector multiplication
     *
     * @param value
     */
    @Hide
    public void mul(byte value) {
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
    public static Byte2 mul(Byte2 a, byte b) {
        Byte2 result = new Byte2();
        result.x = (byte)(a.x * b);
        result.y = (byte)(a.y * b);

        return result;
    }

    /**
     * Vector division
     *
     * @param a
     */
    @Hide
    public void div(Byte2 a) {
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
    public static Byte2 div(Byte2 a, Byte2 b) {
        Byte2 result = new Byte2();
        result.x = (byte)(a.x / b.x);
        result.y = (byte)(a.y / b.y);

        return result;
    }

    /**
     * Vector division
     *
     * @param value
     */
    @Hide
    public void div(byte value) {
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
    public static Byte2 div(Byte2 a, byte b) {
        Byte2 result = new Byte2();
        result.x = (byte)(a.x / b);
        result.y = (byte)(a.y / b);

        return result;
    }

    /**
     * get vector length
     *
     * @return
     */
    @Hide
    public byte length() {
        return 2;
    }

    /**
     * set vector negate
     */
    @Hide
    public void negate() {
        this.x = (byte)(-x);
        this.y = (byte)(-y);
    }

    /**
     * Vector dot Product
     *
     * @param a
     * @return
     */
    @Hide
    public byte dotProduct(Byte2 a) {
        return (byte)((x * a.x) + (y * a.y));
    }

    /**
     * Vector dot Product
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static byte dotProduct(Byte2 a, Byte2 b) {
        return (byte)((b.x * a.x) + (b.y * a.y));
    }

    /**
     * Vector add Multiple
     *
     * @param a
     * @param factor
     */
    @Hide
    public void addMultiple(Byte2 a, byte factor) {
        x += a.x * factor;
        y += a.y * factor;
    }

    /**
     * set vector value by Byte2
     *
     * @param a
     */
    @Hide
    public void set(Byte2 a) {
        this.x = a.x;
        this.y = a.y;
    }

    /**
     * set the vector field value by Char
     *
     * @param a
     * @param b
     */
    @Hide
    public void setValues(byte a, byte b) {
        this.x = a;
        this.y = b;
    }

    /**
     * return the element sum of vector
     *
     * @return
     */
    @Hide
    public byte elementSum() {
        return (byte)(x + y);
    }

    /**
     * get the vector field value by index
     *
     * @param i
     * @return
     */
    @Hide
    public byte get(int i) {
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
     * set the vector field value by index
     *
     * @param i
     * @param value
     */
    @Hide
    public void setAt(int i, byte value) {
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
    public void addAt(int i, byte value) {
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
     * copy the vector to Char array
     *
     * @param data
     * @param offset
     */
    @Hide
    public void copyTo(byte[] data, int offset) {
        data[offset] = x;
        data[offset + 1] = y;
    }

}




