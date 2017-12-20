package id.web.devnull.ostdroid.scp;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;

import java.io.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.snappydb.DB;
import android.util.Log;

public class scp {
        private static final String LOGIN_ACT           = "login.php";
        public static Map<String, String> config        = new HashMap<String, String>();
        public static final String ENCODING             = "UTF-8";
        private static DB db                            = dbsetup.db;
        private static final int INDEX_MAX_SIZE         = 100;
        public static boolean DEBUG                     = true;
        private static final String TAG                 = "osTDroid";

        public static boolean setup(String url, String user, String pass)
        throws Exception
        {
                int w = 0;
                String lnk = null;

                if (!db.exists("config_url")) {
                        lnk = http.ck_url(url);
                        if (lnk == null) {
                                Log.e(TAG, "Malformed url or connection problem");
                                return false;
                        }

                        config.put("url", lnk);
                        config.put("user", user);
                        config.put("pass", pass);

                        w = 1;
                } else {
                        config.put("url", db.get("config_url"));
                        config.put("user", db.get("config_user"));
                        config.put("pass", db.get("config_pass"));
                }

                if(!config.get("url").matches(".*" + url + ".*") 
                                || !user.equals(config.get("user"))
                                || !pass.equals(config.get("pass")))
                {
                        lnk = http.ck_url(url);
                        if (lnk == null) {
                                Log.e(TAG, "Malformed url or connection problem");
                                return false;
                        }
                        
                        config.put("url", lnk);
                        config.put("user", user);
                        config.put("pass", pass);

                        http.rst_cookie();
                        w = 1;
                }

                if (w == 1) {
                        db.put("config_url", config.get("url"));
                        db.put("config_user", config.get("user"));
                        db.put("config_pass", config.get("pass"));
                }

                return true;
        }

        public static int islogin(String html) {
                if (html == null) {
                        if (scp.DEBUG)
                                Log.d(TAG, "islogin requires html tags, null received");
                        return -1;
                }

                try {
                        Document doc = Jsoup.parse(html);
        
                        if (doc != null) {
                                Elements forms = doc.select("form");
                                for (Element form : forms) {
                                        String action = form.attr("action");
                                        if (action.matches("login.php.*"))
                                        return 0;
                                }
        
                                Element navbar = doc.getElementById("info");
                                Elements links = navbar.getElementsByTag("a");
        
                                for (Element link : links) {
                                        if (link.text().matches(".*Log Out.*"))
                                                return 1;
                                }
                        }
                } catch (Exception e) {
                        Log.e(TAG, "URL is nor valid OSTicket web", e);
                }
                return -1;
        }

        public static boolean login (String user, String pass)
        throws Exception
        {
                String url = config.get("url") + "/login.php";
                String html = http.get(url);
                if (html == null)
                        return false;

                String params = set_params(html, user, pass);
                if (params == null) {
                        Log.e(TAG, "URL is not valid OSTicket staff panel, staff panel by default is at scp directory. Make sure URL is correct");
                        return false;
                }

                int retval = http.post(config.get("url") + "/" + LOGIN_ACT, params);
                if (retval == http.HTTP_FOUND)
                        return true;
                if (retval == http.HTTP_OK)
                        Log.e(TAG, "login failed, please check username and password");
                if (retval == http.HTTP_NOT_FOUND)
                        Log.e(TAG, "Login http POST returned error");

                return false;
        }

        private static String set_params(String html, String user, String pass) {
                try {
                        Document doc = Jsoup.parse(html);
        
	                Element loginform = doc.getElementById("loginBox");
	                Elements inputElements = loginform.getElementsByTag("input");
	                List<String> paramList = new ArrayList<String>();
        
	                for (Element inputElement : inputElements) {
		                String key = inputElement.attr("name");
		                String value = inputElement.attr("value");
                
		                if (key.equals("userid") && user != null)
			                value = user;
		                else if (key.equals("passwd") && pass != null)
			                value = pass;
        
		                paramList.add(key + "=" + URLEncoder.encode(value, "UTF-8"));
	                }
                
	                StringBuilder result = new StringBuilder();
	                for (String param : paramList) {
		                if (result.length() == 0)
			                result.append(param);
		                else
			                result.append("&" + param);
	                }
	                
                        return result.toString();
                }
                catch (Exception e) {
                        return null;
                }
        }

