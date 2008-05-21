package org.fabric3.fabric.services.contribution.manifest;

import javax.xml.stream.XMLStreamReader;

import org.fabric3.introspection.xml.LoaderException;

/**
 * Temporary exception
 *
 * @version $Revision$ $Date$
 */
public class MissingMainifestAttributeException extends LoaderException {
    public MissingMainifestAttributeException(String message, XMLStreamReader reader) {
        super(message, reader);
    }
}
