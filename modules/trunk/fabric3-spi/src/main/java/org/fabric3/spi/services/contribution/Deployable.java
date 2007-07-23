package org.fabric3.spi.services.contribution;

import java.io.Serializable;
import javax.xml.namespace.QName;

import org.fabric3.spi.model.type.ModelObject;

/**
 * Represents a deployable artifact in a contribution
 *
 * @version $Rev$ $Date$
 */
@SuppressWarnings({"SerializableHasSerializationMethods"})
public class Deployable implements Serializable {
    private static final long serialVersionUID = -710863113841788110L;
    private final QName name;
    private final ModelObject modelObject;

    public Deployable(QName name, ModelObject modelObject) {
        this.name = name;
        this.modelObject = modelObject;
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
     * Returns the deployable model object.
     *
     * @return the deployable model object
     */
    public ModelObject getModelObject() {
        return modelObject;
    }


}
