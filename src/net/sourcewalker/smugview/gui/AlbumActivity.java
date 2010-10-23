package net.sourcewalker.smugview.gui;

import java.io.File;

import net.sourcewalker.smugview.R;
import net.sourcewalker.smugview.data.ImageDownloadService;
import net.sourcewalker.smugview.data.ImageStore;
import net.sourcewalker.smugview.data.SmugView;
import net.sourcewalker.smugview.parcel.AlbumInfo;
import net.sourcewalker.smugview.parcel.Extras;
import android.app.ListActivity;
import android.content.ContentUris;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class AlbumActivity extends ListActivity implements OnClickListener {

    private static String[] LIST_COLUMNS = new String[] {
            SmugView.Image.DESCRIPTION, SmugView.Image.FILENAME,
            SmugView.Image.CONTENT };

    private static int[] LIST_VIEWS = new int[] { R.id.image_desc,
            R.id.image_filename, R.id.image_thumb };

    private SimpleCursorAdapter listAdapter;
    private long albumId;
    private ImageView singleImageView;
    private ContentObserver singleImageObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.album);

        singleImageView = (ImageView) findViewById(R.id.album_imageview);
        singleImageView.setOnClickListener(this);

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

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        singleImageView.setVisibility(View.VISIBLE);
        singleImageObserver = new FullImageObserver(new Handler(), id);
        getContentResolver().registerContentObserver(
                ContentUris.withAppendedId(SmugView.Image.CONTENT_URI, id),
                false, singleImageObserver);
        new LoadFullImageTask().execute(id);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.album_imageview) {
            hideSingleImage();
        }
    }

    private void hideSingleImage() {
        singleImageView.setVisibility(View.GONE);
        getContentResolver().unregisterContentObserver(singleImageObserver);
    }

    @Override
    protected void onPause() {
        if (singleImageObserver != null) {
            getContentResolver().unregisterContentObserver(singleImageObserver);
        }
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (singleImageView.getVisibility() == View.VISIBLE) {
            hideSingleImage();
        } else {
            super.onBackPressed();
        }
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

    private class LoadFullImageTask extends AsyncTask<Long, Void, Drawable> {

        private boolean success = false;

        @Override
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected Drawable doInBackground(Long... params) {
            long imageId = params[0];
            Drawable result;
            File imageFile = ImageStore.getImageFile(AlbumActivity.this,
                    imageId, false);
            if (!imageFile.exists()) {
                File thumbnail = ImageStore.getImageFile(AlbumActivity.this,
                        imageId, true);
                if (thumbnail.exists()) {
                    result = ImageStore.readImage(thumbnail);
                } else {
                    result = getResources().getDrawable(
                            ImageStore.LOADING_IMAGE);
                }
                ImageDownloadService.startDownload(AlbumActivity.this, imageId,
                        false);
            } else {
                result = ImageStore.readImage(imageFile);
                success = true;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Drawable result) {
            if (success) {
                setProgressBarIndeterminateVisibility(false);
            }

            singleImageView.setImageDrawable(result);
        }

    }

    private class FullImageObserver extends ContentObserver {

        private final long imageId;

        public FullImageObserver(Handler handler, long imageId) {
            super(handler);
            this.imageId = imageId;
        }

        @Override
        public void onChange(boolean selfChange) {
            if (!selfChange) {
                new LoadFullImageTask().execute(imageId);
            }
        }

    }

}
