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

package android.app.customization.clock;

import static android.app.Flags.FLAG_CLOCK_MANAGER_SERVICE;

import android.annotation.Hide;
import android.annotation.FlaggedApi;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.content.ComponentName;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.util.Log;

import com.android.modules.utils.TypedXmlPullParser;
import com.android.modules.utils.TypedXmlSerializer;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Describes a Clock, including associated metadata and optional content.
 *
 * <p>This class is used to communicate among a clock rendering service, a clock chooser UI, and
 * {@link android.app.ClockManager}. This class describes a specific instance of a clock.
 */
@Hide
@FlaggedApi(FLAG_CLOCK_MANAGER_SERVICE)
public final class ClockDescription implements Parcelable {
    private static final String TAG = "ClockDescription";
    private static final String XML_TAG_CONTENT = "content";
    private static final String XML_TAG_ASSET = "asset";

    @NonNull private final ComponentName mComponentName;
    @NonNull private final String mId;
    @Nullable private final String mTitle;
    @Nullable private final String mDescription;
    @Nullable private final Integer mSeedColor;
    @Nullable private final List<String> mAssetNames;
    @NonNull private final PersistableBundle mContent;

    public ClockDescription(
            @NonNull ComponentName componentName,
            @NonNull String id,
            @Nullable String title,
            @Nullable String description,
            @Nullable Integer seedColor,
            @Nullable List<String> assetNames,
            @Nullable PersistableBundle content) {
        mComponentName = componentName;
        mId = id;
        mTitle = title;
        mDescription = description;
        mSeedColor = seedColor;
        mAssetNames = assetNames;
        mContent = (content != null) ? content : new PersistableBundle();
    }

    public ComponentName getComponentName() {
        return mComponentName;
    }

    public String getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public Integer getSeedColor() {
        return mSeedColor;
    }

    public List<String> getAssetNames() {
        return mAssetNames;
    }

    public PersistableBundle getContent() {
        return mContent;
    }

    ////// Comparison overrides
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ClockDescription that)) {
            return false;
        }
        return Objects.equals(mComponentName, that.mComponentName)
                && Objects.equals(mId, that.mId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mComponentName, mId);
    }

    ////// Parcelable implementation
    ClockDescription(@NonNull Parcel in) {
        mComponentName = ComponentName.readFromParcel(in);
        mId = in.readString8();
        mTitle = in.readString8();
        mDescription = in.readString8();
        mSeedColor = (Integer) in.readValue(Integer.class.getClassLoader());
        mAssetNames = in.createStringArrayList();

        PersistableBundle content = PersistableBundle.CREATOR.createFromParcel(in);
        mContent = (content == null) ? new PersistableBundle() : content;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        ComponentName.writeToParcel(mComponentName, dest);
        dest.writeString8(mId);
        dest.writeString8(mTitle);
        dest.writeString8(mDescription);
        dest.writeValue(mSeedColor);
        dest.writeStringList(mAssetNames);
        dest.writePersistableBundle(mContent);
    }

    ////// XML storage
    public void saveToXml(TypedXmlSerializer out) throws IOException, XmlPullParserException {
        if (mComponentName != null) {
            out.attribute(null, "component", mComponentName.flattenToShortString());
        }
        if (mId != null) {
            out.attribute(null, "id", mId);
        }
        if (mTitle != null) {
            out.attribute(null, "title", mTitle);
        }
        if (mDescription != null) {
            out.attribute(null, "description", mDescription);
        }
        if (mSeedColor != null) {
            out.attributeInt(null, "seedColor", mSeedColor);
        }

        if (mAssetNames != null) {
            for (String assetName : mAssetNames) {
                out.startTag(null, XML_TAG_ASSET);
                out.attribute(null, "name", assetName);
                out.endTag(null, XML_TAG_ASSET);
            }
        }

        try {
            out.startTag(null, XML_TAG_CONTENT);
            mContent.saveToXml(out);
        } catch (XmlPullParserException e) {
            Log.e(TAG, "unable to convert clock description content to XML");
        } finally {
            out.endTag(null, XML_TAG_CONTENT);
        }
    }

    public static ClockDescription restoreFromXml(TypedXmlPullParser in)
            throws IOException, XmlPullParserException {
        final int outerDepth = in.getDepth();
        String component = in.getAttributeValue(null, "component");
        ComponentName componentName =
                (component != null) ? ComponentName.unflattenFromString(component) : null;
        String id = in.getAttributeValue(null, "id");
        String title = in.getAttributeValue(null, "title");
        String description = in.getAttributeValue(null, "description");
        String seedColorStr = in.getAttributeValue(null, "seedColor");
        Integer seedColor = (seedColorStr != null) ? in.getAttributeInt(null, "seedColor") : null;

        List<String> assetNames = new ArrayList<>();
        PersistableBundle content = null;

        int type;
        while ((type = in.next()) != XmlPullParser.END_DOCUMENT
                && (type != XmlPullParser.END_TAG || in.getDepth() > outerDepth)) {
            if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                continue;
            }
            String name = in.getName();
            if (XML_TAG_CONTENT.equals(name)) {
                content = PersistableBundle.restoreFromXml(in);
            } else if (XML_TAG_ASSET.equals(name)) {
                String assetName = in.getAttributeValue(null, "name");
                if (assetName != null) {
                    assetNames.add(assetName);
                }
            }
        }
        return new ClockDescription(componentName, id, title, description, seedColor,
                assetNames.isEmpty() ? null : assetNames, content);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @NonNull
    public static final Parcelable.Creator<ClockDescription> CREATOR =
            new Parcelable.Creator<ClockDescription>() {
                @Override
                public ClockDescription createFromParcel(Parcel source) {
                    return new ClockDescription(source);
                }

                @Override
                public ClockDescription[] newArray(int size) {
                    return new ClockDescription[size];
                }
            };
}