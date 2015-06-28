package cz.rekola.app.Utils;

import java.text.DateFormat;
import java.util.Date;

/**
 * TODO add class description
 * Created by Tomas Krabac[tomas.krabac@ackee.cz] on {28. 6. 2015}
 **/
public class DateUtils {
    public static String getDate(Date date) {
        DateFormat dateFormat = DateFormat.getDateInstance();
        return dateFormat.format(date);
    }

    public static String getTime(Date date) {
        DateFormat timeFormat = DateFormat.getTimeInstance();
        return timeFormat.format(date);
    }
}
