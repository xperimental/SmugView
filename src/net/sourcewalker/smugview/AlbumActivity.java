package net.sourcewalker.smugview;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.ListActivity;
import android.database.DataSetObserver;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.kallasoft.smugmug.api.json.entity.Image;
import com.kallasoft.smugmug.api.json.v1_2_0.APIVersionConstants;
import com.kallasoft.smugmug.api.json.v1_2_0.images.Get;
import com.kallasoft.smugmug.api.json.v1_2_0.images.Get.GetResponse;

public class AlbumActivity extends ListActivity {

    private LoginResult login;
    private AlbumInfo album;
    private AlbumAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.album);

        login = (LoginResult) getIntent().getExtras().get(Extras.EXTRA_LOGIN);
        album = (AlbumInfo) getIntent().getExtras().get(Extras.EXTRA_ALBUM);

        adapter = new AlbumAdapter();
        setListAdapter(adapter);

        List<ImageInfo> cachedImages = Cache.getAlbumImages(album);
        if (cachedImages == null) {
            startGetImages();
        } else {
            setImageList(cachedImages);
        }
    }

    private void startGetImages() {
        new GetImagesTask().execute(login, album);
    }

    private void setImageList(List<ImageInfo> imageList) {
        adapter.clear();
        adapter.addAll(imageList);
    }

    private class GetImagesTask extends
            AsyncTask<Object, Void, List<ImageInfo>> {

        @Override
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected List<ImageInfo> doInBackground(Object... params) {
            LoginResult login = (LoginResult) params[0];
            AlbumInfo album = (AlbumInfo) params[1];
            List<ImageInfo> result = new ArrayList<ImageInfo>();

            GetResponse response = new Get().execute(
                    APIVersionConstants.SECURE_SERVER_URL, ApiConstants.APIKEY,
                    login.getSession(), album.getId(), album.getKey(), true);
            if (!response.isError()) {
                for (Image i : response.getImageList()) {
                    result.add(new ImageInfo(i));
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(List<ImageInfo> result) {
            setProgressBarIndeterminateVisibility(false);

            Cache.setAlbumImages(album, result);
            setImageList(result);
        }

    }

    private class AlbumAdapter implements ListAdapter {

        private List<ImageInfo> images = new ArrayList<ImageInfo>();
        private List<DataSetObserver> observers = new ArrayList<DataSetObserver>();

        public void addAll(Collection<ImageInfo> values) {
            images.addAll(values);
            notifyObservers();
        }

        public void clear() {
            images.clear();
            notifyObservers();
        }

        private void notifyObservers() {
            for (DataSetObserver o : observers) {
                o.onChanged();
            }
        }

        @Override
        public int getCount() {
            return images.size();
        }

        @Override
        public Object getItem(int position) {
            return images.get(position);
        }

        @Override
        public long getItemId(int position) {
            return images.get(position).getId();
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageHolder holder;
            if (convertView == null) {
                convertView = View.inflate(AlbumActivity.this,
                        R.layout.album_row, null);
                holder = new ImageHolder();
                holder.fileName = (TextView) convertView
                        .findViewById(R.id.filename);
                holder.description = (TextView) convertView
                        .findViewById(R.id.description);
                holder.thumbnail = (ImageView) convertView
                        .findViewById(R.id.thumbnail);
                convertView.setTag(holder);
            } else {
                holder = (ImageHolder) convertView.getTag();
            }
            ImageInfo item = images.get(position);
            holder.fileName.setText(item.getFileName());
            holder.description.setText(item.getDescription());
            if (item.getThumbnail() == null) {
                new GetThumbnailTask().execute(this, item);
                holder.thumbnail.setImageDrawable(getResources().getDrawable(
                        android.R.drawable.ic_menu_rotate));
            } else {
                holder.thumbnail.setImageDrawable(item.getThumbnail());
            }
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
            return images.size() == 0;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {
            observers.add(observer);
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            observers.remove(observer);
        }

        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }

        @Override
        public boolean isEnabled(int position) {
            return true;
        }

        public void update(ImageInfo item) {
            if (images.contains(item)) {
                notifyObservers();
            }
        }

    }

    private class ImageHolder {

        public TextView fileName;
        public TextView description;
        public ImageView thumbnail;
    }

    private class GetThumbnailTask extends AsyncTask<Object, Void, Object[]> {

        @Override
        protected Object[] doInBackground(Object... params) {
            AlbumAdapter adapter = (AlbumAdapter) params[0];
            ImageInfo image = (ImageInfo) params[1];
            String thumbUrl = image.getThumbUrl();
            Drawable result = null;
            if (thumbUrl != null) {
                HttpGet get = new HttpGet(thumbUrl);
                HttpClient client = new DefaultHttpClient();
                try {
                    HttpResponse response = client.execute(get);
                    result = new BitmapDrawable(response.getEntity()
                            .getContent());
                } catch (IOException e) {
                    Log.e("GetThumbnailTask", "Error getting thumbnail: "
                            + e.getMessage());
                }
            }
            return new Object[] { adapter, image, result };
        }

        @Override
        protected void onPostExecute(Object[] result) {
            AlbumAdapter adapter = (AlbumAdapter) result[0];
            ImageInfo image = (ImageInfo) result[1];
            Drawable thumbnail = (Drawable) result[2];
            if (thumbnail != null) {
                image.setThumbnail(thumbnail);
                adapter.update(image);
            }
        }
    }

}
