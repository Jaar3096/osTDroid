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
        public String dbid;
        public String tid;
        public String date;
        public String title;
        public String user;
        public String user_email;
        public String prio;
        public String dprt;
        public String topic;
        public String source;
        public String status;
        public String mdfy;
        public String due_date;
        public String overdue;
        public String answered;
        public String asgn;
        public String agent_asgn;
        public String team_asgn;
        public int rd_flag;

        public int change       = 0;
        private List<Map> ticket_thread;

        public static final int OPEN    = 0;
        public static final int OVERDUE = 1;
        public static final int CLOSE   = 2;
        public static final DB db       = dbsetup.db;

        public Ticket()
        {
                this.dbid       = "";
                this.tid        = "";
                this.date       = "";
                this.title      = "";
                this.user       = "";
                this.user_email = "";
                this.prio       = "";
                this.dprt       = "";
                this.topic      = "";
                this.source     = "";
                this.status     = "";
                this.mdfy       = "";
                this.due_date   = "";
                this.overdue    = "";
                this.answered   = "";
                this.asgn       = "";
                this.agent_asgn = "";
                this.team_asgn  = "";
                this.rd_flag    = 0;
        }

        public String view()
        {
                if (scp.DEBUG)
                        Log.d(TAG, "Viewing ticket id #" + this.tid);

                String dbid;
                if (this.dbid.length() == 0) {
                        if (scp.DEBUG)
                                Log.d(TAG, "Ticket dbid is not set, tryng to get dbid from server");
                        dbid = get_dbid(this.tid);
                } else  dbid = this.dbid;

                if (dbid == null || !dbid.matches("[0-9]+")) {
                        Log.e(TAG, "database id not valid");
                        return null;
                }

                try {
                        if (!dbid.equals(this.dbid)) {
                                this.dbid =  dbid;
                                if (scp.DEBUG)
                                        Log.d(TAG, "Saving ticket data to db");
                                if (!save())
                                        return null;
                        }

                        List<Map> remote = get_thread(scp.config.get("url") + 
                                        "/tickets.php?id=" + dbid);
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
                        Log.e(TAG, "Error writing ticket to db", e);
                        return false;
                }
        }

        private static List<Map> get_thread(String url)
        throws Exception
        {
                String THREAD_SELECTOR  = "ticket_thread";
                Map<String, String> msg;

                String html = http.get(url);
                if (html == null) {
                        Log.e(TAG, "get ticket thread error");
                        return null;
                }

                Document doc = Jsoup.parse(html);
                Element thd = doc.getElementById(THREAD_SELECTOR);
                Elements threads = thread.getElementsByTag("table");
                ticket_thread = new LinkedList<Map>();

                for (Element thread : threads) {
                        msg = new HashMap<String, String>();
                        String cls = thread.attr("class");
                        if (cls != null) {
                                if (cls.equals("thread-entry note"))
                                        msg.add("type", "internal");
                                else    msg.add("type", "external");
                        } else continue;
                        msg.add("content", thread.toString());
                        ticket_thread.add(msg);
                }

                return ticket_thread;
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
