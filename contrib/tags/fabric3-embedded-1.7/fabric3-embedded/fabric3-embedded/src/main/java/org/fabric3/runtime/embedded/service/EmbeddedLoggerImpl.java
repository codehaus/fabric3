package org.fabric3.runtime.embedded.service;

import org.fabric3.runtime.embedded.api.service.EmbeddedLogger;

/**
 * Simple implementation of Logger. Just print log messages to system output stream.
 *
 * @author Michal Capo
 */
public class EmbeddedLoggerImpl implements EmbeddedLogger {

    private static String prefix = "Embedded fabric3: ";

    public void initialize() {
        // no-op
    }

    public void log(String message) {
        System.out.println(prefix + message);
    }

    public void log(String message, Exception e) {
        System.out.println(prefix + message);
        e.printStackTrace();
    }
}
