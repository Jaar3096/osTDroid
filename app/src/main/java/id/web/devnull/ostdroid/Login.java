package id.web.devnull.ostdroid;

import com.snappydb.DB;

import android.os.Bundle;
import android.os.AsyncTask;
import android.util.Log;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ProgressBar;
import android.content.Intent;
import android.content.Context;

import id.web.devnull.ostdroid.scp.*;

public class Login extends AppCompatActivity
{
        private EditText et_link;
        private EditText et_user;
        private EditText et_pass;
        private Button btn_login;
        private TextView tv_err;

        private String link;
        private String user;
        private String pass;
        private Http http;

        private DB db = dbsetup.db;
        private Context ctx;
        private ProgressBar pbar;

        private static final String TAG = "osTDroid";

        @Override
        public void onCreate(Bundle bundle) {
                super.onCreate(bundle);
                setContentView(R.layout.login);

                et_link = (EditText) findViewById(R.id.login_link);
                et_user = (EditText) findViewById(R.id.login_user);
                et_pass = (EditText) findViewById(R.id.login_pass);
                btn_login = (Button) findViewById(R.id.login_btn);
                tv_err = (TextView) findViewById(R.id.login_err);
                pbar = findViewById(R.id.login_pbar);
                pbar.setVisibility(ProgressBar.GONE);

                http = new Http();
                ctx = this;

                btn_login.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                user = et_user.getText().toString();
                                pass = et_pass.getText().toString();
                                link = et_link.getText().toString();

                                if (!isvalid())
                                        return;
                                        
                                Setup setup = new Setup();
                                setup.execute();
                        }
                });
        }

        private boolean isvalid() {
                boolean res = true;

                if (!link.matches("^http.*")) {
                        et_link.setError("Please specify protocol http or https");
                        res = false;
                }
                if (et_user.getText().toString().length() == 0) {
                        et_user.setError("Please input username");
                        res = false;
                }
                if (et_pass.getText().toString().length() == 0) {
                        et_pass.setError("Please input password");
                        res = false;
                }
                
                return res;
        }

        private class Setup extends AsyncTask<Void, Void, Boolean>
        {
                private String err = null;
                private String url = null;

                @Override
                protected void onPreExecute() {
                        pbar.setIndeterminate(true);
                        pbar.setVisibility(ProgressBar.VISIBLE);
                        btn_login.setEnabled(false);
                        tv_err.setText("");
                        et_link.setEnabled(false);
                        et_user.setEnabled(false);
                        et_pass.setEnabled(false);
                }

                @Override
                protected void onPostExecute(Boolean bool) {
                        pbar.setVisibility(ProgressBar.GONE);
                        btn_login.setEnabled(true);
                        et_link.setEnabled(true);
                        et_user.setEnabled(true);
                        et_pass.setEnabled(true);

                        if (!bool) {
                                tv_err.setText(err);
                                return;
                        }

                        String dir = ctx.getExternalFilesDir(null).getAbsolutePath();

                        try {
                                if (!db.isOpen()) {
                                        if (!dbsetup.open(ctx))
                                                return;
                                        db = dbsetup.db;
                                }
                                db.put("config_url", link);
                                db.put("config_user", user);
                                db.put("config_pass", pass);
                                db.put("config_dir", dir);
                        } catch(Exception e){
                                if (scp.DEBUG)
                                        Log.e(TAG, "error saving config to db", e);
                                return;
                        }

                        try {
                                dbsetup.close();
                                Intent intent = new Intent(ctx, MainActivity.class);
                                startActivity(intent);
                                finish();
                        } catch(Exception e) {
                                if (scp.DEBUG)
                                        Log.e(TAG, "Error closing database");
                        }
                }

                @Override
                protected Boolean doInBackground(Void... foo) {
                        String err = null;
                        int http_res = http.ck_url(link);
                        if (http_res > 0) {
                                switch(http_res) {
                                        case Http.E_HTTP_MALFORM:
                                                this.err = "Malformed url";
                                                break;
                                        case Http.E_HTTP_TLS:
                                                this.err = "TLS Handshake failed";
                                                break;
                                        case Http.E_HTTP_REFUSED:
                                                this.err = "Connection refused";
                                                break;
                                        case Http.E_HTTP_UNAVAIL:
                                                this.err = "Resource unavailable, check connection";
                                                break;
                                        default:
                                                this.err = "Connection failed, unknown error";
                                }

                                return false;
                        }

                        scp.config.put("url", link);
                        int login_res = scp.login(user, pass);
                        if (login_res > 0) {
                                switch(login_res) {
                                        case scp.E_LOGIN_UNAVAIL:
                                                this.err = "Error fetching login page";
                                                break;
                                        case scp.E_LOGIN_NOSCP_URL:
                                                this.err = "Url is not valid, staff url by default ends with /scp";
                                                break;
                                        case scp.E_LOGIN_USERPASS:
                                                this.err = "username or password is invalid";
                                                break;
                                        case scp.E_LOGIN_HTTP_POST:
                                                this.err = "Error sending http POST";
                                                break;
                                        case scp.E_LOGIN_UNKNOWN:
                                                this.err = "Login failed, unknown error";
                                                break;
                                }

                                return false;
                        }

                        return true;
                }
        }
}
