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
import android.annotation.NonNull;
import android.graphics.pdf.flags.Flags;

@FlaggedApi(Flags.FLAG_ENABLE_EDIT_PDF_TEXT_OBJECTS)
public class PdfPageTextObjectFont {
    private @PdfPageTextObjectFontFamily.Type int mFontFamily;
    private boolean mIsBold;
    private boolean mIsItalic;

    public PdfPageTextObjectFont(@PdfPageTextObjectFontFamily.Type int fontFamily,
            boolean isBold, boolean isItalic) {
        mFontFamily = fontFamily;
        mIsBold = isBold;
        mIsItalic = isItalic;
    }

    public PdfPageTextObjectFont(@NonNull PdfPageTextObjectFont font) {
        this.mFontFamily = font.getFontFamily();
        this.mIsBold = font.isBold();
        this.mIsItalic = font.isItalic();
    }

    /**
     * Returns the font-family which is of type {@link PdfPageTextObjectFontFamily}
     *
     * @return The font-family.
     */
    public @PdfPageTextObjectFontFamily.Type int getFontFamily() {
        return mFontFamily;
    }

    /**
     * Set the font family of the object.
     *
     * @param fontFamily The font family to be set.
     */
    public void setFontFamily(@PdfPageTextObjectFontFamily.Type int fontFamily) {
        mFontFamily = fontFamily;
    }

    /**
     * Determines if the text is bold.
     *
     * @return true if the text is bold, false otherwise.
     */
    public boolean isBold() {
        return mIsBold;
    }

    /**
     * Sets whether the text should be bold or not.
     *
     * @param bold true if the text should be bold, false otherwise.
     */
    public void setBold(boolean bold) {
        mIsBold = bold;
    }

    /**
     * Determines if the text is italic.
     *
     * @return true if the text is italic, false otherwise.
     */
    public boolean isItalic() {
        return mIsItalic;
    }

    /**
     * Set whether the text should be italic or not.
     *
     * @param italic true if the text should be italic, false otherwise.
     */
    public void setItalic(boolean italic) {
        mIsItalic = italic;
    }
}
