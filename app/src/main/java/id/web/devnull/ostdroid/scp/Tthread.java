package id.web.devnull.ostdroid.scp;

import java.util.List;
import java.util.ArrayList;
import java.net.URL;
import java.io.Serializable;
import java.lang.StringBuilder;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Tthread implements Serializable
{
        public static final int INTERNAL = 0x01;
        public static final int EXTERNAL = 0x02;
        private static final String DATE_REGEX = "[0-9]+/[0-9]+/[0-9]+.*";
        private static final String SEP = "\\0";
        private static final String FILE_REGEX = ".*/file.php\\?.*";
        private static String HOST = null;

        public int type;
        public String poster;
        public String date;
        public String extra;
        public List<String> content;
               
        public Tthread() throws Exception {
                this.content = new ArrayList<String>();
                this.type &= 0;
                
                URL url = new URL(scp.config.get("url"));
                String proto = url.getProtocol();
                String host = url.getHost();
                HOST = proto + "://" + host;
        }
        
        public void extract(Element table) {
                switch(table.attr("class")) {
                        case "thread-entry note":
                                type |= INTERNAL;
                                break;
                        case "thread-entry message":
                                type |= EXTERNAL;
                                break;
                        case "thread-entry response":
                                type |= EXTERNAL;
                                break;
                }

                Elements trs = table.select("tr");

                int i = 0;
                for (Element tr : trs) {
                        StringBuilder sb = new StringBuilder();

                        if (i == 0) {
                                Elements spans = tr.select("span");
                                String tmp = "";
                                for (Element span : spans) {
                                        String hdr = span.text();
                                        if (hdr.matches(DATE_REGEX)) {
                                                if (tmp.length() == 0)
                                                        tmp = hdr;
                                                this.date = hdr;
                                        }
                                }

                                tmp = tmp.replaceAll(" *" + this.date + " *", "");
                                this.extra = tmp;
                                               
                                this.poster = spans.get(spans.size() - 1).text();

                                i++;
                                continue;
                        }

                        Element td = tr.select("td").first();
                        Elements imgs = td.select("img");
                        Elements as = td.select("a");

                        String[] lines = td.html().split("\n");

                        int j;
                        int img = 0;
                        int a = 0;
                        String s;
                        for (j = 0; j < lines.length; j++) {
                                if (lines[j].matches(".*<img.*>.*")) {
                                        if (sb.length() > 0) {
                                                this.content.add(sb.toString());
                                                sb.delete(0, sb.length());
                                        }

                                        sb.append("img" + SEP);
                                        s = HOST + imgs.get(img).attr("src");
                                        sb.append(s);
                                        this.content.add(sb.toString());
                                        sb.delete(0, sb.length());
                                        img++;
                                        continue;
                                }

                                if (lines[j].matches(".*<a.*>.*")) {
                                        if (sb.length() > 0) {
                                                this.content.add(sb.toString());
                                                sb.delete(0, sb.length());
                                        }

                                        String txt  = as.get(a).text();
                                        if (!txt.matches(".*[0-9a-zA-Z]+")) {
                                                a++;
                                                continue;
                                        } else {
                                                s = as.get(a).attr("href");
                                                if (s.matches(FILE_REGEX))
                                                        sb.append("attach" + SEP + HOST + s);
                                                else    sb.append("link" + SEP + s);
                                        }

                                        sb.append(SEP + txt);
                                        this.content.add(sb.toString());
                                        sb.delete(0, sb.length());
                                        a++;
                                        continue;
                                }

                                if (lines[j].matches(".*<(br|p).*>.*"))
                                        sb.append("\n");

                                s = lines[j].replaceAll("<.*>", "");
                                s = s.replaceAll("^ +", "");
                                s = s.replaceAll("&amp;", "&");
                                s = s.replaceAll("&lt;", "<");
                                s = s.replaceAll("&gt;", ">");
                                s = s.replaceAll("&nbsp;", " ");
                                sb.append(s);
                        }
                        if (sb.length() > 0)
                                this.content.add(sb.toString());
                }
        }
}
