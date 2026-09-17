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
 * Class for exposing the native RenderScript byte3 type back to the Android system.
 *
 * @deprecated Renderscript has been deprecated in API level 31. Please refer to the <a
 * href="https://developer.android.com/guide/topics/renderscript/migration-guide">migration
 * guide</a> for the proposed alternatives.
 **/
@Deprecated
public class Byte3 {
    public byte x;
    public byte y;
    public byte z;

    public Byte3() {
    }

    public Byte3(byte initX, byte initY, byte initZ) {
        x = initX;
        y = initY;
        z = initZ;
    }

    @Hide
    public Byte3(Byte3 source) {
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
    public void add(Byte3 a) {
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
    public static Byte3 add(Byte3 a, Byte3 b) {
        Byte3 result = new Byte3();
        result.x = (byte)(a.x + b.x);
        result.y = (byte)(a.y + b.y);
        result.z = (byte)(a.z + b.z);

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
    public static Byte3 add(Byte3 a, byte b) {
        Byte3 result = new Byte3();
        result.x = (byte)(a.x + b);
        result.y = (byte)(a.y + b);
        result.z = (byte)(a.z + b);

        return result;
    }

    /**
     * Vector subtraction
     *
     * @param a
     */
    @Hide
    public void sub(Byte3 a) {
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
    public static Byte3 sub(Byte3 a, Byte3 b) {
        Byte3 result = new Byte3();
        result.x = (byte)(a.x - b.x);
        result.y = (byte)(a.y - b.y);
        result.z = (byte)(a.z - b.z);

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
    public static Byte3 sub(Byte3 a, byte b) {
        Byte3 result = new Byte3();
        result.x = (byte)(a.x - b);
        result.y = (byte)(a.y - b);
        result.z = (byte)(a.z - b);

        return result;
    }

    /**
     * Vector multiplication
     *
     * @param a
     */
    @Hide
    public void mul(Byte3 a) {
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
    public static Byte3 mul(Byte3 a, Byte3 b) {
        Byte3 result = new Byte3();
        result.x = (byte)(a.x * b.x);
        result.y = (byte)(a.y * b.y);
        result.z = (byte)(a.z * b.z);

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
    public static Byte3 mul(Byte3 a, byte b) {
        Byte3 result = new Byte3();
        result.x = (byte)(a.x * b);
        result.y = (byte)(a.y * b);
        result.z = (byte)(a.z * b);

        return result;
    }

    /**
     * Vector division
     *
     * @param a
     */
    @Hide
    public void div(Byte3 a) {
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
    public static Byte3 div(Byte3 a, Byte3 b) {
        Byte3 result = new Byte3();
        result.x = (byte)(a.x / b.x);
        result.y = (byte)(a.y / b.y);
        result.z = (byte)(a.z / b.z);

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
    public static Byte3 div(Byte3 a, byte b) {
        Byte3 result = new Byte3();
        result.x = (byte)(a.x / b);
        result.y = (byte)(a.y / b);
        result.z = (byte)(a.z / b);

        return result;
    }

    /**
     * get vector length
     *
     * @return
     */
    @Hide
    public byte length() {
        return 3;
    }

    /**
     * set vector negate
     */
    @Hide
    public void negate() {
        this.x = (byte)(-x);
        this.y = (byte)(-y);
        this.z = (byte)(-z);
    }

    /**
     * Vector dot Product
     *
     * @param a
     * @return
     */
    @Hide
    public byte dotProduct(Byte3 a) {
        return (byte)((byte)((byte)(x * a.x) + (byte)(y * a.y)) + (byte)(z * a.z));
    }

    /**
     * Vector dot Product
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static byte dotProduct(Byte3 a, Byte3 b) {
        return (byte)((byte)((byte)(b.x * a.x) + (byte)(b.y * a.y)) + (byte)(b.z * a.z));
    }

    /**
     * Vector add Multiple
     *
     * @param a
     * @param factor
     */
    @Hide
    public void addMultiple(Byte3 a, byte factor) {
        x += a.x * factor;
        y += a.y * factor;
        z += a.z * factor;
    }

    /**
     * set vector value by Byte3
     *
     * @param a
     */
    @Hide
    public void set(Byte3 a) {
        this.x = a.x;
        this.y = a.y;
        this.z = a.z;
    }

    /**
     * set the vector field value by Char
     *
     * @param a
     * @param b
     * @param c
     */
    @Hide
    public void setValues(byte a, byte b, byte c) {
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
    public byte elementSum() {
        return (byte)(x + y + z);
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
        case 2:
            return z;
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
    public void addAt(int i, byte value) {
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
     * copy the vector to Char array
     *
     * @param data
     * @param offset
     */
    @Hide
    public void copyTo(byte[] data, int offset) {
        data[offset] = x;
        data[offset + 1] = y;
        data[offset + 2] = z;
    }
}




