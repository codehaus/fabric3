package org.fabric3.scdl;

/**
 * Represents an injection site on a Java-based component implementation.
 *
 * @version $Revision$ $Date$
 */
public class InjectionSite extends ModelObject {
    private static final long serialVersionUID = 7792895640425046691L;

    // Name of type being injected
    private String type;

    protected InjectionSite(String type) {
        this.type = type;
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
