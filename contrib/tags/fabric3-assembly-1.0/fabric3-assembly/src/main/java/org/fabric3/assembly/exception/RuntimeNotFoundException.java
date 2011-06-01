package org.fabric3.assembly.exception;

/**
 * @author Michal Capo
 */
public class RuntimeNotFoundException extends RuntimeException {

    public RuntimeNotFoundException(String pRuntimeName) {
        super(String.format("Runtime %s not found", pRuntimeName));
    }

}
