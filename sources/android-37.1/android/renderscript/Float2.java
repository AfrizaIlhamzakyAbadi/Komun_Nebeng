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
 * Provides two float fields packed.
 *
 * @deprecated Renderscript has been deprecated in API level 31. Please refer to the <a
 * href="https://developer.android.com/guide/topics/renderscript/migration-guide">migration
 * guide</a> for the proposed alternatives.
 */
@Deprecated
public  class Float2 {
    public float x;
    public float y;

    public Float2() {
    }

    @Hide
    public Float2(Float2 data) {
        this.x = data.x;
        this.y = data.y;
    }

    public Float2(float x, float y) {
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
    public static Float2 add(Float2 a, Float2 b) {
        Float2 res = new Float2();
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
    public void add(Float2 value) {
        x += value.x;
        y += value.y;
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
    }

    /**
     * Vector add
     *
     * @param a
     * @param b
     * @return
     */
    @Hide
    public static Float2 add(Float2 a, float b) {
        Float2 res = new Float2();
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
    public void sub(Float2 value) {
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
    public static Float2 sub(Float2 a, Float2 b) {
        Float2 res = new Float2();
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
    public void sub(float value) {
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
    public static Float2 sub(Float2 a, float b) {
        Float2 res = new Float2();
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
    public void mul(Float2 value) {
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
    public static Float2 mul(Float2 a, Float2 b) {
        Float2 res = new Float2();
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
    public void mul(float value) {
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
    public static Float2 mul(Float2 a, float b) {
        Float2 res = new Float2();
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
    public void div(Float2 value) {
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
    public static Float2 div(Float2 a, Float2 b) {
        Float2 res = new Float2();
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
    public void div(float value) {
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
    public static Float2 div(Float2 a, float b) {
        Float2 res = new Float2();
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
    public float dotProduct(Float2 a) {
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
    public static float dotProduct(Float2 a, Float2 b) {
        return (b.x * a.x) + (b.y * a.y);
    }

    /**
     * Vector add Multiple
     *
     * @param a
     * @param factor
     */
    @Hide
    public void addMultiple(Float2 a, float factor) {
        x += a.x * factor;
        y += a.y * factor;
    }

    /**
     * set vector value by float2
     *
     * @param a
     */
    @Hide
    public void set(Float2 a) {
        this.x = a.x;
        this.y = a.y;
    }

    /**
     * set vector negate
     */
    @Hide
    public void negate() {
        x = -x;
        y = -y;
    }

    /**
     * get vector length
     *
     * @return
     */
    @Hide
    public int length() {
        return 2;
    }

    /**
     * return the element sum of vector
     *
     * @return
     */
    @Hide
    public float elementSum() {
        return x + y;
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
        default:
            throw new IndexOutOfBoundsException("Index: i");
        }
    }

    /**
     * set the vector field value
     *
     * @param x
     * @param y
     */
    @Hide
    public void setValues(float x, float y) {
        this.x = x;
        this.y = y;
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
    }
}
