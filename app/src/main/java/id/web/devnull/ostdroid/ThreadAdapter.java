package id.web.devnull.ostdroid;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.net.Uri;
import android.content.Intent;
import android.content.Context;
import android.app.Activity;
import android.support.v4.content.FileProvider;
import android.os.AsyncTask;
import java.io.*;
import java.net.URLDecoder;

import java.util.List;
import id.web.devnull.ostdroid.scp.*;

public class ThreadAdapter extends RecyclerView.Adapter<ThreadAdapter.ThreadVHolder>
{
        private static final String TAG = "osTDroid";
        public List<Tthread> data = null;
        private Context ctx;
        private LinearLayout ll = null;
        private static String DIR = null;
        private static final int MAXTRACK = 50;
        private static char[] thread_track;     /* char size in Java == 2 bytes */
        private String file_provider;
        private static final int IMG_WIDTH =  700;
        private static final int IMG_HEIGHT=  700;

        public ThreadAdapter(Context ctx)
        {
                this.ctx = ctx;
                DIR = setenv();
                thread_track = new char[MAXTRACK];
                clr_track();
                this.file_provider = BuildConfig.APPLICATION_ID + ".provider";
        }

        private boolean thread_ckbit(int pos) {
                if ((thread_track[pos >> 4] & 0x01 << (pos & 0x0f)) > 0)
                        return true;
                else    return false;
        }

        private void thread_setbit(int pos) {
                thread_track[pos >> 4] &= ~(0x01 << (pos & 0x0f));
                thread_track[pos >> 4] |= 0x01 << (pos & 0x0f);
        }

        public void clr_track() {
                int i;
                for (i = 0; i < MAXTRACK; i++)
                        thread_track[i] &= 0x00;
        }

        public class ThreadVHolder extends RecyclerView.ViewHolder
        {
                public TextView date;
                public TextView poster;

                public ThreadVHolder(View v) {
                        super(v);

                        this.date = (TextView) v.findViewById(R.id.thread_date);
                        this.poster = (TextView) v.findViewById(R.id.thread_poster);
                }
        }

        @Override
        public ThreadVHolder onCreateViewHolder(ViewGroup vg, int vtype)
        {
                View v = LayoutInflater.from(vg.getContext())
                         .inflate(R.layout.thread_item, vg, false);
                ll = (LinearLayout) v.findViewById(R.id.thread_content);

                final ThreadVHolder vh = new ThreadVHolder(v);
                return vh;
        }

        @Override
        public void onBindViewHolder(ThreadVHolder vh, int pos)
        {
                vh.date.setText(data.get(pos).date);
                vh.poster.setText(data.get(pos).poster);
                if (!thread_ckbit(pos)) {
                        show_thread(pos);
                        thread_setbit(pos % (MAXTRACK * 16));
                }
        }

        @Override
        public int getItemCount()
        {
                if (data == null)
                        return 0;
                else    return data.size();
        }

        private void show_thread(int pos) {
                List<String> contents = data.get(pos).content;
                if (contents != null) {
                        int i = 0;
                        for (String content : contents) {
                                String[] arr_content = content.split("\\\\0");
                                if (arr_content[0].equals("img")) {
                                        final String url = arr_content[1];
                                        ImageView iv = new ImageView(ctx);
                                        iv.setLayoutParams(new LayoutParams(IMG_WIDTH, IMG_WIDTH));
                                        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                        iv.setPadding(20, 20, 20, 20);
                                        iv.setBackgroundResource(R.drawable.img);
                                        ll.addView(iv);
                                        
                                        Uri uri =  ck_file(url, false);
                                        if (uri != null) {
                                                load_img(uri.getPath(), iv);
                                                img_click(iv, uri);

                                                i++;
                                                continue;
                                        }

                                        download dl = new download(pos, i, "img");
                                        dl.iv = iv;
                                        dl.execute(url);
                                        
                                        i++;
                                        continue;
                                }

                                if (arr_content[0].equals("link")) {
                                        final String url = arr_content[1];
                                        String txt = arr_content[2];

                                        TextView lnk = new TextView(ctx);
                                        lnk.setText(txt);
                                        lnk.setTextColor(Color.BLUE);
                                        lnk.setLayoutParams(new LayoutParams(
                                                LayoutParams.MATCH_PARENT,
                                                LayoutParams.WRAP_CONTENT
                                        ));

                                        lnk.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                        Intent i = new Intent(Intent.ACTION_VIEW);
                                                        i.setData(Uri.parse(url));
                                                        ctx.startActivity(i);
                                                }
                                        });
                                        ll.addView(lnk);
                                        i++;
                                        continue;
                                }

