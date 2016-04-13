package test.SpringMVC.util;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by Himx on 31/3/2016.
 *
 */
public class JSON {
    public static String toJSONstr(Map<String, String> map) {
        String res = "{";
        Iterator i = map.entrySet().iterator();
        int nMap = map.size();
        while ((nMap--) != 0) {
            Map.Entry entry = (Map.Entry) i.next();
            res += "\"" + entry.getKey() + "\": \"" + entry.getValue() + "\"";
            if (nMap != 0) res += ", ";
        }
        res += "}";
        return res;
    }
    public static String threeDimArrToJSONstr(double[][][] myList) {
        String res = "";
        int onlen = myList.length;
        res += "[";
        for (int i = 0; i< onlen; i++) {
            int twlen = myList[i].length;
            res += "[";
            for (int j = 0; j < twlen; j++) {
                int thlen = myList[i][j].length;
                res += "[";
                for (int k = 0; k < thlen; k++) {
                    res += myList[i][j][k];
                    if (k != thlen -1) res += ",";
                }
                res += "]";
                if (j != twlen - 1) res += ",";
            }
            res += "]";
            if (i != onlen -1) res += ",";
        }
        res += "]";
        return res;
    }
}
