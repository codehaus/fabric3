package org.fabric3.assembly.utils;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Michal Capo
 */
public class TimeUtils {

    public static int day(Long dateInMillis) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(dateInMillis));
        return cal.get(Calendar.DAY_OF_MONTH);
    }

}
