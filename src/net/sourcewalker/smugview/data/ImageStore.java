package net.sourcewalker.smugview.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

public class ImageStore {

    public static final int LOADING_IMAGE = android.R.drawable.ic_menu_rotate;
    private static final String TAG = "ImageStore";

    public static File getImageFile(Context context, long imageId,
            boolean thumbnail) {
        String directory = thumbnail ? "thumb/" : "image/";
        return new File(context.getExternalFilesDir(null), directory + imageId);
    }

    public static void writeImage(BitmapDrawable image, File imageFile) {
        try {
            File directory = imageFile.getParentFile();
            if (!directory.exists()) {
                directory.mkdirs();
            }
            Bitmap bitmap = image.getBitmap();
            FileOutputStream stream = new FileOutputStream(imageFile);
            bitmap.compress(CompressFormat.JPEG, 90, stream);
            stream.close();
        } catch (IOException e) {
            Log.e(TAG, "Couldn't write image: " + e.getMessage());
        }
    }

    public static BitmapDrawable readImage(File imageFile) {
        BitmapDrawable result = null;
        try {
            FileInputStream stream = new FileInputStream(imageFile);
            result = new BitmapDrawable(stream);
            stream.close();
        } catch (IOException e) {
            Log.e(TAG, "Couldn't read image: " + e.getMessage());
        }
        return result;
    }

}
