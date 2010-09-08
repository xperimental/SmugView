package net.sourcewalker.smugview.data;

import java.io.IOException;
import java.util.List;

import net.sourcewalker.smugview.auth.Authenticator;
import net.sourcewalker.smugview.parcel.AlbumInfo;
import net.sourcewalker.smugview.parcel.ImageInfo;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

/**
 * This sync adapter will synchronize all albums and the metadata of every image
 * they contain with a local database. The images itself (including thumbnails)
 * will not be downloaded with the sync adapter.
 * 
 * @author Xperimental
 */
public class SmugViewSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = "SmugViewSyncAdapter";

    private AccountManager accountManager;
    private boolean canceled;

    public SmugViewSyncAdapter(Context context) {
        super(context, true);

        this.accountManager = AccountManager.get(context);
    }

    /*
     * (non-Javadoc)
     * @see
     * android.content.AbstractThreadedSyncAdapter#onPerformSync(android.accounts
     * .Account, android.os.Bundle, java.lang.String,
     * android.content.ContentProviderClient, android.content.SyncResult)
     */
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
            ContentProviderClient provider, SyncResult syncResult) {
        canceled = false;
        try {
            String sessionId = accountManager.blockingGetAuthToken(account,
                    Authenticator.TOKEN_TYPE, true);
            if (sessionId != null) {
                // Remove current content
                syncResult.stats.numDeletes += provider.delete(
                        SmugView.Image.CONTENT_URI, null, null);
                syncResult.stats.numDeletes += provider.delete(
                        SmugView.Album.CONTENT_URI, null, null);
                // Get list of albums
                List<AlbumInfo> albumList = ServerOperations
                        .getAlbums(sessionId);
                for (AlbumInfo album : albumList) {
                    if (canceled) {
                        throw new OperationCanceledException();
                    }
                    Log.d(TAG, "Getting images for: " + album.getTitle());
                    provider.insert(SmugView.Album.CONTENT_URI,
                            album.toValues());
                    syncResult.stats.numInserts++;
                    List<ImageInfo> images = ServerOperations.getImages(
                            sessionId, album.getId(), album.getKey());
                    for (ImageInfo image : images) {
                        provider.insert(SmugView.Image.CONTENT_URI,
                                image.toValues());
                        syncResult.stats.numInserts++;
                    }
                }
            }
        } catch (OperationCanceledException e) {
            Log.d(TAG, "Sync canceled.");
        } catch (AuthenticatorException e) {
            syncResult.stats.numAuthExceptions++;
        } catch (IOException e) {
            syncResult.stats.numIoExceptions++;
        } catch (RemoteException e) {
            syncResult.stats.numIoExceptions++;
        }
    }

    /*
     * (non-Javadoc)
     * @see android.content.AbstractThreadedSyncAdapter#onSyncCanceled()
     */
    @Override
    public void onSyncCanceled() {
        canceled = true;
    }

}