                                if (arr_content[0].equals("attach")) {
                                        final String url = arr_content[1];
                                        String txt;
                                        if (arr_content.length == 3)
                                                txt =  arr_content[2];
                                        else    txt =  arr_content[1];

                                        TextView lnk = new TextView(ctx);
                                        lnk.setText("attachment : " + txt);
                                        lnk.setTextColor(Color.BLUE);
                                        lnk.setLayoutParams(new LayoutParams(
                                                LayoutParams.MATCH_PARENT,
                                                LayoutParams.WRAP_CONTENT
                                        ));

                                        ll.addView(lnk);

                                        Uri uri = ck_file(url, true);
                                        Log.i(TAG, url);
                                        if (uri != null) {
                                                tv_click(lnk, uri);
                                                i++;
                                                continue;
                                        } else {
                                                if (scp.DEBUG)
                                                        Log.e(TAG, "uri == null");
                                        }

                                        download dl = new download(pos, i, "attach");
                                        dl.tv = lnk;
                                        dl.execute(url);

                                        i++;
                                        continue;
                                }

                                TextView t = new TextView(ctx); 
                                t.setText(content);
                                t.setLayoutParams(new LayoutParams(
                                        LayoutParams.MATCH_PARENT,
                                        LayoutParams.WRAP_CONTENT
                                ));

