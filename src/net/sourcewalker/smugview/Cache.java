package net.sourcewalker.smugview;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cache {

    private static final Map<Integer, List<ImageInfo>> albumImages = new HashMap<Integer, List<ImageInfo>>();

    public static List<ImageInfo> getAlbumImages(AlbumInfo album) {
        return albumImages.get(album.getId());
    }

    public static void setAlbumImages(AlbumInfo album, List<ImageInfo> images) {
        albumImages.put(album.getId(), images);
    }

}
