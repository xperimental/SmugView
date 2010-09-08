package net.sourcewalker.smugview.gui;

import net.sourcewalker.smugview.R;
import net.sourcewalker.smugview.data.ImageCache;
import net.sourcewalker.smugview.data.ImageDownloadService;
import net.sourcewalker.smugview.data.SmugView;
import net.sourcewalker.smugview.parcel.AlbumInfo;
import net.sourcewalker.smugview.parcel.Extras;
import android.app.ListActivity;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;

public class AlbumActivity extends ListActivity {

    private static final int LOADING_IMAGE = android.R.drawable.ic_menu_rotate;

    private SimpleCursorAdapter listAdapter;
    private long albumId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.album);

        albumId = (Long) getIntent().getExtras().get(Extras.EXTRA_ALBUM);

        Cursor cursor = managedQuery(
                ContentUris.withAppendedId(SmugView.Album.CONTENT_URI, albumId),
                SmugView.Album.DEFAULT_PROJECTION, null, null, null);
        cursor.moveToFirst();
        AlbumInfo album = new AlbumInfo(cursor);

        setTitle(album.getTitle());

        startGetImages();
    }

    private void startGetImages() {
        new GetImagesTask().execute(albumId);
    }

    private class ThumbnailBinder implements ViewBinder {

        private ImageCache imageCache = new ImageCache(AlbumActivity.this);

        /*
         * (non-Javadoc)
         * @see
         * android.widget.SimpleCursorAdapter.ViewBinder#setViewValue(android
         * .view.View, android.database.Cursor, int)
         */
        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            if (view.getId() == R.id.image_thumb
                    && columnIndex == cursor
                            .getColumnIndex(SmugView.Image.THUMBNAIL_URL)) {
                ImageView image = (ImageView) view;
                String url = cursor.getString(columnIndex);
                Drawable cachedImage = imageCache.getFromMemory(url);
                if (cachedImage == null) {
                    image.setImageDrawable(getResources().getDrawable(
                            LOADING_IMAGE));
                    Uri notifyUri = ContentUris.withAppendedId(
                            SmugView.Image.CONTENT_URI, cursor.getInt(cursor
                                    .getColumnIndex(SmugView.Image._ID)));
                    ImageDownloadService.startDownload(AlbumActivity.this, url,
                            notifyUri);
                } else {
                    image.setImageDrawable(cachedImage);
                }
                return true;
            } else {
                return false;
            }
        }
    }

    private class GetImagesTask extends AsyncTask<Long, Void, Cursor> {

        @Override
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected Cursor doInBackground(Long... params) {
            return managedQuery(SmugView.Image.CONTENT_URI,
                    SmugView.Image.DEFAULT_PROJECTION, SmugView.Image.ALBUM_ID
                            + " = ?", new String[] { params[0].toString() },
                    null);
        }

        @Override
        protected void onPostExecute(Cursor result) {
            setProgressBarIndeterminateVisibility(false);

            listAdapter = new SimpleCursorAdapter(AlbumActivity.this,
                    R.layout.album_row, result, new String[] {
                            SmugView.Image.DESCRIPTION,
                            SmugView.Image.FILENAME,
                            SmugView.Image.THUMBNAIL_URL }, new int[] {
                            R.id.image_desc, R.id.image_filename,
                            R.id.image_thumb });
            listAdapter.setViewBinder(new ThumbnailBinder());
            setListAdapter(listAdapter);
        }

    }

}
