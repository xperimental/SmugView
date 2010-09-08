package net.sourcewalker.smugview.data;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

/**
 * This class provides access to the local image cache on the device's SD card.
 * 
 * @author Xperimental
 */
public class ImageCache {

    private static final int BUFSIZE = 8 * 1024;
    private static final String TAG = "ImageCache";

    public ImageCache(Context ctx) {
        this.context = ctx;
    }

    private static final Map<String, String> idMap = new HashMap<String, String>();
    private static final Map<String, Drawable> images = new HashMap<String, Drawable>();
    private final Context context;

    public Drawable getFromMemory(String imageUrl) {
        String cacheId = getCacheId(imageUrl);
        return images.get(cacheId);
    }

    public boolean loadFromSD(String imageUrl) {
        String cacheId = getCacheId(imageUrl);
        boolean result = false;
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            File cacheFile = getCacheFile(cacheId);
            if (cacheFile.exists()) {
                try {
                    Drawable cached = new BitmapDrawable(new FileInputStream(
                            cacheFile));
                    images.put(cacheId, cached);
                    result = true;
                } catch (FileNotFoundException e) {
                    Log.e("Cache",
                            "Error reading cached thumbnail: " + e.getMessage());
                }
            }
        }
        return result;
    }

    private String getCacheId(String url) {
        if (idMap.containsKey(url)) {
            return idMap.get(url);
        } else {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-1");
                byte[] input = url.getBytes();
                byte[] output = digest.digest(input);
                StringBuilder sb = new StringBuilder();
                for (byte outByte : output) {
                    sb.append(String.format("%02x", outByte));
                }
                String cacheId = sb.toString();
                idMap.put(url, cacheId);
                return cacheId;
            } catch (NoSuchAlgorithmException e) {
                Log.e(TAG, "Can't cache images, no SHA1 implementation!");
                throw new RuntimeException(e);
            }
        }
    }

    private File getCacheFile(String cacheId) {
        return new File(context.getExternalFilesDir(null), cacheId + ".jpg");
    }

    public void saveImage(String imageUrl, Drawable image) {
        String cacheId = getCacheId(imageUrl);
        images.put(cacheId, image);
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            File cacheFile = getCacheFile(cacheId);
            try {
                Bitmap bitmap = ((BitmapDrawable) image).getBitmap();
                BufferedOutputStream output = new BufferedOutputStream(
                        new FileOutputStream(cacheFile), BUFSIZE);
                bitmap.compress(CompressFormat.JPEG, 92, output);
                output.close();
            } catch (IOException e) {
                Log.e("Cache",
                        "Error writing cached thumbnail: " + e.getMessage());
            }
        }
    }

}
