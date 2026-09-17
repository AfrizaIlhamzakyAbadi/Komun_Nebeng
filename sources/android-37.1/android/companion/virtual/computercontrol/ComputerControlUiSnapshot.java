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

package android.companion.virtual.computercontrol;

import android.annotation.Hide;
import android.annotation.IntDef;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import com.android.internal.annotations.VisibleForTesting;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A read-only snapshot of the visible UI structure of the screen.
 *
 * <p>This structure provides a hierarchical and categorical representation of the current visible
 * UI state, containing the visible windows and their respective visible UI hierarchies.</p>
 */
@Hide
public final class ComputerControlUiSnapshot {

    private final List<WindowNode> mWindows;

    /**
     * Creates a snapshot of the visible UI structure from the given accessibility windows.
     *
     * @param windows The list of accessibility windows to capture.
     * @return A read-only snapshot of the visible UI structure.
     */
    @VisibleForTesting
    @NonNull
    public static ComputerControlUiSnapshot fromAccessibilityWindowInfos(
            @NonNull List<AccessibilityWindowInfo> windows) {
        Objects.requireNonNull(windows);
        List<WindowNode> windowNodes = new ArrayList<>(windows.size());
        for (AccessibilityWindowInfo window : windows) {
            if (window != null) {
                AccessibilityNodeInfo rootNode = window.getRoot();
                if (rootNode != null && rootNode.isVisibleToUser()) {
                    windowNodes.add(new WindowNode(window, rootNode));
                }
            }
        }
        return new ComputerControlUiSnapshot(windowNodes);
    }

    private ComputerControlUiSnapshot(@NonNull List<WindowNode> windows) {
        mWindows = Collections.unmodifiableList(windows);
    }

    /**
     * Returns the list of visible window nodes present on the screen.
     */
    @NonNull
    public List<WindowNode> getWindows() {
        return mWindows;
    }

    /**
     * Represents a visible window in the UI structure snapshot.
     */
    public static final class WindowNode {
        private final Rect mBounds;
        @Nullable private final String mTitle;
        @NonNull private final UiNode mRoot;

        private WindowNode(@NonNull AccessibilityWindowInfo window,
                @NonNull AccessibilityNodeInfo rootNode) {
            mBounds = new Rect();
            window.getBoundsInScreen(mBounds);

            CharSequence title = window.getTitle();
            mTitle = title != null ? title.toString() : null;

            mRoot = UiNode.fromAccessibilityNodeInfo(rootNode);
        }

        /**
         * Returns the bounding box of the window in screen pixels.
         */
        @NonNull
        public Rect getBounds() {
            return new Rect(mBounds);
        }

        /**
         * Returns the title of the window, if available.
         */
        @Nullable
        public String getTitle() {
            return mTitle;
        }

        /**
         * Returns the root UI node of the window's visible UI hierarchy.
         */
        @NonNull
        public UiNode getRoot() {
            return mRoot;
        }
    }

    /**
     * Represents a visible UI element (node) within a window in the UI structure snapshot.
     */
    public static final class UiNode {
        private static final int FLAG_CLICKABLE = 1 << 0;
        private static final int FLAG_LONG_CLICKABLE = 1 << 1;
        private static final int FLAG_CHECKABLE = 1 << 2;
        private static final int FLAG_CHECKED = 1 << 3;
        private static final int FLAG_FOCUSABLE = 1 << 4;
        private static final int FLAG_FOCUSED = 1 << 5;
        private static final int FLAG_EDITABLE = 1 << 6;
        private static final int FLAG_SCROLLABLE = 1 << 7;
        private static final int FLAG_ENABLED = 1 << 8;
        private static final int FLAG_PASSWORD = 1 << 9;
        private static final int FLAG_SELECTED = 1 << 10;

