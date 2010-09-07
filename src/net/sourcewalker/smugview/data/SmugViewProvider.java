package net.sourcewalker.smugview.data;

import java.util.Date;
import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

/**
 * This content provider is used to get all the data for this application.
 * 
 * @author Xperimental
 */
public class SmugViewProvider extends ContentProvider {

    private static final UriMatcher uriMatcher;
    private static final HashMap<String, String> albumProjection;
    private static final HashMap<String, String> imageProjection;

    private static final int MATCH_ALBUM = 1;
    private static final int MATCH_ALBUM_ID = 2;
    private static final int MATCH_IMAGE = 3;
    private static final int MATCH_IMAGE_ID = 4;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(SmugView.AUTHORITY, "albums", MATCH_ALBUM);
        uriMatcher.addURI(SmugView.AUTHORITY, "albums/#", MATCH_ALBUM_ID);
        uriMatcher.addURI(SmugView.AUTHORITY, "images", MATCH_IMAGE);
        uriMatcher.addURI(SmugView.AUTHORITY, "images/#", MATCH_IMAGE_ID);

        albumProjection = new HashMap<String, String>();
        for (String key : SmugView.Album.DEFAULT_PROJECTION) {
            albumProjection.put(key, key);
        }

        imageProjection = new HashMap<String, String>();
        for (String key : SmugView.Image.DEFAULT_PROJECTION) {
            imageProjection.put(key, key);
        }
    }

    private SmugViewDbHelper dbHelper;

    /*
     * (non-Javadoc)
     * @see android.content.ContentProvider#delete(android.net.Uri,
     * java.lang.String, java.lang.String[])
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        switch (uriMatcher.match(uri)) {
        case MATCH_ALBUM:
            return db.delete(SmugView.Album.TABLE, selection, selectionArgs);
        case MATCH_ALBUM_ID:
            return db.delete(SmugView.Album.TABLE, SmugView.Album._ID + " = ?",
                    new String[] { uri.getLastPathSegment() });
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    /*
     * (non-Javadoc)
     * @see android.content.ContentProvider#getType(android.net.Uri)
     */
    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
        case MATCH_ALBUM:
            return SmugView.Album.CONTENT_TYPE;
        case MATCH_ALBUM_ID:
            return SmugView.Album.CONTENT_TYPE_ITEM;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    /*
     * (non-Javadoc)
     * @see android.content.ContentProvider#insert(android.net.Uri,
     * android.content.ContentValues)
     */
    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        switch (uriMatcher.match(uri)) {
        case MATCH_ALBUM:
            return insertAlbum(initialValues);
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    /**
     * Insert a new album into the database.
     * 
     * @param initialValues
     *            Values of album row.
     */
    private Uri insertAlbum(ContentValues initialValues) {
        ContentValues values = new ContentValues(initialValues);
        if (values.containsKey(SmugView.Album._ID) == false) {
            throw new IllegalArgumentException("No ID for album: " + values);
        }

        if (values.containsKey(SmugView.Album.TITLE) == false) {
            throw new IllegalArgumentException("No title for album: " + values);
        }

        if (values.containsKey(SmugView.Album.DESCRIPTION) == false) {
            values.put(SmugView.Album.DESCRIPTION, "");
        }

        if (values.containsKey(SmugView.Album.KEY) == false) {
            values.put(SmugView.Album.KEY, "");
        }

        if (values.containsKey(SmugView.Album.MODIFIED) == false) {
            values.put(SmugView.Album.MODIFIED, System.currentTimeMillis());
        }

        values.put(SmugView.Album.SYNC, new Date().toString());

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.insert(SmugView.Album.TABLE, SmugView.Album.TITLE, values);
        Uri albumUri = ContentUris.withAppendedId(SmugView.Album.CONTENT_URI,
                values.getAsInteger(SmugView.Album._ID));
        getContext().getContentResolver().notifyChange(albumUri, null);
        return albumUri;
    }

    /*
     * (non-Javadoc)
     * @see android.content.ContentProvider#onCreate()
     */
    @Override
    public boolean onCreate() {
        dbHelper = new SmugViewDbHelper(getContext());
        return true;
    }

    /*
     * (non-Javadoc)
     * @see android.content.ContentProvider#query(android.net.Uri,
     * java.lang.String[], java.lang.String, java.lang.String[],
     * java.lang.String)
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (uriMatcher.match(uri)) {
        case MATCH_ALBUM:
            qb.setTables(SmugView.Album.TABLE);
            qb.setProjectionMap(albumProjection);
            if (sortOrder == null) {
                sortOrder = SmugView.Album.DEFAULT_SORT_ORDER;
            }
            break;
        case MATCH_ALBUM_ID:
            qb.setTables(SmugView.Album.TABLE);
            qb.setProjectionMap(albumProjection);
            if (sortOrder == null) {
                sortOrder = SmugView.Album.DEFAULT_SORT_ORDER;
            }
            qb.appendWhere(SmugView.Album._ID + " = "
                    + ContentUris.parseId(uri));
            break;
        case MATCH_IMAGE:
            qb.setTables(SmugView.Image.TABLE);
            qb.setProjectionMap(imageProjection);
            if (sortOrder == null) {
                sortOrder = SmugView.Image.DEFAULT_SORT_ORDER;
            }
            break;
        case MATCH_IMAGE_ID:
            qb.setTables(SmugView.Image.TABLE);
            qb.setProjectionMap(imageProjection);
            if (sortOrder == null) {
                sortOrder = SmugView.Image.DEFAULT_SORT_ORDER;
            }

            qb.appendWhere(SmugView.Image._ID + " = "
                    + ContentUris.parseId(uri));
            break;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Log.d("SmugViewProvider", "query(" + uri + ", " + projection + ", "
                + selection + ", " + selectionArgs + ", " + sortOrder + ")");
        Cursor c = qb.query(db, projection, selection, selectionArgs, null,
                null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    /*
     * (non-Javadoc)
     * @see android.content.ContentProvider#update(android.net.Uri,
     * android.content.ContentValues, java.lang.String, java.lang.String[])
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

}
