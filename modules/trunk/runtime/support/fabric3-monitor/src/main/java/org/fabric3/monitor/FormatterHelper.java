package org.fabric3.monitor;

import java.io.PrintWriter;

/**
 * @version $Rev$ $Date$
 */
public final class FormatterHelper {

    private FormatterHelper() {
    }

    /**
     * Writes a truncated stacktrace fr the exception.
     *
     * @param writer    the writer to output the stacktrace to
     * @param exception the stacktrace to write
     * @param cause     a nested exception. The wrapping exception stacktrace will be truncated based on where it
     *                  converges with the nested exception's stacktrace.
     */
    public static void writeStackTrace(PrintWriter writer, Throwable exception, Throwable cause) {
        StackTraceElement[] trace = exception.getStackTrace();
        StackTraceElement[] causedTrace = cause.getStackTrace();
        int depth = calculateTraceDepth(trace, causedTrace);
        int framesInCommon = trace.length - depth - 1;
        for (int i = 0; i <= depth; i++) {
            writer.println("\tat " + trace[i]);
        }
        if (framesInCommon != 0) {
            writer.println("\t... " + framesInCommon + " more");
        }
    }

    private static int calculateTraceDepth(StackTraceElement[] trace, StackTraceElement[] causedTrace) {
        int tracePos = trace.length - 1;
        int causedPos = causedTrace.length - 1;
        while (tracePos >= 0 && causedPos >= 0 && trace[tracePos].equals(causedTrace[causedPos])) {
            tracePos--;
            causedPos--;
        }
        return tracePos;
    }

}