                                ll.addView(t);
                                i++;
                        }
                }
        }

        private Uri ck_file(final String url, boolean attachment) {
                try {
                        String file = DIR + "/" + url;
                        File f = new File(file);
                        if (f.exists()) {
                                if (attachment == false)
                                        return Uri.parse("file://" + file);
                                return FileProvider.getUriForFile(ctx,
                                                file_provider,
                                                new File(file));
                        } else    return null;
                } catch(Exception ex) {
                        if (scp.DEBUG)
                        Log.e(TAG, "check file failed", ex);
                        return null;
                }

        }

        private void img_click(ImageView iv, final Uri uri) {
                iv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                Intent i = new Intent(ctx, ImgView.class);
                                i.putExtra("imguri", uri.toString());
                                ctx.startActivity(i);
                        }
                });
        }

        private void tv_click(TextView tv, final Uri uri) {
                tv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(uri);
                                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                ctx.startActivity(i);
                        }
                });
        }

        private void load_img(final String path, final ImageView iv) {
                Log.i(TAG, "load_img called");
                new Thread(new Runnable() {
                        @Override
                        public void run() {
                                try {
                                        final int MAX_SZ = ((IMG_WIDTH + IMG_HEIGHT) >> 1) - 100;

                                        BitmapFactory.Options opt = new BitmapFactory.Options();
                                        opt.inJustDecodeBounds = true;
                                        BitmapFactory.decodeFile(path, opt);

                                        int sz = opt.outWidth >= opt.outHeight ? opt.outWidth : opt.outHeight;
                                        Log.i(TAG, "orig size : " + sz);
                                        int res = 1;
                                        if (sz >= MAX_SZ) {
                                                sz = sz >> 1;
                                                while (sz > MAX_SZ) {
                                                        sz = sz >> 1;
                                                        res = res << 1;
                                                }
                                        }

                                        opt.inSampleSize = res;
                                        opt.inJustDecodeBounds = false;

                                        final Bitmap bitmap = BitmapFactory.decodeFile(path, opt);
                                        Log.i(TAG, "new height : " + res);
                                        ((Activity) ctx).runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                        iv.setImageBitmap(bitmap);
                                                }
                                        });
                                } catch (Exception e) {
                                        if (scp.DEBUG)
                                                Log.e(TAG, "Error loading image", e);
                                }
                        }
                }).start();
        }

        private class download extends AsyncTask<String, Void, String>
        {
                private int data_pos     = -1;
                private int content_pos  = -1;
                public ImageView iv = null;
                public TextView tv = null;
                private Http http = new Http();
                private String prefix = null;

                public download(int data_pos, int content_pos, String prefix) {
                        this.data_pos = data_pos;
                        this.content_pos = content_pos;
                        this.prefix = prefix + "\\0";
                }

                @Override
                protected void onPostExecute(String fname) {
                        if (fname == null || data_pos == -1
                            || content_pos == -1)
                                return;

                        data.get(data_pos).content.set(content_pos, prefix + fname);
                        Log.i(TAG, prefix + fname);
                        if (DIR == null)
                                return;

                        String uri;
                        uri = "file://" + DIR + "/" + fname;

                        if (iv != null)
                                load_img(DIR + "/" + fname, iv);
                                img_click(iv, Uri.parse(uri));
                        if (tv != null) {
                                Uri u = FileProvider.getUriForFile(ctx,
                                                file_provider,
                                                new File(DIR + "/" + fname));
                                tv_click(tv, u);
                        }
                }

                @Override
                protected String doInBackground(String... urls) {
                        String url = urls[0];
                        if (scp.DEBUG)
                                Log.i(TAG, "Downloading file from " + url);
                        String fname = null;
                        File tempfile = null;
                        
                        if (DIR == null)
                                return null;
                        tempfile = http.get_byte(url);
                        if (tempfile== null) {
                                if (scp.DEBUG)
                                        Log.e(TAG, "unable to download data from "
                                                   + url);
                                return null;
                        }

                        fname = get_fname();
                        if (fname == null) {
                                if (scp.DEBUG)
                                        Log.i(TAG, "filename empty");
                                return null;
                        }

                        try {
                                File out = new File(DIR + "/" + fname);
                                InputStream fin = new FileInputStream(tempfile);
                                OutputStream fout = new FileOutputStream(out);
                                byte[] buf = new byte[1024 * 8];
                                int bytes_read;

                                while((bytes_read = fin.read(buf)) != -1)
                                        fout.write(buf, 0, bytes_read);

                                fin.close();
                                fout.close();
                                tempfile.delete();

                                return fname;
                        } catch(Exception e) {
                                if (scp.DEBUG)
                                        Log.e(TAG, "Error moving downloaded data");
                                return null;
                        }
                }

                private String get_fname() {
                        if (http.header == null)
                                return null;

                        String cd = http.header.get("Content-Disposition").get(0);
                        String[] headers = cd.split("=");
                        int i;
                        String url = null;
                        for (i = 0; i < headers.length; i++) {
                                if (headers[i].matches(".*filename.*")) {
                                       url = headers[i+1];
                                       break;
                                }
                        }

                        try {
                                /* FIXME: Ugly hard code */
                                if (url !=null) {
                                        return URLDecoder.decode(url.substring(7), "UTF-8");
                                }
                        } catch(Exception e) {
                                if (scp.DEBUG)
                                        Log.e(TAG, "Decode filename error", e);
                        }

                        return null;
                }
        }

        private String setenv()
        {
                try {
                        String dir = scp.config.get("dir") + "/" + "attachments";
                        File fdir = new File(dir);
                        if (fdir.exists() && fdir.isDirectory())
                                return fdir.getAbsolutePath();

                        if(!fdir.exists()) {
                                fdir.mkdir();
                                return fdir.getAbsolutePath();
                        }

                        return null;
                } catch(Exception e) {
                        if (scp.DEBUG)
                                Log.e(TAG, "unable to setup attachments dir", e);
                        return null;
                }
        }
}
