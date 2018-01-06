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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.github.chrisbanes.photoview.PhotoView;
import com.github.chrisbanes.photoview.OnPhotoTapListener;

public class ImgView extends AppCompatActivity
{
        private String path;
        private static final String TAG = "osTDroid";
        private ActionBar ab;
        private static final int MAX_SZ = 500;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.imgview);
                final Toolbar toolbar = (Toolbar)findViewById(R.id.imgview_toolbar);
                setSupportActionBar(toolbar);
                ab = getSupportActionBar();
                ab.setDisplayHomeAsUpEnabled(true);

                final PhotoView pv = (PhotoView) findViewById(R.id.imgview);
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

                path = b.getString("imgpath");
                if (path == null)
                        return;

                String[] s = path.split("/");
                String filename = s[s.length - 1];
                this.setTitle(filename);

                try {
                        BitmapFactory.Options opt = new BitmapFactory.Options();
                        opt.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(path, opt);

                        int sz = opt.outWidth >= opt.outHeight ? opt.outWidth : opt.outHeight;
                        int res = 1;
                        if (sz >= MAX_SZ) {
                                sz = sz >> 1;
                                while (sz > MAX_SZ) {
                                        sz = sz >> 1;
                                        res = res << 1;
                                }

                                res = res << 1;
                        }

                        opt.inSampleSize = res;
                        opt.inJustDecodeBounds = false;

                        pv.setImageBitmap(BitmapFactory.decodeFile(path, opt));
                } catch(Exception e) {
                        if (scp.DEBUG)
                                Log.e(TAG, "Error loading image");
                }

                new Thread(new Runnable() {
                        @Override
                        public void run() {
                                final Bitmap bitmap = BitmapFactory.decodeFile(path);
                                runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                        if (bitmap != null)
                                                pv.setImageBitmap(bitmap);
                                }
                                });
                        }
                }).start();
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
