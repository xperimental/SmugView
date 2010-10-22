package net.sourcewalker.smugview.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
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
    private static final int MATCH_ALBUM_IMAGE = 3;
    private static final int MATCH_IMAGE = 4;
    private static final int MATCH_IMAGE_ID = 5;
    private static final int MATCH_IMAGE_ALBUM = 6;
    private static final int MATCH_THUMBNAIL = 7;
    private static final int LOADING_IMAGE = android.R.drawable.ic_menu_rotate;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(SmugView.AUTHORITY, "albums", MATCH_ALBUM);
        uriMatcher.addURI(SmugView.AUTHORITY, "albums/#", MATCH_ALBUM_ID);
        uriMatcher.addURI(SmugView.AUTHORITY, "albums/byImage/#",
                MATCH_ALBUM_IMAGE);
        uriMatcher.addURI(SmugView.AUTHORITY, "images", MATCH_IMAGE);
        uriMatcher.addURI(SmugView.AUTHORITY, "images/#", MATCH_IMAGE_ID);
        uriMatcher.addURI(SmugView.AUTHORITY, "images/byAlbum/#",
                MATCH_IMAGE_ALBUM);
        uriMatcher.addURI(SmugView.AUTHORITY, "thumbnail/#", MATCH_THUMBNAIL);

        albumProjection = new HashMap<String, String>();
        for (String key : SmugView.Album.DEFAULT_PROJECTION) {
            albumProjection.put(key, key);
        }
        albumProjection.put(SmugView.Image.CONTENT, "'"
                + SmugView.Thumbnail.CONTENT_URI + "/' || imageid AS content");

        imageProjection = new HashMap<String, String>();
        for (String key : SmugView.Image.DEFAULT_PROJECTION) {
            if (key.equals(SmugView.Image.CONTENT)) {
                imageProjection.put(SmugView.Image.CONTENT, "'"
                        + SmugView.Thumbnail.CONTENT_URI
                        + "/' || _id AS content");
            } else {
                imageProjection.put(key, key);
            }
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
        int count = 0;
        String[] id;
        switch (uriMatcher.match(uri)) {
        case MATCH_ALBUM:
            count += db.delete(SmugView.Image.TABLE, null, null);
            count += db.delete(SmugView.AlbumImage.TABLE, null, null);
            count += db.delete(SmugView.Album.TABLE, null, null);
            break;
        case MATCH_ALBUM_ID:
            id = new String[] { uri.getLastPathSegment() };
            count += db.delete(SmugView.AlbumImage.TABLE,
                    SmugView.AlbumImage.ALBUMID + " = ?", id);
            count += db.delete(SmugView.Album.TABLE, SmugView.Album._ID
                    + " = ?", id);
            break;
        case MATCH_IMAGE:
            count += db.delete(SmugView.AlbumImage.TABLE, null, null);
            count += db.delete(SmugView.Image.TABLE, selection, selectionArgs);
            break;
        case MATCH_IMAGE_ID:
            id = new String[] { uri.getLastPathSegment() };
            count += db.delete(SmugView.AlbumImage.TABLE,
                    SmugView.AlbumImage.IMAGEID + " = ?", id);
            count += db.delete(SmugView.Image.TABLE, SmugView.Image._ID
                    + " = ?", id);
            break;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        return count;
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
        case MATCH_IMAGE:
            return insertImage(initialValues);
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    private Uri insertImage(ContentValues initialValues) {
        for (String column : SmugView.Image.DEFAULT_PROJECTION) {
            if (column.equals(SmugView.Image.CONTENT)) {
                continue;
            }
            if (initialValues.containsKey(column) == false) {
                throw new IllegalArgumentException("Need column '" + column
                        + "' : " + initialValues);
            }
        }
        ContentValues albumImageValues = getAlbumImageValues(initialValues);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long insert = db.insert(SmugView.Image.TABLE,
                SmugView.Image.DESCRIPTION, initialValues);
        if (insert != -1) {
            db.insert(SmugView.AlbumImage.TABLE, SmugView.AlbumImage.ALBUMID,
                    albumImageValues);
            Uri imageUri = ContentUris.withAppendedId(
                    SmugView.Image.CONTENT_URI, initialValues
                            .getAsInteger(SmugView.Image._ID));
            getContext().getContentResolver().notifyChange(imageUri, null);
            return imageUri;
        } else {
            return null;
        }
    }

    private ContentValues getAlbumImageValues(ContentValues initialValues) {
        ContentValues result = new ContentValues();
        result.put(SmugView.AlbumImage.ALBUMID, initialValues
                .getAsLong(SmugView.AlbumImage.ALBUMID));
        result.put(SmugView.AlbumImage.IMAGEID, initialValues
                .getAsLong(SmugView.Image._ID));
        initialValues.remove(SmugView.AlbumImage.ALBUMID);
        return result;
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
        String groupBy = null;
        switch (uriMatcher.match(uri)) {
        case MATCH_ALBUM:
            qb.setTables(SmugView.Album.TABLE + ", "
                    + SmugView.AlbumImage.TABLE);
            qb.setProjectionMap(albumProjection);
            if (sortOrder == null) {
                sortOrder = SmugView.Album.DEFAULT_SORT_ORDER;
            }
            qb.appendWhere(SmugView.Album._ID + " = "
                    + SmugView.AlbumImage.ALBUMID);
            groupBy = SmugView.Album._ID;
            projection = appendString(projection, SmugView.Image.CONTENT);
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
        case MATCH_ALBUM_IMAGE:
            qb.setTables(SmugView.Album.TABLE + ", "
                    + SmugView.AlbumImage.TABLE);
            qb.setProjectionMap(albumProjection);

            qb.appendWhere(SmugView.Album._ID + " = "
                    + SmugView.AlbumImage.ALBUMID);
            qb.appendWhere(" AND " + SmugView.AlbumImage.IMAGEID + " = "
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
        case MATCH_IMAGE_ALBUM:
            qb.setTables(SmugView.AlbumImage.TABLE + ", "
                    + SmugView.Image.TABLE);
            qb.setProjectionMap(imageProjection);

            qb.appendWhere(SmugView.AlbumImage.IMAGEID + " = "
                    + SmugView.Image._ID);
            qb.appendWhere(" AND " + SmugView.AlbumImage.ALBUMID + " = "
                    + ContentUris.parseId(uri));
            break;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Log.d("SmugViewProvider", "query(" + uri + ", " + projection + ", "
                + selection + ", " + selectionArgs + ", " + sortOrder + ")");
        Cursor c = qb.query(db, projection, selection, selectionArgs, groupBy,
                null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    private String[] appendString(String[] array, String item) {
        ArrayList<String> list = new ArrayList<String>(array.length + 1);
        list.addAll(Arrays.asList(array));
        list.add(item);
        return list.toArray(array);
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

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode)
            throws FileNotFoundException {
        if (!"r".equals(mode)) {
            throw new IllegalArgumentException("Only read mode allowed!");
        }
        switch (uriMatcher.match(uri)) {
        case MATCH_THUMBNAIL:
            long id = ContentUris.parseId(uri);
            File thumbnailFile = ImageStore
                    .getImageFile(getContext(), id, true);
            if (thumbnailFile.exists()) {
                return ParcelFileDescriptor.open(thumbnailFile,
                        ParcelFileDescriptor.MODE_READ_ONLY);
            } else {
                ImageDownloadService.startDownload(getContext(), id, true);
                return getLoadingImage();
            }
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    private ParcelFileDescriptor getLoadingImage() throws FileNotFoundException {
        File loadingFile = new File(getContext().getExternalFilesDir(null),
                "loading.png");
        if (!loadingFile.exists()) {
            BitmapDrawable loading = (BitmapDrawable) getContext()
                    .getResources().getDrawable(LOADING_IMAGE);
            FileOutputStream stream = new FileOutputStream(loadingFile);
            loading.getBitmap().compress(CompressFormat.PNG, 100, stream);
            try {
                stream.close();
            } catch (IOException e) {
                throw new RuntimeException("Couldn't write image: "
                        + e.getMessage(), e);
            }
        }
        return ParcelFileDescriptor.open(loadingFile,
                ParcelFileDescriptor.MODE_READ_ONLY);
    }

}
