package id.web.devnull.ostdroid.scp;

import android.content.Context;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

public class dbsetup
{
        private static String DATABASE = "data";
        public static DB db = null;

        public static boolean open(Context ctx)
        {
                try {
                        db = DBFactory.open(ctx, DATABASE);
                        return true;
                } catch (SnappydbException dbe) {
                        return false;
                }
        }

        public static void close()
        throws Exception
        {
                if (db != null)
                        db.close();
        }
}
