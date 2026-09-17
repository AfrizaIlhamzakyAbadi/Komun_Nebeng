/*
 * Copyright (C) 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.app.people;

import android.annotation.Hide;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.FlaggedApi;
import android.annotation.IntDef;
import android.annotation.SuppressLint;
import android.annotation.SystemApi;
import android.app.people.flags.Flags;
import android.os.Parcel;
import android.os.Parcelable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

/** Defines all operators supported in an {@link Expression}. */
@SystemApi
@FlaggedApi(Flags.FLAG_ENABLE_PWM_SERVICE)
public final class Operator implements Parcelable {

    /**
     * Operator types.
     */
    @Hide
    @IntDef({
        IDENTITY,
        PRESENT,
        ABSENT,
        NOT,
        ABS,
        SGN,
        RELU,
        SIGMOID,
        LOGIT,
        COUNT,
        SORT,
        ANY_IN,
        ALL_IN,
        EQ,
        NE,
        LT,
        LE,
        GT,
        GE,
        SUB,
        DIV,
        POW,
        LOG,
        REGEX_MATCH,
        FIRST_N,
        LAST_N,
        TAKE_IF,
        SUM,
        PRODUCT,
        MEAN,
        MEDIAN,
        MIN,
        MAX,
        AND,
        OR,
        CONCAT,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {}

    /** Next operator ID: 36 */

    /* --- UNARY OPERATION START --- */
    /**
     * The following operators are for unary expressions. When specified in an expression, only the
     * first {@link Operand} is used and all other {@link Operand}s are ignored.
     */

    /** Identity operator that simply passes through the first operand. */
    public static final int IDENTITY = 0;

    /** Whether the operand has any value present. */
    public static final int PRESENT = 1;

    /** Whether the operand has no value. */
    public static final int ABSENT = 2;

    /**
     * Logical NOT of each value of the operand.
     *
     * <p>Only applicable to boolean values.
     */
    public static final int NOT = 3;

    /**
     * Converts each value of the operand to its absolute value.
     *
     * <p>Only applicable to numeric values.
     */
    public static final int ABS = 4;

    /**
     * Applies signum function to each value of the operand.
     *
     * <p>Only applicable to numeric values.
     */
    public static final int SGN = 5;

    /**
     * Applies ReLU function to each value of the operand.
     *
     * <p>Only applicable to numeric values.
     */
    public static final int RELU = 6;

    /**
     * Applies sigmoid function to each value of the operand.
     *
     * <p>Only applicable to numeric values.
     */
    public static final int SIGMOID = 7;

    /**
     * Applies logit function to each value of the operand.
     *
     * <p>Only applicable to numeric values in the range (0, 1).
     */
    public static final int LOGIT = 8;

    /** Returns the number of values in the operand. */
    public static final int COUNT = 9;

    /** Sorts the values of the operand in natural order. */
    public static final int SORT = 10;

    /* --- UNARY OPERATION END --- */

    /* --- BINARY OPERATION START --- */
    /**
     * The following operators are for binary expressions. When specified in an expression, the
     * first {@link Operand} is the left-hand side (lhs) and the second {@link Operand} is the
     * right-hand side (rhs). All other {@link Operand}s are ignored.
     */

    /** Whether any value of lhs operand is in the values of rhs operand. */
    public static final int ANY_IN = 11;

    /** Whether all values of lhs operand are in the values of rhs operand. */
    public static final int ALL_IN = 12;

    /**
     * Element-wise equality of the values of lhs operand and rhs operand if both operands are
     * multivalent. If only one is multivalent, the other is broadcasted to match the length.
     */
    public static final int EQ = 13;

    /**
     * Element-wise inequality of the values of lhs operand and rhs operand if both operands are
     * multivalent. If only one is multivalent, the other is broadcasted to match the length.
     */
    public static final int NE = 14;

    /**
     * Element-wise comparison of whether the value of lhs operand is less than the value of rhs
     * operand if both operands are multivalent. If only one is multivalent, the other is
     * broadcasted to match the length.
     *
     * <p>Only applicable to numeric values.
     */
    public static final int LT = 15;

    /**
     * Element-wise comparison of whether the value of lhs operand is less than or equal to the
     * value of rhs operand if both operands are multivalent. If only one is multivalent, the other
     * is broadcasted to match the length.
     *
     * <p>Only applicable to numeric values.
     */
    public static final int LE = 16;

    /**
     * Element-wise comparison of whether the value of lhs operand is greater than the value of rhs
     * operand if both operands are multivalent. If only one is multivalent, the other is
     * broadcasted to match the length.
     *
     * <p>Only applicable to numeric values.
     */
    public static final int GT = 17;

    /**
     * Element-wise comparison of whether the value of lhs operand is greater than or equal to the
     * value of rhs operand if both operands are multivalent. If only one is multivalent, the other
     * is broadcasted to match the length.
     *
     * <p>Only applicable to numeric values.
     */
    public static final int GE = 18;

    /**
     * Subtracts the value of rhs operand from the value of lhs operand. lhs and rhs cannot both be
     * multivalent. If only one is multivalent, the other is broadcasted to match the length.
     *
     * <p>Only applicable to numeric values.
     */
    public static final int SUB = 19;

    /**
     * Divides the value of lhs operand by the value of rhs operand. lhs and rhs cannot both be
     * multivalent. If only one is multivalent, the other is broadcasted to match the length.
     *
     * <p>Only applicable to numeric values.
     */
    public static final int DIV = 20;

    /**
     * Raises the value of lhs operand to the power of the value of rhs operand. lhs and rhs cannot
     * both be multivalent. If only one is multivalent, the other is broadcasted to match the
     * length.
     *
     * <p>Only applicable to numeric values.
     */
    public static final int POW = 21;

    /**
     * Logarithm of the value of lhs operand with the base as the value of rhs operand. lhs and rhs
     * cannot both be multivalent. If only one is multivalent, the other is broadcasted to match the
     * length.
     *
     * <p>Only applicable to numeric values.
     */
    public static final int LOG = 22;

    /**
     * Whether the value of lhs operand full-matches the regex in rhs operand. lhs and rhs cannot
     * both be multivalent. If only one is multivalent, the other is broadcasted to match the
     * length.
     *
     * <p>Only applicable to string values.
     */
    public static final int REGEX_MATCH = 23;

    /** Takes the first N values of the lhs operand, where N is the value of the rhs operand. */
    public static final int FIRST_N = 24;

    /** Takes the last N values of the lhs operand, where N is the value of the rhs operand. */
    public static final int LAST_N = 25;

    /** Takes the values of the lhs operand that match the predicate defined in the rhs operand. */
    public static final int TAKE_IF = 26;

    /* --- BINARY OPERATION END --- */

    /* --- LIST OPERATION START --- */
    /**
     * The following operators are for list expressions. When specified in an expression, all {@link
     * Operand}s are used.
     */

    /**
     * Element-wise sum of the values of the operands if multiple operands are multivalent. The
     * result has the largest length of the operand value lists where missing values default to 0.
     * If only one operand is multivalent, the others are broadcasted to match the length.
     *
     * <ul>
     *   <li>Only applicable to numeric values.
     *   <li>Applicable for {@link #reduce}.
     * </ul>
     */
    public static final int SUM = 27;

    /**
     * Element-wise product of the values of the operands if both operands are multivalent. The
     * result has the largest length of the operand value lists where missing values default to 1.
     * If only one operand is multivalent, the other is broadcasted to match the length.
     *
     * <ul>
     *   <li>Only applicable to numeric values.
     *   <li>Applicable for {@link #reduce}.
     * </ul>
     */
    public static final int PRODUCT = 28;

    /**
     * Mean of the values of all operands. Effectively, the values of all operands are concatenated
     * first and then the mean is calculated.
     *
     * <p>Only applicable to numeric values.
     */
    public static final int MEAN = 29;

    /**
     * Median of the values of all operands. Effectively, the values of all operands are
     * concatenated first and then the median is calculated.
     *
     * <p>Only applicable to numeric values.
     */
    public static final int MEDIAN = 30;

    /**
     * Element-wise minimum of the values of the operands. All multivalent operands must have the
     * same length. Scalar operands are broadcasted to match the length.
     *
     * <ul>
     *   <li>Only applicable to numeric values.
     *   <li>Applicable for {@link #reduce}.
     * </ul>
     */
    public static final int MIN = 31;

    /**
     * Element-wise maximum of the values of the operands. All multivalent operands must have the
     * same length. Scalar operands are broadcasted to match the length.
     *
     * <ul>
     *   <li>Only applicable to numeric values.
     *   <li>Applicable for {@link #reduce}.
     * </ul>
     */
    public static final int MAX = 32;

    /**
     * Element-wise logical AND of the values of the operands. All multivalent operands must have
     * the same length. Scalar operands are broadcasted to match the length.
     *
     * <ul>
     *   <li>Only applicable to boolean values.
     *   <li>Applicable for {@link #reduce}.
     * </ul>
     */
    public static final int AND = 33;

    /**
     * Element-wise logical OR of the values of the operands. All multivalent operands must have the
     * same length. Scalar operands are broadcasted to match the length.
     *
     * <ul>
     *   <li>Only applicable to boolean values.
     *   <li>Applicable for {@link #reduce}.
     * </ul>
     */
    public static final int OR = 34;

    /** Concatenates the values of the operands. */
    public static final int CONCAT = 35;

    /* --- LIST OPERATION END --- */

    @Type private final int mType;
    private final boolean mReduce;

    private Operator(@NonNull Builder builder) {
        mType = builder.mType;
        mReduce = builder.mReduce;
    }

    private Operator(@NonNull Parcel in) {
        mType = in.readInt();
        mReduce = in.readBoolean();
    }

    /** Returns the type of the operator. */
    @Type
    public int getType() {
        return mType;
    }

    /** Returns whether to reduce to a single value using the operator (if applicable). */
    public boolean reduce() {
        return mReduce;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o != null && o instanceof Operator other) {
            return mType == other.mType && mReduce == other.mReduce;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mType, mReduce);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(mType);
        dest.writeBoolean(mReduce);
    }

    @NonNull
    public static final Parcelable.Creator<Operator> CREATOR =
            new Parcelable.Creator<Operator>() {
                @Override
                public Operator createFromParcel(@NonNull Parcel in) {
                    return new Operator(in);
                }

                @Override
                public Operator[] newArray(int size) {
                    return new Operator[size];
                }
            };

    /** Builder for {@link Operator}. */
    @SystemApi
    @FlaggedApi(Flags.FLAG_ENABLE_PWM_SERVICE)
    public static final class Builder {
        @Type private final int mType;
        private boolean mReduce = false;

        /**
         * Constructs a new {@link Builder} with the given operator type.
         *
         * @param type The operator type.
         */
        public Builder(@Type int type) {
            mType = type;
        }

        /**
         * Sets whether to reduce to a single value when applying the operator. If the operator does
         * not support reduction, this value is ignored.
         *
         * @param reduce Whether to reduce to a single value.
         * @return The {@link Builder} for chaining.
         */
        @SuppressLint("MissingGetterMatchingBuilder")
        @NonNull
        public Builder setReduce(boolean reduce) {
            mReduce = reduce;
            return this;
        }

        @NonNull
        public Operator build() {
            return new Operator(this);
        }
    }
}
