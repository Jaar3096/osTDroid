package id.web.devnull.ostdroid.scp;

import java.util.List;
import java.util.ArrayList;
import java.lang.StringBuilder;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Tthread 
{
        public static final int INTERNAL = 0x01;
        public static final int MESSAGE  = 0x02;
        public static final int RESPONSE = 0x04;
        private static final String date_regex = "[0-9]+/[0-9]+/[0-9]+.*";
        private static final String SEP = "\\0";

        public int type;
        public String poster;
        public String date;
        public String extra;
        public List<String> content;
               
        public Tthread() {
              this.content = new ArrayList<String>();
              this.type &= 0;
        }
        
        public void extract(Element table) {
                switch(table.attr("class")) {
                        case "thread-entry note":
                                type |= INTERNAL;
                                break;
                        case "thread-entry message":
                                type |= MESSAGE;
                                break;
                        case "thread-entry response":
                                type |= RESPONSE;
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
                                        if (hdr.matches(date_regex)) {
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
                                        s = imgs.get(img).attr("src");
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

                                        s = as.get(a).text();
                                        if (!s.matches(".*[0-9a-zA-Z]+")) {
                                                a++;
                                                continue;
                                        } else  sb.append("link" + SEP + s);
                                        s = as.get(a).attr("href");
                                        sb.append("" + SEP + s);
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
