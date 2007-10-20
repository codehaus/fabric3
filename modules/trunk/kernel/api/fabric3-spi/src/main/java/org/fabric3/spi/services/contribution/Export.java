package org.fabric3.spi.services.contribution;

import java.io.Serializable;
import javax.xml.namespace.QName;

/**
 * Represents an exported artifact in a contribution
 *
 * @version $Rev$ $Date$
 */
@SuppressWarnings({"serial", "SerializableHasSerializationMethods"})
public abstract class Export implements Serializable {
    public static final int NO_MATCH = -1;
    public static final int EXACT_MATCH = 1; 

    public abstract int match(Import contributionImport);

    public abstract QName getType();

}
