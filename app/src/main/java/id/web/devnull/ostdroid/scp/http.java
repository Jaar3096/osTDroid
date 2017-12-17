package id.web.devnull.ostdroid.scp;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
import javax.net.ssl.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import android.util.Log;

import com.snappydb.DB;
import com.snappydb.DBFactory;

public class http {

        private static final String USER_AGENT  = "osTDroid";
        private static List<String> cookies     = null;
        private static String cookie            = null;
        private static String cookie_exp        = null;
        private static String saved_cookie      = null;
        private static URLConnection conn;
        public static String errmsg             = "";
        private static final String ENCODING    = "UTF-8";

        public static final int HTTP_OK         = 200;
        public static final int HTTP_FOUND      = 302;
        public static final int HTTP_NOT_FOUND  = 404;
        private static final String TAG         = "osTDroid";

        http()
        throws Exception
        {
                cookie_man();
        }

        private static void cookie_man()
        {
                String DATABASE         = "cookie";
                String DATA_DIR         = "data";
                String DATEFORMAT       = "EEE, dd-MMM-yyyy hh:mm:ss z";
                String TIMEZONE         = "GMT";
                DB db                   = dbsetup.db;

                if (cookie == null) {
                        if (scp.DEBUG)
                                Log.d(TAG, "Cookie is empty.");

                        String key;

                        try {
                                int i;
                                for(i = 0; i < 2; i++) {
                                        if (i == 0)
                                                key = "cookie";
                                        else
                                                key = "cookie_expires";

                                        String val = db.get(key);
                                        if (i == 0) {
                                                cookie = val;
                                                saved_cookie = cookie;
                                        } else
                                                cookie_exp = val;
                                }

                                Date d = new Date();
                                SimpleDateFormat df = new SimpleDateFormat(DATEFORMAT);
                                df.setTimeZone(TimeZone.getTimeZone(TIMEZONE));
        
                                if (cookie_exp != null && 
                                d.compareTo(df.parse(cookie_exp)) >= 0) {
                                        cookie          = null;
                                        cookie_exp      = null;
                                }
                        } catch (Exception e) {
                                Log.e(TAG, "Error fetch data from db", e);
                                return;
                        }
                } else {
                        try {
                                if (db == null)
                                        return;
        
                                String key = null;
                                String data = null;
        
                                int i;
                                for(i = 0; i < 2; i++) {
                                        if (i == 0) {
                                                key = "cookie";
                                                data    = cookie;
                                        } else {
                                                key = "cookie_expires";
                                                data    = cookie_exp;
                                        }
        
                                        db.put(key, data);
                                }

                        } catch (Exception e) {
                                Log.e(TAG, "Error writing cookie to db");
                                return;
                        }
                }
        }

        public static String ck_url (String url) {
                int unspec = 0;
                URL link = null;
                HttpsURLConnection secure;
                HttpURLConnection unsecure;

                if (!url.matches("http.*")) {
                        unspec = 1;
                        url = "https://" + url;
                }

                try {
                        link = new URL(url);
                        if (scp.DEBUG)
                                Log.d(TAG, "Trying to connect to " + link.toString());
                        conn = link.openConnection();
                        conn.connect();
                } catch(MalformedURLException e) {
                        Log.e(TAG, "url is not valid");
                        return null;
                }
                catch(SSLHandshakeException ssl_err) {
                        if (unspec == 0) {
                                Log.e(TAG, "SSL/TLS Hnadshake failed, make sure tls certificate is valid and trusted");
                                return null;
                        } else {
                                url = url.replace("https", "http");
                                try {
                                        link = new URL(url);
                                        if (scp.DEBUG)
                                                Log.d(TAG, "Trying to connect to " + link.toString());
                                        conn = link.openConnection();
                                        conn.connect();

                                } catch (IOException er) {
                                        Log.e(TAG, "Unspecified http or https in the url, tried both but failed", er);
                                        return null;
                                }
                        }
                } catch(ConnectException ce) {
                        Log.e(TAG, "Connection refused, check if the webserver running or check firewall.", ce);
                        return null;
                } catch(IOException ce) {
                        Log.e(TAG, "Connect failed, check connection", ce);
                        return null;
                } catch(Exception exc) {
                        Log.e(TAG, "Failed to connect to " + link.toString() + ", unkown error", exc);
                        return null;
                }
                
                return link.toString();
        }

        private static boolean set_conn(String url) {
                try {
                        URL link = new URL(url);
                        conn = link.openConnection();
                        return true;
                }
                catch(Exception e) {
                        if (scp.DEBUG)
                                Log.e(TAG, "Failed setting up connection to " + url);
                        return false;
                }
        }

