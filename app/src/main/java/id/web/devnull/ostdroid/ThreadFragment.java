package id.web.devnull.ostdroid;

import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.os.Bundle;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import id.web.devnull.ostdroid.scp.*;

public class ThreadFragment extends Fragment
{
        public Ticket ticket = null;
        private ThreadAdapter adapter;
        private RecyclerView rcview;
        private LinearLayoutManager lmgr;

        public static final int EXTERNAL = 0x01;
        public static final int INTERNAL = 0x02;
        public int thread_type;
        public static final String TAG = "osTDroid";

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
                super.onCreate(savedInstanceState);

                adapter = new ThreadAdapter(getActivity());
                adapter.data = null;

                if ((this.thread_type & EXTERNAL) > 0)
                        adapter.data = ticket.thread_external;
                if ((this.thread_type & INTERNAL) > 0)
                        adapter.data = ticket.thread_internal;
                adapter.notifyDataSetChanged();

                bg bg_thread =  new bg();
                bg_thread.execute();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState)
        {
                ViewGroup v = (ViewGroup)
                                inflater.inflate(R.layout.thread_layout,
                                                viewGroup, false);
                
                rcview = (RecyclerView) v.findViewById(R.id.thread_rcview);
                lmgr = new LinearLayoutManager(getActivity());
                rcview.setLayoutManager(lmgr);
                rcview.setAdapter(adapter);

                return v;
        }

        private class bg extends AsyncTask<Void, Void, Integer>
        {
                private int int_sz = 0;
                private int ext_sz = 0;

                @Override
                protected void onPreExecute() {
                       this.int_sz = ticket.thread_internal.size(); 
                       this.ext_sz = ticket.thread_external.size(); 
                }

                @Override
                protected Integer doInBackground(Void... v) {
                        try {
                                return ticket.view();
                        } catch(Exception e) {
                                Log.e(TAG, "view thread error", e);
                                return null;
                        }
                }

                @Override
                protected void onPostExecute(Integer i) {
                        if (i == 0) {
                                Log.e(TAG, "Error loading ticket thread");
                                return;
                        } else {
                                int count;
                                if ((thread_type & INTERNAL) > 0) {
                                        count = ticket.thread_internal.size() - int_sz;
                                        if (count > 0) {
                                                int j;
                                                for (j = 0; j < count; j++) {
                                                        adapter.notifyItemInserted(int_sz + j);
                                                }
                                        }
                                }
                                if ((thread_type & EXTERNAL) > 0) {
                                        count = ticket.thread_external.size() - ext_sz;
                                        if (count > 0) {
                                                int j;
                                                for (j = 0; j < count; j++) {
                                                        adapter.notifyItemInserted(ext_sz + j);
                                                }
                                        }
                                }
                        }
                }
        }
}
