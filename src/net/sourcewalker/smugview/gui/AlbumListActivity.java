package net.sourcewalker.smugview.gui;

import java.io.IOException;

import net.sourcewalker.smugview.R;
import net.sourcewalker.smugview.auth.Authenticator;
import net.sourcewalker.smugview.data.SmugView;
import net.sourcewalker.smugview.parcel.Extras;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class AlbumListActivity extends ListActivity {

    private static final String TAG = "AlbumListActivity";

    private AccountManager accountManager;
    private SimpleCursorAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.albumlist);

        accountManager = AccountManager.get(this);

        startGetAlbums();
    }

    private void startGetAlbums() {
        Account[] accounts = accountManager
                .getAccountsByType(Authenticator.TYPE);
        if (accounts.length == 0) {
            startAddAccount();
        } else {
            AccountManagerCallback<Bundle> callback = new AccountManagerCallback<Bundle>() {

                @Override
                public void run(AccountManagerFuture<Bundle> future) {
                    try {
                        future.getResult();
                        new GetAlbumsTask().execute();
                    } catch (OperationCanceledException e) {
                    } catch (AuthenticatorException e) {
                        Log.e(TAG, "Exception while getting authtoken: "
                                + e.getMessage());
                    } catch (IOException e) {
                        Log.e(TAG, "Exception while getting authtoken: "
                                + e.getMessage());
                    }
                }
            };
            accountManager.getAuthToken(accounts[0], Authenticator.TOKEN_TYPE,
                    null, this, callback, null);
        }
    }

    /**
     * Launches the activity to create a new account. If the account was created
     * successfully the album list is refreshed.
     */
    private void startAddAccount() {
        AccountManagerCallback<Bundle> callback = new AccountManagerCallback<Bundle>() {

            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    // Get result (throws exceptions)
                    future.getResult();
                    // Retry getting albums
                    startGetAlbums();
                } catch (OperationCanceledException e) {
                    Toast.makeText(AlbumListActivity.this,
                            R.string.auth_accountneeded, Toast.LENGTH_LONG)
                            .show();
                    finish();
                } catch (AuthenticatorException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        };
        accountManager.addAccount(Authenticator.TYPE, null, null, null, this,
                callback, null);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(this, AlbumActivity.class);
        intent.putExtra(Extras.EXTRA_ALBUM, id);
        startActivity(intent);
    }

    private class GetAlbumsTask extends AsyncTask<Void, Void, Cursor> {

        @Override
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected Cursor doInBackground(Void... params) {
            return managedQuery(SmugView.Album.CONTENT_URI,
                    SmugView.Album.DEFAULT_PROJECTION, null, null, null);
        }

        @Override
        protected void onPostExecute(Cursor result) {
            setProgressBarIndeterminateVisibility(false);

            listAdapter = new SimpleCursorAdapter(AlbumListActivity.this,
                    R.layout.albumlist_row, result, new String[] {
                            SmugView.Album.TITLE, SmugView.Album.DESCRIPTION,
                            SmugView.Image.CONTENT }, new int[] {
                            R.id.albumrow_title, R.id.albumrow_desc,
                            R.id.albumrow_image });
            setListAdapter(listAdapter);
        }
    }

}
