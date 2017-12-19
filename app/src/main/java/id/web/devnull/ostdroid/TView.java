package id.web.devnull.ostdroid;

import android.os.Bundle;
import android.os.AsyncTask;
import android.util.Log;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import com.snappydb.DB;
import android.text.Html;

import id.web.devnull.ostdroid.scp.*;

public class TView extends AppCompatActivity
{
        private String tid = null;
        private final String TAG = "osTDroid";
        private DB db = dbsetup.db;
        private Ticket ticket;
        private TextView text;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);

                setContentView(R.layout.tview);
                Toolbar toolbar = (Toolbar)findViewById(R.id.view_toolbar);
                setSupportActionBar(toolbar);
                
                text = (TextView) findViewById(R.id.test);
                BottomNavigationView bnav = (BottomNavigationView)
                                                findViewById(R.id.bnav);

                Bundle b = getIntent().getExtras();
                if (b != null)
                        this.tid = b.getString("tid");


                if (this.tid != null) {
                        try {
                                ticket = db.getObject(tid, Ticket.class);
                                bg view_ticket = new bg();
                                view_ticket.execute(ticket);
                        } catch(Exception e) {
                        }
                }
        }

        private class bg extends AsyncTask<Ticket, Void, String>
        {
                @Override
                protected String doInBackground(Ticket... tickets) {
                        try {
                                ticket = tickets[0];
                                return ticket.view();
                        } catch(Exception e) {
                                Log.e(TAG, "view thread error", e);
                                return null;
                        }
                }

                @Override
                protected void onPostExecute(String s) {
                        if (s == null) {
                                Log.e(TAG, "Error loading ticket thread");
                                return;
                        }

                        text.setText(Html.fromHtml(s), null);
                }
        }
}
