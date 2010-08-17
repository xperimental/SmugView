package net.sourcewalker.smugview.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourcewalker.smugview.parcel.AlbumInfo;
import net.sourcewalker.smugview.parcel.ImageInfo;
import android.graphics.drawable.Drawable;

public class Cache {

    private static final Map<Integer, List<ImageInfo>> albumImages = new HashMap<Integer, List<ImageInfo>>();
    private static final Map<Integer, Drawable> thumbnails = new HashMap<Integer, Drawable>();
    private static final Map<Integer, Drawable> images = new HashMap<Integer, Drawable>();

    public static List<ImageInfo> getAlbumImages(AlbumInfo album) {
        return albumImages.get(album.getId());
    }

    public static void setAlbumImages(AlbumInfo album, List<ImageInfo> images) {
        albumImages.put(album.getId(), images);
    }

    public static Drawable getThumbnail(int imageId) {
        return thumbnails.get(imageId);
    }

    public static void saveThumbnail(int imageId, Drawable thumbnail) {
        thumbnails.put(imageId, thumbnail);
    }

    public static Drawable getImage(int imageId) {
        return images.get(imageId);
    }

    public static void saveImage(int imageId, Drawable image) {
        images.put(imageId, image);
    }

    public static ImageInfo getPreviousInAlbum(ImageInfo image) {
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

    public static ImageInfo getNextInAlbum(ImageInfo image) {
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

    /**
     * Can't create instance of Cache class. Only for static members.
     */
    private Cache() {
    }

}
