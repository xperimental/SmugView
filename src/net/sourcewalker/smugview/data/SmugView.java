package net.sourcewalker.smugview.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines the data structure used in the database and content provider.
 * 
 * @author Xperimental
 */
public final class SmugView {

    public static final String AUTHORITY = "net.sourcewalker.smugview";

    private SmugView() {
    };

    public static final class Album implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/albums");

        /**
         * Content type of a list of albums.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/net.sourcewalker.smugview.album";

        /**
         * Content type of a single album.
         */
        public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/net.sourcewalker.smugview.album";

        /**
         * Table name.
         */
        public static final String TABLE = "albums";

        /**
         * Title column.
         */
        public static final String TITLE = "title";

        /**
         * Description column.
         */
        public static final String DESCRIPTION = "description";

        /**
         * Access key column.
         */
        public static final String KEY = "key";

        /**
         * Timestamp of last modification on server.
         */
        public static final String MODIFIED = "modified";

        /**
         * Column for last sync.
         */
        public static final String SYNC = "sync";

        /**
         * Create statement.
         */
        public static final String SCHEMA = "CREATE TABLE " + TABLE + "(" + _ID
                + " INTEGER PRIMARY KEY," + TITLE + " TEXT," + DESCRIPTION
                + " TEXT," + KEY + " TEXT, " + MODIFIED + " INTEGER," + SYNC
                + " TEXT)";

        /**
         * Default projection containing all columns.
         */
        public static final String[] DEFAULT_PROJECTION = new String[] { _ID,
                TITLE, DESCRIPTION, KEY, MODIFIED, SYNC };

        /**
         * Default sort order (newest albums first).
         */
        public static final String DEFAULT_SORT_ORDER = MODIFIED + " DESC";
    }

    public static final class Image implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/images");

        /**
         * Content type of a list of albums.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/net.sourcewalker.smugview.image";

        /**
         * Content type of a single album.
         */
        public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/net.sourcewalker.smugview.image";

        /**
         * Table name.
         */
        public static final String TABLE = "images";

        public static final String ALBUM_ID = "album";

        public static final String POSITION = "position";

        public static final String FILENAME = "filename";

        public static final String KEY = "key";

        public static final String DESCRIPTION = "description";

        public static final String THUMBNAIL_URL = "thumbUrl";

        public static final String IMAGE_URL = "imgUrl";

        public static final String MODIFIED = "modified";

        public static final String CONTENT = "content";

        /**
         * Create statement.
         */
        public static final String SCHEMA = "CREATE TABLE " + TABLE + "(" + _ID
                + " INTEGER PRIMARY KEY, " + ALBUM_ID + " INTEGER, " + POSITION
                + " INTEGER, " + FILENAME + " TEXT, " + KEY + " TEXT, "
                + DESCRIPTION + " TEXT, " + THUMBNAIL_URL + " TEXT, "
                + IMAGE_URL + " TEXT, " + MODIFIED + " TEXT)";

        /**
         * Default projection containing all columns.
         */
        public static final String[] DEFAULT_PROJECTION = new String[] { _ID,
                ALBUM_ID, POSITION, FILENAME, KEY, DESCRIPTION, THUMBNAIL_URL,
                IMAGE_URL, MODIFIED, CONTENT };

        /**
         * Default sort order (by position in album).
         */
        public static final String DEFAULT_SORT_ORDER = ALBUM_ID + " ASC, "
                + POSITION + " ASC";

    }

    public static final class Thumbnail implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/thumbnail");

        /**
         * Content type of a list of albums.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/net.sourcewalker.smugview.thumbnail";

        /**
         * Content type of a single album.
         */
        public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/net.sourcewalker.smugview.thumbnail";

    }

}
