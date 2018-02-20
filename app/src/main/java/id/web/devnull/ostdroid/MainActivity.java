package id.web.devnull.ostdroid;

import android.os.Bundle;
import android.util.Log;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.PagerAdapter;
import android.app.FragmentTransaction;
import android.content.Intent;

import id.web.devnull.ostdroid.scp.*;

import java.util.List;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
        private static final String TAG = "osTDroid";

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                if (!dbsetup.open(this)) {
                        Log.e("Activity", "Error opening database");
                }

                
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_main);

                if (!scp.load_config()) {
                        Intent i = new Intent(this, Login.class);
                        startActivity(i);
                        finish();
                }

                Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                setSupportActionBar(toolbar);

                final ViewPager vp = (ViewPager) findViewById(R.id.vpager);
                final PagerAdapter adapter = new VPagerAdapter(getSupportFragmentManager());

                vp.setAdapter(adapter);

                TabLayout tab = (TabLayout) findViewById(R.id.tabs);
                tab.setupWithViewPager(vp);

                FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                fab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        }
                });

                TFragment.activity_exit = false;
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
                return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
               int id = item.getItemId();

                if (id == R.id.action_settings) {
                        return true;
                }

                return super.onOptionsItemSelected(item);
        }


        @Override
        public void onDestroy() {
                super.onDestroy();
                TFragment.activity_exit = true;
                TFragment.fr_exit();

        }

        class VPagerAdapter extends FragmentPagerAdapter
        {
                String[] tabs = {
                        "OPEN",
                        "OVERDUE",
                        "CLOSED"
                };

                public VPagerAdapter(FragmentManager fm) {
                        super(fm);
                }
        
                @Override
                public Fragment getItem(int pos) {
                        TFragment f = new TFragment();
                        f.ticket_state = pos;
                        return f;
                }

                @Override
                public int getCount() {
                        return tabs.length;
                }

                @Override
                public CharSequence getPageTitle(int pos) {
                        return tabs[pos];
                }
    }
}
