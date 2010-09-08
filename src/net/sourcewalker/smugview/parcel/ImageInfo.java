package net.sourcewalker.smugview.parcel;

import net.sourcewalker.smugview.data.SmugView;
import android.content.ContentValues;
import android.graphics.drawable.Drawable;

import com.kallasoft.smugmug.api.json.entity.Image;

public class ImageInfo implements Comparable<ImageInfo> {

    private int id;
    private Integer albumId;
    private int position;
    private String fileName;
    private String key;
    private String thumbUrl;
    private String viewUrl;
    private String description;
    private String lastUpdated;
    private Drawable thumbnail;
    private Drawable image;

    public int getId() {
        return id;
    }

    public int getAlbumId() {
        return albumId;
    }

    public int getPosition() {
        return position;
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

    public String getLastUpdated() {
        return lastUpdated;
    }

    public ImageInfo(int albumId, Image source) {
        this.id = source.getID();
        this.albumId = albumId;
        this.position = source.getPosition();
        this.fileName = source.getFileName();
        this.key = source.getImageKey();
        this.thumbUrl = source.getThumbURL();
        this.viewUrl = source.getLargeURL();
        this.description = source.getCaption();
        this.lastUpdated = source.getLastUpdated();
    }

    public ImageInfo(ContentValues values) {
        this.id = values.getAsInteger(SmugView.Image._ID);
        this.albumId = values.getAsInteger(SmugView.Image.ALBUM_ID);
        this.position = values.getAsInteger(SmugView.Image.POSITION);
        this.fileName = values.getAsString(SmugView.Image.FILENAME);
        this.key = values.getAsString(SmugView.Image.KEY);
        this.thumbUrl = values.getAsString(SmugView.Image.THUMBNAIL_URL);
        this.viewUrl = values.getAsString(SmugView.Image.IMAGE_URL);
        this.description = values.getAsString(SmugView.Image.DESCRIPTION);
        this.lastUpdated = values.getAsString(SmugView.Image.MODIFIED);
    }

    public Drawable getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Drawable thumbnail) {
        this.thumbnail = thumbnail;
    }

    public Drawable getImage() {
        return image;
    }

    public void setImage(Drawable image) {
        this.image = image;
    }

    @Override
    public int compareTo(ImageInfo another) {
        return id - another.id;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ImageInfo) {
            return id == ((ImageInfo) o).id;
        } else {
            return false;
        }
    }

    public ContentValues toValues() {
        ContentValues result = new ContentValues();
        result.put(SmugView.Image._ID, getId());
        result.put(SmugView.Image.ALBUM_ID, getAlbumId());
        result.put(SmugView.Image.POSITION, getPosition());
        result.put(SmugView.Image.FILENAME, getFileName());
        result.put(SmugView.Image.KEY, getKey());
        result.put(SmugView.Image.THUMBNAIL_URL, getThumbUrl());
        result.put(SmugView.Image.IMAGE_URL, getViewUrl());
        result.put(SmugView.Image.DESCRIPTION, getDescription());
        result.put(SmugView.Image.MODIFIED, getLastUpdated());
        return result;
    }

}
