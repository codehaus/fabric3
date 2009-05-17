/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
import org.fabric3.binding.jms.runtime.Fabric3JmsException;

/**
 * The connection factory is never looked up, it is always created.
 *
 * @version $Revision$ $Date$
 */
public class AlwaysConnectionFactoryStrategy implements ConnectionFactoryStrategy {

    public ConnectionFactory getConnectionFactory(ConnectionFactoryDefinition definition, Hashtable<String, String> env) {

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
            throw new Fabric3JmsException("Unable to create connection factory", ex);
        } catch (IllegalAccessException ex) {
            throw new Fabric3JmsException("Unable to create connection factory", ex);
        } catch (ClassNotFoundException ex) {
            throw new Fabric3JmsException("Unable to create connection factory", ex);
        } catch (IntrospectionException ex) {
            throw new Fabric3JmsException("Unable to create connection factory", ex);
        } catch (InvocationTargetException ex) {
            throw new Fabric3JmsException("Unable to create connection factory", ex);
        }

    }

}
