package org.fabric3.runtime.development.host;

import java.net.URI;

import org.fabric3.spi.builder.WiringException;

/**
 * @version $Rev$ $Date$
 */
public class MockObjectNotRegisteredException extends WiringException {

    protected MockObjectNotRegisteredException(String message, URI sourceUri, URI targetUri) {
        super(message, sourceUri, targetUri);
    }
}