        public static List<Ticket> list(int ticket_state)
        {
                List<Ticket> ticket;
                String key;

                switch(ticket_state){
                        case Ticket.CLOSE:
                                key = "index_close";
                                break;
                        case Ticket.OVERDUE:
                                key = "index_overdue";
                                break;
                        default:
                                key = "index_open";
                                break;
                }

                try {
                        if (!db.exists(key)) {
                                return null;
                        }

                        ticket = new LinkedList<Ticket>();
                        Ticket t;
                        String[] data = db.getObjectArray(key, String.class);

                        for (String val : data) {
                                if (val == null)
                                        continue;
                                t = db.getObject(val, Ticket.class);
                                ticket.add(t);
                        }

                        return ticket;
                } catch(Exception e) {
                        Log.e(TAG, "error building list", e);
                        return null;
                }
        }

        public static List<Ticket> sync(int ticket_state)
        throws Exception
        {
                int TICKET_ID   = 0;
                int DATE        = 0;
                int TITLE       = 0;
                int USER        = 0;
                int USER_EMAIL  = 0;
                int PRIO        = 0;
                int DPRT        = 0;
                int TOPIC       = 0;
                int SOURCE      = 0;
                int STATUS      = 0;
                int MDFY        = 0;
                int DUE_DATE    = 0;
                int OVERDUE     = 0;
                int ANSWERED    = 0;
                int ASGN        = 0;
                int AGENT_ASGN  = 0;
                int TEAM_ASGN   = 0;

                int CSV_COL     = 17;

                String SELECT           = "a.export-csv.no-pjax";
                String url              = config.get("url");
                List<Ticket> ticket_lst = new LinkedList<Ticket>();
                
                String href= null;
                
                if (!login(config.get("user"), config.get("pass"))) {
                        Log.e(TAG, "Login error");
                        return null;
                }

                String s;
                switch(ticket_state) {
                        case Ticket.CLOSE:
                                s = url + "/tickets.php?status=closed";
                                break;
                        case Ticket.OVERDUE:
                                s = url + "/tickets.php?status=overdue";
                                break;
                        default:
                                s = url + "/tickets.php";
                                break;
                }

                if (scp.DEBUG)
                        Log.d(TAG, "Trying to fetch csv from " + s);

                String http_res = http.get(s);

                Document doc = Jsoup.parse(http_res);
                Elements as = doc.select(SELECT);
                for (Element a : as)
                        href = a.attr("href");

                String export_link = url + "/tickets.php" + href;

                if (scp.DEBUG)
                        Log.d(TAG, "Fetching csv from " + export_link);

                http_res = http.get(export_link);
                if (http_res == null) {
                        Log.e(TAG, "Fetch csv returns null");
                        return null;
                }

                BufferedReader inbuf = new BufferedReader(new StringReader(http_res));
                String ln;

                String[] tmp;
                String key = null;
                Ticket ticket;

                int i = 0;
                while ((ln = inbuf.readLine()) != null) {
                        ln = ln.replaceAll("\"", "");
                        tmp = ln.split(",", -1);

                        if (tmp.length < CSV_COL) {
                                Log.e(TAG, "CSV column < " + CSV_COL);
                                return null;
                        }

                        /* See CSV exported file */
                        if (i == 0) {
                                int j;
                                for (j = 0; j < tmp.length; j++) {
                                        switch(tmp[j]) {
                                                case "Ticket Number":
                                                        TICKET_ID = j;
                                                        break;
                                                case "Date":
                                                        DATE = j;
                                                        break;
                                                case "Subject":
                                                        TITLE = j;
                                                        break;
                                                case "From":
                                                        USER = j;
                                                        break;
                                                case "From Email":
                                                        USER_EMAIL= j;
                                                        break;
                                                case "Priority":
                                                        PRIO = j;
                                                        break;
                                                case "Department":
                                                        DPRT = j;
                                                        break;
                                                case "Help Topic":
                                                        TOPIC = j;
                                                        break;
                                                case "Source":
                                                        SOURCE = j;
                                                        break;
                                                case "Current Status":
                                                        STATUS = j;
                                                        break;
                                                case "Last Updated":
                                                        MDFY = j;
                                                        break;
                                                case "Due Date":
                                                        DUE_DATE = j;
                                                        break;
                                                case "Overdue":
                                                        OVERDUE = j;
                                                        break;
                                                case "Answered":
                                                        ANSWERED = j;
                                                        break;
                                                case "Assigned To":
                                                        ASGN = j;
                                                        break;
                                                case "Agent Assigned":
                                                        AGENT_ASGN= j;
                                                        break;
                                                case "Team Assigned":
                                                        TEAM_ASGN = j;
                                                        break;
                                        }
                                }

                                i++;
                                continue;
                        }

                        key = tmp[TICKET_ID];

                        int w = 0;
                        if (db.exists(key)) {
                                ticket = db.getObject(key, Ticket.class);
                                if (!ticket.prio.equals(tmp[PRIO])) {
                                        ticket.prio = tmp[PRIO];
                                        w |= 1;
                                }
                                if (!ticket.asgn.equals(tmp[ASGN])) {
                                        ticket.asgn= tmp[ASGN];
                                        w |= 1;
                                }
                                if (!ticket.status.equals(tmp[STATUS])) {
                                        ticket.status= tmp[STATUS];
                                        w |= 1;
                                }
                                if (!ticket.team_asgn.equals(tmp[TEAM_ASGN])) {
                                        ticket.team_asgn= tmp[TEAM_ASGN];
                                        w |= 1;
                                }
                                if (!ticket.mdfy.equals(tmp[MDFY])) {
                                        ticket.mdfy= tmp[MDFY];
                                        w |= 1;
                                }

                                if (w == 1)
                                        db.put(key, ticket);
                        } else {
                                ticket = new Ticket();
                                ticket.tid      = tmp[TICKET_ID];
                                ticket.date     = tmp[DATE];
                                ticket.title    = tmp[TITLE];
                                ticket.user     = tmp[USER];
                                ticket.user_email = tmp[USER_EMAIL];
                                ticket.prio     = tmp[PRIO];
                                ticket.dprt     = tmp[DPRT];
                                ticket.topic    = tmp[TOPIC];
                                ticket.source   = tmp[SOURCE];
                                ticket.status   = tmp[STATUS];
                                ticket.mdfy     = tmp[MDFY];
                                ticket.due_date = tmp[DUE_DATE];
                                ticket.overdue  = tmp[OVERDUE];
                                ticket.answered = tmp[ANSWERED];
                                ticket.asgn     = tmp[ASGN];
                                ticket.agent_asgn= tmp[AGENT_ASGN];
                                ticket.team_asgn= tmp[TEAM_ASGN];
                                
                                db.put(key, ticket);
                        }

                        ticket_lst.add(ticket);
                        i++;
                }

                return ticket_lst;
        }

        public static void write_index(List<Ticket> tickets, int ticket_state)
        throws Exception
        {
                if (tickets == null) {
                        Log.e(TAG, "Error writing index, ticket list == null");
                        return;
                }
                String[] index = new String[INDEX_MAX_SIZE];
                String key;

                switch(ticket_state){
                        case Ticket.CLOSE:
                                key = "index_close";
                                break;
                        case Ticket.OVERDUE:
                                key = "index_overdue";
                                break;
                        default:
                                key = "index_open";
                                break;
                }

                int i = 0;
                for (Ticket t : tickets) {
                        if (i >= INDEX_MAX_SIZE)
                                break;
                        if (t.tid.length() < 5)
                                continue;

                        index[i] = t.tid;
                        i++;
                }

                db.put(key, index);
        }

}
