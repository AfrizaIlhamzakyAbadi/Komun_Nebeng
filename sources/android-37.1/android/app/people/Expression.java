/**
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

import static com.android.internal.util.Preconditions.checkArgument;
import static com.android.internal.util.Preconditions.checkNotNull;

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.SuppressLint;
import android.annotation.SystemApi;
import android.app.people.flags.Flags;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Generic representation of an expression (logical, arithmetic, etc.). The operators will be
 * applied in the order as they appear in the list of {@link #getOperators}. For each operator, the
 * operands are consumed sequentially from {@link #getOperands} as needed. For example, given an
 * expression with
 *
 * <ul>
 *   <li>operators = [SUB, ABS, POW, DIV, SUM, RELU]
 *   <li>operands = [x1, x2, x3, x4, x5, x6]
 * </ul>
 *
 * The expression will be evaluated as ReLU(|x1 - x2|^x3 / x4 + x5 + x6).
 *
 * <p>Note that only up to 2048 {@link Operand}s are supported per {@link Expression}.
 */
@SystemApi
@FlaggedApi(Flags.FLAG_ENABLE_PWM_SERVICE)
public final class Expression implements Parcelable {
    @NonNull private final List<Operator> mOperators;
    @NonNull private final List<Operand> mOperands;

    private Expression(@NonNull Builder builder) {
        checkArgument(!builder.mOperators.isEmpty(), "operators cannot be empty");
        mOperators = Collections.unmodifiableList(builder.mOperators);
        mOperands = Collections.unmodifiableList(builder.mOperands);
    }

    /**
     * Creates an {@link Expression} from the given parcel with recursion depth check.
     *
     * @param in The parcel to read from.
     * @param depth The current recursion depth.
     */
    @Hide
    public Expression(@NonNull Parcel in, int depth) {
        depth = ParcelUtils.checkRecursionDepth(depth);
        List<Operator> operators = new ArrayList<>();
        in.readTypedList(operators, Operator.CREATOR);
        checkArgument(!operators.isEmpty(), "operators cannot be empty");
        mOperators = Collections.unmodifiableList(operators);
        mOperands = ParcelUtils.readTypedList(in, depth, Operand::new);
        checkArgument(!mOperands.isEmpty(), "operands cannot be empty");
    }

    /** Returns the operators of the expression. */
    @NonNull
    public List<Operator> getOperators() {
        return mOperators;
    }

    /** Returns the operands of the expression. */
    @NonNull
    public List<Operand> getOperands() {
        return mOperands;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o != null && o instanceof Expression other) {
            return Objects.equals(mOperators, other.mOperators)
                    && Objects.equals(mOperands, other.mOperands);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mOperators, mOperands);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeTypedList(mOperators, flags);
        dest.writeTypedList(mOperands, flags);
    }

    @NonNull
    public static final Parcelable.Creator<Expression> CREATOR =
            new Parcelable.Creator<Expression>() {
                @Override
                public Expression createFromParcel(@NonNull Parcel in) {
                    return new Expression(in, /* depth= */ 0);
                }

                @Override
                public Expression[] newArray(int size) {
                    return new Expression[size];
                }
            };

    /** Builder for {@link Expression}. */
    @SystemApi
    @FlaggedApi(Flags.FLAG_ENABLE_PWM_SERVICE)
    public static final class Builder {
        @NonNull private final List<Operator> mOperators = new ArrayList<>();
        @NonNull private final List<Operand> mOperands = new ArrayList<>();

        /**
         * Constructs a new {@link Builder} with the first operand.
         *
         * @param operand The first operand of the expression.
         */
        public Builder(@NonNull Operand operand) {
            mOperands.add(checkNotNull(operand, "operand cannot be null"));
        }

        /**
         * Appends a unary operation to the expression.
         *
         * @param operator The unary operator to append.
         * @return The {@link Builder} for chaining.
         */
        @SuppressLint("MissingGetterMatchingBuilder")
        @NonNull
        public Builder addUnaryOperation(@NonNull Operator operator) {
            mOperators.add(checkNotNull(operator, "operator cannot be null"));
            return this;
        }

        /**
         * Appends a binary operation to the expression.
         *
         * @param operator The binary operator to append.
         * @param rhs The right-hand side operand of the binary operator.
         * @return The {@link Builder} for chaining.
         */
        @SuppressLint("MissingGetterMatchingBuilder")
        @NonNull
        public Builder addBinaryOperation(@NonNull Operator operator, @NonNull Operand rhs) {
            mOperators.add(checkNotNull(operator, "operator cannot be null"));
            mOperands.add(checkNotNull(rhs, "rhs cannot be null"));
            return this;
        }

        /**
         * Appends a list operation to the expression.
         *
         * @param operator The list operator to append.
         * @param operands The remaining operands for the list operation, other than the ones
         *     already added in the previous operations.
         * @return The {@link Builder} for chaining.
         */
        @SuppressLint("MissingGetterMatchingBuilder")
        @NonNull
        public Builder addListOperation(@NonNull Operator operator, @NonNull Operand... operands) {
            mOperators.add(checkNotNull(operator, "operator cannot be null"));
            for (Operand operand : operands) {
                mOperands.add(checkNotNull(operand, "operand cannot be null"));
            }
            return this;
        }

        /** Builds a {@link Expression}. */
        @NonNull
        public Expression build() {
            return new Expression(this);
        }
    }
}
