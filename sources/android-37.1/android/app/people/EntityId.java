/**
 * Copyright (C) 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.app.people;

import static com.android.internal.util.Preconditions.checkNotNull;

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.IntDef;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.SystemApi;
import android.app.people.flags.Flags;
import android.content.pm.ShareTargetId;
import android.content.pm.ShortcutId;
import android.content.pm.UserPackage;
import android.os.Parcel;
import android.os.Parcelable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

/**
 * Wrapper for identifiers of different types of {@link Entity}.
 */
@SystemApi
@FlaggedApi(Flags.FLAG_ENABLE_PWM_SERVICE)
public final class EntityId implements Parcelable {

    /**
     * Supported types of entities.
     */
    @Hide
    @IntDef(
            value = {
                TYPE_CONTACT_ID,
                TYPE_NAME,
                TYPE_EMAIL_ADDRESS,
                TYPE_PHONE_NUMBER,
                TYPE_SHORTCUT_ID,
                TYPE_SHARE_TARGET_ID,
                TYPE_USER_PACKAGE,
            })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {}

    /** ID type of a contact ID. */
    public static final int TYPE_CONTACT_ID = 1;

    /** ID type of a name. */
    public static final int TYPE_NAME = 2;

    /** ID type of an email address. */
    public static final int TYPE_EMAIL_ADDRESS = 3;

    /** ID type of a phone number. */
    public static final int TYPE_PHONE_NUMBER = 4;

    /** ID type of a shortcut ID. */
    public static final int TYPE_SHORTCUT_ID = 5;

    /** ID type of a share target ID. */
    public static final int TYPE_SHARE_TARGET_ID = 6;

    /** ID type of a user package. */
    public static final int TYPE_USER_PACKAGE = 7;

    @Type private final int mType;
    @NonNull private final Object mValue;

    private EntityId(@Type int type, @NonNull Object value) {
        mType = type;
        mValue = checkNotNull(value, "value cannot be null");
    }

    /**
     * Creates a new {@link EntityId} of type {@link #TYPE_CONTACT_ID}.
     *
     * @param contactId The long-valued contact ID.
     * @return A new {@link EntityId} with the given contact ID.
     */
    @NonNull
    public static EntityId contactId(long contactId) {
        return new EntityId(TYPE_CONTACT_ID, contactId);
    }

    /**
     * Creates a new {@link EntityId} of type {@link #TYPE_NAME}.
     *
     * @param name The string-valued name.
     * @return A new {@link EntityId} with the given name.
     */
    @NonNull
    public static EntityId name(@NonNull String name) {
        return new EntityId(TYPE_NAME, name);
    }

    /**
     * Creates a new {@link EntityId} of type {@link #TYPE_EMAIL_ADDRESS}.
     *
     * @param emailAddress The string-valued email address.
     * @return A new {@link EntityId} with the given email address.
     */
    @NonNull
    public static EntityId emailAddress(@NonNull String emailAddress) {
        return new EntityId(TYPE_EMAIL_ADDRESS, emailAddress);
    }

    /**
     * Creates a new {@link EntityId} of type {@link #TYPE_PHONE_NUMBER}. Any valid phone number is
     * allowed. E164 normalization is preferred but not required.
     *
     * @param phoneNumber The string-valued phone number.
     * @return A new {@link EntityId} with the given phone number.
     */
    @NonNull
    public static EntityId phoneNumber(@NonNull String phoneNumber) {
        return new EntityId(TYPE_PHONE_NUMBER, phoneNumber);
    }

    /**
     * Creates a new {@link EntityId} of type {@link #TYPE_SHORTCUT_ID}.
     *
     * @param shortcutId The {@link ShortcutId} of a shortcut.
     * @return A new {@link EntityId} with the given shortcut ID.
     */
    @NonNull
    public static EntityId shortcutId(@NonNull ShortcutId shortcutId) {
        return new EntityId(TYPE_SHORTCUT_ID, shortcutId);
    }

    /**
     * Creates a new {@link EntityId} of type {@link #TYPE_SHARE_TARGET_ID}.
     *
     * @param shareTargetId The {@link ShareTargetId} of a share target.
     * @return A new {@link EntityId} with the given share target ID.
     */
    @NonNull
    public static EntityId shareTargetId(@NonNull ShareTargetId shareTargetId) {
        return new EntityId(TYPE_SHARE_TARGET_ID, shareTargetId);
    }

