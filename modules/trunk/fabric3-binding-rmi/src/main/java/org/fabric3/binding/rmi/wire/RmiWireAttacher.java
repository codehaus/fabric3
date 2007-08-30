/*
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
package org.fabric3.binding.rmi.wire;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.rmi.model.logical.RmiBindingDefinition;
import org.fabric3.binding.rmi.model.physical.RmiWireSourceDefinition;
import org.fabric3.binding.rmi.model.physical.RmiWireTargetDefinition;
import org.fabric3.binding.rmi.transport.RmiServiceHandler;
import org.fabric3.binding.rmi.transport.RmiTargetInterceptor;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.WireAttachException;
import org.fabric3.spi.builder.component.WireAttacher;
import org.fabric3.spi.builder.component.WireAttacherRegistry;
import org.fabric3.spi.deployer.CompositeClassLoader;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

@EagerInit
public class RmiWireAttacher implements
        WireAttacher<RmiWireSourceDefinition, RmiWireTargetDefinition> {


    static {
        System.setProperty("java.rmi.server.ignoreStubClasses", "true");
    }

    private final ClassLoaderRegistry registry;
    private final Map<String, CodeGenClassLoader> classLoaderMap =
            new WeakHashMap<String, CodeGenClassLoader>(11);
    private static final WireProxyGenerator PROXY_GENERATOR =
            WireProxyGenerator.getInstance();
    private final Map<Integer, Registry> registryMap =
            new ConcurrentHashMap<Integer, Registry>(11);
    private final Map<String, Remote> remoteObjects =
            new ConcurrentHashMap<String, Remote>(11);

    /**
     * Injects the wire attacher registry and servlet host.
     *
     * @param wireAttacherRegistry Wire attacher registry.
     */
    public RmiWireAttacher(@Reference WireAttacherRegistry wireAttacherRegistry
            , @Reference ClassLoaderRegistry registry) {
        wireAttacherRegistry.register(RmiWireSourceDefinition.class, this);
        wireAttacherRegistry.register(RmiWireTargetDefinition.class, this);
        this.registry = registry;
    }

    public void attachToSource(RmiWireSourceDefinition sourceDefinition,
                               PhysicalWireTargetDefinition targetDefinition,
                               Wire wire) throws WiringException {

        Map<Method, Map.Entry<PhysicalOperationDefinition, InvocationChain>> ops =
                new HashMap<Method, Map.Entry<PhysicalOperationDefinition, InvocationChain>>();
        Class interfaceClass = null;
        try {
            String interfaceName = sourceDefinition.getInterfaceName();
            interfaceClass = generateRemoteInterface(
                    interfaceName, sourceDefinition.getClassLoaderURI());
            for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry :
                    wire.getInvocationChains().entrySet()) {

                Signature signature = new Signature(
                        entry.getKey().getName(), entry.getKey().getParameters());
                ops.put(signature.getMethod(interfaceClass), entry);
            }
        } catch (IOException ioe) {
            throwWireAttachException(sourceDefinition.getUri(), targetDefinition.getUri(), ioe);
        } catch (ClassNotFoundException cnfe) {
            throwWireAttachException(sourceDefinition.getUri(), targetDefinition.getUri(), cnfe);
        } catch (NoSuchMethodException nsme) {
            throwWireAttachException(sourceDefinition.getUri(), targetDefinition.getUri(), nsme);
        }
        RmiServiceHandler handler = new RmiServiceHandler(wire, ops);
        Remote proxy = generateProxy(interfaceClass,
                                     handler,
                                     sourceDefinition.getUri(), targetDefinition.getUri());


        String serviceName =
                sourceDefinition.getBindingDefinition().getServiceName();
        int port = sourceDefinition.getBindingDefinition().getPort();
        if (serviceName != null) {
            Registry registry = null;
            try {
                registry = findOrCreateRegistry(port);
                Remote stub = UnicastRemoteObject.exportObject(proxy, port);
                registry.rebind(serviceName, stub);
                //TODO We should have a way to remove objects from map upon undeploy
                remoteObjects.put(serviceName, proxy);
            } catch (RemoteException ne) {
                throw new WireAttachException("Error binding Rmi binding to JNDI name: " + serviceName,
                                              sourceDefinition.getUri(), targetDefinition.getUri(), ne);
            }
        }


    }

    private Class generateRemoteInterface(String name, URI uri)
            throws IOException {
        String key = uri.toString();
        CodeGenClassLoader cl = classLoaderMap.get(name);
        CompositeClassLoader compositeCL;
        if (cl == null) {
            compositeCL =
                    (CompositeClassLoader) registry.getClassLoader(uri);
            ClassLoader ccl = Thread.currentThread().getContextClassLoader();
            if (ccl != null)
                compositeCL.addParent(ccl);
            cl = new CodeGenClassLoader(key, compositeCL);
            classLoaderMap.put(name, cl);
        }
        String resourceName = name.replace('.', '/') + ".class";
        return InterfacePreProcessor.generateRemoteInterface(name,
                                                             cl.getResourceAsStream(resourceName), cl);

    }

    private Registry findOrCreateRegistry(int port) throws RemoteException {
        Registry r = null;
        try {
            Integer portAsInterger = new Integer(port);
            r = registryMap.get(portAsInterger);
            if (r == null) {
                r = LocateRegistry.createRegistry(port);
                registryMap.put(portAsInterger, r);
            }
        } catch (RemoteException re) {

        }
        return r;
    }

    private Remote generateProxy(Class clazz, RmiServiceHandler handler,
                                 URI source,
                                 URI target) throws WireAttachException {
        try {
            return (Remote) PROXY_GENERATOR.generateRemoteWrapper(clazz,
                                                                  Proxy.newProxyInstance(
                                                                          clazz.getClassLoader(),
                                                                          new Class[]{clazz},
                                                                          handler));
        } catch (ClassNotFoundException cnfe) {
            throwWireAttachException(source, target, cnfe);
        } catch (IllegalAccessException iae) {
            throwWireAttachException(source, target, iae);
        } catch (InvocationTargetException ite) {
            throwWireAttachException(source, target, ite);
        } catch (InstantiationException ie) {
            throwWireAttachException(source, target, ie);
        }
        return null;
    }


    public void attachToTarget(PhysicalWireSourceDefinition sourceDefinition,
                               RmiWireTargetDefinition targetDefn,
                               Wire wire) throws WiringException {
        RmiBindingDefinition defn = targetDefn.getBindingDefinition();
        RmiReferenceFactory referenceFactory =
                new RmiReferenceFactory(defn.getServiceName(),
                                        defn.getHost(), defn.getPort());
        try {
            Class clazz = generateRemoteInterface(targetDefn.getInterfaceName(),
                                                  targetDefn.getClassLoaderURI());
            for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry :
                    wire.getInvocationChains().entrySet()) {
                PhysicalOperationDefinition op = entry.getKey();
                RmiTargetInterceptor eti =
                        new RmiTargetInterceptor(locateMethod(
                                op, clazz), referenceFactory);
                InvocationChain chain = entry.getValue();
                chain.addInterceptor(eti);
            }
        } catch (IOException ioe) {
            StringBuilder sb = new StringBuilder("Error resolving Rmi binding service interface ");
            sb.append(targetDefn.getInterfaceName()).append(" using ");
            sb.append(targetDefn.getClassLoaderURI().toString());
            throw new WireAttachException(sb.toString(), null, null, ioe);
        }
    }

    private static Method locateMethod(PhysicalOperationDefinition operation,
                                       Class clazz) {
        assert clazz.isInterface();
        ClassLoader cl = clazz.getClassLoader();
        List<String> paramsAsString = operation.getParameters();
        Class[] params = new Class[paramsAsString.size()];
        int i = 0;
        try {
            for (String str : paramsAsString) {
                params[i++] = cl.loadClass(str);
            }
            return clazz.getMethod(operation.getName(), params);
        } catch (NoSuchMethodException e) {
            AssertionError we = new AssertionError("Failed to match operation " + operation.getName());
            we.initCause(e);
            throw we;
        } catch (ClassNotFoundException cnfe) {
            AssertionError we = new AssertionError("Failed to match operation " + operation.getName());
            we.initCause(cnfe);
            throw we;
        }
    }

    private void throwWireAttachException(
            URI source, URI target, Exception e) throws WireAttachException {
        throwWireAttachException("Error attaching Rmi binding source",
                                 source, target, e);
    }

    private void throwWireAttachException(String msg, URI source, URI target,
                                          Exception e) throws WireAttachException {
        WireAttachException we =
                new WireAttachException(msg,
                                        source, target, e);
        throw we;
    }
}
