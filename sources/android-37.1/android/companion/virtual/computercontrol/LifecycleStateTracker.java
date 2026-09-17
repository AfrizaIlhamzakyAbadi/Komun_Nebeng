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

package android.companion.virtual.computercontrol;

import static android.companion.virtual.computercontrol.LifecycleState.ACTIVE;

import android.annotation.CallbackExecutor;
import android.annotation.Hide;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.companion.virtual.computercontrol.LifecycleState.Active;
import android.companion.virtual.computercontrol.LifecycleState.Blocked;
import android.companion.virtual.computercontrol.LifecycleState.Closed;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Tracks the lifecycle state transitions for a ComputerControlSession. It is used to notify
 * lifecycle callbacks.
 *
 * <p>Callbacks are fired in the order they are added. All callbacks are fired from the calling
 * thread.
 *
 * NOTE: This class is NOT thread safe.
 */
@Hide
public final class LifecycleStateTracker implements ComputerControlSession.LifecycleCallback {

    // This is the initial state that callbacks are expected to be in before they are added.
    // If this changes, we must also update the LifecycleCallback documentation.
    // Callbacks will start in an "uninitialized" state.
    private static final LifecycleState INITIAL_STATE = null;

    private final List<CallbackRecord> mCallbacks = new ArrayList<>();
    private final AtomicBoolean mIsNotifyingCallbacks = new AtomicBoolean(false);

    private LifecycleState mState = INITIAL_STATE;

    private record CallbackRecord(Consumer<LifecycleState> stateConsumer, Object callbackToken) {}

    /**
     * Adds a lifecycle callback that should be notified for state changes. When a new callback is
     * added, it is assumed to be in an "active" state.
     *
     * <p>This tracker's state must NOT be updated from inside one of the callbacks.
     */
    public void addCallback(@NonNull @CallbackExecutor Executor executor,
            @NonNull ComputerControlSession.LifecycleCallback callback) {
        addStateConsumerInternal(executor, state -> notifyCallback(state, callback), callback);
    }

    /**
     * Removes a lifecycle callback.
     */
    public void removeCallback(@NonNull ComputerControlSession.LifecycleCallback callback) {
        if (!mCallbacks.removeIf(record -> record.callbackToken == callback)) {
            throw new IllegalStateException("Callback not registered");
        }
    }

    /**
     * Adds a consumer that should be notified for state changes.
     */
    public void addStateConsumer(@NonNull @CallbackExecutor Executor executor,
            @NonNull Consumer<LifecycleState> consumer) {
        addStateConsumerInternal(executor, consumer, consumer);
    }

    private void addStateConsumerInternal(@NonNull @CallbackExecutor Executor executor,
            @NonNull Consumer<LifecycleState> consumer, Object token) {
        if (containsIf(mCallbacks, record -> record.callbackToken == token)) {
            throw new IllegalStateException("Callback already registered");
        }
        var record =
                new CallbackRecord(state -> executor.execute(() -> consumer.accept(state)), token);
        mCallbacks.add(record);
        if (Objects.equals(mState, INITIAL_STATE)) {
            return;
        }
        record.stateConsumer.accept(mState);
    }

    /**
     * Get the current lifecycle state.
     *
     * @return the current lifecycle state, or null if the tracker is in an "uninitialized" state.
     */
    public LifecycleState getCurrentState() {
        return mState;
    }

    private void notifyAllCallbacks() {
        if (!mIsNotifyingCallbacks.compareAndSet(false, true)) {
            throw new IllegalStateException(
                    "Concurrent state modifications: "
                            + "The state must not be updated from inside a callback!");
        }

        for (var record : new ArrayList<>(mCallbacks)) {
            record.stateConsumer.accept(mState);
        }
        mIsNotifyingCallbacks.set(false);
    }

    private static void notifyCallback(LifecycleState state,
            ComputerControlSession.LifecycleCallback callback) {
        switch (state) {
            case Active active -> callback.onActive();
            case Blocked blocked -> callback.onBlocked(blocked.reason, blocked.blockingPackage);
            case Closed closed -> callback.onClosed(closed.reason);
        }
    }

    @Override
    public void onActive() {
        transitionTo(ACTIVE);
    }

    @Override
    public void onBlocked(@ComputerControlSession.SessionBlockReason int initialBlockReason,
            @Nullable String blockingPackage) {
        transitionTo(new Blocked(initialBlockReason, blockingPackage));
    }

    @Override
    public void onClosed(@ComputerControlSession.SessionCloseReason int reason) {
        if (mState instanceof Closed) {
            return;
        }
        transitionTo(new Closed(reason));
    }

    private void transitionTo(@NonNull LifecycleState state) {
        if (mState instanceof Closed) {
            throw new IllegalStateException("Cannot change state: Session is closed");
        }
        if (Objects.equals(state, mState)) {
            throw new IllegalStateException("Cannot change state: Session is already in:" + state);
        }
        mState = state;
        notifyAllCallbacks();
    }

    private static <T> boolean containsIf(List<T> list, Predicate<T> predicate) {
        for (int i = 0; i < list.size(); i++) {
            if (predicate.test(list.get(i))) {
                return true;
            }
        }
        return false;
    }
}
