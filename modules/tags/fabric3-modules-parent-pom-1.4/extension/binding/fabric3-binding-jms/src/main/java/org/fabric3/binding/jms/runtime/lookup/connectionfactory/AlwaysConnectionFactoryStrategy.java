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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.binding.jms.runtime.lookup.connectionfactory;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Map;
import java.util.Collections;
import javax.jms.ConnectionFactory;

import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.jms.common.ConnectionFactoryDefinition;
import org.fabric3.binding.jms.runtime.lookup.ConnectionFactoryStrategy;
import org.fabric3.binding.jms.runtime.lookup.JmsLookupException;
import org.fabric3.binding.jms.spi.runtime.factory.ConnectionFactoryManager;
import org.fabric3.binding.jms.spi.runtime.factory.FactoryRegistrationException;

/**
 * Implementation that always attempts to create a connection factory.
 *
 * @version $Revision$ $Date$
 */
public class AlwaysConnectionFactoryStrategy implements ConnectionFactoryStrategy {
    private ConnectionFactoryManager manager;

    public AlwaysConnectionFactoryStrategy(@Reference ConnectionFactoryManager manager) {
        this.manager = manager;
    }

    public ConnectionFactory getConnectionFactory(ConnectionFactoryDefinition definition, Hashtable<String, String> env) throws JmsLookupException {

        try {
            String name = definition.getName();
            Map<String, String> props = definition.getProperties();
            String className = props.get("class");
            if (className == null) {
                throw new JmsLookupException("The 'class' attribute must be set");
            }
            ConnectionFactory factory = (ConnectionFactory) Class.forName(className).newInstance();
            // TODO We may need to factor this into provider specific classes rather than making the general assumption on bean style props
            for (PropertyDescriptor pd : Introspector.getBeanInfo(factory.getClass()).getPropertyDescriptors()) {
                String propName = pd.getName();
                String propValue = props.get(propName);
                Method writeMethod = pd.getWriteMethod();
                if (propValue != null && writeMethod != null) {
                    writeMethod.invoke(factory, propValue);
                }
            }
            return manager.register(name, factory, Collections.<String, String>emptyMap());
        } catch (InstantiationException e) {
            throw new JmsLookupException("Unable to create connection factory", e);
        } catch (IllegalAccessException e) {
            throw new JmsLookupException("Unable to create connection factory", e);
        } catch (ClassNotFoundException e) {
            throw new JmsLookupException("Unable to create connection factory", e);
        } catch (IntrospectionException e) {
            throw new JmsLookupException("Unable to create connection factory", e);
        } catch (InvocationTargetException e) {
            throw new JmsLookupException("Unable to create connection factory", e);
        } catch (FactoryRegistrationException e) {
            throw new JmsLookupException("Unable to create connection factory", e);
        }

    }

}
