package net.sourcewalker.smugview.parcel;

import net.sourcewalker.smugview.data.SmugView;
import android.database.Cursor;

public class AlbumInfo {

    private int id;
    private String title;
    private String description;
    private String key;

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

    public AlbumInfo(Cursor source) {
        this.id = source.getInt(source.getColumnIndex(SmugView.Album._ID));
        this.key = source.getString(source.getColumnIndex(SmugView.Album.KEY));
        this.title = source.getString(source
                .getColumnIndex(SmugView.Album.TITLE));
        this.description = source.getString(source
                .getColumnIndex(SmugView.Album.DESCRIPTION));
    }

}
