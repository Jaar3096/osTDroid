package id.web.devnull.ostdroid;

import android.os.Bundle;
import android.util.Log;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import com.snappydb.DB;

import id.web.devnull.ostdroid.scp.*;

public class TView extends AppCompatActivity
{
        private String tid = null;
        private final String TAG = "osTDroid";
        private DB db = dbsetup.db;
        private Ticket ticket;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);

                setContentView(R.layout.tview);
                Toolbar toolbar = (Toolbar)findViewById(R.id.view_toolbar);
                setSupportActionBar(toolbar);

                Bundle b = getIntent().getExtras();
                if (b != null)
                        this.tid = b.getString("tid");


                TextView text = (TextView) findViewById(R.id.test);
                if (this.tid != null) {
                        try {
                                ticket = db.getObject(tid, Ticket.class);
                        } catch(Exception e) {
                        }
                        text.setText(ticket.title);
                }
        }
}
