/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.jmx.runtime;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.management.IntrospectionException;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.jmx.provision.JMXWireSourceDefinition;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.spi.wire.Wire;

/**
 * @version $Rev$ $Date$
 */
public class JMXWireAttacher implements SourceWireAttacher<JMXWireSourceDefinition> {
    private final MBeanServer mBeanServer;
    private final ClassLoaderRegistry classLoaderRegistry;
    private final String domain;

    public JMXWireAttacher(@Reference MBeanServer mBeanServer,
                           @Reference ClassLoaderRegistry classLoaderRegistry,
                           @Property(name = "domain")String domain) {
        this.mBeanServer = mBeanServer;
        this.classLoaderRegistry = classLoaderRegistry;
        this.domain = domain;
    }

    public void attachToSource(JMXWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire) throws WiringException {
        throw new UnsupportedOperationException();
    }

    public void attachObjectFactory(JMXWireSourceDefinition source, ObjectFactory<?> objectFactory) throws WiringException {
        if (mBeanServer == null) {
            return;
        }
        
        URI uri = source.getUri();
        String component = UriHelper.getDefragmentedNameAsString(uri);
        System.err.println("***************:" + component);
        String service = uri.getFragment();
        try {
            Class<?> managementInterface = classLoaderRegistry.loadClass(source.getClassLoaderId(), source.getInterfaceName());
            ObjectName name = new ObjectName(domain + ":type=service,component=\"" + component + "\",service=" + service);
            OptimizedMBean<?> mbean = createOptimizedMBean(objectFactory, managementInterface);
            mBeanServer.registerMBean(mbean, name);
        } catch (JMException e) {
            throw new WiringException(e);
        } catch (ClassNotFoundException e) {
            throw new WiringException(e);
        }
    }

    private <T> OptimizedMBean<T> createOptimizedMBean(ObjectFactory<T> objectFactory, Class<?> service) throws IntrospectionException {
        String className = service.getName();
        Set<String> attributeNames = new HashSet<String>();
        Map<String, Method> getters = new HashMap<String, Method>();
        Map<String, Method> setters = new HashMap<String, Method>();
        Map<OperationKey, Method> operations = new HashMap<OperationKey, Method>();
        for (Method method : service.getMethods()) {
            switch (getType(method)){
            case GETTER:
                String getterName = getAttributeName(method);
                attributeNames.add(getterName);
                getters.put(getterName, method);
                break;
            case SETTER:
                String setterName = getAttributeName(method);
                attributeNames.add(setterName);
                setters.put(setterName, method);
                break;
            case OPERATION:
                operations.put(new OperationKey(method), method);
                break;
            }
        }

        MBeanAttributeInfo[] mbeanAttributes = createAttributeInfo(attributeNames, getters, setters);
        MBeanOperationInfo[] mbeanOperations = createOperationInfo(operations.values());
        MBeanInfo mbeanInfo = new MBeanInfo(className, null, mbeanAttributes, null, mbeanOperations, null);
        return new OptimizedMBean<T>(objectFactory, mbeanInfo, getters, setters, operations);
    }

    private MBeanOperationInfo[] createOperationInfo(Collection<Method> operations) {
        MBeanOperationInfo[] mbeanOperations = new MBeanOperationInfo[operations.size()];
        int i = 0;
        for (Method method : operations) {
            mbeanOperations[i++] = new MBeanOperationInfo(null, method);
        }
        return mbeanOperations;
    }

    private MBeanAttributeInfo[] createAttributeInfo(Set<String> attributeNames, Map<String, Method> getters, Map<String, Method> setters)
            throws IntrospectionException {
        MBeanAttributeInfo[] mbeanAttributes = new MBeanAttributeInfo[attributeNames.size()];
        int i = 0;
        for (String name : attributeNames) {
            mbeanAttributes[i++] = new MBeanAttributeInfo(name, null, getters.get(name), setters.get(name));
        }
        return mbeanAttributes;
    }

    private static enum MethodType {
        GETTER, SETTER, OPERATION
    }

    private static MethodType getType(Method method) {
        String name = method.getName();
        Class<?> returnType = method.getReturnType();
        int paramCount = method.getParameterTypes().length;

        if (Void.TYPE.equals(returnType) && name.length() > 3 && name.startsWith("set") && paramCount == 1) {
            return MethodType.SETTER;
        } else if (Boolean.TYPE.equals(returnType) && name.length() > 2 && name.startsWith("is") && paramCount == 0) {
            return MethodType.GETTER;
        } else if (name.length() > 3 && name.startsWith("get") && paramCount == 0) {
            return MethodType.GETTER;
        } else {
            return MethodType.OPERATION;
        }
    }

    private static String getAttributeName(Method method) {
        String name = method.getName();
        if (name.startsWith("is")) {
            return name.substring(2);
        } else {
            return name.substring(3);
        }
    }
}
