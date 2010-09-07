package net.sourcewalker.smugview.data;

import java.io.IOException;

import net.sourcewalker.smugview.ApiConstants;
import net.sourcewalker.smugview.auth.Authenticator;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import com.kallasoft.smugmug.api.json.entity.Album;
import com.kallasoft.smugmug.api.json.v1_2_1.APIVersionConstants;
import com.kallasoft.smugmug.api.json.v1_2_1.albums.Get;
import com.kallasoft.smugmug.api.json.v1_2_1.albums.Get.GetResponse;

/**
 * @author Xperimental
 */
public class ReloadAlbumsTask extends AsyncTask<Void, Void, Boolean> implements
        AccountManagerCallback<Bundle> {

    private final Context context;

    public ReloadAlbumsTask(Context ctx) {
        this.context = ctx;
    }

    /*
     * (non-Javadoc)
     * @see android.os.AsyncTask#doInBackground(Params[])
     */
    @Override
    protected Boolean doInBackground(Void... params) {

        AccountManager accountManager = AccountManager.get(context);
        Account[] accounts = accountManager
                .getAccountsByType(Authenticator.TYPE);
        if (accounts.length != 0) {
            accountManager.getAuthToken(accounts[0], Authenticator.TOKEN_TYPE,
                    false, this, null);
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * @see android.accounts.AccountManagerCallback#run(android.accounts.
     * AccountManagerFuture)
     */
    @Override
    public void run(AccountManagerFuture<Bundle> future) {
        try {
            Bundle result = future.getResult();
            String sessionId = result.getString(AccountManager.KEY_AUTHTOKEN);
            getAlbums(sessionId);
        } catch (OperationCanceledException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (AuthenticatorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void getAlbums(String sessionId) {
        GetResponse response = new Get().execute(
                APIVersionConstants.SECURE_SERVER_URL, ApiConstants.APIKEY,
                sessionId, true);
        if (!response.isError()) {
            ContentResolver cr = context.getContentResolver();
            cr.delete(SmugView.Album.CONTENT_URI, null, null);
            for (Album a : response.getAlbumList()) {
                ContentValues values = new ContentValues();
                values.put(SmugView.Album._ID, a.getID());
                values.put(SmugView.Album.TITLE, a.getTitle());
                values.put(SmugView.Album.DESCRIPTION, a.getDescription());
                values.put(SmugView.Album.KEY, a.getAlbumKey());
                values.put(SmugView.Album.MODIFIED, a.getLastUpdated());
                cr.insert(SmugView.Album.CONTENT_URI, values);
            }
        }
    }

}
