package org.fabric3.binding.ws.jaxws.runtime;

import java.net.URL;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Map;
import java.util.List;
import java.lang.reflect.Method;
import javax.xml.ws.Service;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.ws.jaxws.provision.JaxWsWireTargetDefinition;
import org.fabric3.binding.codegen.ProxyGenerator;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.wire.Wire;
import org.fabric3.spi.wire.InvocationChain;

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

public class JaxWsTargetWireAttacher implements TargetWireAttacher<JaxWsWireTargetDefinition> {

    private final ClassLoaderRegistry registry;
    private final ProxyGenerator generator;

    public JaxWsTargetWireAttacher(@Reference ClassLoaderRegistry registry,
                                   @Reference ProxyGenerator generator) {
        this.registry = registry;
        this.generator = generator;
    }

    public void attachToTarget(PhysicalWireSourceDefinition source,
                               JaxWsWireTargetDefinition target, Wire wire)
            throws WiringException {

        Service service;
        String wsdlElement = target.getWsdlElement();
        String targetNamespace = null;
        String serviceName = null;
        String portName = null;
        if (wsdlElement != null) {
            int index = wsdlElement.indexOf("#wsdl.port");
            int lastSlashIndex = wsdlElement.lastIndexOf("/");
            targetNamespace = wsdlElement.substring(0, index);
            serviceName = wsdlElement.substring(index + 11, lastSlashIndex);
            portName = wsdlElement.substring(lastSlashIndex + 1, wsdlElement.length()
                    - 1);
        }
        try {
            Class clazz = loadClass(target.getReferenceInterface(), target.getClassloaderURI());
            QName qname = new QName(targetNamespace, serviceName);
            service = Service.create(new URL(target.getWsdlLocation()), qname);
            clazz = generator.getWrapperClass(clazz, targetNamespace, wsdlElement, serviceName, portName);
            for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry :
                    wire.getInvocationChains().entrySet()) {
                PhysicalOperationDefinition op = entry.getKey();
                WsTargetInterceptor wti =
                        new WsTargetInterceptor(locateMethod(op, clazz), service, clazz,
                                                new QName(targetNamespace, portName));
                InvocationChain chain = entry.getValue();
                chain.addInterceptor(wti);
            }

        } catch (ClassNotFoundException cnfe) {
            AssertionError ae = new AssertionError("Unexpected exception");
            ae.initCause(cnfe);
            throw ae;
        } catch (MalformedURLException mue) {
            AssertionError ae = new AssertionError("Unexpected exception");
            ae.initCause(mue);
            throw ae;
        }

    }


    public ObjectFactory<?> createObjectFactory(JaxWsWireTargetDefinition definition) {
        throw new AssertionError();
    }

    private static Method locateMethod(PhysicalOperationDefinition operation,
                                       Class clazz) {
        try {
            assert clazz.isInterface();
            ClassLoader cl = clazz.getClassLoader();
            List<String> paramsAsString = operation.getParameters();
            Class[] params = new Class[paramsAsString.size()];
            int i = 0;
            for (String str : paramsAsString) {
                params[i++] = cl.loadClass(str);
            }
            return clazz.getMethod(operation.getName(), params);
        } catch (NoSuchMethodException e) {
            AssertionError we = new AssertionError("Failed to match operation "
                    + operation.getName());
            we.initCause(e);
            throw we;
        } catch (ClassNotFoundException cnfe) {
            AssertionError we = new AssertionError("Failed to match operation "
                    + operation.getName());
            we.initCause(cnfe);
            throw we;
        }
    }


    private Class loadClass(String className, URI uri) throws ClassNotFoundException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (uri != null) {
          cl = registry.getClassLoader(uri);
        }
        assert cl != null;
        try {
            return cl.loadClass(className);
        } catch (ClassNotFoundException cnfe) {
            cl = Thread.currentThread().getContextClassLoader();
            return cl.loadClass(className);
        }

    }

}
