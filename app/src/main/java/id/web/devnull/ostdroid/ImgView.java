package id.web.devnull.ostdroid;

import id.web.devnull.ostdroid.scp.*;
import android.os.Bundle;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.MotionEvent;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.net.Uri;
import android.content.Intent;
import android.util.Log;

import com.github.chrisbanes.photoview.PhotoView;
import com.github.chrisbanes.photoview.OnPhotoTapListener;

public class ImgView extends AppCompatActivity
{
        private String uri;
        private static final String TAG = "osTDroid";
        private ActionBar ab;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.imgview);
                final Toolbar toolbar = (Toolbar)findViewById(R.id.imgview_toolbar);
                setSupportActionBar(toolbar);
                ab = getSupportActionBar();
                ab.setDisplayHomeAsUpEnabled(true);

                PhotoView pv = (PhotoView) findViewById(R.id.imgview);
                pv.setZoomable(true);
                pv.setOnPhotoTapListener(new OnPhotoTapListener() {
                        @Override
                        public void onPhotoTap(ImageView iv, float x, float y) {
                                if (ab.isShowing())
                                        ab.hide();
                                else    ab.show();
                                immersive();
                        }
                });

                Bundle b = getIntent().getExtras();
                if (b == null)
                        return;

                uri = b.getString("imguri");
                if (uri == null)
                        return;

                String[] s = uri.split("/");
                String filename = s[s.length - 1];
                this.setTitle(filename);

                pv.setImageURI(Uri.parse(uri));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                        case android.R.id.home:
                                super.onBackPressed();
                                return true;
                }
                return super.onOptionsItemSelected(item);
        }

        private void immersive() {
                int opt = getWindow().getDecorView().getSystemUiVisibility();

                if (Build.VERSION.SDK_INT >= 14)
                        opt ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
                if (Build.VERSION.SDK_INT >= 16)
                        opt ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
                if (Build.VERSION.SDK_INT >= 18)
                        opt ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

                getWindow().getDecorView().setSystemUiVisibility(opt);
        }
}
