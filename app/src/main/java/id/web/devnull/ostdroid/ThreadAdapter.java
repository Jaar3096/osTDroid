package id.web.devnull.ostdroid;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.util.Log;
import android.content.Intent;
import android.content.Context;

import java.util.List;
import id.web.devnull.ostdroid.scp.*;

public class ThreadAdapter extends RecyclerView.Adapter<ThreadAdapter.ThreadVHolder>
{
        private static final String TAG = "osTDroid";
        public List<Tthread> data = null;
        private Context ctx;
        private LinearLayout ll = null;

        public ThreadAdapter(Context ctx)
        {
                this.ctx = ctx;
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
                show_thread(pos);
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
                        for (String content : contents) {
                                TextView t = new TextView(ctx); 
                                t.setText(content);
                                t.setLayoutParams(new LayoutParams(
                                        LayoutParams.MATCH_PARENT,
                                        LayoutParams.WRAP_CONTENT
                                ));

                                ll.addView(t);
                        }
                }
        }
/*
        private class download_image extends AsyncTask<String, Void, String>
        {
                public int data_pos     = -1;
                public int content_pos  = -1;
                public ImageView iv = null;
                private Http http = new Http();

                @Override
                protected String doInBackground(String... urls) {
                        String url = urls[0];
                        
                }
        }
*/

}