        /**
         * The maximum depth of the accessibility node tree hierarchy that we will traverse.
         * Potentially untrusted applications (via AccessibilityNodeProvider) can provide an
         * arbitrarily deep or cyclic tree structure. Enforcing a depth limit prevents
         * StackOverflowError and OutOfMemoryError during recursive traversal, bounding resource
         * consumption and synchronous Binder call overhead.
         */
        private static final int MAX_DEPTH = 50;

        @IntDef(flag = true, prefix = { "FLAG_" }, value = {
                FLAG_CLICKABLE,
                FLAG_LONG_CLICKABLE,
                FLAG_CHECKABLE,
                FLAG_CHECKED,
                FLAG_FOCUSABLE,
                FLAG_FOCUSED,
                FLAG_EDITABLE,
                FLAG_SCROLLABLE,
                FLAG_ENABLED,
                FLAG_PASSWORD,
                FLAG_SELECTED
        })
        @Retention(RetentionPolicy.SOURCE)
        private @interface UiNodeFlag {}

        private final Rect mBounds;
        private final List<UiNode> mChildren;
        @Nullable private final String mText;
        @Nullable private final String mContentDescription;
        @Nullable private final String mHintText;
        @Nullable private final String mClassName;
        @Nullable private final String mPackageName;
        @Nullable private final String mEntryId;
        private final int mFlags;

        private UiNode(@NonNull AccessibilityNodeInfo node, @NonNull List<UiNode> children) {
            mBounds = new Rect();
            node.getBoundsInScreen(mBounds);

            CharSequence text = node.getText();
            mText = text != null ? text.toString() : null;

            CharSequence desc = node.getContentDescription();
            mContentDescription = desc != null ? desc.toString() : null;

            CharSequence hint = node.getHintText();
            mHintText = hint != null ? hint.toString() : null;

            CharSequence className = node.getClassName();
            mClassName = className != null ? className.toString() : null;

            CharSequence packageName = node.getPackageName();
            mPackageName = packageName != null ? packageName.toString() : null;

            mEntryId = node.getViewIdResourceName();

            int flags = 0;
            if (node.isClickable()) flags |= FLAG_CLICKABLE;
            if (node.isLongClickable()) flags |= FLAG_LONG_CLICKABLE;
            if (node.isCheckable()) flags |= FLAG_CHECKABLE;
            if (node.isChecked()) flags |= FLAG_CHECKED;
            if (node.isFocusable()) flags |= FLAG_FOCUSABLE;
            if (node.isFocused()) flags |= FLAG_FOCUSED;
            if (node.isEditable()) flags |= FLAG_EDITABLE;
            if (node.isScrollable()) flags |= FLAG_SCROLLABLE;
            if (node.isEnabled()) flags |= FLAG_ENABLED;
            if (node.isPassword()) flags |= FLAG_PASSWORD;
            if (node.isSelected()) flags |= FLAG_SELECTED;
            mFlags = flags;

            mChildren = Collections.unmodifiableList(children);
        }

        /**
         * Creates a UiNode hierarchy from the given AccessibilityNodeInfo root node.
         */
        @NonNull
        public static UiNode fromAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo rootNode) {
            Objects.requireNonNull(rootNode);
            Set<NodeKey> visited = new HashSet<>();
            visited.add(new NodeKey(rootNode));
            return buildUiNodeRecursively(rootNode, 0, visited);
        }

        private static UiNode buildUiNodeRecursively(
                @NonNull AccessibilityNodeInfo node,
                int depth,
                @NonNull Set<NodeKey> visited) {

            int childCount = node.getChildCount();
            if (childCount == 0 || depth >= MAX_DEPTH) {
                return new UiNode(node, Collections.emptyList());
            }

            List<UiNode> children = new ArrayList<>(childCount);
            for (int i = 0; i < childCount; i++) {
                AccessibilityNodeInfo childNode = node.getChild(i);
                if (childNode != null && childNode.isVisibleToUser()) {
                    NodeKey childKey = new NodeKey(childNode);
                    if (!visited.contains(childKey)) {
                        visited.add(childKey);
                        children.add(buildUiNodeRecursively(childNode, depth + 1, visited));
                    }
                }
            }
            return new UiNode(node, children);
        }

