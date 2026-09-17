/*
 * Copyright (C) 2024 The Android Open Source Project
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

import android.annotation.ColorInt;
import android.annotation.FlaggedApi;
import android.annotation.NonNull;
import android.graphics.pdf.flags.Flags;

/**
 * Represents a text object on a PDF page.
 * This class extends PageObject and provides methods to access and modify the text content.
 */
@FlaggedApi(Flags.FLAG_ENABLE_EDIT_PDF_TEXT_OBJECTS)
public final class PdfPageTextObject extends PdfPageObject {
    private String mText;
    private final PdfPageTextObjectFont mFont;
    private final float mFontSize;
    private @ColorInt int mStrokeColor;
    private float mStrokeWidth = 1.0f;
    private @ColorInt int mFillColor;
    private @PdfPageObjectRenderMode.Type int mRenderMode;

    /**
     * Constructor for the PdfPageTextObject.
     * Sets the object type to TEXT and initializes the text color to black.
     *
     * @param font The font of the text.
     * @param fontSize The font size of the text.
     */
    public PdfPageTextObject(@NonNull String text, @NonNull PdfPageTextObjectFont font,
            float fontSize) {
        super(PdfPageObjectType.TEXT);
        this.mText = text;
        this.mFont = font;
        this.mFontSize = fontSize;
        if (Flags.enableEditPdfPageObjects()) {
            this.mRenderMode = PdfPageObjectRenderMode.FILL;
        }
    }

    /**
     * Returns the text content of the object.
     *
     * @return The text content.
     */
    @NonNull
    public String getText() {
        return mText;
    }

    /**
     * Sets the text content of the object.
     *
     * @param text The text content to set.
     */
    public void setText(@NonNull String text) {
        this.mText = text;
    }

    /**
     * Returns the font size of the object.
     *
     * @return The font size.
     */
    public float getFontSize() {
        return mFontSize;
    }

    /**
     * Returns the font of the text.
     *
     * @return A copy of the font object.
     */
    @NonNull
    public PdfPageTextObjectFont getFont() {
        return new PdfPageTextObjectFont(mFont);
    }

    /**
     * Returns the fill color of the object.
     *
     * @return The fill color of the object.
     */
    public @ColorInt int getFillColor() {
        return mFillColor;
    }

    /**
     * Sets the fill color of the object.
     *
     * @param  fillColor The fill color of the object.
     */
    public void setFillColor(@ColorInt int fillColor) {
        this.mFillColor = fillColor;
    }

    /**
     * Returns the stroke width of the object.
     *
     * @return The stroke width of the object.
     */
    public float getStrokeWidth() {
        return mStrokeWidth;
    }

    /**
     * Sets the stroke width of the object.
     *
     * @param strokeWidth The stroke width of the object.
     */
    public void setStrokeWidth(float strokeWidth) {
        mStrokeWidth = strokeWidth;
    }

    /**
     * Returns the stroke color of the object.
     *
     * @return The stroke color of the object.
     */
    public @ColorInt int getStrokeColor() {
        return mStrokeColor;
    }

    /**
     * Sets the stroke color of the object.
     *
     * @param strokeColor The stroke color of the object.
     */
    public void setStrokeColor(@ColorInt int strokeColor) {
        this.mStrokeColor = strokeColor;
    }

    /**
     * Returns the render mode of the object.
     *
     * @return The render mode of the object.
     */
    public @PdfPageObjectRenderMode.Type int getRenderMode() {
        return mRenderMode;
    }

    /**
     * Sets the render mode of the object.
     *
     * @param renderMode The render mode to be set.
     */
    public void setRenderMode(@PdfPageObjectRenderMode.Type int renderMode) {
        mRenderMode = renderMode;
    }
}
