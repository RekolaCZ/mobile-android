package cz.rekola.app.Utils;

import java.text.DateFormat;
import java.util.Date;

/**
 * Date utils for uniform date formatting
 * Created by Tomas Krabac[tomas.krabac@ackee.cz] on {28. 6. 2015}
 **/
public class DateUtils {

    /**
     * get string representation of date from Date
     *
     * @param date input date
     * @return string in short format eg. HH:MM
     */
    public static String getDate(Date date) {
        DateFormat dateFormat = DateFormat.getDateInstance();
        return dateFormat.format(date);
    }

    /**
     * get string representation of time from Date
     * @param date input date
     * @return string in short format eg. HH:MM
     */
    public static String getTime(Date date) {
        DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
        return timeFormat.format(date);
    }
}
