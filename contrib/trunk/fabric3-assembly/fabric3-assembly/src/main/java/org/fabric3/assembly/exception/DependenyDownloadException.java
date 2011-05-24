package org.fabric3.assembly.exception;

/**
 * @author Michal Capo
 */
public class DependenyDownloadException extends RuntimeException {

    public DependenyDownloadException(Throwable cause) {
        super(cause);
    }

    public DependenyDownloadException(String pDependency, Throwable cause) {
        super(String.format("Dependency '%s' download failure.", pDependency), cause);
    }
}