    /**
     * Creates a new {@link EntityId} of type {@link #TYPE_USER_PACKAGE}.
     *
     * @param userPackage A {@link UserPackage}.
     * @return A new {@link EntityId} with the given {@link UserPackage}.
     */
    @NonNull
    public static EntityId userPackage(@NonNull UserPackage userPackage) {
        return new EntityId(TYPE_USER_PACKAGE, userPackage);
    }

    /** Returns the type of this {@link EntityId}. */
    @Type
    public int getType() {
        return mType;
    }

    /** Returns the contact ID if the type is {@link #TYPE_CONTACT_ID}, otherwise -1. */
    public long getContactId() {
        return mType == TYPE_CONTACT_ID ? (long) mValue : -1;
    }

    /** Returns the name if the type is {@link #TYPE_NAME}, otherwise null. */
    @Nullable
    public String getName() {
        return mType == TYPE_NAME ? (String) mValue : null;
    }

    /** Returns the email address if the type is {@link #TYPE_EMAIL_ADDRESS}, otherwise null. */
    @Nullable
    public String getEmailAddress() {
        return mType == TYPE_EMAIL_ADDRESS ? (String) mValue : null;
    }

    /** Returns the phone number if the type is {@link #TYPE_PHONE_NUMBER}, otherwise null. */
    @Nullable
    public String getPhoneNumber() {
        return mType == TYPE_PHONE_NUMBER ? (String) mValue : null;
    }

    /** Returns the {@link ShortcutId} if the type is {@link #TYPE_SHORTCUT_ID}, otherwise null. */
    @Nullable
    public ShortcutId getShortcutId() {
        return mType == TYPE_SHORTCUT_ID ? (ShortcutId) mValue : null;
    }

    /**
     * Returns the {@link ShareTargetId} if the type is {@link #TYPE_SHARE_TARGET_ID}, otherwise
     * null.
     */
    @Nullable
    public ShareTargetId getShareTargetId() {
        return mType == TYPE_SHARE_TARGET_ID ? (ShareTargetId) mValue : null;
    }

    /**
     * Returns the {@link UserPackage} if the type is {@link #TYPE_USER_PACKAGE}, otherwise null.
     */
    @Nullable
    public UserPackage getUserPackage() {
        return mType == TYPE_USER_PACKAGE ? (UserPackage) mValue : null;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o != null && o instanceof EntityId other) {
            return mType == other.mType && Objects.equals(mValue, other.mValue);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mType, mValue);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(mType);
        switch (mType) {
            case TYPE_CONTACT_ID -> dest.writeLong(getContactId());
            case TYPE_NAME -> dest.writeString8(getName());
            case TYPE_EMAIL_ADDRESS -> dest.writeString8(getEmailAddress());
            case TYPE_PHONE_NUMBER -> dest.writeString8(getPhoneNumber());
            case TYPE_SHORTCUT_ID -> dest.writeTypedObject(getShortcutId(), flags);
            case TYPE_SHARE_TARGET_ID -> dest.writeTypedObject(getShareTargetId(), flags);
            case TYPE_USER_PACKAGE -> dest.writeTypedObject(getUserPackage(), flags);
        }
    }

    @NonNull
    public static final Parcelable.Creator<EntityId> CREATOR =
            new Parcelable.Creator<EntityId>() {
                @Override
                public EntityId createFromParcel(@NonNull Parcel in) {
                    int type = in.readInt();
                    return switch (type) {
                        case TYPE_CONTACT_ID -> EntityId.contactId(in.readLong());
                        case TYPE_NAME -> EntityId.name(in.readString8());
                        case TYPE_EMAIL_ADDRESS -> EntityId.emailAddress(in.readString8());
                        case TYPE_PHONE_NUMBER -> EntityId.phoneNumber(in.readString8());
                        case TYPE_SHORTCUT_ID ->
                                EntityId.shortcutId(in.readTypedObject(ShortcutId.CREATOR));
                        case TYPE_SHARE_TARGET_ID ->
                                EntityId.shareTargetId(in.readTypedObject(ShareTargetId.CREATOR));
                        case TYPE_USER_PACKAGE ->
                                EntityId.userPackage(in.readTypedObject(UserPackage.CREATOR));
                        default -> throw new IllegalArgumentException("Unknown ID type: " + type);
                    };
                }

                @Override
                public EntityId[] newArray(int size) {
                    return new EntityId[size];
                }
            };
}
