/*
 * Copyright (C) 2026 The Android Open Source Project
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

package com.android.internal.widget;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.app.Flags;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RemoteViews;

import java.util.ArrayList;

/**
 * A custom FrameLayout used in notification templates to fix accessibility traversal order.
 *
 * NotificationFrameLayout overrides addChildrenForAccessibility to enforce linear XML
 * declaration-order traversal. This ensures TalkBack navigates notification templates in
 * the correct order without needing nested layout wrappers that could cause
 * performance regressions. See b/434604376.
 * @hide
 */
@RemoteViews.RemoteView
public class NotificationFrameLayout extends FrameLayout {

    public NotificationFrameLayout(@NonNull Context context) {
        super(context);
    }

    public NotificationFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public NotificationFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs,
            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public NotificationFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs,
            int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void addChildrenForAccessibility(ArrayList<View> outChildren) {
        if (!Flags.fixExpandedNotificationA11yTraversal()) {
            super.addChildrenForAccessibility(outChildren);
            return;
        }

        if (getAccessibilityNodeProvider() != null) {
            return;
        }
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == VISIBLE) {
                if (child.includeForAccessibility()) {
                    outChildren.add(child);
                } else {
                    child.addChildrenForAccessibility(outChildren);
                }
            }
        }
    }
}
