package org.fabric3.runtime.embedded.api.service;

/**
 * @author Michal Capo
 */
public interface EmbeddedLoggerService {

    void initialize();

    void log(String message);

    void log(String message, Exception e);

}
