package net.sourcewalker.smugview.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Helper for creating and accessing the database.
 * 
 * @author Xperimental
 */
public class SmugViewDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smugview.db";
    private static final int DATABASE_VERSION = 10;

    public SmugViewDbHelper(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /*
     * (non-Javadoc)
     * @see
     * android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite
     * .SQLiteDatabase)
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SmugView.Album.SCHEMA);
        db.execSQL(SmugView.Image.SCHEMA);
    }

    /*
     * (non-Javadoc)
     * @see
     * android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite
     * .SQLiteDatabase, int, int)
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("SmugViewDbHelper", "Upgrading database from " + oldVersion
                + " to " + newVersion + " erases all data!");
        db.execSQL("DROP TABLE IF EXISTS " + SmugView.Album.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + SmugView.Image.TABLE);
        onCreate(db);
    }

}
