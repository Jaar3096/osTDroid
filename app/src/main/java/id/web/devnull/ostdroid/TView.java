package id.web.devnull.ostdroid;

import android.os.Bundle;
import android.os.AsyncTask;
import android.util.Log;
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
import com.snappydb.DB;

import id.web.devnull.ostdroid.scp.*;

public class TView extends AppCompatActivity
{
        private String tid = null;
        private final String TAG = "osTDroid";
        private DB db = dbsetup.db;
        private Ticket ticket;
        private BottomNavigationView bnav;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);

                setContentView(R.layout.tview);
                Toolbar toolbar = (Toolbar)findViewById(R.id.view_toolbar);
                setSupportActionBar(toolbar);
                
                Bundle b = getIntent().getExtras();
                if (b != null)
                        this.tid = b.getString("tid");

                if (this.tid != null) {
                        try {
                                ticket = db.getObject(tid, Ticket.class);
                        } catch(Exception e) {
                        }
                }

                bnav();
        }

        private void bnav() {
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
                                                ThreadFragment fr_external = 
                                                        new ThreadFragment();
                                                fr_external.ticket = ticket;
                                                fr_external.thread_type =
                                                        ThreadFragment.EXTERNAL;

                                                ft.replace(R.id.fragment_frame,
                                                                fr_external);
                                                ft.commit();
                                                return true;
                                        }
                                        if (menu_id == R.id.tinternal) {
                                                ThreadFragment fr_internal = 
                                                        new ThreadFragment();
                                                fr_internal.ticket = ticket;
                                                fr_internal.thread_type =
                                                        ThreadFragment.INTERNAL;

                                                ft.replace(R.id.fragment_frame,
                                                                fr_internal);
                                                ft.commit();
                                                return true;
                                        }

                                        return false;
                                }
                        }
                );

                bnav.setSelectedItemId(R.id.tdetail);
        }
}
