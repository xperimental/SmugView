package net.sourcewalker.smugview.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import net.sourcewalker.smugview.ApiConstants;
import net.sourcewalker.smugview.R;
import net.sourcewalker.smugview.auth.Authenticator;
import net.sourcewalker.smugview.data.Cache;
import net.sourcewalker.smugview.parcel.AlbumInfo;
import net.sourcewalker.smugview.parcel.Extras;
import net.sourcewalker.smugview.parcel.ImageInfo;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.ListActivity;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kallasoft.smugmug.api.json.entity.Album;
import com.kallasoft.smugmug.api.json.v1_2_0.APIVersionConstants;
import com.kallasoft.smugmug.api.json.v1_2_0.albums.Get;
import com.kallasoft.smugmug.api.json.v1_2_0.albums.Get.GetResponse;

public class AlbumListActivity extends ListActivity {

    private static final String TAG = "AlbumListActivity";

    private AlbumListAdapter listAdapter;

    private AccountManager accountManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.albumlist);

        if (Cache.get() == null) {
            Cache.init(this);
        }

        listAdapter = new AlbumListAdapter();
        setListAdapter(listAdapter);

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
                        Bundle result = future.getResult();
                        String session = result
                                .getString(AccountManager.KEY_AUTHTOKEN);
                        new GetAlbumsTask().execute(session);
                    } catch (OperationCanceledException e) {
                    } catch (AuthenticatorException e) {
                        Log.e(TAG,
                                "Exception while getting authtoken: "
                                        + e.getMessage());
                    } catch (IOException e) {
                        Log.e(TAG,
                                "Exception while getting authtoken: "
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
        AlbumInfo album = (AlbumInfo) listAdapter.getItem(position);
        Intent intent = new Intent(this, AlbumActivity.class);
        intent.putExtra(Extras.EXTRA_ALBUM, album);
        startActivity(intent);
    }

    private class GetAlbumsTask extends
            AsyncTask<String, Void, List<AlbumInfo>> {

        @Override
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected List<AlbumInfo> doInBackground(String... params) {
            String sessionId = params[0];
            List<AlbumInfo> result = new ArrayList<AlbumInfo>();
            GetResponse response = new Get().execute(
                    APIVersionConstants.SECURE_SERVER_URL, ApiConstants.APIKEY,
                    sessionId, false);
            if (!response.isError()) {
                for (Album a : response.getAlbumList()) {
                    result.add(new AlbumInfo(a));
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(List<AlbumInfo> result) {
            setProgressBarIndeterminateVisibility(false);

            listAdapter.clear();
            listAdapter.addAll(result);
        }
    }

    private class AlbumListAdapter implements ListAdapter {

        private List<AlbumInfo> albums = new ArrayList<AlbumInfo>();
        private List<DataSetObserver> observers = new ArrayList<DataSetObserver>();
        private Random rnd = new Random();

        public void addAll(Collection<? extends AlbumInfo> items) {
            albums.addAll(items);
            notifyObservers();
        }

        public void clear() {
            albums.clear();
            notifyObservers();
        }

        private void notifyObservers() {
            for (DataSetObserver observer : observers) {
                observer.onChanged();
            }
        }

        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }

        @Override
        public boolean isEnabled(int position) {
            return true;
        }

        @Override
        public int getCount() {
            return albums.size();
        }

        @Override
        public Object getItem(int position) {
            return albums.get(position);
        }

        @Override
        public long getItemId(int position) {
            return albums.get(position).getId();
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AlbumListHolder holder;
            if (convertView == null) {
                convertView = View.inflate(AlbumListActivity.this,
                        R.layout.albumlist_row, null);
                holder = new AlbumListHolder();
                holder.title = (TextView) convertView
                        .findViewById(R.id.albumrow_title);
                holder.desc = (TextView) convertView
                        .findViewById(R.id.albumrow_desc);
                holder.example = (ImageView) convertView
                        .findViewById(R.id.albumrow_image);
                convertView.setTag(holder);
            } else {
                holder = (AlbumListHolder) convertView.getTag();
            }
            AlbumInfo item = albums.get(position);
            holder.title.setText(item.getTitle());
            holder.desc.setText(item.getDescription());
            Drawable thumbnail = null;
            List<ImageInfo> cache = Cache.get().getAlbumImages(item);
            if (cache != null && cache.size() > 0) {
                ImageInfo random = cache.get(rnd.nextInt(cache.size()));
                thumbnail = random.getThumbnail();
            }
            holder.example.setImageDrawable(thumbnail);
            return convertView;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public boolean isEmpty() {
            return albums.size() == 0;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {
            observers.add(observer);
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            observers.remove(observer);
        }
    }

    private class AlbumListHolder {

        public TextView title;
        public TextView desc;
        public ImageView example;
    }
}
