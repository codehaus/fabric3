package org.fabric3.binding.ws.jaxws.runtime;

import java.net.URI;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.HashMap;

import javax.xml.ws.Endpoint;

import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.ws.jaxws.provision.JaxWsWireSourceDefinition;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.builder.component.WireAttachException;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.wire.Wire;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.scdl.Signature;
import org.fabric3.services.codegen.ProxyGenerator;

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

public class JaxWsSourceWireAttacher implements SourceWireAttacher<JaxWsWireSourceDefinition> {

    private final ClassLoaderRegistry registry;
    private final ProxyGenerator generator;

    public JaxWsSourceWireAttacher(@Reference ClassLoaderRegistry registry,
                                   @Reference ProxyGenerator generator) {
        this.registry = registry;
        this.generator = generator;
    }

    public void attachToSource(JaxWsWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire)
            throws WiringException {
        Class clazz = null;
        Map<Method, Map.Entry<PhysicalOperationDefinition, InvocationChain>> ops =
                new HashMap<Method, Map.Entry<PhysicalOperationDefinition, InvocationChain>>();
        try {
            ClassLoader cl = registry.getClassLoader(source.getClassloaderURI());
            assert cl != null;
            clazz = cl.loadClass(source.getServiceInterface());

            for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry :
                    wire.getInvocationChains().entrySet()) {

                Signature signature = new Signature(
                        entry.getKey().getName(), entry.getKey().getParameters());
                ops.put(signature.getMethod(clazz), entry);
            }


        } catch (ClassNotFoundException cnfe) {
            throwWireAttachException(source.getUri(), target.getUri(), cnfe);
        } catch (NoSuchMethodException nsme) {
            throwWireAttachException(source.getUri(), target.getUri(), nsme);
        }
        ServiceHandler handler = new ServiceHandler(ops);
        String wsdlElement = source.getWsdlElement();
        String targetNamespace = null;
        String serviceName;
        String portName = null;
        if (wsdlElement != null) {
            int index = wsdlElement.indexOf("#wsdl.port");
            int lastSlashIndex = wsdlElement.lastIndexOf("/");
            targetNamespace = wsdlElement.substring(0, index);
            serviceName = wsdlElement.substring(index + 11, lastSlashIndex);
            portName = wsdlElement.substring(lastSlashIndex + 1, wsdlElement.length()
                    - 1);
        } else {
            serviceName = source.getUri().toString();
        }
        Object proxy = generateProxy(clazz, handler, source, target,
                                     targetNamespace, source.getWsdlLocation(), serviceName, portName);
        try {
            Endpoint.publish(source.getUri().toString(), proxy);
        } catch (Exception e) {
            throwWireAttachException("Unexpected exception", source.getUri(),
                                     target.getUri(), e);
        }
    }


    public void attachObjectFactory(JaxWsWireSourceDefinition source, ObjectFactory<?> objectFactory)
            throws WiringException {
        throw new AssertionError();
    }

    private Object generateProxy(Class interfaceClass, ServiceHandler handler,
                                 JaxWsWireSourceDefinition sourceDefinition,
                                 PhysicalWireTargetDefinition targetDefinition,
                                 String targetNamespace,
                                 String wsdlLocation,
                                 String serviceName,
                                 String portName)
            throws WireAttachException {
        try {
            return generator.getWrapper(interfaceClass,
                                        Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                                                                                   new Class[]{interfaceClass}, handler),
                                        targetNamespace, wsdlLocation, serviceName, portName);
        } catch (ClassNotFoundException cnfe) {
            throwWireAttachException(sourceDefinition.getUri(),
                                     targetDefinition.getUri(), cnfe);
        } catch (IllegalAccessException iae) {
            throwWireAttachException(sourceDefinition.getUri(),
                                     targetDefinition.getUri(), iae);
        } catch (InvocationTargetException ite) {
            throwWireAttachException(sourceDefinition.getUri(),
                                     targetDefinition.getUri(), ite);
        } catch (InstantiationException ie) {
            throwWireAttachException(sourceDefinition.getUri(),
                                     targetDefinition.getUri(), ie);
        }
        return null;
    }


    private void throwWireAttachException(
            URI source, URI target, Exception e) throws WireAttachException {
        throwWireAttachException("Error attaching ws binding source",
                                 source, target, e);
    }

    private void throwWireAttachException(String msg, URI source, URI target,
                                          Exception e) throws WireAttachException {
        throw new WireAttachException(msg, source, target, e);
    }
}
