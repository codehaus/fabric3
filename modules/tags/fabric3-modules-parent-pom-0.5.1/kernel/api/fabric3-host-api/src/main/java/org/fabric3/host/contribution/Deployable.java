package org.fabric3.host.contribution;

import java.io.Serializable;
import javax.xml.namespace.QName;

/**
 * Represents a deployable artifact in a contribution
 *
 * @version $Rev$ $Date$
 */
@SuppressWarnings({"SerializableHasSerializationMethods"})
public class Deployable implements Serializable {
    private static final long serialVersionUID = -710863113841788110L;
    private final QName name;
    private final QName type;

    public Deployable(QName name, QName type) {
        this.name = name;
        this.type = type;
    }

    /**
     * The QName of the deployable component.
     *
     * @return QName of the deployable component
     */
    public QName getName() {
        return name;
    }

    /**
     * Returns the deployable type.
     *
     * @return the deployable type
     */
    public QName getType() {
        return type;
    }


}
