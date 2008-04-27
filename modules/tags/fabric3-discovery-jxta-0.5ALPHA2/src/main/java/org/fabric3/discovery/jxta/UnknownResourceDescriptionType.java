package org.fabric3.discovery.jxta;

import org.fabric3.jxta.impl.Fabric3JxtaException;

/**
 * @version $Rev$ $Date$
 */
public class UnknownResourceDescriptionType extends Fabric3JxtaException {
    private static final long serialVersionUID = 6135780023709720006L;

    public UnknownResourceDescriptionType(String message, String identifier) {
        super(message, identifier);
    }
}