        private static final class NodeKey {
            private final long mId;
            private final AccessibilityNodeInfo mNode;

            NodeKey(AccessibilityNodeInfo node) {
                mNode = node;
                mId = node.getSourceNodeId();
            }

            @Override
            @SuppressWarnings("ReferenceEquality")
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof NodeKey)) return false;
                NodeKey other = (NodeKey) o;
                if (mId != 0 && other.mId != 0) {
                    return mId == other.mId;
                }
                // Fall back to reference equality if source node IDs are 0 (not initialized).
                // Value equality is not used because hashCode() uses System.identityHashCode(mNode)
                // when mId is 0; using value equality would break the equals/hashCode contract.
                return mNode == other.mNode;
            }

            @Override
            public int hashCode() {
                return mId != 0 ? Long.hashCode(mId) : System.identityHashCode(mNode);
            }
        }

        /**
         * Returns the bounding box in screen pixels.
         */
        @NonNull
        public Rect getBounds() {
            return new Rect(mBounds);
        }

        /**
         * Returns visible child UI nodes of this element in the UI hierarchy.
         */
        @NonNull
        public List<UiNode> getChildren() {
            return mChildren;
        }

        /**
         * Returns primary text content.
         */
        @Nullable
        public String getText() {
            return mText;
        }

        /**
         * Returns accessibility description or alternate text.
         */
        @Nullable
        public String getContentDescription() {
            return mContentDescription;
        }

        /**
         * Returns placeholder or instructional hint text.
         */
        @Nullable
        public String getHintText() {
            return mHintText;
        }

        /**
         * Returns the UI component's class identifier.
         */
        @Nullable
        public String getClassName() {
            return mClassName;
        }

        /**
         * Returns the application package hosting the element.
         */
        @Nullable
        public String getPackageName() {
            return mPackageName;
        }

        /**
         * Returns the element's unique resource identifier.
         */
        @Nullable
        public String getEntryId() {
            return mEntryId;
        }

        /**
         * Returns whether element accepts standard click interactions.
         */
        public boolean isClickable() {
            return (mFlags & FLAG_CLICKABLE) != 0;
        }

        /**
         * Returns whether element accepts press-and-hold interactions.
         */
        public boolean isLongClickable() {
            return (mFlags & FLAG_LONG_CLICKABLE) != 0;
        }

        /**
         * Returns whether element supports toggling checked states (e.g., checkboxes).
         */
        public boolean isCheckable() {
            return (mFlags & FLAG_CHECKABLE) != 0;
        }

        /**
         * Returns whether element is currently checked.
         */
        public boolean isChecked() {
            return (mFlags & FLAG_CHECKED) != 0;
        }

        /**
         * Returns whether element can receive input focus.
         */
        public boolean isFocusable() {
            return (mFlags & FLAG_FOCUSABLE) != 0;
        }

        /**
         * Returns whether element currently holds input focus.
         */
        public boolean isFocused() {
            return (mFlags & FLAG_FOCUSED) != 0;
        }

        /**
         * Returns whether element allows user text modification.
         */
        public boolean isEditable() {
            return (mFlags & FLAG_EDITABLE) != 0;
        }

        /**
         * Returns whether element acts as a scrollable container.
         */
        public boolean isScrollable() {
            return (mFlags & FLAG_SCROLLABLE) != 0;
        }

        /**
         * Returns whether element is active and responsive to user input.
         */
        public boolean isEnabled() {
            return (mFlags & FLAG_ENABLED) != 0;
        }

        /**
         * Returns whether element contains masked text for secure entry.
         */
        public boolean isPassword() {
            return (mFlags & FLAG_PASSWORD) != 0;
        }

        /**
         * Returns whether element is currently highlighted or selected.
         */
        public boolean isSelected() {
            return (mFlags & FLAG_SELECTED) != 0;
        }
    }
}
