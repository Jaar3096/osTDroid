package id.web.devnull.ostdroid;

import android.os.Bundle;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.ActionBar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import com.snappydb.DB;

import java.util.List;
import java.util.LinkedList;

import id.web.devnull.ostdroid.scp.*;

public class TView extends AppCompatActivity
{
        private String tid = null;
        private final String TAG = "osTDroid";
        private DB db = dbsetup.db;
        private Ticket ticket;
        private BottomNavigationView bnav;
        private ProgressBar pbar;

        private ThreadFragment fr_external;
        private ThreadFragment fr_internal;

        private List<Tthread> internal = new LinkedList<Tthread>();
        private List<Tthread> external = new LinkedList<Tthread>();

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);

                fr_internal = new ThreadFragment();
                fr_internal.adapter = new ThreadAdapter(this);

                fr_external = new ThreadFragment();
                fr_external.adapter = new ThreadAdapter(this);

                setContentView(R.layout.tview);
                ActionBar ab;
                Toolbar toolbar = (Toolbar)findViewById(R.id.view_toolbar);
                setSupportActionBar(toolbar);
                pbar   = (ProgressBar) findViewById(R.id.tview_pbar);
                pbar.setVisibility(ProgressBar.GONE);

                ab = getSupportActionBar();
                ab.setDisplayHomeAsUpEnabled(true);
                
                Bundle b = getIntent().getExtras();
                if (b != null)
                        this.tid = b.getString("tid");

                if (this.tid != null) {
                        this.setTitle(this.tid);
                        try {
                                ticket = db.getObject(tid, Ticket.class);
                        } catch(Exception e) {
                        }

                        split_thread(ticket.load_thread());
                        fr_external.data        = this.external;
                        fr_internal.data        = this.internal;
                }

                if (savedInstanceState != null)
                        bnav(savedInstanceState.getInt("bnav"));
                else    bnav(0);
                sync();
        }

        @Override
        protected void onDestroy() {
                super.onDestroy();
                save_thread();
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

        @Override
        public void onSaveInstanceState(Bundle b) {
                if (bnav != null) {
                        b.putInt("bnav", bnav.getSelectedItemId());
                }

                super.onSaveInstanceState(b);
        }

        private void bnav(int tab) {
                final Vdetail vdetail = new Vdetail();
                vdetail.ticket = ticket;
                bnav = (BottomNavigationView) findViewById(R.id.bnav);

                bnav.setOnNavigationItemSelectedListener
                (
                        new BottomNavigationView
                        .OnNavigationItemSelectedListener() {
                                @Override
                                public boolean onNavigationItemSelected(MenuItem menu) {
                                        int menu_id = menu.getItemId();

                                        FragmentTransaction ft = 
                                                getSupportFragmentManager()
                                                .beginTransaction();

                                        if (menu_id == R.id.tdetail) {
                                                ft.replace(R.id.fragment_frame, vdetail);
                                                ft.commit();
                                                return true;
                                        }
                                        if (menu_id == R.id.tthread) {
                                                ft.replace(R.id.fragment_frame,
                                                                fr_external);
                                                ft.commit();
                                                return true;
                                        }
                                        if (menu_id == R.id.tinternal) {
                                                ft.replace(R.id.fragment_frame,
                                                                fr_internal);
                                                ft.commit();
                                                return true;
                                        }

                                        return false;
                                }
                        }
                );

                if (tab != 0)
                        bnav.setSelectedItemId(tab);
                else    bnav.setSelectedItemId(R.id.tthread);
        }

        private void split_thread(List<Tthread> data) {
                if (data == null)
                        return;

                List<Tthread> internal = new LinkedList<Tthread>();
                List<Tthread> external = new LinkedList<Tthread>();

                for (Tthread item : data) {
                        if ((item.type & Tthread.INTERNAL) > 0)
                                internal.add(item);
                        else    external.add(item);
                }

                if (internal.size() > this.internal.size()) {
                        int i;
                        int orig = this.internal.size();
                        int mod = internal.size();
                        for (i = orig; i < mod; i++) {
                               this.internal.add(internal.get(i));
                               fr_internal.adapter.notifyItemInserted(i);
                        }
                }
                if (external.size() > this.external.size()) {
                       int i;
                       int orig = this.external.size();
                       int mod = external.size();
                       for (i = orig; i < mod; i++) {
                               this.external.add(external.get(i));
                               fr_external.adapter.notifyItemInserted(i);
                       }
                }
        }

        private void save_thread() {
                List<Tthread> data = new LinkedList<Tthread>();
                for (Tthread thread : this.internal)
                        data.add(thread);
                for (Tthread thread : this.external)
                        data.add(thread);

                ticket.save_thread(data);
        }

        private void sync() {
                pbar.setVisibility(ProgressBar.VISIBLE);

                new Thread(new Runnable() {
                @Override
                public void run() {
                        final List<Tthread> thread_list = ticket.sync_thread();
                                
                        runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                        if (thread_list != null)
                                                split_thread(thread_list);
                                        pbar.setVisibility(ProgressBar.GONE);
                                }
                        });
                }
                }).start();
        }
}
