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
 * Provides three float fields packed.
 *
 * @deprecated Renderscript has been deprecated in API level 31. Please refer to the <a
 * href="https://developer.android.com/guide/topics/renderscript/migration-guide">migration
 * guide</a> for the proposed alternatives.
 */
@Deprecated
public class Float3 {
    public float x;
    public float y;
    public float z;

    public Float3() {
    }

    @Hide
    public Float3(Float3 data) {
        this.x = data.x;
        this.y = data.y;
        this.z = data.z;
    }

    public Float3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Vector add
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Float3 add(Float3 a, Float3 b) {
        Float3 res = new Float3();
        res.x = a.x + b.x;
        res.y = a.y + b.y;
        res.z = a.z + b.z;

        return res;
    }

    /**
     * Vector add
     *
     * @param value
     */
    @Hide
    public void add(Float3 value) {
        x += value.x;
        y += value.y;
        z += value.z;
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
    }

    /**
     * Vector add
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Float3 add(Float3 a, float b) {
        Float3 res = new Float3();
        res.x = a.x + b;
        res.y = a.y + b;
        res.z = a.z + b;

        return res;
    }

    /**
     * Vector subtraction
     *
     * @param value
     */
    @Hide
    public void sub(Float3 value) {
        x -= value.x;
        y -= value.y;
        z -= value.z;
    }

    /**
     * Vector subtraction
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Float3 sub(Float3 a, Float3 b) {
        Float3 res = new Float3();
        res.x = a.x - b.x;
        res.y = a.y - b.y;
        res.z = a.z - b.z;

        return res;
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
    }

    /**
     * Vector subtraction
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Float3 sub(Float3 a, float b) {
        Float3 res = new Float3();
        res.x = a.x - b;
        res.y = a.y - b;
        res.z = a.z - b;

        return res;
    }

    /**
     * Vector multiplication
     *
     * @param value
     */
    @Hide
    public void mul(Float3 value) {
        x *= value.x;
        y *= value.y;
        z *= value.z;
    }

    /**
     * Vector multiplication
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Float3 mul(Float3 a, Float3 b) {
        Float3 res = new Float3();
        res.x = a.x * b.x;
        res.y = a.y * b.y;
        res.z = a.z * b.z;

        return res;
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
    }

    /**
     * Vector multiplication
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Float3 mul(Float3 a, float b) {
        Float3 res = new Float3();
        res.x = a.x * b;
        res.y = a.y * b;
        res.z = a.z * b;

        return res;
    }

    /**
     * Vector division
     *
     * @param value
     */
    @Hide
    public void div(Float3 value) {
        x /= value.x;
        y /= value.y;
        z /= value.z;
    }

    /**
     * Vector division
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Float3 div(Float3 a, Float3 b) {
        Float3 res = new Float3();
        res.x = a.x / b.x;
        res.y = a.y / b.y;
        res.z = a.z / b.z;

        return res;
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
    }

    /**
     * Vector division
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Float3 div(Float3 a, float b) {
        Float3 res = new Float3();
        res.x = a.x / b;
        res.y = a.y / b;
        res.z = a.z / b;

        return res;
    }

    /**
     * Vector dot Product
     *
     * @param a
     * @return
     */
    @Hide
    public Float dotProduct(Float3 a) {
        return new Float((x * a.x) + (y * a.y) + (z * a.z));
    }

    /**
     * Vector dot Product
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Float dotProduct(Float3 a, Float3 b) {
        return new Float((b.x * a.x) + (b.y * a.y) + (b.z * a.z));
    }

    /**
     * Vector add Multiple
     *
     * @param a
     * @param factor
     */
    @Hide
    public void addMultiple(Float3 a, float factor) {
        x += a.x * factor;
        y += a.y * factor;
        z += a.z * factor;
    }

    /**
     * set vector value by float3
     *
     * @param a
     */
    @Hide
    public void set(Float3 a) {
        this.x = a.x;
        this.y = a.y;
        this.z = a.z;
    }

    /**
     * set vector negate
     */
    @Hide
    public void negate() {
        x = -x;
        y = -y;
        z = -z;
    }

    /**
     * get vector length
     *
     * @return
     */
    @Hide
    public int length() {
        return 3;
    }

    /**
     * return the element sum of vector
     *
     * @return
     */
    @Hide
    public Float elementSum() {
        return new Float(x + y + z);
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
     */
    @Hide
    public void setValues(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
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
    }
}
