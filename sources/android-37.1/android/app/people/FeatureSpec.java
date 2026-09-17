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

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.SystemApi;
import android.app.people.flags.Flags;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

/**
 * Configures how a derived feature should be calculated. For example, given a base feature
 * "contact_name", a derived feature can be defined as
 *
 * <pre>
 * FeatureSpec contactNameHasKeyword =
 *     new FeatureSpec.Builder(
 *             new Expression.Builder(Operator.REGEX_MATCH)
 *                 .setOperands(
 *                     Arrays.asList(
 *                         Operand.key(Constants.CONTACT_DISPLAY_NAME),
 *                         Operand.constant(Feature.stringFeature("keyword"))))
 *                 .build())
 *         .setDefaultFeature(Feature.booleanFeature(false))
 *         .build();
 * </pre>
 *
 * At inference time, the actual feature value of "contact_name" will be substituted into this
 * feature specification to produce the derived feature value.
 */
@SystemApi
@FlaggedApi(Flags.FLAG_ENABLE_PWM_SERVICE)
public final class FeatureSpec implements Parcelable {
    @NonNull private final Expression mFormula;
    @Nullable private final Feature mDefaultFeature;

    private FeatureSpec(@NonNull Builder builder) {
        mFormula = builder.mFormula;
        mDefaultFeature = builder.mDefaultFeature;
    }

    /**
     * Creates a {@link FeatureSpec} from the given parcel with recursion depth check.
     *
     * @param in The parcel to read from.
     * @param depth The current recursion depth.
     */
    @Hide
    public FeatureSpec(@NonNull Parcel in, int depth) {
        depth = ParcelUtils.checkRecursionDepth(depth);
        Expression formula = ParcelUtils.readTypedObject(in, depth, Expression::new);
        mFormula = checkNotNull(formula, "formula cannot be null");
        mDefaultFeature = in.readTypedObject(Feature.CREATOR);
    }

    /** Returns the formula for calculating the feature. */
    @NonNull
    public Expression getFormula() {
        return mFormula;
    }

    /**
     * Returns the default value for the feature. This is used in inference when the runtime value
     * is absent, e.g. the base features it's derived from are absent.
     */
    @Nullable
    public Feature getDefaultFeature() {
        return mDefaultFeature;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o != null && o instanceof FeatureSpec other) {
            return Objects.equals(mFormula, other.mFormula)
                    && Objects.equals(mDefaultFeature, other.mDefaultFeature);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mFormula, mDefaultFeature);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeTypedObject(mFormula, flags);
        dest.writeTypedObject(mDefaultFeature, flags);
    }

    @NonNull
    public static final Parcelable.Creator<FeatureSpec> CREATOR =
            new Parcelable.Creator<FeatureSpec>() {
                @Override
                public FeatureSpec createFromParcel(@NonNull Parcel in) {
                    return new FeatureSpec(in, /* depth= */ 0);
                }

                @Override
                public FeatureSpec[] newArray(int size) {
                    return new FeatureSpec[size];
                }
            };

    /** Builder for {@link FeatureSpec}. */
    @SystemApi
    @FlaggedApi(Flags.FLAG_ENABLE_PWM_SERVICE)
    public static final class Builder {
        @NonNull private final Expression mFormula;
        @Nullable private Feature mDefaultFeature = null;

        /**
         * Constructs a new {@link Builder} with the given formula.
         *
         * @param formula The formula for calculating the feature.
         */
        public Builder(@NonNull Expression formula) {
            mFormula = checkNotNull(formula, "formula cannot be null");
        }

        /**
         * Sets the default value for the feature.
         *
         * @param defaultFeature The default value for the feature.
         * @return The {@link Builder} for chaining.
         */
        @NonNull
        public Builder setDefaultFeature(@Nullable Feature defaultFeature) {
            mDefaultFeature = defaultFeature;
            return this;
        }

        /** Builds a {@link FeatureSpec}. */
        @NonNull
        public FeatureSpec build() {
            return new FeatureSpec(this);
        }
    }
}
