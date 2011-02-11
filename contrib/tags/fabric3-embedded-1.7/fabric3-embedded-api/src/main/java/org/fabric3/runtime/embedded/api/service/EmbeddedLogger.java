package org.fabric3.runtime.embedded.api.service;

/**
 * Output messages to user.
 *
 * @author Michal Capo
 */
public interface EmbeddedLogger {

    /**
     * Write simple message to logger. Can be info or warning message.
     *
     * @param message to be display/written by logger
     */
    void log(String message);

    /**
     * Write or display an error message with given exception to logger.
     *
     * @param message an error message
     * @param e       an exception to be written/displayed
     */
    void log(String message, Exception e);

}
