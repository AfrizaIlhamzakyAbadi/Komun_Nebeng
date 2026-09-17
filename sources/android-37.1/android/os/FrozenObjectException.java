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

package android.os;

import android.annotation.FlaggedApi;
import android.annotation.NonNull;

/**
 * Exception thrown when a binder transaction is made on a frozen binder object.
 * <p>
 * Frozen binder objects cannot receive incoming transactions. Callers must
 * handle this failure by either dropping the transaction or rescheduling
 * it to be retried later.
 *
 * @see android.os.IBinder.FrozenStateChangeCallback
 */
@FlaggedApi(Flags.FLAG_ENABLE_FROZEN_OBJECT_EXCEPTION)
public class FrozenObjectException extends RemoteException {

    @FlaggedApi(Flags.FLAG_ENABLE_FROZEN_OBJECT_EXCEPTION)
    public FrozenObjectException() {
        super();
    }

    @FlaggedApi(Flags.FLAG_ENABLE_FROZEN_OBJECT_EXCEPTION)
    public FrozenObjectException(@NonNull String message) {
        super(message);
    }
}
