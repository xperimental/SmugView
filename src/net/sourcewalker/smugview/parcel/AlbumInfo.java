package net.sourcewalker.smugview.parcel;

import android.os.Parcel;
import android.os.Parcelable;

import com.kallasoft.smugmug.api.json.entity.Album;

public class AlbumInfo implements Parcelable {

    private String title;
    private String description;
    private int id;
    private String key;

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public AlbumInfo(Album source) {
        this.id = source.getID();
        this.key = source.getAlbumKey();
        this.title = source.getTitle();
        this.description = source.getDescription();
    }

    public AlbumInfo(Parcel source) {
        title = source.readString();
        description = source.readString();
        id = source.readInt();
        key = source.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(description);
        dest.writeInt(id);
        dest.writeString(key);
    }

    public static final Parcelable.Creator<AlbumInfo> CREATOR = new Creator<AlbumInfo>() {

        @Override
        public AlbumInfo[] newArray(int size) {
            return new AlbumInfo[size];
        }

        @Override
        public AlbumInfo createFromParcel(Parcel source) {
            return new AlbumInfo(source);
        }
    };

}
