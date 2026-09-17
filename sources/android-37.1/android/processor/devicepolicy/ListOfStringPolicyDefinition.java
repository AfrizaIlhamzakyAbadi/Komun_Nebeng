/*
 * Copyright (C) 2025 The Android Open Source Project
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

package android.processor.devicepolicy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Metadata for a string list policy. */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD})
public @interface ListOfStringPolicyDefinition {
    /** Base data for all policies. */
    PolicyDefinition base();

    /**
     * By default an empty list is not allowed as a policy value. Set it to true if it should be
     * allowed.
     */
    boolean emptyListAllowed() default false;

    /**
     * By default an empty string is not allowed as a policy value. Set it to true if it should be
     * allowed.
     */
    boolean emptyStringAllowed() default false;

    /**
     * By default unprintable characters are not allowed in the policy value. Set it to true if they
     * should be allowed.
     *
     * <p>ISO control characters (a set of unprintable characters mostly used to control terminal
     * functionality) are the only unprintable characters being checked.
     */
    boolean unprintableCharactersAllowed() default false;

    /**
     * By default a string that only contains whitespace characters is not allowed as a policy
     * value. A string is considered to be pure whitespace if it's not empty and {@link
     * java.lang.String#isBlank()} returns true. Set this to true if pure whitespace strings should
     * be allowed.
     */
    boolean pureWhitespaceAllowed() default false;

    /**
     * By default a string that contains leading or trailing whitespace is not allowed. A string is
     * considered unstripped if it starts or ends with characters for which {@link
     * java.lang.String#isBlank()} returns true.
     */
    boolean unstrippedStringAllowed() default false;

    /** Indicates the conflict resolution mechanism used by this policy. */
    ListResolutionMechanism resolutionMechanism();

    /**
     * The maximum length allowed (inclusive) for each string in the list. If not set, no maximum
     * length is enforced. The length is equal to the number of Unicode code units in the string.
     */
    int maxStringLength() default Integer.MAX_VALUE;

    /**
     * The maximum length allowed (inclusive) for the list. If not set, a default maximum length of
     * 10000 is enforced.
     */
    int maxListLength() default 10000;
}
