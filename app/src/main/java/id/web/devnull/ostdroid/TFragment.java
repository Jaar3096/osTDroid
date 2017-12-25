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

public class TFragment extends Fragment
{
        private TAdapter adapter;
        private RecyclerView rcview;
        private LinearLayoutManager lmgr;
        public static final String TAG = "osTDroid";

        public int ticket_state                = Ticket.OPEN;

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
                super.onCreate(savedInstanceState);

                adapter = new TAdapter(getActivity());
                adapter.data = scp.list(ticket_state);
                adapter.notifyDataSetChanged();
                bg task =  new bg();
                task.execute(ticket_state);
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
        public void onDestroy() {
                adapter.write_index(ticket_state);
                super.onDestroy();
        }


        private class bg extends AsyncTask<Integer, Void, List<Ticket>>
        {
                private static final String url = "http://helpdesk.transkon.net.id/scp";
                private static final String user = "dhani";
                private static final String pass = "obi wan kenobi";
                private int tstate = -1;

                @Override
                protected List<Ticket> doInBackground(Integer... ticket_state) {
                        try {
                                tstate = ticket_state[0];
                                if (scp.DEBUG)
                                        Log.d(TAG, "loading configuration");

                                if (!scp.setup(url, user, pass)) {
                                        Log.e(TAG, "Error setup");
                                        return null;
                                }

                                return scp.sync(tstate);
                        } catch(Exception e) {
                                Log.e(TAG, "thread error", e);
                                return null;
                        }
                }

                @Override
                protected void onPostExecute(List<Ticket> t) {
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
