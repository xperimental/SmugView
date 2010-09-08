package net.sourcewalker.smugview.data;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;

/**
 * This service downloads images to the local cache and then notifies the
 * content provider.
 * 
 * @author Xperimental
 */
public class ImageDownloadService extends IntentService {

    public static final String EXTRA_NOTIFYURI = "net.sourcewalker.smugview.data.ImageDownloadService.EXTRA_NOTIFYURI";
    private static final String TAG = "ImageDownloadService";

    public static void startDownload(Context context, String downloadUrl,
            Uri notifyUri) {
        Intent intent = new Intent(context, ImageDownloadService.class);
        intent.setAction(downloadUrl);
        intent.putExtra(EXTRA_NOTIFYURI, notifyUri);
        context.startService(intent);
    }

    private ImageCache cache = new ImageCache(this);

    public ImageDownloadService() {
        super("ImageDownloadThread");
    }

    /*
     * (non-Javadoc)
     * @see android.app.IntentService#onHandleIntent(android.content.Intent)
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        String downloadUrl = intent.getAction();
        Uri notifyUri = (Uri) intent.getExtras().get(EXTRA_NOTIFYURI);
        Log.d(TAG, "Downloading image: " + downloadUrl);
        if (cache.getFromMemory(downloadUrl) == null) {
            if (cache.loadFromSD(downloadUrl) == false) {
                Drawable image = ServerOperations.downloadImage(downloadUrl);
                cache.saveImage(downloadUrl, image);
            }
            Log.d(TAG, "Notifying URI: " + notifyUri);
            getContentResolver().notifyChange(notifyUri, null);
        } else {
            Log.d(TAG, "Skipping: Cached.");
        }
    }

}
