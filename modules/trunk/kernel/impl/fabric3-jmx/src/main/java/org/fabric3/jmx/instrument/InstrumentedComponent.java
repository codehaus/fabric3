  /*
   * Fabric3
   * Copyright (c) 2009 Metaform Systems
   *
   * Fabric3 is free software: you can redistribute it and/or modify
   * it under the terms of the GNU General Public License as
   * published by the Free Software Foundation, either version 3 of
   * the License, or (at your option) any later version, with the
   * following exception:
   *
   * Linking this software statically or dynamically with other
   * modules is making a combined work based on this software.
   * Thus, the terms and conditions of the GNU General Public
   * License cover the whole combination.
   *
   * As a special exception, the copyright holders of this software
   * give you permission to link this software with independent
   * modules to produce an executable, regardless of the license
   * terms of these independent modules, and to copy and distribute
   * the resulting executable under terms of your choice, provided
   * that you also meet, for each linked independent module, the
   * terms and conditions of the license of that module. An
   * independent module is a module which is not derived from or
   * based on this software. If you modify this software, you may
   * extend this exception to your version of the software, but
   * you are not obligated to do so. If you do not wish to do so,
   * delete this exception statement from your version.
   *
   * Fabric3 is distributed in the hope that it will be useful,
   * but WITHOUT ANY WARRANTY; without even the implied warranty
   * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
   * See the GNU General Public License for more details.
   *
   * You should have received a copy of the
   * GNU General Public License along with Fabric3.
   * If not, see <http://www.gnu.org/licenses/>.
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
