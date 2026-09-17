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
 * Class for exposing the native RenderScript byte4 type back to the Android system.
 *
 * @deprecated Renderscript has been deprecated in API level 31. Please refer to the <a
 * href="https://developer.android.com/guide/topics/renderscript/migration-guide">migration
 * guide</a> for the proposed alternatives.
 **/
@Deprecated
public class Byte4 {
    public byte x;
    public byte y;
    public byte z;
    public byte w;

    public Byte4() {
    }

    public Byte4(byte initX, byte initY, byte initZ, byte initW) {
        x = initX;
        y = initY;
        z = initZ;
        w = initW;
    }

    @Hide
    public Byte4(Byte4 source) {
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
    public void add(Byte4 a) {
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
    public static Byte4 add(Byte4 a, Byte4 b) {
        Byte4 result = new Byte4();
        result.x = (byte)(a.x + b.x);
        result.y = (byte)(a.y + b.y);
        result.z = (byte)(a.z + b.z);
        result.w = (byte)(a.w + b.w);

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
    public static Byte4 add(Byte4 a, byte b) {
        Byte4 result = new Byte4();
        result.x = (byte)(a.x + b);
        result.y = (byte)(a.y + b);
        result.z = (byte)(a.z + b);
        result.w = (byte)(a.w + b);

        return result;
    }

    /**
     * Vector subtraction
     *
     * @param a
     */
    @Hide
    public void sub(Byte4 a) {
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
    public static Byte4 sub(Byte4 a, Byte4 b) {
        Byte4 result = new Byte4();
        result.x = (byte)(a.x - b.x);
        result.y = (byte)(a.y - b.y);
        result.z = (byte)(a.z - b.z);
        result.w = (byte)(a.w - b.w);

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
    public static Byte4 sub(Byte4 a, byte b) {
        Byte4 result = new Byte4();
        result.x = (byte)(a.x - b);
        result.y = (byte)(a.y - b);
        result.z = (byte)(a.z - b);
        result.w = (byte)(a.w - b);

        return result;
    }

    /**
     * Vector multiplication
     *
     * @param a
     */
    @Hide
    public void mul(Byte4 a) {
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
    public static Byte4 mul(Byte4 a, Byte4 b) {
        Byte4 result = new Byte4();
        result.x = (byte)(a.x * b.x);
        result.y = (byte)(a.y * b.y);
        result.z = (byte)(a.z * b.z);
        result.w = (byte)(a.w * b.w);

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
    public static Byte4 mul(Byte4 a, byte b) {
        Byte4 result = new Byte4();
        result.x = (byte)(a.x * b);
        result.y = (byte)(a.y * b);
        result.z = (byte)(a.z * b);
        result.w = (byte)(a.w * b);

        return result;
    }

    /**
     * Vector division
     *
     * @param a
     */
    @Hide
    public void div(Byte4 a) {
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
    public static Byte4 div(Byte4 a, Byte4 b) {
        Byte4 result = new Byte4();
        result.x = (byte)(a.x / b.x);
        result.y = (byte)(a.y / b.y);
        result.z = (byte)(a.z / b.z);
        result.w = (byte)(a.w / b.w);

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
    public static Byte4 div(Byte4 a, byte b) {
        Byte4 result = new Byte4();
        result.x = (byte)(a.x / b);
        result.y = (byte)(a.y / b);
        result.z = (byte)(a.z / b);
        result.w = (byte)(a.w / b);

        return result;
    }

    /**
     * get vector length
     *
     * @return
     */
    @Hide
    public byte length() {
        return 4;
    }

    /**
     * set vector negate
     */
    @Hide
    public void negate() {
        this.x = (byte)(-x);
        this.y = (byte)(-y);
        this.z = (byte)(-z);
        this.w = (byte)(-w);
    }

    /**
     * Vector dot Product
     *
     * @param a
     * @return
     */
    @Hide
    public byte dotProduct(Byte4 a) {
        return (byte)((x * a.x) + (y * a.y) + (z * a.z) + (w * a.w));
    }

    /**
     * Vector dot Product
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static byte dotProduct(Byte4 a, Byte4 b) {
        return (byte)((b.x * a.x) + (b.y * a.y) + (b.z * a.z) + (b.w * a.w));
    }

    /**
     * Vector add Multiple
     *
     * @param a
     * @param factor
     */
    @Hide
    public void addMultiple(Byte4 a, byte factor) {
        x += a.x * factor;
        y += a.y * factor;
        z += a.z * factor;
        w += a.w * factor;
    }

    /**
     * set vector value by Byte4
     *
     * @param a
     */
    @Hide
    public void set(Byte4 a) {
        this.x = a.x;
        this.y = a.y;
        this.z = a.z;
        this.w = a.w;
    }

    /**
     * set the vector field values
     *
     * @param a
     * @param b
     * @param c
     * @param d
     */
    @Hide
    public void setValues(byte a, byte b, byte c, byte d) {
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
    public byte elementSum() {
        return (byte)(x + y + z + w);
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
        case 3:
            return w;
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
        case 3:
            w += value;
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
        data[offset + 3] = w;
    }
}



