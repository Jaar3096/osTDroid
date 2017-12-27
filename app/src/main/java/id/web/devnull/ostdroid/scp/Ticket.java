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

        public static final int OPEN    = 0;
        public static final int OVERDUE = 1;
        public static final int CLOSE   = 2;
        private int thread_size;
        private static String thread_dir = null;
        private static final DB db      = dbsetup.db;
        private static Http http  = new Http();

        public Ticket()
        {
                this.thread_dir = scp.config.get("dir") + "/threads/";
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

        public List<Tthread> sync_thread()
        {
                if (scp.DEBUG)
                        Log.d(TAG, "Viewing ticket id #" + this.tid);

                String dbid;
                if (this.dbid.length() == 0) {
                        if (scp.DEBUG)
                                Log.d(TAG, "Ticket dbid is not set, tryng to get dbid from server");
                        if (!scp.login(scp.config.get("user"), scp.config.get("pass")))
                                if (scp.DEBUG)
                                        Log.d(TAG, "Error login to server");

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

                        List<Tthread> data = new LinkedList<Tthread>();
                        get_thread(scp.config.get("url") + 
                                        "/tickets.php?id=" + dbid, data);

                        return data;
                } catch (Exception e) {
                        Log.e(TAG, "error saving ticket to db", e);
                        return null;
                }
        }

        public boolean save()
        {
                try {
                        db.put(this.tid, this);
                        return true;
                } catch (Exception e) {
                        Log.e(TAG, "Error writing ticket to db", e);
                        return false;
                }
        }

        private void get_thread(String url, List<Tthread> data)
        throws Exception
        {
                final String THREAD_SELECTOR  = "ticket_thread";
                Tthread thread;

                String html = http.get(url);
                if (html == null)
                        Log.e(TAG, "Unable to get thread: html string is null");

                Document doc = Jsoup.parse(html);

                Element tthread = doc.getElementById(THREAD_SELECTOR);
                Elements tables = tthread.getElementsByTag("table");
                if (tables == null || tables.size() == 0)
                        return;

                if (tables.size() <= this.thread_size)
                        return;

                List<Tthread> local = load_thread();
                if (local != null)
                        for (Tthread t : local)
                                data.add(t);

                int i = 0;
                for (Element table : tables) {
                        if (i < this.thread_size) {
                                i++;
                                continue;
                        }

                        thread = new Tthread();
                        thread.extract(table);
                        
                        data.add(thread);
                }
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
                                        Log.d(TAG, doc.toString(), e);
                        }
                        return null;
                }
        }

        public List<Tthread> load_thread() {
                String file = this.thread_dir + "/" + this.tid + ".thread";

                if (file == null)
                        return null;
                File f = new File(file);
                if (!f.exists())
                        return null;

                try {
                        FileInputStream fin = new FileInputStream(f);
                        ObjectInputStream objin = new ObjectInputStream(fin);
                        List<Tthread> data = (List<Tthread>) objin.readObject();
                        objin.close();
                        fin.close();

                        this.thread_size = data.size();
                        return data;
                } catch (Exception e) {
                        if (scp.DEBUG)
                                Log.e(TAG, "Error loading thread for ticket #" + this.tid, e);
                        return null;
                }
        }

        public void save_thread(List<Tthread> data) {
                String file = this.thread_dir + "/" + this.tid + ".thread";
                
                if (data == null)
                        return;
                try {
                        File d = new File(thread_dir);
                        if (!d.exists())
                                d.mkdir();
                        File f = new File(file);
                        if (!f.exists())
                                f.createNewFile();

                        FileOutputStream fout = new FileOutputStream(f);
                        ObjectOutputStream objout = new ObjectOutputStream(fout);

                        objout.writeObject(data);
                        objout.close();
                        fout.close();
                } catch(Exception e) {
                        if (scp.DEBUG)
                                Log.e(TAG, "Error saving thread for ticket #" + this.tid, e);
                }
        }

}
