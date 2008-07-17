package org.fabric3.scdl;

import java.lang.annotation.ElementType;

/**
 * Represents an injection site on a Java-based component implementation.
 *
 * @version $Revision$ $Date$
 */
public class InjectionSite extends ModelObject {

    // Element type
    private ElementType elementType;

    // Name of type being injected
    private String type;

    protected InjectionSite(ElementType elementType, String type) {
        this.elementType = elementType;
        this.type = type;
    }

    /**
     * Gets the element type.
     *
     * @return Element type.
     */
    public ElementType getElementType() {
        return elementType;
    }

    /**
     * Returns the type being injected.
     *
     * @return the name of the type being injected
     */
    public String getType() {
        return type;
    }
}
