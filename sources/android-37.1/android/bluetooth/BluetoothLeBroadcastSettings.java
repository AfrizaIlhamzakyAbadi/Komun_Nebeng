/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.bluetooth;

import static java.util.Objects.requireNonNull;

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.RequiresNoPermission;
import android.annotation.SystemApi;
import android.media.AudioManager;
import android.os.Parcel;
import android.os.Parcelable;

import com.android.bluetooth.flags.Flags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/** This class contains the broadcast group settings information for this Broadcast Group. */
@Hide
@SystemApi
public final class BluetoothLeBroadcastSettings implements Parcelable {
    private final boolean mIsPublicBroadcast;
    private final String mBroadcastName;
    private final byte[] mBroadcastCode;
    private final BluetoothLeAudioContentMetadata mPublicBroadcastMetadata;
    private final List<BluetoothLeBroadcastSubgroupSettings> mSubgroupSettings;
    private final int mAudioRecordingSessionId;

    private BluetoothLeBroadcastSettings(
            boolean isPublicBroadcast,
            String broadcastName,
            byte[] broadcastCode,
            BluetoothLeAudioContentMetadata publicBroadcastMetadata,
            List<BluetoothLeBroadcastSubgroupSettings> subgroupSettings,
            int audioRecordingSessionId) {
        mIsPublicBroadcast = isPublicBroadcast;
        mBroadcastName = broadcastName;
        mBroadcastCode = broadcastCode;
        mPublicBroadcastMetadata = publicBroadcastMetadata;
        mSubgroupSettings = subgroupSettings;
        mAudioRecordingSessionId = audioRecordingSessionId;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (!(o instanceof BluetoothLeBroadcastSettings)) {
            return false;
        }
        final BluetoothLeBroadcastSettings other = (BluetoothLeBroadcastSettings) o;
        return mIsPublicBroadcast == other.isPublicBroadcast()
                && Objects.equals(mBroadcastName, other.getBroadcastName())
                && Arrays.equals(mBroadcastCode, other.getBroadcastCode())
                && Objects.equals(mPublicBroadcastMetadata, other.getPublicBroadcastMetadata())
                && mSubgroupSettings.equals(other.getSubgroupSettings())
                && mAudioRecordingSessionId == other.mAudioRecordingSessionId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                mIsPublicBroadcast,
                mBroadcastName,
                Arrays.hashCode(mBroadcastCode),
                mPublicBroadcastMetadata,
                mSubgroupSettings,
                mAudioRecordingSessionId);
    }

    /**
     * Return {@code true} if this Broadcast Group is set to broadcast Public Broadcast Announcement
     * otherwise return {@code false}.
     */
    @Hide
    @SystemApi
    @RequiresNoPermission
    public boolean isPublicBroadcast() {
        return mIsPublicBroadcast;
    }

    /**
     * Return the broadcast code for this Broadcast Group.
     *
     * @return Broadcast name for this Broadcast Group, null if no name provided
     */
    @Hide
    @SystemApi
    @RequiresNoPermission
    public @Nullable String getBroadcastName() {
        return mBroadcastName;
    }

    /**
     * Get the Broadcast Code currently set for this broadcast group.
     *
     * <p>Only needed when encryption is enabled
     *
     * <p>As defined in Volume 3, Part C, Section 3.2.6 of Bluetooth Core Specification, Version
     * 5.3, Broadcast Code is used to encrypt a broadcast audio stream.
     *
     * <p>It must be a UTF-8 string that has at least 4 octets and should not exceed 16 octets.
     *
     * @return Broadcast Code currently set for this broadcast group, null if code is not required
     *     or code is currently unknown
     */
    @Hide
    @SystemApi
    @RequiresNoPermission
    public @Nullable byte[] getBroadcastCode() {
        return mBroadcastCode;
    }

    /**
     * Get public broadcast metadata for this Broadcast Group.
     *
     * @return public broadcast metadata for this Broadcast Group, null if no public metadata exists
     */
    @Hide
    @SystemApi
    @RequiresNoPermission
    public @Nullable BluetoothLeAudioContentMetadata getPublicBroadcastMetadata() {
        return mPublicBroadcastMetadata;
    }

    /**
     * Get the audio session ID of the audio recording stream
     * (e.g., {@link android.media.AudioRecord}) that is feeding this broadcast.
     *
     * @return audio session ID of the audio recording stream, or
     *         {@link AudioManager#AUDIO_SESSION_ID_GENERATE} if not explicitly set
     */
    @Hide
    @SystemApi
    @RequiresNoPermission
    @FlaggedApi(Flags.FLAG_LEAUDIO_AURACAST_LIVE_MIC_FEATURE)
    public int getAudioRecordingSessionId() {
        return mAudioRecordingSessionId;
    }

    /**
     * Get available subgroup settings in the broadcast group.
     *
     * @return list of subgroup settings in the broadcast group
     */
    @Hide
    @SystemApi
    @RequiresNoPermission
    public @NonNull List<BluetoothLeBroadcastSubgroupSettings> getSubgroupSettings() {
        return mSubgroupSettings;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeBoolean(mIsPublicBroadcast);
        BluetoothUtils.writeStringToParcel(out, mBroadcastName);
        out.writeByteArray(mBroadcastCode);
        out.writeTypedObject(mPublicBroadcastMetadata, 0);
        out.writeTypedList(mSubgroupSettings);
        out.writeInt(mAudioRecordingSessionId);
    }

    /** A {@link Parcelable.Creator} to create {@link BluetoothLeBroadcastSettings} from parcel. */
    @Hide @SystemApi @NonNull
    public static final Creator<BluetoothLeBroadcastSettings> CREATOR =
            new Creator<>() {
                public @NonNull BluetoothLeBroadcastSettings createFromParcel(@NonNull Parcel in) {
                    Builder builder = new Builder();
                    builder.setPublicBroadcast(in.readBoolean());
                    builder.setBroadcastName(in.readString());
                    byte[] broadcastCode = in.createByteArray();
                    builder.setBroadcastCode(broadcastCode);
                    builder.setPublicBroadcastMetadata(
                            in.readTypedObject(BluetoothLeAudioContentMetadata.CREATOR));
                    final List<BluetoothLeBroadcastSubgroupSettings> subgroupSettings =
                            new ArrayList<>();
                    in.readTypedList(
                            subgroupSettings, BluetoothLeBroadcastSubgroupSettings.CREATOR);
                    for (BluetoothLeBroadcastSubgroupSettings setting : subgroupSettings) {
                        builder.addSubgroupSettings(setting);
                    }
                    final int audioRecordingSessionId = in.readInt();
                    if (Flags.leaudioAuracastLiveMicFeature()) {
                        builder.setAudioRecordingSessionId(audioRecordingSessionId);
                    }
                    return builder.build();
                }

                public @NonNull BluetoothLeBroadcastSettings[] newArray(int size) {
                    return new BluetoothLeBroadcastSettings[size];
                }
            };

    /** Builder for {@link BluetoothLeBroadcastSettings}. */
    @Hide
    @SystemApi
    public static final class Builder {
        private boolean mIsPublicBroadcast = false;
        private String mBroadcastName = null;
        private byte[] mBroadcastCode = null;
        private BluetoothLeAudioContentMetadata mPublicBroadcastMetadata = null;
        private List<BluetoothLeBroadcastSubgroupSettings> mSubgroupSettings = new ArrayList<>();
        private int mAudioRecordingSessionId = AudioManager.AUDIO_SESSION_ID_GENERATE;

        /** Create an empty builder. */
        @Hide
        @SystemApi
        public Builder() {}

        /**
         * Create a builder with copies of information from original object.
         *
         * @param original original object
         */
        @Hide
        @SystemApi
        public Builder(@NonNull BluetoothLeBroadcastSettings original) {
            mIsPublicBroadcast = original.isPublicBroadcast();
            mBroadcastName = original.getBroadcastName();
            mBroadcastCode = original.getBroadcastCode();
            mPublicBroadcastMetadata = original.getPublicBroadcastMetadata();
            mSubgroupSettings = original.getSubgroupSettings();
            mAudioRecordingSessionId = original.mAudioRecordingSessionId;
        }

        /**
         * Set whether the Public Broadcast is on for this broadcast group.
         *
         * @param isPublicBroadcast whether the Public Broadcast is enabled
         * @return this builder
         */
        @Hide
        @SystemApi
        @RequiresNoPermission
        public @NonNull Builder setPublicBroadcast(boolean isPublicBroadcast) {
            mIsPublicBroadcast = isPublicBroadcast;
            return this;
        }

        /**
         * Set broadcast name for the broadcast group.
         *
         * <p>As defined in Public Broadcast Profile V1.0, section 5.1. Broadcast_Name AD Type is a
         * UTF-8 encoded string containing a minimum of 4 characters and a maximum of 32
         * human-readable characters.
         *
         * @param broadcastName Broadcast name for this broadcast group, null if no name provided
         * @throws IllegalArgumentException if name is non-null and its length is less than 4
         *     characters or greater than 32 characters
         * @return this builder
         */
        @Hide
        @SystemApi
        @RequiresNoPermission
        public @NonNull Builder setBroadcastName(@Nullable String broadcastName) {
            if (broadcastName != null
                    && ((broadcastName.length() > 32) || (broadcastName.length() < 4))) {
                throw new IllegalArgumentException("Invalid broadcast name length");
            }
            mBroadcastName = broadcastName;
            return this;
        }

        /**
         * Set the Broadcast Code currently set for this broadcast group.
         *
         * <p>Only needed when encryption is enabled As defined in Volume 3, Part C, Section 3.2.6
         * of Bluetooth Core Specification, Version 5.3, Broadcast Code is used to encrypt a
         * broadcast audio stream. It must be a UTF-8 string that has at least 4 octets and should
         * not exceed 16 octets.
         *
         * @param broadcastCode Broadcast Code for this broadcast group, null if code is not
         *     required for non-encrypted broadcast
         * @throws IllegalArgumentException if name is non-null and its length is less than 4
         *     characters or greater than 16 characters
         * @return this builder
         */
        @Hide
        @SystemApi
        @RequiresNoPermission
        public @NonNull Builder setBroadcastCode(@Nullable byte[] broadcastCode) {
            if (broadcastCode != null
                    && ((broadcastCode.length > 16) || (broadcastCode.length < 4))) {
                throw new IllegalArgumentException("Invalid broadcast code length");
            }
            mBroadcastCode = broadcastCode;
            return this;
        }

        /**
         * Set public broadcast metadata for this Broadcast Group. PBS should include the
         * Program_Info length-type-value (LTV) structure metadata
         *
         * @param publicBroadcastMetadata public broadcast metadata for this Broadcast Group, null
         *     if no public meta data provided
         * @return this builder
         */
        @Hide
        @SystemApi
        @RequiresNoPermission
        public @NonNull Builder setPublicBroadcastMetadata(
                @Nullable BluetoothLeAudioContentMetadata publicBroadcastMetadata) {
            mPublicBroadcastMetadata = publicBroadcastMetadata;
            return this;
        }

        /**
         * Add a subgroup settings to the broadcast group.
         *
         * @param subgroupSettings contains subgroup's setting data
         * @return this builder
         */
        @Hide
        @SystemApi
        @RequiresNoPermission
        public @NonNull Builder addSubgroupSettings(
                @NonNull BluetoothLeBroadcastSubgroupSettings subgroupSettings) {
            requireNonNull(subgroupSettings);
            mSubgroupSettings.add(subgroupSettings);
            return this;
        }

        /**
         * Clear subgroup settings list so that one can reset the builder
         *
         * @return this builder
         */
        @Hide
        @SystemApi
        @RequiresNoPermission
        public @NonNull Builder clearSubgroupSettings() {
            mSubgroupSettings.clear();
            return this;
        }

        /**
         * Set the audio session ID of the audio recording stream that is feeding this broadcast.
         *
         * <p>Establishing this binding enables the Bluetooth stack to verify the topological link
         * between an active audio recording session (e.g., {@link android.media.AudioRecord}) and
         * the broadcast transmission. This allows the system to authorize the concurrent audio
         * recording stream, preventing the broadcast from being inadvertently suspended by global
         * recording privacy policies.
         *
         * <p>This is an optional configuration. If not explicitly set, the broadcast will maintain
         * its default privacy behavior by adopting {@link AudioManager#AUDIO_SESSION_ID_GENERATE}.
         *
         * @param sessionId audio session ID of the audio recording stream
         * @return this builder
         * @throws IllegalArgumentException if the provided {@code sessionId} is not greater than 0
         *         or {@link AudioManager#AUDIO_SESSION_ID_GENERATE}
         */
        @Hide
        @SystemApi
        @RequiresNoPermission
        @FlaggedApi(Flags.FLAG_LEAUDIO_AURACAST_LIVE_MIC_FEATURE)
        public @NonNull Builder setAudioRecordingSessionId(int sessionId) {
            if (sessionId < 0) {
                throw new IllegalArgumentException(
                        "Invalid audio recording session ID: " + sessionId);
            }
            mAudioRecordingSessionId = sessionId;
            return this;
        }

        /**
         * Build {@link BluetoothLeBroadcastSettings}.
         *
         * @return {@link BluetoothLeBroadcastSettings}
         * @throws IllegalArgumentException if the object cannot be built
         */
        @Hide
        @SystemApi
        @RequiresNoPermission
        public @NonNull BluetoothLeBroadcastSettings build() {
            if (mSubgroupSettings.isEmpty()) {
                throw new IllegalArgumentException("Must contain at least one subgroup");
            }
            return new BluetoothLeBroadcastSettings(
                    mIsPublicBroadcast,
                    mBroadcastName,
                    mBroadcastCode,
                    mPublicBroadcastMetadata,
                    mSubgroupSettings,
                    mAudioRecordingSessionId);
        }
    }
}
