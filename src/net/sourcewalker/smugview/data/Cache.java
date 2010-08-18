package net.sourcewalker.smugview.data;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourcewalker.smugview.parcel.AlbumInfo;
import net.sourcewalker.smugview.parcel.ImageInfo;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

public class Cache {

    private static final int BUFSIZE = 8 * 1024;
    private static Cache instance;

    public static void init(Context ctx) {
        instance = new Cache(ctx.getApplicationContext());
    }

    public static Cache get() {
        return instance;
    }

    /**
     * Can't create instance of Cache class. Only for static members.
     */
    private Cache(Context ctx) {
        this.context = ctx;
    }

    private final Map<Integer, List<ImageInfo>> albumImages = new HashMap<Integer, List<ImageInfo>>();
    private final Map<Integer, Drawable> thumbnails = new HashMap<Integer, Drawable>();
    private final Map<Integer, Drawable> images = new HashMap<Integer, Drawable>();
    private final Context context;

    public List<ImageInfo> getAlbumImages(AlbumInfo album) {
        return albumImages.get(album.getId());
    }

    public void setAlbumImages(AlbumInfo album, List<ImageInfo> images) {
        albumImages.put(album.getId(), images);
    }

    public Drawable getThumbnail(int imageId) {
        Drawable cached = thumbnails.get(imageId);
        if (cached == null
                && Environment.getExternalStorageState().equals(
                        Environment.MEDIA_MOUNTED)) {
            File cacheFile = getThumbnailFile(imageId);
            if (cacheFile.exists()) {
                try {
                    cached = new BitmapDrawable(new FileInputStream(cacheFile));
                    thumbnails.put(imageId, cached);
                } catch (FileNotFoundException e) {
                    Log.e("Cache", "Error reading cached thumbnail: "
                            + e.getMessage());
                }
            }
        }
        return cached;
    }

    private File getThumbnailFile(int imageId) {
        return new File(context.getExternalFilesDir(null), "thumbnail-"
                + imageId + ".jpg");
    }

    public void saveThumbnail(int imageId, Drawable thumbnail) {
        thumbnails.put(imageId, thumbnail);
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            File cacheFile = getThumbnailFile(imageId);
            try {
                Bitmap bitmap = ((BitmapDrawable) thumbnail).getBitmap();
                BufferedOutputStream output = new BufferedOutputStream(
                        new FileOutputStream(cacheFile), BUFSIZE);
                bitmap.compress(CompressFormat.JPEG, 92, output);
                output.close();
            } catch (IOException e) {
                Log.e("Cache", "Error writing cached thumbnail: "
                        + e.getMessage());
            }
        }
    }

    public Drawable getImage(int imageId) {
        Drawable cached = images.get(imageId);
        if (cached == null
                && Environment.getExternalStorageState().equals(
                        Environment.MEDIA_MOUNTED)) {
            File cacheFile = getImageFile(imageId);
            if (cacheFile.exists()) {
                try {
                    cached = new BitmapDrawable(new FileInputStream(cacheFile));
                    images.put(imageId, cached);
                } catch (FileNotFoundException e) {
                    Log.e("Cache", "Error reading cached image: "
                            + e.getMessage());
                }
            }
        }
        return cached;
    }

    private File getImageFile(int imageId) {
        return new File(context.getExternalFilesDir(null), "image-" + imageId
                + ".jpg");
    }

    public void saveImage(int imageId, Drawable image) {
        if (image != null) {
            images.put(imageId, image);
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                File cacheFile = getImageFile(imageId);
                try {
                    Bitmap bitmap = ((BitmapDrawable) image).getBitmap();
                    BufferedOutputStream output = new BufferedOutputStream(
                            new FileOutputStream(cacheFile), BUFSIZE);
                    bitmap.compress(CompressFormat.JPEG, 92, output);
                    output.close();
                } catch (IOException e) {
                    Log.e("Cache", "Error writing cached image: "
                            + e.getMessage());
                }
            }
        } else {
            images.remove(imageId);
        }
    }

    public ImageInfo getPreviousInAlbum(ImageInfo image) {
        for (List<ImageInfo> list : albumImages.values()) {
            int index = list.indexOf(image);
            if (index != -1) {
                if (index != 0) {
                    return list.get(index - 1);
                }
            }
        }
        return null;
    }

    public ImageInfo getNextInAlbum(ImageInfo image) {
        for (List<ImageInfo> list : albumImages.values()) {
            int index = list.indexOf(image);
            if (index != -1) {
                if (index != list.size() - 1) {
                    return list.get(index + 1);
                }
            }
        }
        return null;
    }

}
