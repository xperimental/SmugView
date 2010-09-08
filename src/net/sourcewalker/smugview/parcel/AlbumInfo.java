package net.sourcewalker.smugview.parcel;

import net.sourcewalker.smugview.data.SmugView;
import android.content.ContentValues;
import android.database.Cursor;

import com.kallasoft.smugmug.api.json.entity.Album;

public class AlbumInfo {

    private int id;
    private String title;
    private String description;
    private String key;
    private String lastUpdated;

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getKey() {
        return key;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public AlbumInfo(Cursor source) {
        this.id = source.getInt(source.getColumnIndex(SmugView.Album._ID));
        this.key = source.getString(source.getColumnIndex(SmugView.Album.KEY));
        this.title = source.getString(source
                .getColumnIndex(SmugView.Album.TITLE));
        this.description = source.getString(source
                .getColumnIndex(SmugView.Album.DESCRIPTION));
    }

    public AlbumInfo(ContentValues values) {
        this.id = values.getAsInteger(SmugView.Album._ID);
        this.key = values.getAsString(SmugView.Album.KEY);
        this.title = values.getAsString(SmugView.Album.TITLE);
        this.description = values.getAsString(SmugView.Album.DESCRIPTION);
        this.lastUpdated = values.getAsString(SmugView.Album.MODIFIED);
    }

    public AlbumInfo(Album album) {
        this.id = album.getID();
        this.key = album.getAlbumKey();
        this.title = album.getTitle();
        this.description = album.getDescription();
        this.lastUpdated = album.getLastUpdated();
    }

    public ContentValues toValues() {
        ContentValues result = new ContentValues();
        result.put(SmugView.Album._ID, getId());
        result.put(SmugView.Album.TITLE, getTitle());
        result.put(SmugView.Album.DESCRIPTION, getDescription());
        result.put(SmugView.Album.KEY, getKey());
        result.put(SmugView.Album.MODIFIED, getLastUpdated());
        return result;
    }

}
