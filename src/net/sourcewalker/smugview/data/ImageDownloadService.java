package net.sourcewalker.smugview.data;

import java.io.File;

import android.app.IntentService;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.util.Log;

/**
 * This service downloads images to the local cache and then notifies the
 * content provider.
 * 
 * @author Xperimental
 */
public class ImageDownloadService extends IntentService {

    private static final String TAG = "ImageDownloadService";
    private static final String KEY_IMAGEID = "_id";
    private static final String KEY_THUMBNAIL = "thumbnail";

    public static void startDownload(Context context, long imageId,
            boolean thumbnail) {
        Intent intent = new Intent(context, ImageDownloadService.class);
        intent.putExtra(KEY_IMAGEID, imageId);
        intent.putExtra(KEY_THUMBNAIL, thumbnail);
        context.startService(intent);
    }

    public ImageDownloadService() {
        super("ImageDownloadThread");
    }

    /*
     * (non-Javadoc)
     * @see android.app.IntentService#onHandleIntent(android.content.Intent)
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        long imageId = intent.getExtras().getLong(KEY_IMAGEID);
        boolean thumbnail = intent.getExtras().getBoolean(KEY_THUMBNAIL);
        String column = thumbnail ? SmugView.Image.THUMBNAIL_URL
                : SmugView.Image.IMAGE_URL;
        File imageFile = ImageStore.getImageFile(this, imageId, thumbnail);
        if (!imageFile.exists()) {
            Cursor cursor = null;
            try {
                Uri imageUri = ContentUris.withAppendedId(
                        SmugView.Image.CONTENT_URI, imageId);
                cursor = getContentResolver().query(imageUri,
                        new String[] { column }, null, null, null);
                if (cursor.moveToFirst()) {
                    String downloadUrl = cursor.getString(0);
                    Log.d(TAG, "Downloading image: " + downloadUrl);
                    BitmapDrawable image = ServerOperations
                            .downloadImage(downloadUrl);
                    ImageStore.writeImage(image, imageFile);
                    getContentResolver().notifyChange(imageUri, null);
                    notifyAllAlbums(imageId);
                } else {
                    Log.e(TAG, "Image not found: " + imageUri);
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    private void notifyAllAlbums(long imageId) {
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(
                    ContentUris.withAppendedId(
                            SmugView.AlbumImage.CONTENT_URI_ALBUMS, imageId),
                    new String[] { SmugView.Album._ID }, null, null, null);
            while (cursor.moveToNext()) {
                long albumId = cursor.getLong(0);
                getContentResolver().notifyChange(
                        ContentUris.withAppendedId(SmugView.Album.CONTENT_URI,
                                albumId), null);
                getContentResolver().notifyChange(
                        ContentUris
                                .withAppendedId(
                                        SmugView.AlbumImage.CONTENT_URI_IMAGES,
                                        albumId), null);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
