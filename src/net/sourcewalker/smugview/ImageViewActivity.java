package net.sourcewalker.smugview;

import android.app.Activity;
import android.os.Bundle;
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

        viewer = (ImageView) findViewById(R.id.imageview);
        if (image.getThumbnail() != null) {
            viewer.setImageDrawable(image.getThumbnail());
        }
    }

}
