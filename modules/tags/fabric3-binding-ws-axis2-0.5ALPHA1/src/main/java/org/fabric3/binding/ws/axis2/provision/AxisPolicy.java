package org.fabric3.binding.ws.axis2.provision;

import org.w3c.dom.Element;

/**
 * Axis policy definition.
 * 
 * @version $Revision$ $Date$
 */
public final class AxisPolicy {
    
    private final String message;
    private final String module;
    private final Element opaquePolicy;
    
    /**
     * Initializes the message, module and policy definition.
     * 
     * @param message Message against in which policy is applied.
     * @param module Axis module needs to engage for the policy.
     * @param opaquePolicy Opaque policy definition.
     */
    public AxisPolicy(final String message, final String module, final Element opaquePolicy) {
        this.message = "".equals(message) ? null : message;
        this.module = module;
        this.opaquePolicy = opaquePolicy;
    }

    /**
     * Gets the message against which the policy is applied.
     * 
     * @return Message against which the policy is applied.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Name of the Axis module to be enagged for the policy.
     * 
     * @return Axis module to be engaged for the policy.
     */
    public String getModule() {
        return module;
    }

    /**
     * Gets the opaque policy definition.
     * 
     * @return Opaque XML policy definition.
     */
    public Element getOpaquePolicy() {
        return opaquePolicy;
    }

}
