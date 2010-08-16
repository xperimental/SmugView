package net.sourcewalker.smugview;

import android.graphics.drawable.Drawable;

import com.kallasoft.smugmug.api.json.entity.Image;

public class ImageInfo {

    private AlbumInfo album;
    private int id;
    private String fileName;
    private String key;
    private String thumbUrl;
    private String viewUrl;
    private Drawable thumbnail;
    private String description;

    public AlbumInfo getAlbum() {
        return album;
    }

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

    public ImageInfo(AlbumInfo album, Image source) {
        this.album = album;
        this.id = source.getID();
        this.fileName = source.getFileName();
        this.key = source.getImageKey();
        this.thumbUrl = source.getThumbURL();
        this.viewUrl = source.getOriginalURL();
        this.description = source.getCaption();
    }

    public Drawable getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Drawable thumbnail) {
        this.thumbnail = thumbnail;
    }

}
