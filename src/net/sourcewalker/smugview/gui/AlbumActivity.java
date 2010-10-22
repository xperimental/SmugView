package net.sourcewalker.smugview.gui;

import net.sourcewalker.smugview.R;
import net.sourcewalker.smugview.data.SmugView;
import net.sourcewalker.smugview.parcel.AlbumInfo;
import net.sourcewalker.smugview.parcel.Extras;
import android.app.ListActivity;
import android.content.ContentUris;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Window;
import android.widget.SimpleCursorAdapter;

public class AlbumActivity extends ListActivity {

    private static String[] LIST_COLUMNS = new String[] {
            SmugView.Image.DESCRIPTION, SmugView.Image.FILENAME,
            SmugView.Image.CONTENT };

    private static int[] LIST_VIEWS = new int[] { R.id.image_desc,
            R.id.image_filename, R.id.image_thumb };

    private SimpleCursorAdapter listAdapter;
    private long albumId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.album);

        albumId = (Long) getIntent().getExtras().get(Extras.EXTRA_ALBUM);

        Cursor cursor = managedQuery(ContentUris.withAppendedId(
                SmugView.Album.CONTENT_URI, albumId),
                SmugView.Album.DEFAULT_PROJECTION, null, null, null);
        cursor.moveToFirst();
        AlbumInfo album = new AlbumInfo(cursor);

        setTitle(album.getTitle());

        startGetImages();
    }

    private void startGetImages() {
        new GetImagesTask().execute(albumId);
    }

    private class GetImagesTask extends AsyncTask<Long, Void, Cursor> {

        @Override
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected Cursor doInBackground(Long... params) {
            return managedQuery(ContentUris.withAppendedId(
                    SmugView.AlbumImage.CONTENT_URI_IMAGES, params[0]),
                    SmugView.Image.DEFAULT_PROJECTION, null, null, null);
        }

        @Override
        protected void onPostExecute(Cursor result) {
            setProgressBarIndeterminateVisibility(false);

            listAdapter = new SimpleCursorAdapter(AlbumActivity.this,
                    R.layout.album_row, result, LIST_COLUMNS, LIST_VIEWS);
            setListAdapter(listAdapter);
        }

    }

}
