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
package org.fabric3.binding.ejb.wire;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Remotable;

import org.fabric3.binding.ejb.model.logical.EjbBindingDefinition;
import org.fabric3.binding.ejb.model.physical.EjbWireSourceDefinition;
import org.fabric3.binding.ejb.model.physical.EjbWireTargetDefinition;
import org.fabric3.binding.ejb.spi.EjbRegistry;
import org.fabric3.binding.ejb.transport.EjbHomeServiceHandler;
import org.fabric3.binding.ejb.transport.EjbServiceHandler;
import org.fabric3.binding.ejb.transport.EjbTargetInterceptorFactory;
import org.fabric3.binding.rmi.wire.WireProxyGenerator;
import org.fabric3.pojo.instancefactory.Signature;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.builder.component.SourceWireAttacherRegistry;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.builder.component.TargetWireAttacherRegistry;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.deployer.CompositeClassLoader;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;


/**
 * Wire attacher for EJB binding.
 *
 * @version $Revision: 1 $ $Date: 2007-05-14 10:40:37 -0700 (Mon, 14 May 2007) $
 */
@EagerInit
public class EjbWireAttacher implements SourceWireAttacher<EjbWireSourceDefinition>, TargetWireAttacher<EjbWireTargetDefinition> {
    private final SourceWireAttacherRegistry sourceWireAttacherRegistry;
    private final TargetWireAttacherRegistry targetWireAttacherRegistry;
    private final EjbRegistry ejbRegistry;
    private final ClassLoaderRegistry classLoaderRegistry;
    private final ScopeRegistry scopeRegistry;
    private ClassLoader cl;

    /**
     * Injects the wire attacher classLoaderRegistry and servlet host.
     */
    public EjbWireAttacher(@Reference SourceWireAttacherRegistry sourceWireAttacherRegistry,
                           @Reference TargetWireAttacherRegistry targetWireAttacherRegistry,
                           @Reference ClassLoaderRegistry classLoaderRegistry,
                           @Reference EjbRegistry ejbRegistry,
                           @Reference ScopeRegistry scopeRegistry) {
        this.sourceWireAttacherRegistry = sourceWireAttacherRegistry;
        this.targetWireAttacherRegistry = targetWireAttacherRegistry;
        this.ejbRegistry = ejbRegistry;
        this.classLoaderRegistry = classLoaderRegistry;
        this.scopeRegistry = scopeRegistry;
    }

    @Init
    public void start() {
        sourceWireAttacherRegistry.register(EjbWireSourceDefinition.class, this);
        targetWireAttacherRegistry.register(EjbWireTargetDefinition.class, this);
    }

    @Destroy
    public void stop() {
        sourceWireAttacherRegistry.unregister(EjbWireSourceDefinition.class, this);
        targetWireAttacherRegistry.unregister(EjbWireTargetDefinition.class, this);
    }

    public void attachToSource(EjbWireSourceDefinition sourceDefinition,
                               PhysicalWireTargetDefinition targetDefinition,
                               Wire wire) throws WiringException {


        Map<Signature, Map.Entry<PhysicalOperationDefinition, InvocationChain>> ops =
                new HashMap<Signature, Map.Entry<PhysicalOperationDefinition, InvocationChain>>();

        for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry : wire.getInvocationChains().entrySet()) {
            Signature signature = new Signature(entry.getKey().getName(), entry.getKey().getParameters());
            ops.put(signature, entry);
        }

        Object ejbFacade = null;
        if (sourceDefinition.getBindingDefinition().isEjb3()) {
            ejbFacade = generateEjb3Facade(sourceDefinition, wire, ops);
        } else {
            ejbFacade = generateEjb2Facade(sourceDefinition, wire, ops);
        }

        URI uri = sourceDefinition.getBindingDefinition().getTargetUri();
        if (uri != null) {
            ejbRegistry.registerEjb(uri, ejbFacade);
        }

