package net.sourcewalker.smugview.data;

import java.util.ArrayList;
import java.util.List;

import net.sourcewalker.smugview.ApiConstants;
import net.sourcewalker.smugview.parcel.AlbumInfo;
import net.sourcewalker.smugview.parcel.ImageInfo;

import com.kallasoft.smugmug.api.json.entity.Album;
import com.kallasoft.smugmug.api.json.entity.Image;
import com.kallasoft.smugmug.api.json.v1_2_1.APIVersionConstants;

/**
 * @author Xperimental
 */
public class ServerOperations {

    private ServerOperations() {
    }

    public static List<AlbumInfo> getAlbums(String sessionId) {
        com.kallasoft.smugmug.api.json.v1_2_1.albums.Get.GetResponse response = new com.kallasoft.smugmug.api.json.v1_2_1.albums.Get()
                .execute(APIVersionConstants.SECURE_SERVER_URL,
                        ApiConstants.APIKEY, sessionId, true);
        List<AlbumInfo> result = new ArrayList<AlbumInfo>();
        if (!response.isError()) {
            for (Album a : response.getAlbumList()) {
                result.add(new AlbumInfo(a));
            }
        }
        return result;
    }

    public static List<ImageInfo> getImages(String sessionId, int albumId,
            String albumKey) {
        List<ImageInfo> result = new ArrayList<ImageInfo>();
        com.kallasoft.smugmug.api.json.v1_2_1.images.Get.GetResponse response = new com.kallasoft.smugmug.api.json.v1_2_1.images.Get()
                .execute(APIVersionConstants.SECURE_SERVER_URL,
                        ApiConstants.APIKEY, sessionId, albumId, albumKey, true);
        if (!response.isError()) {
            for (Image image : response.getImageList()) {
                result.add(new ImageInfo(albumId, image));
            }
        }
        return result;
    }

}
