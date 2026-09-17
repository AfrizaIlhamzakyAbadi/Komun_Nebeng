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
 * Defines rendering modes for PDF page objects (fill, stroke, etc.).
 *
 * <p>
 * This final class provides constants for specifying how graphical elements
 * are rendered on a PDF page. It cannot be instantiated.
 *
 * <p>
 * Rendering modes:
 * <ul>
 * <li>{@link #UNKNOWN}: Unknown mode.
 * <li>{@link #FILL}: Fill object.
 * <li>{@link #STROKE}: Stroke object.
 * <li>{@link #FILL_STROKE}: Fill and stroke object. </ul>
 */
@FlaggedApi(Flags.FLAG_ENABLE_EDIT_PDF_PAGE_OBJECTS)
public final class PdfPageObjectRenderMode {
    // Private constructor
    private PdfPageObjectRenderMode() {
    }

    /**
     * Unknown Mode
     */
    public static final int UNKNOWN = -1;

    /**
     * Fill Mode
     */
    public static final int FILL = 0;

    /**
     * Stroke Mode
     */
    public static final int STROKE = 1;

    /**
     * FillStroke Mode
     */
    public static final int FILL_STROKE = 2;

    /** @hide */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({UNKNOWN, FILL, STROKE, FILL_STROKE})
    public @interface Type {
    }
}
