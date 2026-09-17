/*
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

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.IntDef;
import android.annotation.SystemApi;
import android.app.people.flags.Flags;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Constants for {@link PeopleInferenceConfig}. For example, base features and common math constants
 * for composing a {@link RankingSpec}.
 */
@SystemApi
@FlaggedApi(Flags.FLAG_ENABLE_PWM_SERVICE)
public final class Constants {
    private Constants() {}

    /**
     * Defines the identifiers of all base features that are natively supported in {@link
     * RankingSpec}.
     *
     * <p>Similarly, when specifying customly-defined derived features in {@link RankingSpec}, it's
     * recommended to also use integers as keys to limit the size of the config. The integer must be
     * larger than {@link #MAX_BASE_FEATURE_ID} to avoid collision with the base features.
     */
    @Hide
    @IntDef({
        CONTACT_IS_STARRED,
        CONTACT_IS_VIP,
        CONTACT_DISPLAY_NAME,
        CONTACT_FIRST_NAME,
        CONTACT_LAST_NAME,
        CONTACT_NICKNAME,
        CONTACT_AVATAR,
        CONTACT_PINNED_POSITION,
        CONTACT_EMAIL_ADDRESSES,
        CONTACT_EMAIL_LABELS,
        CONTACT_PHONE_NUMBERS,
        CONTACT_PHONE_LABELS,
        CONTACT_POSTAL_ADDRESSES,
        CONTACT_POSTAL_LABELS,
        CONTACT_RELATIONS,
        CONTACT_RELATION_LABELS,
        CONTACT_BIRTHDAY,
        CONTACT_ANNIVERSARIES,
        CONTACT_OTHER_DATES,
        CONTACT_CUSTOM_RINGTONE,
        CONTACT_SENT_TO_VOICEMAIL,
        CONTACT_ACCOUNT_NAMES,
        CONTACT_ACCOUNT_TYPES,
        CONTACT_METHOD_IS_PRIMARY,
        CONTACT_METHOD_LABEL,
        SHORTCUT_LABEL,
        SHORTCUT_RANK,
        SHORTCUT_IS_PINNED,
        SHORTCUT_LAST_CHANGE_TIME_MILLIS,
        SHORTCUT_IS_SHARE_TARGET,
        CALL_ANSWERED_TIME_MILLIS,
        CALL_DIALED_TIME_MILLIS,
        SMS_SENT_TIME_MILLIS,
        NOTIFICATION_CLICK_TIME_MILLIS,
        INAPP_MESSAGE_SENT_TIME_MILLIS,
        SHARESHEET_EVENT_TIME_MILLIS,
        SHARESHEET_EVENT_TYPES,
        SHARESHEET_EVENT_MIMETYPES,
        SHARESHEET_EVENT_ORIGIN_APPS,
        FOREGROUND_APPS,
        FOREGROUND_APP_LAST_TIME_MILLIS,
        BLOCKS_IS_PHONE_BLOCKED,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface BaseFeature {}

    /**
     * The maximum ID value for a base feature. All derived features must use IDs greater than this
     * value in {@link RankingSpec}. When creating a people inference session, this value will be
     * checked. If any {@link FeatureSpec} key is equal to or less than this value, the session
     * creation will fail.
     */
    @Hide
    public static final int MAX_BASE_FEATURE_ID = 1000;

    /** Next feature ID: 43 */

    /** [boolean] Whether a contact is starred. */
    public static final int CONTACT_IS_STARRED = 1;

    /** [boolean] Whether a contact is a VIP. */
    public static final int CONTACT_IS_VIP = 2;

    /** [string] The display name of a contact. */
    public static final int CONTACT_DISPLAY_NAME = 3;

    /** [string] The first name of a contact. */
    public static final int CONTACT_FIRST_NAME = 4;

    /** [string] The last name of a contact. */
    public static final int CONTACT_LAST_NAME = 5;

    /** [string] The nickname of a contact. */
    public static final int CONTACT_NICKNAME = 6;

    /** [string] The URI to the profile photo of a contact. */
    public static final int CONTACT_AVATAR = 7;

    /** [double] The pinned position of a contact. */
    public static final int CONTACT_PINNED_POSITION = 8;

    /** [list<string>] The list of email addresses of a contact. */
    public static final int CONTACT_EMAIL_ADDRESSES = 9;

    /**
     * [list<long>] The list of int-typed labels on the email addresses of a contact, e.g. HOME,
     * WORK. The values will be exactly aligned with the email addresses in {@link
     * #CONTACT_EMAIL_ADDRESSES}.
     */
    public static final int CONTACT_EMAIL_LABELS = 10;

    /** [list<string>] The list of phone numbers of a contact. */
    public static final int CONTACT_PHONE_NUMBERS = 11;

    /**
     * [list<long>] The list of int-typed labels on the phone numbers of a contact, e.g. HOME, WORK.
     * The values will be exactly aligned with the phone numbers in {@link #CONTACT_PHONE_NUMBERS}.
     */
    public static final int CONTACT_PHONE_LABELS = 12;

    /** [list<string>] The list of postal addresses of a contact. */
    public static final int CONTACT_POSTAL_ADDRESSES = 13;

    /**
     * [list<long>] The list of int-typed labels on the postal addresses of a contact, e.g. HOME,
     * WORK. The values will be exactly aligned with the postal addresses in {@link
     * #CONTACT_POSTAL_ADDRESSES}.
     */
    public static final int CONTACT_POSTAL_LABELS = 14;

    /** [list<string>] The list of relations of a contact. */
    public static final int CONTACT_RELATIONS = 15;

    /**
     * [list<long>] The list of int-typed labels on the relations of a contact, e.g. MOTHER, FATHER,
     * etc. The values will be exactly aligned with the relations in {@link #CONTACT_RELATIONS}.
     */
    public static final int CONTACT_RELATION_LABELS = 16;

    /** [string] The birthday of a contact. */
    public static final int CONTACT_BIRTHDAY = 17;

    /** [list<string>] The list of anniversaries of a contact. */
    public static final int CONTACT_ANNIVERSARIES = 18;

    /**
     * [list<string>] The list of significant dates of a contact, other than birthday and
     * anniversaries.
     */
    public static final int CONTACT_OTHER_DATES = 19;

    /** [string] The URI to the custom ringtone set on a contact. */
    public static final int CONTACT_CUSTOM_RINGTONE = 20;

    /** [boolean] Whether the contact is automatically sent to voicemail. */
    public static final int CONTACT_SENT_TO_VOICEMAIL = 21;

    /**
     * [list<string>] The list of the account names of the source accounts from which a contact is
     * created.
     */
    public static final int CONTACT_ACCOUNT_NAMES = 22;

    /**
     * [list<string>] The list of the account types of the source accounts from which a contact is
     * created. The values will be exactly aligned with the account names in {@link
     * #CONTACT_ACCOUNT_NAMES}.
     */
    public static final int CONTACT_ACCOUNT_TYPES = 23;

    /** [boolean] Whether a contact method is primary. */
    public static final int CONTACT_METHOD_IS_PRIMARY = 24;

    /** [long] The int-typed label of a contact method, i.e. HOME, WORK, etc. */
    public static final int CONTACT_METHOD_LABEL = 25;

    /**
     * [string] The {@link android.content.pm.ShortcutInfo#getShortLabel} of a shortcut. If the
     * shortcut doesn't have short label, it will fall back to {@link
     * android.content.pm.ShortcutInfo#getLongLabel}.
     */
    public static final int SHORTCUT_LABEL = 26;

    /** [double] The rank of a shortcut. */
    public static final int SHORTCUT_RANK = 27;

    /** [boolean] Whether a shortcut is pinned. */
    public static final int SHORTCUT_IS_PINNED = 28;

    /** [long] The last change timestamp in milliseconds of a shortcut. */
    public static final int SHORTCUT_LAST_CHANGE_TIME_MILLIS = 29;

    /**
     * [boolean] Whether a shortcut is a share target, i.e. the base of a {@link
     * android.content.pm.ShortcutManager#ShareShortcutInfo}.
     */
    public static final int SHORTCUT_IS_SHARE_TARGET = 30;

    /**
     * [list<long>] A chronologically sorted list of timestamps in milliseconds of when calls were
     * answered.
     */
    public static final int CALL_ANSWERED_TIME_MILLIS = 31;

    /**
     * [list<long>] A chronologically sorted list of timestamps in milliseconds of when calls were
     * dialed.
     */
    public static final int CALL_DIALED_TIME_MILLIS = 32;

    /**
     * [list<long>] A chronologically sorted list of timestamps in milliseconds of when SMS were
     * sent.
     */
    public static final int SMS_SENT_TIME_MILLIS = 33;

    /**
     * [list<long>] A chronologically sorted list of timestamps in milliseconds of when
     * notifications were clicked.
     */
    public static final int NOTIFICATION_CLICK_TIME_MILLIS = 34;

    /**
     * [list<long>] A chronologically sorted list of timestamps in milliseconds of when in-app
     * messages were sent.
     */
    public static final int INAPP_MESSAGE_SENT_TIME_MILLIS = 35;

    /**
     * [list<long>] A chronologically sorted list of timestamps in milliseconds of when a Sharesheet
     * occurred.
     */
    public static final int SHARESHEET_EVENT_TIME_MILLIS = 36;

    /**
     * [list<string>] A chronologically sorted list of types of Sharesheet events, corresponding to
     * the timestamps in {@link #SHARESHEET_EVENT_TIME_MILLIS}. The type can be "APP_SHARE",
     * "DIRECT_SHARE" or "DIRECT_TARGET_IGNORE".
     */
    public static final int SHARESHEET_EVENT_TYPES = 37;

    /**
     * [list<string>] A chronologically sorted list of MIME types of the shared content when the
     * Sharesheet events occurred, corresponding to the timestamps in {@link
     * #SHARESHEET_EVENT_TIME_MILLIS}.
     */
    public static final int SHARESHEET_EVENT_MIMETYPES = 38;

    /**
     * [list<string>] A chronologically sorted list of origin apps where the Sharesheet events
     * occurred, corresponding to the timestamps in {@link #SHARESHEET_EVENT_TIME_MILLIS}.
     */
    public static final int SHARESHEET_EVENT_ORIGIN_APPS = 39;

    /**
     * [list<string>] A chronologically sorted list of most recent foreground apps that the user has
     * interacted with.
     */
    public static final int FOREGROUND_APPS = 40;

    /** [list<long>] The timestamp in milliseconds of the last time an app was in the foreground. */
    public static final int FOREGROUND_APP_LAST_TIME_MILLIS = 41;

    /** [boolean] Whether a phone number is blocked. */
    public static final int BLOCKS_IS_PHONE_BLOCKED = 42;
}
