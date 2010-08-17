package net.sourcewalker.smugview;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;

public class ImageViewActivity extends Activity {

    private ImageView viewer;
    private ImageInfo image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.image);

        image = (ImageInfo) getIntent().getExtras().get(Extras.EXTRA_IMAGE);

        setTitle();

        viewer = (ImageView) findViewById(R.id.imageview);
        if (image.getThumbnail() != null) {
            viewer.setImageDrawable(image.getThumbnail());
        }

        if (image.getViewUrl() != null) {
            startGetImage();
        }
    }

    private void setTitle() {
        String description = image.getDescription();
        if (description != null && description.length() > 0) {
            setTitle(description);
        } else {
            setTitle(image.getFileName());
        }
    }

    private void startGetImage() {
        new GetImageTask().execute(image);
    }

    private class GetImageTask extends AsyncTask<ImageInfo, Void, Drawable> {

        @Override
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected Drawable doInBackground(ImageInfo... params) {
            ImageInfo image = params[0];
            Drawable result = null;
            String viewUrl = image.getViewUrl();
            HttpGet get = new HttpGet(viewUrl);
            HttpClient client = new DefaultHttpClient();
            try {
                HttpResponse response = client.execute(get);
                result = new BitmapDrawable(response.getEntity().getContent());
            } catch (IOException e) {
                Log.e("GetImageTask", "Error while getting image: "
                        + e.getMessage());
            }
            return result;
        }

        @Override
        protected void onPostExecute(Drawable result) {
            if (result != null) {
                viewer.setImageDrawable(result);
            }
            setProgressBarIndeterminateVisibility(false);
        }

    }

}
