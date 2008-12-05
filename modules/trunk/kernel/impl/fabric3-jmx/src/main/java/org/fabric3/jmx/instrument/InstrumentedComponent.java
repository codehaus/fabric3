/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.jmx.instrument;

import java.util.Map;
import java.net.URI;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;

import org.fabric3.spi.component.Component;
import org.fabric3.model.type.component.PropertyValue;

/**
 * This is Ruscany component exposed as a dynamic MBean. Currently it only supports a read-only vew of all the
 * properties on the component.
 *
 * @version $Revision$ $Date$
 */
public class InstrumentedComponent implements DynamicMBean {

    /**
     * Properties available on the component.
     */
    private final Map<String, PropertyValue> properties;

    /**
     * Name of the component.
     */
    private URI componentId;

    /**
     * Initializes the property values.
     *
     * @param component Component that is being managed.
     */
    @SuppressWarnings("unchecked")
    public InstrumentedComponent(final Component component) {
        this.properties = component.getDefaultPropertyValues();
        this.componentId = component.getUri();
    }

    /**
     * @see javax.management.DynamicMBean#getAttribute(java.lang.String)
     */
    public final Object getAttribute(final String attribute) throws AttributeNotFoundException {
        PropertyValue propertyValue = properties.get(attribute);
        if (propertyValue != null) {
            return propertyValue.getValue();
        }
        throw new AttributeNotFoundException(attribute + " not found.");
    }

    /**
     * @see javax.management.DynamicMBean#getAttributes(java.lang.String[])
     */
    public final AttributeList getAttributes(final String[] attributes) {

        AttributeList list = new AttributeList();
        for (String attribute : attributes) {
            try {
                list.add(new Attribute(attribute, getAttribute(attribute)));
            } catch (AttributeNotFoundException ex) {
                throw new InstrumentationException(ex);
            }
        }
        return list;

    }

    /**
     * @see javax.management.DynamicMBean#getMBeanInfo()
     */
    public final MBeanInfo getMBeanInfo() {

        final MBeanConstructorInfo[] constructors = null;
        final MBeanOperationInfo[] operations = null;
        final MBeanNotificationInfo[] notifications = null;
    
        int size = properties != null ? properties.size() : 0;
        final MBeanAttributeInfo[] attributes = new MBeanAttributeInfo[size];
    
        if(properties != null) {
            int i = 0;
            for (PropertyValue propertyValue : properties.values()) {
                attributes[i++] =
                    new MBeanAttributeInfo(propertyValue.getName(), String.class.getName(), null, true, false, false);
            }
        }
    
        return new MBeanInfo(componentId.toString(), null, attributes, constructors, operations, notifications);

    }

    /**
     * @see javax.management.DynamicMBean#invoke(java.lang.String,java.lang.Object[],java.lang.String[])
     */
    public final Object invoke(final String actionName, final Object[] params, final String[] signature) {
        throw new UnsupportedOperationException("Managed ops not supported");
    }

    /**
     * @see javax.management.DynamicMBean#setAttribute(javax.management.Attribute)
     */
    public final void setAttribute(final Attribute attribute) {
        throw new UnsupportedOperationException("Mutable props not supported");
    }

    /**
     * @see javax.management.DynamicMBean#setAttributes(javax.management.AttributeList)
     */
    public final AttributeList setAttributes(final AttributeList attributes) {
        throw new UnsupportedOperationException("Mutable props not supported");
    }

}
