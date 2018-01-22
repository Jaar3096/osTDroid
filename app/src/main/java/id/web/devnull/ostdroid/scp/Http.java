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

public class Http {
        private static final String USER_AGENT  = "osTDroid";
        private static List<String> cookies     = null;
        private static String cookie            = null;
        private static String cookie_exp        = null;
        private static String saved_cookie      = null;
        private URLConnection conn;
        private static final String ENCODING    = "UTF-8";
        public Map<String, List<String>> header = null;

        public static final int HTTP_OK         = 200;
        public static final int HTTP_FOUND      = 302;
        public static final int HTTP_NOT_FOUND  = 404;
        private static final String TAG         = "osTDroid";
        private static final DB db              = dbsetup.db;


        public int errno                        = 0;
        public static final int E_HTTP_MALFORM  = 0x01;
        public static final int E_HTTP_TLS      = 0x02;
        public static final int E_HTTP_REFUSED  = 0x04;
        public static final int E_HTTP_UNAVAIL  = 0x08;
        public static final int E_HTTP_UNKNOWN  = 0x10;
        public static final int E_HTTP_CONNFAILED= 0x20;
        public static final int E_HTTP_NOTFOUND = 0x40;

        public Http()
        {
                cookie_man();
        }

        private static void cookie_man()
        {
                String DATABASE         = "cookie";
                String DATA_DIR         = "data";
                String DATEFORMAT       = "EEE, dd-MMM-yyyy hh:mm:ss z";
                String TIMEZONE         = "GMT";

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

                                        if (!db.exists(key))
                                                break;

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
                }
        }

        public int ck_url (String url) {
                URL link = null;
                int res = 0;

                try {
                        link = new URL(url);
                        if (scp.DEBUG)
                                Log.d(TAG, "Trying to connect to " + link.toString());
                        conn = link.openConnection();
                        conn.connect();
                } catch(MalformedURLException e) {
                        res |= E_HTTP_MALFORM;
                        return res;
                } catch(SSLHandshakeException ssl_err) {
                        res |= E_HTTP_TLS;
                        return res;
                } catch(ConnectException ce) {
                        res |= E_HTTP_REFUSED;
                        return res;
                } catch(IOException ce) {
                        res |= E_HTTP_UNAVAIL;
                        return res;
                } catch(Exception exc) {
                        res |= E_HTTP_UNKNOWN;
                        return res;
                }
                
                return res;
        }

        private boolean set_conn(String url) {
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

        public String get(String url) {
                int response = 0;
                StringBuffer html = null;

                if (!set_conn(url)) {
                        errno |= E_HTTP_CONNFAILED;
                        if (scp.DEBUG)
                                Log.e(TAG, "Connection to " + url + " failed");
                        return null;
                }

                try {
                        HttpURLConnection sock = (HttpURLConnection) conn;
                        sock.setRequestMethod("GET");
                        sock.setUseCaches(true);
                        sock.setRequestProperty("User-Agent", USER_AGENT);
                        sock.setFollowRedirects(true);
                        sock.setInstanceFollowRedirects(true);
        
	                sock.setRequestProperty("Accept", "*/*");
	                sock.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

	                if (cookie != null)
                                sock.setRequestProperty("Cookie", cookie);
        
                        response = sock.getResponseCode();
                        if (scp.DEBUG)
                                Log.i(TAG, "HTTP GET returns " + response);
                        
                        InputStream is;
                        
                        if (response == HTTP_OK)
                                is = sock.getInputStream();
                        else    is = sock.getErrorStream();

                        if (is == null) {
                                if (scp.DEBUG)
                                        Log.e(TAG, "Read null data from http socket");
                                return null;
                        }

	                BufferedReader stream = new BufferedReader(new InputStreamReader(is));

	                String ln;
	                html = new StringBuffer();
        
	                while ((ln = stream.readLine()) != null) {
		                html.append(ln + "\n");
	                }
        
	                stream.close();
        
                        header = sock.getHeaderFields();
	                cookies = header.get("Set-Cookie");

	                if (cookies != null) {
                                String[] str = null;
                                for (String t : cookies)
                                        str = t.split(";");

                                if (str != null) {
                                        if (cookie == null || !cookie.equals(str[0])) {
                                                if (scp.DEBUG)
                                                        Log.i(TAG, "New cookies form server, saving...");
                                                cookie = str[0];
                                                cookie_exp = str[1].split("=")[1];
                                                save_cookie();
                                        }
                                }
                        }
                } catch(FileNotFoundException fnf) {
                        if (scp.DEBUG)
                                Log.e(TAG, "HTTP 4XX", fnf);
                        errno |= E_HTTP_NOTFOUND;
                        return null;
                } catch(Exception e) {
                        if (scp.DEBUG)
                                Log.e(TAG, "HTTP GET error", e);
                        errno |= E_HTTP_UNKNOWN;
                        return null;
                }
               
                if (html != null)
                        return html.toString();

                errno |= E_HTTP_UNKNOWN;
                return null;
        }

        public File get_byte(String url) {
                int response = 0;
                try {
                        URL lnk = new URL(url);
                        HttpURLConnection sock = (HttpURLConnection) lnk.openConnection();

                        sock.setUseCaches(true);
                        sock.setRequestProperty("User-Agent", USER_AGENT);
	                if (cookie != null)
                                sock.setRequestProperty("Cookie", cookie);
        
                        response = sock.getResponseCode();
                        header = sock.getHeaderFields();

                        if (scp.DEBUG)
                                Log.i(TAG, "HTTP response code " + response);
                        InputStream data;

                        if (response == HTTP_OK)
                                data = sock.getInputStream();
                        else    data = sock.getErrorStream();

                        if (data == null) {
                                if (scp.DEBUG)
                                        Log.e(TAG, "Read null data from http socket");
                                return null;
                        }

                        byte[] buf = new byte[1024 * 8];
                        int bytes_read;
                        File out = File.createTempFile("ost", ".tmp");
                        OutputStream fout = new FileOutputStream(out);

                        while((bytes_read = data.read(buf)) != -1)
                                fout.write(buf, 0, bytes_read);

                        data.close();
                        fout.close();
                        return out;
                } catch(Exception e) {
                        if (scp.DEBUG)
                                Log.d(TAG, "Error download data from " + url, e);
                        errno |= E_HTTP_UNKNOWN;
                        return null;
                }
        }

        public int post(String url, String params) {
                int response = 0;
                if (!set_conn(url)) {
                        errno |= E_HTTP_CONNFAILED;
                        return 0;
                }

                try {
                        URL lnk = new URL(url);
                        String host = lnk.getHost();

                        HttpURLConnection sock = (HttpURLConnection) conn;
	                sock.setUseCaches(false);
	                sock.setRequestMethod("POST");
	                sock.setRequestProperty("Host", host);
	                sock.setRequestProperty("User-Agent", USER_AGENT);
	                sock.setRequestProperty("Accept",
		                "text/html,text/csv,application/xhtml+xml;q=0.9,*/*;q=0.8");
	                sock.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		        sock.setRequestProperty("Cookie", cookie);
	                sock.setRequestProperty("Connection", "keep-alive");
	                sock.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	                sock.setRequestProperty("Content-Length", Integer.toString(params.length()));
                        sock.setFollowRedirects(false);
                        sock.setInstanceFollowRedirects(false);
                
	                sock.setDoOutput(true);
	                sock.setDoInput(true);
                
	                DataOutputStream wr = new DataOutputStream(sock.getOutputStream());
	                wr.writeBytes(params);
	                wr.flush();
	                wr.close();
        
                        response = sock.getResponseCode();
                } catch(Exception e) {
                        errno |= E_HTTP_UNKNOWN;
                        return 0;
                }

                return response;
        }

        public static void rst_cookie()
        {
                cookie = null;
        }

        private void save_cookie() {
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
