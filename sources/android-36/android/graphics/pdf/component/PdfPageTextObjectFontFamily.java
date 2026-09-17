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

package android.graphics.pdf.component;

import android.annotation.FlaggedApi;
import android.annotation.IntDef;
import android.graphics.pdf.flags.Flags;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * The class holds the set of font families supported by {@link PdfPageTextObject}.
 * The specified font families are standard font families defined
 * in the PDF Spec 1.7 - Page 146.
 */
@FlaggedApi(Flags.FLAG_ENABLE_EDIT_PDF_TEXT_OBJECTS)
public class PdfPageTextObjectFontFamily {
    private PdfPageTextObjectFontFamily() {}

    /**
     * Courier Font
     */
    public static final int COURIER = 0;

    /**
     * Helvetica Font
     */
    public static final int HELVETICA = 1;

    /**
     * Symbol Font (Note: Renders only symbols)
     */
    public static final int SYMBOL = 2;

    /**
     * TimesNewRoman Font
     */
    public static final int TIMES_NEW_ROMAN = 3;

    /** @hide */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({COURIER, HELVETICA, SYMBOL, TIMES_NEW_ROMAN})
    public @interface Type {
    }
}
