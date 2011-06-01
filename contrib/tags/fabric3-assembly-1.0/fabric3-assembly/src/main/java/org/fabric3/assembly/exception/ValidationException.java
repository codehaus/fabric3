package org.fabric3.assembly.exception;

import java.text.MessageFormat;

/**
 * On validation process.
 *
 * @author Michal Capo
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String pMessage, Object... pArguments) {
        super(MessageFormat.format(pMessage, pArguments));
    }

}
