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

import java.util.LinkedList;
import java.util.List;

import id.web.devnull.ostdroid.scp.*;

public class ThreadFragment extends Fragment
{
        public ThreadAdapter adapter;
        private RecyclerView rcview;
        private LinearLayoutManager lmgr;

        public static final String TAG = "osTDroid";

        public List<Tthread> data = null;

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
                super.onCreate(savedInstanceState);

                adapter.data = data;
                adapter.notifyDataSetChanged();
                this.setRetainInstance(true);
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

        @Override
        public void onDestroyView() {
                super.onDestroyView();
                adapter.clr_track();
        }
}
