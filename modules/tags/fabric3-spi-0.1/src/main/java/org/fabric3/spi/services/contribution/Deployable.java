package org.fabric3.spi.services.contribution;

import java.io.Serializable;
import javax.xml.namespace.QName;

/**
 * Represents a deployable component in a contribution
 *
 * @version $Rev$ $Date$
 */
@SuppressWarnings({"SerializableHasSerializationMethods"})
public class Deployable implements Serializable {
    private static final long serialVersionUID = -710863113841788110L;
    private final QName name;

    public Deployable(QName name) {
        this.name = name;
    }

    /**
     * The QName of the deployable component.
     *
     * @return QName of the deployable component
     */
    public QName getName() {
        return name;
    }

}