        String ejbLinkName = sourceDefinition.getBindingDefinition().getEjbLink();
        if (ejbLinkName != null) {
            ejbRegistry.registerEjbLink(ejbLinkName, ejbFacade);
        }

    }

    private Object generateEjb3Facade(EjbWireSourceDefinition sourceDefinition, Wire wire,
                                      Map<Signature, Map.Entry<PhysicalOperationDefinition, InvocationChain>> ops)
            throws WiringException {

        Class interfaceClass = loadClass(sourceDefinition.getInterfaceName(), sourceDefinition.getClassLoaderURI());

        EjbServiceHandler handler = new EjbServiceHandler(wire, ops);

        Object proxy = Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                                              new Class[]{interfaceClass}, handler);

        if (interfaceClass.isAnnotationPresent(Remotable.class)) proxy = generateRemoteWrapper(interfaceClass, proxy);

        return proxy;
    }

    private Object generateEjb2Facade(EjbWireSourceDefinition sourceDefinition, Wire wire,
                                      Map<Signature, Map.Entry<PhysicalOperationDefinition, InvocationChain>> ops)
            throws WiringException {

        String homeInterface = sourceDefinition.getBindingDefinition().getHomeInterface();
        if (homeInterface == null) {
            throw new WiringException("Ejb 2.x bindings on services must specify a home interface name");
        }

        Class homeInterfaceClass = loadClass(homeInterface, sourceDefinition.getClassLoaderURI());

        // For 2.x beans, the EJBObject interface is not necessarily an interface implemented by the POJO
        // Rather than using the service interface from the implementation, use the EJBObject interface
        Class interfaceClass = null;
        for (Method m : homeInterfaceClass.getMethods()) {
            if (m.getName().startsWith("create")) {
                interfaceClass = m.getReturnType();
                break;
            }
        }

        //TODO: we really need an EJB2ServiceHandler to deal with calls to EJBObject
        EjbServiceHandler handler = new EjbServiceHandler(wire, ops);

        Object proxy = Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                                              new Class[]{interfaceClass}, handler);

        boolean isRemote = javax.ejb.EJBHome.class.isAssignableFrom(homeInterfaceClass);

        if (isRemote) proxy = generateRemoteWrapper(interfaceClass, proxy);

        EjbHomeServiceHandler homeHandler = new EjbHomeServiceHandler(proxy);

        proxy = Proxy.newProxyInstance(homeInterfaceClass.getClassLoader(),
                                       new Class[]{homeInterfaceClass}, homeHandler);
        if (isRemote) proxy = generateRemoteWrapper(homeInterfaceClass, proxy);

        return proxy;
    }

    private Object generateRemoteWrapper(Class interfaceClass, Object delegate)
            throws WiringException {

        WireProxyGenerator wpg = WireProxyGenerator.getInstance();
        try {
            return wpg.generateRemoteWrapper(interfaceClass, delegate);
        } catch (Exception e) {
            throw new WiringException(e);
        }

    }

    public void attachToTarget(PhysicalWireSourceDefinition sourceDefinition,
                               EjbWireTargetDefinition targetDefinition,
                               Wire wire) throws WiringException {

        Class interfaceClass = loadClass(targetDefinition.getInterfaceName(), targetDefinition.getClassLoaderURI());
        EjbResolver resolver = new EjbResolver(targetDefinition, ejbRegistry, interfaceClass);
        EjbBindingDefinition bd = targetDefinition.getBindingDefinition();
        EjbTargetInterceptorFactory interceptorFactory =
                new EjbTargetInterceptorFactory(bd, resolver, scopeRegistry, getId(bd));

        for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry : wire.getInvocationChains().entrySet()) {
            PhysicalOperationDefinition op = entry.getKey();
            Signature signature = new Signature(op.getName(), op.getParameters());

            Interceptor targetInterceptor = interceptorFactory.getEjbTargetInterceptor(signature);
            InvocationChain chain = entry.getValue();
            chain.addInterceptor(targetInterceptor);
        }


    }

    private URI getId(EjbBindingDefinition bd)
            throws WiringException {
        try {
            return bd.getTargetUri() != null ? bd.getTargetUri() : new URI(bd.getEjbLink());
        } catch (URISyntaxException se) {
            throw new WiringException(se);
        }
    }

    private Class loadClass(String name, URI classLoaderURI)
            throws WiringException {

        if (cl == null) {
            CompositeClassLoader compositeCL =
                    (CompositeClassLoader) classLoaderRegistry.getClassLoader(classLoaderURI);
            ClassLoader ccl = Thread.currentThread().getContextClassLoader();
            if (ccl != null) {
                compositeCL.addParent(ccl);
            }
            cl = compositeCL;
        }

        try {
            return cl.loadClass(name);
        } catch (ClassNotFoundException cnfe) {
            throw new WiringException(cnfe);
        }

    }


}
