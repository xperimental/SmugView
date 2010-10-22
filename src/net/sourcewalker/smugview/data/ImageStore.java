package net.sourcewalker.smugview.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

public class ImageStore {

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

}
