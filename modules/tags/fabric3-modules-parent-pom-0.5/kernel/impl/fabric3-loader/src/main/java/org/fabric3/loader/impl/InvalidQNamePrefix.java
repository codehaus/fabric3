package org.fabric3.loader.impl;

import javax.xml.stream.XMLStreamReader;

import org.fabric3.introspection.xml.XmlValidationFailure;

/**
 * @version $Revision$ $Date$
 */
public class InvalidQNamePrefix extends XmlValidationFailure<String> {
    public InvalidQNamePrefix(String modelObject, XMLStreamReader reader) {
        super("Invalid prefix: " + modelObject, modelObject, reader);
    }
}
