package org.fabric3.assembly.exception;

import java.text.MessageFormat;

/**
 * @author Michal Capo
 */
public class RunnerException extends RuntimeException {

    public RunnerException(String pMessage, Object... pArguments) {
        super(MessageFormat.format(pMessage, pArguments));
    }

}