        public static String get(String url) throws Exception {
                int response = 0;

                if (!set_conn(url))
                        return null;

                if (conn instanceof HttpURLConnection) {
                        HttpURLConnection connPlain = (HttpURLConnection) conn;
                        connPlain.setRequestMethod("GET");
                        connPlain.setUseCaches(true);
                        connPlain.setRequestProperty("User-Agent", USER_AGENT);
        
	                connPlain.setRequestProperty("Accept",
		                "text/html,text/csv,application/xhtml+xml;q=0.9,*/*;q=0.8");
	                connPlain.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

	                if (cookie != null)
                                connPlain.setRequestProperty("Cookie", cookie);
        
                        response = connPlain.getResponseCode();
                        
	                BufferedReader stream =
                        new BufferedReader(new InputStreamReader(connPlain.getInputStream()));
	                String ln;
	                StringBuffer html = new StringBuffer();
        
	                while ((ln = stream.readLine()) != null) {
		                html.append(ln + "\n");
	                }
        
	                stream.close();
        
	                cookies = connPlain.getHeaderFields().get("Set-Cookie");
	                if (cookies != null) {
                                String[] str = null;
                                for (String t : cookies)
                                        str = t.split(";");

                                if (str != null) {
                                        cookie = str[0];
                                        cookie_exp = str[1].split("=")[1];
                                }
                        }

	                return html.toString();
                }
                if (conn instanceof HttpsURLConnection) {
                        HttpsURLConnection connSsl = (HttpsURLConnection) conn;
                        connSsl.setRequestMethod("GET");
                        connSsl.setUseCaches(false);
                        connSsl.setRequestProperty("User-Agent", USER_AGENT);
        
	                connSsl.setRequestProperty("Accept",
		                "text/html,text/csv,application/xhtml+xml;q=0.9,*/*;q=0.8");
	                connSsl.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

	                if (cookie != null)
                                connSsl.setRequestProperty("Cookie", cookie);

        
                        response = connSsl.getResponseCode();
                        
	                BufferedReader stream =
                        new BufferedReader(new InputStreamReader(connSsl.getInputStream()));
	                String ln;
	                StringBuffer html = new StringBuffer();
        
	                while ((ln = stream.readLine()) != null) {
		                html.append(ln + "\n");
	                }
        
	                stream.close();
        
	                cookies = connSsl.getHeaderFields().get("Set-Cookie");
	                if (cookies != null) {
                                String[] str = null;
                                for (String t : cookies)
                                        str = t.split(";");

                                if (str != null) {
                                        cookie = str[0];
                                        cookie_exp = str[1].split("=")[1];
                                }
                        }

	                return html.toString();
                }

                if (scp.DEBUG)
                        Log.d(TAG, "Connection is not HttpURLConnection nor HttpsURLConnection");
                return null;
        }

        public static int post(String url, String params) throws Exception {
                int response = 0;
                if (!set_conn(url))
                        return 0;

                URL lnk = new URL(url);
                String host = lnk.getHost();

                if (conn instanceof HttpURLConnection) {
                        HttpURLConnection connPlain = (HttpURLConnection) conn;
	                connPlain.setUseCaches(false);
	                connPlain.setRequestMethod("POST");
	                connPlain.setRequestProperty("Host", host);
	                connPlain.setRequestProperty("User-Agent", USER_AGENT);
	                connPlain.setRequestProperty("Accept",
		                "text/html,text/csv,application/xhtml+xml;q=0.9,*/*;q=0.8");
	                connPlain.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		        connPlain.setRequestProperty("Cookie", cookie);
	                connPlain.setRequestProperty("Connection", "keep-alive");
	                connPlain.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	                connPlain.setRequestProperty("Content-Length", Integer.toString(params.length()));
                        connPlain.setFollowRedirects(false);
                        connPlain.setInstanceFollowRedirects(false);
                
	                connPlain.setDoOutput(true);
	                connPlain.setDoInput(true);
                
	                DataOutputStream wr = new DataOutputStream(connPlain.getOutputStream());
	                wr.writeBytes(params);
	                wr.flush();
	                wr.close();
        
                        response = connPlain.getResponseCode();
                }

                if (conn instanceof HttpsURLConnection) {
                        HttpsURLConnection connSsl = (HttpsURLConnection) conn;
	                connSsl.setUseCaches(false);
	                connSsl.setRequestMethod("POST");
	                connSsl.setRequestProperty("Host", host);
	                connSsl.setRequestProperty("User-Agent", USER_AGENT);
	                connSsl.setRequestProperty("Accept",
		                "text/html,text/csv,application/xhtml+xml;q=0.9,*/*;q=0.8");
	                connSsl.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		        connSsl.setRequestProperty("Cookie", cookie);
	                connSsl.setRequestProperty("Connection", "keep-alive");
	                connSsl.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	                connSsl.setRequestProperty("Content-Length", Integer.toString(params.length()));
                        connSsl.setFollowRedirects(false);
                        connSsl.setInstanceFollowRedirects(false);
                
	                connSsl.setDoOutput(true);
	                connSsl.setDoInput(true);
                
	                DataOutputStream wr = new DataOutputStream(connSsl.getOutputStream());
	                wr.writeBytes(params);
	                wr.flush();
	                wr.close();
        
                        response = connSsl.getResponseCode();
                }

                return response;
        }

        public static void rst_cookie()
        {
                cookie = null;
        }

        public static void close()
        throws Exception
        {
                cookie_man();
        }
}
