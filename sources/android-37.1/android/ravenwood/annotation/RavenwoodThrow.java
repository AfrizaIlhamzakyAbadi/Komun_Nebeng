/*
 * Copyright (C) 2023 The Android Open Source Project
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
package android.ravenwood.annotation;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;

import android.annotation.Hide;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// NOTE: THE LINKED DOC IN THE JAVADOC IS AVAILABLE AT:
// $ANDROID_BUILD_TOP/frameworks/base/ravenwood/doc/Ravenwood-API-Annotations.md
/**
 * Forces a method or constructor to throw an exception on Ravenwood, even inside
 * {@link RavenwoodKeepWholeClass}.
 *
 * See <a href="http://ac/frameworks/base/ravenwood/doc/Ravenwood-API-Annotations.md">
 *     {@code frameworks/base/ravenwood/doc/Ravenwood-API-Annotations.md}</a> for more details.
 */
@Hide
@Target({METHOD, CONSTRUCTOR})
@Retention(RetentionPolicy.CLASS)
public @interface RavenwoodThrow {
    /**
     * One or more classes that aren't yet supported by Ravenwood, which is why this method throws.
     */
    Class<?>[] blockedBy() default {};

    /**
     * General free-form description of why this method throws.
     */
    String reason() default "";

    /**
     * Tracking bug number, if any.
     */
    long bug() default 0;

    /** Optional, human-readable comment */
    String comment() default "";
}
