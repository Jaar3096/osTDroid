package id.web.devnull.ostdroid.scp;

import java.io.*;
import java.util.*;
import java.lang.StringBuffer;
import java.net.URLEncoder;

import android.util.Log;
import com.snappydb.DB;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Ticket
{
        private static final String TAG = "osTDroid";
        public String tid;              /* Ticket id */
        public String dbid;             /* Ticket database id */
        public String date;             /* Created date */
        public String title;            /* Ticket title */
        public String prio;             /* Ticket Priority */
        public String user;             /* Ticket user */
        public String asgn;             /* Assigned to */
        public String topic;            /* ticket topic */
        public String status;           /* ticket status */
        public String team;             /* ticket team */
        public String modify_time;      /* Last modified time */
        public int rd_flag;             /* read/unread flag */

        public int change       = 0;

        public static final int OPEN    = 0;
        public static final int OVERDUE = 1;
        public static final int CLOSE   = 2;
        public static final DB db       = dbsetup.db;

        public void Ticket()
        {
                this.tid             = "";
                this.dbid            = "";
                this.date            = "";
                this.title           = "";
                this.prio            = "";
                this.user            = "";
                this.asgn            = "";
                this.topic           = "";
                this.status          = "";
                this.team            = "";
                this.modify_time     = "";
                this.rd_flag         = 0;

        }

        public String view()
        {
                String dbid;
                if (this.dbid == null || this.dbid.length() == 0)
                        dbid = get_dbid(this.tid);
                else    dbid = this.dbid;

                if (dbid == null || !dbid.matches("[0-9]+")) {
                        Log.e(TAG, "database id not valid");
                        return null;
                }

                try {
                        if (!dbid.equals(this.dbid)) {
                                this.dbid =  dbid;
                                if (!save())
                                        return null;
                        }

                        String remote = get_thread(scp.config.get("url") + "/tickets.php?id=" + dbid);
                        if (remote == null) {
                                Log.e(TAG, "Get ticket thread returns null");
                                return null;
                        }

                        return remote;

                } catch (Exception e) {
                        Log.e(TAG, "error create ticket thread file");
                        return null;
                }
        }

        private boolean save()
        {
                try {
                        db.put(this.tid, this);
                        return true;
                } catch (Exception e) {
                        Log.e(TAG, "Error writing ticket to db");
                        return false;
                }
        }

        private static String get_thread(String url)
        throws Exception
        {
                String THREAD_SELECTOR  = "ticket_thread";

                String html = http.get(url);
                if (html == null) {
                        Log.e(TAG, "get ticket thread error");
                        return null;
                }

                Document doc = Jsoup.parse(html);
                Element thread = doc.getElementById(THREAD_SELECTOR);

                return thread.toString();
        }

        private static String get_dbid(String tid)
        {
                String srch_tag = "basic_search";
                String url = scp.config.get("url");
                List<String> params = new ArrayList<String>();
                String srch_url;
                Document doc = null;

                try {
                        String html = http.get(url);
                        if (html == null)
                                return null;

                        if (scp.DEBUG)
                                Log.d(TAG, "parse search form from " + url);
                        doc = Jsoup.parse(html);
                        Element form = doc.getElementById(srch_tag);
                        Elements inputs = form.getElementsByTag("input");
        
                        for (Element input : inputs) {
                                String k = input.attr("name"); 
                                String v = input.attr("value"); 
        
                                if (k.equals("basic_search"))
                                        continue;
                                if (k.equals("query"))
                                        v = tid;
        
                                params.add(k + "=" + URLEncoder.encode(v, "UTF-8"));
                        }

        
                        StringBuilder res = new StringBuilder();
                        for (String param : params) {
                                if (res.length() == 0)
                                        res.append(param);
                                else    res.append("&" + param);
                        }
        
                        srch_url = url + "/tickets.php?" + res.toString();
                        if (scp.DEBUG)
                                Log.d(TAG, "Ticket search uri : " + srch_url);

                        html = http.get(srch_url);

                        if (html == null) {
                                Log.e(TAG, "Error getting search result form " + srch_url);
                                return null;
                        }

                        doc = Jsoup.parse(html);
                        Elements trs = doc.select("tbody tr"); 

                        String id;
                        for (Element tr : trs) {
                                id = tr.attr("id");
                                if (id.length() == 0 || id == null)
                                        continue;

                                return id;
                        }

                        if (scp.DEBUG) {
                                Log.d(TAG, "Error getting html tr attr \"id\"");
                                Log.d(TAG, trs.toString());
                        }

                        return null;

                } catch (Exception e) {
                        if (scp.DEBUG) {
                                Log.d(TAG, "Exception", e);
                                if (doc != null)
                                        Log.d(TAG, doc.toString());
                        }
                        return null;
                }
        }
}
