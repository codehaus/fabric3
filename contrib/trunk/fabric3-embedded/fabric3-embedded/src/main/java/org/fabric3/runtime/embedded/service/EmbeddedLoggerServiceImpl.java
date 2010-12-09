package org.fabric3.runtime.embedded.service;

import org.fabric3.runtime.embedded.api.service.EmbeddedLoggerService;

/**
 * @author Michal Capo
 */
public class EmbeddedLoggerServiceImpl implements EmbeddedLoggerService {

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
