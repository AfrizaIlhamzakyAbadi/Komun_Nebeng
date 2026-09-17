/*
 * Copyright (C) 2018 The Android Open Source Project
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
package android.view.contentcapture;

import android.annotation.Hide;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.content.ComponentName;
import android.graphics.Insets;
import android.graphics.Rect;
import android.os.IBinder;
import android.util.SparseArray;
import android.view.autofill.AutofillId;
import android.view.contentcapture.ViewNode.ViewStructureImpl;

import com.android.internal.annotations.VisibleForTesting;

import java.util.ArrayList;

/**
 * A session that is explicitly created by the app (and hence is a descendant of
 * {@link MainContentCaptureSession}).
 */
@Hide
@VisibleForTesting
public final class ChildContentCaptureSession extends ContentCaptureSession {

    @NonNull
    private final ContentCaptureSession mParent;

    @Hide
    @VisibleForTesting
    public ChildContentCaptureSession(@NonNull ContentCaptureSession parent,
            @NonNull ContentCaptureContext clientContext) {
        super(clientContext);
        mParent = parent;
    }

    @NonNull
    @Override
    protected ContentCaptureSession getMainCaptureSession() {
        return mParent.getMainCaptureSession();
    }

    @Override
    protected void start(@NonNull IBinder token, @NonNull IBinder shareableActivityToken,
            @NonNull ComponentName component, int flags) {
        getMainCaptureSession().start(token, shareableActivityToken, component, flags);
    }

    @Override
    protected boolean isDisabled() {
        return getMainCaptureSession().isDisabled();
    }

    @Override
    protected boolean setDisabled(boolean disabled) {
        return getMainCaptureSession().setDisabled(disabled);
    }

    @Override
    protected ContentCaptureSession newChild(@NonNull ContentCaptureContext clientContext) {
        final ContentCaptureSession child = new ChildContentCaptureSession(this, clientContext);
        internalNotifyChildSessionStarted(mId, child.mId, clientContext);
        return child;
    }

    @Override
    protected void flush(@FlushReason int reason) {
        mParent.flush(reason);
    }

    @Override
    public void updateContentCaptureContext(@Nullable ContentCaptureContext context) {
        internalNotifyContextUpdated(mId, context);
    }

    @Override
    protected void onDestroy() {
        internalNotifyChildSessionFinished(mParent.mId, mId);
    }

    @Override
    protected void internalNotifyChildSessionStarted(int parentSessionId, int childSessionId,
            @NonNull ContentCaptureContext clientContext) {
        getMainCaptureSession()
                .internalNotifyChildSessionStarted(parentSessionId, childSessionId, clientContext);
    }

    @Override
    protected void internalNotifyChildSessionFinished(int parentSessionId, int childSessionId) {
        getMainCaptureSession().internalNotifyChildSessionFinished(parentSessionId, childSessionId);
    }

    @Override
    protected void internalNotifyContextUpdated(int sessionId,
            @Nullable ContentCaptureContext context) {
        getMainCaptureSession().internalNotifyContextUpdated(sessionId, context);
    }

    @Override
    protected void internalNotifyViewAppeared(int sessionId, @NonNull ViewStructureImpl node) {
        getMainCaptureSession().internalNotifyViewAppeared(sessionId, node);
    }

    @Override
    protected void internalNotifyViewDisappeared(int sessionId, @NonNull AutofillId id) {
        getMainCaptureSession().internalNotifyViewDisappeared(sessionId, id);
    }

    @Override
    protected void internalNotifyViewTextChanged(
            int sessionId, @NonNull AutofillId id, @Nullable CharSequence text) {
        getMainCaptureSession().internalNotifyViewTextChanged(sessionId, id, text);
    }

    @Override
    protected void internalNotifyViewInsetsChanged(int sessionId, @NonNull Insets viewInsets) {
        getMainCaptureSession().internalNotifyViewInsetsChanged(mId, viewInsets);
    }

    @Override
    protected void internalNotifyViewTreeEvent(int sessionId, boolean started) {
        getMainCaptureSession().internalNotifyViewTreeEvent(sessionId, started);
    }

    @Override
    protected void internalNotifySessionResumed() {
        getMainCaptureSession().internalNotifySessionResumed();
    }

    @Override
    protected void internalNotifySessionPaused() {
        getMainCaptureSession().internalNotifySessionPaused();
    }

    @Override
    protected void internalNotifySessionFlushEvent(int sessionId) {
        getMainCaptureSession().internalNotifySessionFlushEvent(sessionId);
    }

    @Override
    protected void internalNotifyContentInteractionEvent(int sessionId,
            @NonNull AutofillId autofillId) {
        getMainCaptureSession().internalNotifyContentInteractionEvent(sessionId, autofillId);
    }

    @Override
    protected boolean isContentCaptureEnabled() {
        return getMainCaptureSession().isContentCaptureEnabled();
    }

    @Override
    public void notifyWindowBoundsChanged(int sessionId, @NonNull Rect bounds) {
        getMainCaptureSession().notifyWindowBoundsChanged(sessionId, bounds);
    }

    @Override
    public void notifyContentCaptureEvents(
            @NonNull SparseArray<ArrayList<Object>> contentCaptureEvents) {
        getMainCaptureSession().notifyContentCaptureEvents(contentCaptureEvents);
    }

    @Override
    public void notifyContentCaptureInteractionEvents(
            @NonNull SparseArray<ArrayList<Object>> contentCaptureInteractionEvents) {
        getMainCaptureSession().notifyContentCaptureInteractionEvents(
                contentCaptureInteractionEvents);
    }

    @Override
    @ContentCaptureVersion
    public int getContentCaptureVersion() {
        return getMainCaptureSession().getContentCaptureVersion();
    }
}
