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
import javax.jms.ConnectionFactory;

import org.fabric3.binding.jms.common.ConnectionFactoryDefinition;
import org.fabric3.binding.jms.runtime.lookup.ConnectionFactoryStrategy;
import org.fabric3.binding.jms.runtime.lookup.JmsLookupException;

/**
 * Implementation that always attempts to create a connection factory.
 *
 * @version $Revision$ $Date$
 */
public class AlwaysConnectionFactoryStrategy implements ConnectionFactoryStrategy {

    public ConnectionFactory getConnectionFactory(ConnectionFactoryDefinition definition, Hashtable<String, String> env) throws JmsLookupException {

        try {
            ConnectionFactory cf = (ConnectionFactory) Class.forName(definition.getName()).newInstance();
            Map<String, String> props = definition.getProperties();
            // TODO We may need to factor this into provider specific classes rather than making the general assumption on bean style props
            for (PropertyDescriptor pd : Introspector.getBeanInfo(cf.getClass()).getPropertyDescriptors()) {
                String propName = pd.getName();
                String propValue = props.get(propName);
                Method writeMethod = pd.getWriteMethod();
                if (propValue != null && writeMethod != null) {
                    writeMethod.invoke(cf, propValue);
                }
            }
            return cf;
        } catch (InstantiationException ex) {
            throw new JmsLookupException("Unable to create connection factory", ex);
        } catch (IllegalAccessException ex) {
            throw new JmsLookupException("Unable to create connection factory", ex);
        } catch (ClassNotFoundException ex) {
            throw new JmsLookupException("Unable to create connection factory", ex);
        } catch (IntrospectionException ex) {
            throw new JmsLookupException("Unable to create connection factory", ex);
        } catch (InvocationTargetException ex) {
            throw new JmsLookupException("Unable to create connection factory", ex);
        }

    }

}
