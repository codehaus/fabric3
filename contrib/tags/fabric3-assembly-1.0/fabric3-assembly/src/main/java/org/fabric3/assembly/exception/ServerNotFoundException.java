package org.fabric3.assembly.exception;

import java.text.MessageFormat;

/**
 * When cannot find suitable server.
 *
 * @author Michal Capo
 */
public class ServerNotFoundException extends RuntimeException {

    public ServerNotFoundException(String pMessage) {
        super(pMessage);
    }

    public ServerNotFoundException(String pMessage, Object... pArguments) {
        super(MessageFormat.format(pMessage, pArguments));
    }
}
