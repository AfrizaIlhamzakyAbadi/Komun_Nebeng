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
 * Vector version of the basic float type.
 * Provides four float fields packed.
 *
 * @deprecated Renderscript has been deprecated in API level 31. Please refer to the <a
 * href="https://developer.android.com/guide/topics/renderscript/migration-guide">migration
 * guide</a> for the proposed alternatives.
 */
@Deprecated
public class Float4 {
    public float x;
    public float y;
    public float z;
    public float w;

    public Float4() {
    }

    @Hide
    public Float4(Float4 data) {
        this.x = data.x;
        this.y = data.y;
        this.z = data.z;
        this.w = data.w;
    }

    public Float4(float x, float y, float z, float w) {
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
    public static Float4 add(Float4 a, Float4 b) {
        Float4 res = new Float4();
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
    public void add(Float4 value) {
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
    public void add(float value) {
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
    public static Float4 add(Float4 a, float b) {
        Float4 res = new Float4();
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
    public void sub(Float4 value) {
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
    public void sub(float value) {
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
    public static Float4 sub(Float4 a, float b) {
        Float4 res = new Float4();
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
    public static Float4 sub(Float4 a, Float4 b) {
        Float4 res = new Float4();
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
    public void mul(Float4 value) {
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
    public void mul(float value) {
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
    public static Float4 mul(Float4 a, Float4 b) {
        Float4 res = new Float4();
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
    public static Float4 mul(Float4 a, float b) {
        Float4 res = new Float4();
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
    public void div(Float4 value) {
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
    public void div(float value) {
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
    public static Float4 div(Float4 a, float b) {
        Float4 res = new Float4();
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
    public static Float4 div(Float4 a, Float4 b) {
        Float4 res = new Float4();
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
    public float dotProduct(Float4 a) {
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
    public static float dotProduct(Float4 a, Float4 b) {
        return (b.x * a.x) + (b.y * a.y) + (b.z * a.z) + (b.w * a.w);
    }

    /**
     * Vector add Multiple
     *
     * @param a
     * @param factor
     */
    @Hide
    public void addMultiple(Float4 a, float factor) {
        x += a.x * factor;
        y += a.y * factor;
        z += a.z * factor;
        w += a.w * factor;
    }

    /**
     * set vector value by float4
     *
     * @param a
     */
    @Hide
    public void set(Float4 a) {
        this.x = a.x;
        this.y = a.y;
        this.z = a.z;
        this.w = a.w;
    }

    /**
     * set vector negate
     */
    @Hide
    public void negate() {
        x = -x;
        y = -y;
        z = -z;
        w = -w;
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
     * return the element sum of vector
     *
     * @return
     */
    @Hide
    public float elementSum() {
        return x + y + z + w;
    }

    /**
     * get the vector field value by index
     *
     * @param i
     * @return
     */
    @Hide
    public float get(int i) {
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
    public void setAt(int i, float value) {
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
    public void addAt(int i, float value) {
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
     * set the vector field value
     *
     * @param x
     * @param y
     * @param z
     * @param w
     */
    @Hide
    public void setValues(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    /**
     * copy the vector to float array
     *
     * @param data
     * @param offset
     */
    @Hide
    public void copyTo(float[] data, int offset) {
        data[offset] = x;
        data[offset + 1] = y;
        data[offset + 2] = z;
        data[offset + 3] = w;
    }
}
