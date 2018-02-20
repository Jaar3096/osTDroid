package id.web.devnull.ostdroid;

import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.os.Bundle;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import id.web.devnull.ostdroid.scp.*;

public class TFragment extends Fragment
{
        private TAdapter adapter;
        private RecyclerView rcview;
        private LinearLayoutManager lmgr;
        public static final String TAG  = "osTDroid";

        public int ticket_state         = Ticket.OPEN;
        private static int bg_task      = 0;
        public static boolean activity_exit = false;

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
                super.onCreate(savedInstanceState);
                adapter = new TAdapter(getActivity(), scp.list(ticket_state));
                adapter.notifyDataSetChanged();
                this.setRetainInstance(true);
                bg task =  new bg();
                task.execute(ticket_state);
                setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState)
        {
                ViewGroup v = (ViewGroup) inflater.inflate(R.layout.frlist, viewGroup, false);
                
                rcview = (RecyclerView) v.findViewById(R.id.ticket_list);
                lmgr = new LinearLayoutManager(getActivity());
                rcview.setLayoutManager(lmgr);
                rcview.addItemDecoration(new DividerItemDecoration(rcview.getContext(),
                                        lmgr.getOrientation()));

                rcview.setAdapter(adapter);

                return v;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
                inflater.inflate(R.menu.menu_main, menu);

                SearchView search = (SearchView) menu.findItem(R.id.action_search).getActionView();
                search.setOnQueryTextListener(
                        new SearchView.OnQueryTextListener() {
                                @Override
                                public boolean onQueryTextSubmit(String q) {
                                        adapter.getFilter().filter(q);
                                        return false;
                                }

                                @Override
                                public boolean onQueryTextChange(String q) {
                                        adapter.getFilter().filter(q);
                                        return false;
                                }
                        }
                );

                search.setOnCloseListener(
                        new SearchView.OnCloseListener() {
                                @Override
                                public boolean onClose() {
                                        adapter.getFilter().filter(null);
                                        return true;
                                }
                        }
                );
        }

        @Override
        public void onDestroy() {
                adapter.write_index(ticket_state);
                super.onDestroy();
        }

        /*
         * close db only if there is no thread running
         *
         */
        public static void fr_exit() {
                if (bg_task > 0)
                        return;
                try {
                        dbsetup.close();
                        if (scp.DEBUG)
                                Log.i(TAG, "Closing database");
                } catch(Exception e) {
                        Log.e("osTDroid", "Error closing database", e);
                }
        }

        private class bg extends AsyncTask<Integer, Void, List<Ticket>>
        {
                private int tstate = -1;

                public bg() {
                       bg_task = bg_task << 1;
                       bg_task |= 0x01;
                }

                @Override
                protected List<Ticket> doInBackground(Integer... ticket_state) {
                        try {
                                tstate = ticket_state[0];
                                return scp.sync(tstate);
                        } catch(Exception e) {
                                Log.e(TAG, "thread error", e);
                                return null;
                        }
                }

                @Override
                protected void onPostExecute(List<Ticket> t) {
                        bg_task = bg_task >> 1;
                        if (activity_exit) {
                                fr_exit();
                                return;
                        }

                        if (t == null) {
                                Log.e(TAG, "Error loading ticket list");
                                Snackbar.make(getActivity()
                                                .findViewById(R.id.clayout),
                                                "Unable to sync with the server",
                                                Snackbar.LENGTH_SHORT).show();
                                return;
                        }

                        adapter.data = t;
                        adapter.notifyDataSetChanged();
                }
        }
}
