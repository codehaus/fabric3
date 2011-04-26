package org.fabric3.assembly.utils;

import java.text.MessageFormat;

/**
 * Simple implementation of Logger. Just print log messages to system output stream.
 *
 * @author Michal Capo
 */
public class LoggerUtils {

    private static final String prefix = "Assembly: ";

    /**
     * Write simple message to logger. Can be info or warning message.
     *
     * @param message to be display/written by logger
     */
    public static void log(String message) {
        System.out.println(prefix + message);
    }

    /**
     * Write simple message to logger with MessageFormat.format support.
     *
     * @param pattern   of message
     * @param arguments to append into MessageFormat-er
     */
    public static void log(String pattern, Object... arguments) {
        System.out.println(prefix + MessageFormat.format(pattern, arguments));
    }

    /**
     * Write or display an error message with given exception to logger.
     *
     * @param message an error message
     * @param e       an exception to be written/displayed
     */
    public static void log(String message, Exception e) {
        System.out.println(prefix + message);
        e.printStackTrace();
    }

    public static void log(Exception e, String pattern, Object... arguments) {
        System.out.println(prefix + MessageFormat.format(pattern, arguments));
        e.printStackTrace();
    }

    public static void logWarn(String message) {
        System.out.println(prefix + "WARNING " + message);
    }

    public static void logWarn(String pattern, Object... arguments) {
        System.out.println(prefix + "WARNING " + MessageFormat.format(pattern, arguments));
    }
}
