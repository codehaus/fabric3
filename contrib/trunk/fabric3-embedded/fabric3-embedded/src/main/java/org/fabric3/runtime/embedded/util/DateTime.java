package org.fabric3.runtime.embedded.util;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Michal Capo
 */
public class DateTime {

    public static int day(Long dateInMillis) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(dateInMillis));
        return cal.get(Calendar.DAY_OF_MONTH);
    }

}
