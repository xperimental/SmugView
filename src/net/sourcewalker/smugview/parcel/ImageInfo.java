package net.sourcewalker.smugview.parcel;

import net.sourcewalker.smugview.data.Cache;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import com.kallasoft.smugmug.api.json.entity.Image;

public class ImageInfo implements Parcelable {

    private int id;
    private String fileName;
    private String key;
    private String thumbUrl;
    private String viewUrl;
    private String description;

    public int getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getKey() {
        return key;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public String getViewUrl() {
        return viewUrl;
    }

    public String getDescription() {
        return description;
    }

    public ImageInfo(Image source) {
        this.id = source.getID();
        this.fileName = source.getFileName();
        this.key = source.getImageKey();
        this.thumbUrl = source.getThumbURL();
        this.viewUrl = source.getLargeURL();
        this.description = source.getCaption();
    }

    private ImageInfo(Parcel source) {
        this.id = source.readInt();
        this.fileName = source.readString();
        this.key = source.readString();
        this.thumbUrl = source.readString();
        this.viewUrl = source.readString();
        this.description = source.readString();
    }

    public Drawable getThumbnail() {
        return Cache.getThumbnail(id);
    }

    public void setThumbnail(Drawable thumbnail) {
        Cache.saveThumbnail(id, thumbnail);
    }

    public Drawable getImage() {
        return Cache.getImage(id);
    }

    public void setImage(Drawable image) {
        Cache.saveImage(id, image);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(fileName);
        dest.writeString(key);
        dest.writeString(thumbUrl);
        dest.writeString(viewUrl);
        dest.writeString(description);
    }

    public static final Parcelable.Creator<ImageInfo> CREATOR = new Creator<ImageInfo>() {

        @Override
        public ImageInfo[] newArray(int size) {
            return new ImageInfo[size];
        }

        @Override
        public ImageInfo createFromParcel(Parcel source) {
            return new ImageInfo(source);
        }
    };

}
