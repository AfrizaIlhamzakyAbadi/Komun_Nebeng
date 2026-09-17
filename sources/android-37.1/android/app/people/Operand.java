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

import static com.android.internal.util.Preconditions.checkNotNull;
import static com.android.internal.util.Preconditions.checkState;

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.IntDef;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.SystemApi;
import android.app.people.flags.Flags;
import android.os.Parcel;
import android.os.Parcelable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

/** Representation of an operand in an {@link Expression}. */
@SystemApi
@FlaggedApi(Flags.FLAG_ENABLE_PWM_SERVICE)
public final class Operand implements Parcelable {

    /**
     * Supported types of operands.
     */
    @Hide
    @IntDef({
        TYPE_KEY,
        TYPE_CONSTANT,
        TYPE_EXPRESSION,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {}

    /**
     * Denotes the type of operand as the key of a {@link Feature} or a common constant (e.g. pi).
     * When it refers to a feature, the key should be either the id of a predefined base feature
     * (defined by {@link Constants#BaseFeature}) or the id of a derived feature represented as map
     * keys in {@link RankingSpec#getFeatures}.
     */
    public static final int TYPE_KEY = 1;

    /** Denotes the type of operand as a custom constant. */
    public static final int TYPE_CONSTANT = 2;

    /** Denotes the type of operand as an expression. */
    public static final int TYPE_EXPRESSION = 3;

    @Type private final int mType;
    @NonNull private final Object mValue;

    private Operand(@Type int type, @NonNull Object value) {
        mType = type;
        mValue = checkNotNull(value, "value cannot be null");
    }

    /**
     * Creates an {@link Operand} from the given parcel with recursion depth check.
     *
     * @param in The parcel to read from.
     * @param depth The current recursion depth.
     */
    @Hide
    public Operand(@NonNull Parcel in, int depth) {
        depth = ParcelUtils.checkRecursionDepth(depth);
        mType = in.readInt();
        Object value =
                switch (mType) {
                    case TYPE_KEY -> in.readInt();
                    case TYPE_CONSTANT -> in.readTypedObject(Feature.CREATOR);
                    case TYPE_EXPRESSION -> ParcelUtils.readTypedObject(in, depth, Expression::new);
                    default -> throw new IllegalArgumentException("Unknown operand type: " + mType);
                };
        mValue = checkNotNull(value, "value cannot be null");
    }

    /**
     * Creates an {@link Operand} with the given key, which is either the id of a predefined base
     * feature (defined by {@link Constants#BaseFeature}) or the id of a derived feature represented
     * as map keys in {@link RankingSpec#getFeatures}.
     *
     * @param key The key of the operand.
     * @return A new {@link Operand} with the given key.
     */
    @NonNull
    public static Operand key(int key) {
        return new Operand(TYPE_KEY, key);
    }

    /**
     * Creates an {@link Operand} with the given constant.
     *
     * @param constant The constant of the operand.
     * @return A new {@link Operand} with the given constant.
     */
    @NonNull
    public static Operand constant(@NonNull Feature constant) {
        return new Operand(TYPE_CONSTANT, constant);
    }

    /**
     * Creates an {@link Operand} with the given expression.
     *
     * @param expression The expression of the operand.
     * @return A new {@link Operand} with the given expression.
     */
    @NonNull
    public static Operand expression(@NonNull Expression expression) {
        return new Operand(TYPE_EXPRESSION, expression);
    }

    /** Returns the type of the operand. */
    @Type
    public int getType() {
        return mType;
    }

    /** Returns the int key if the operand is a key, otherwise returns {@link Integer#MIN_VALUE}. */
    public int getKey() {
        return mType == TYPE_KEY ? (int) mValue : Integer.MIN_VALUE;
    }

    /** Returns the constant if the operand is a constant, otherwise returns null. */
    @Nullable
    public Feature getConstant() {
        return mType == TYPE_CONSTANT ? (Feature) mValue : null;
    }

    /** Returns the expression if the operand is an expression, otherwise returns null. */
    @Nullable
    public Expression getExpression() {
        return mType == TYPE_EXPRESSION ? (Expression) mValue : null;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o != null && o instanceof Operand other) {
            return mType == other.mType && Objects.equals(mValue, other.mValue);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mType, mValue);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(mType);
        switch (mType) {
            case TYPE_KEY -> dest.writeInt((int) mValue);
            case TYPE_CONSTANT -> dest.writeTypedObject((Feature) mValue, flags);
            case TYPE_EXPRESSION -> dest.writeTypedObject((Expression) mValue, flags);
        }
    }

    @NonNull
    public static final Parcelable.Creator<Operand> CREATOR =
            new Parcelable.Creator<Operand>() {
                @Override
                public Operand createFromParcel(@NonNull Parcel in) {
                    return new Operand(in, /* depth= */ 0);
                }

                @Override
                public Operand[] newArray(int size) {
                    return new Operand[size];
                }
            };
}
