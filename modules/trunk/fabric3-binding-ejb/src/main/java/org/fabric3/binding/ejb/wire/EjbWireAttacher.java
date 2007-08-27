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
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.net.URI;
import java.io.IOException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.osoa.sca.annotations.Remotable;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.EagerInit;

import org.fabric3.binding.ejb.model.physical.EjbWireSourceDefinition;
import org.fabric3.binding.ejb.model.physical.EjbWireTargetDefinition;
import org.fabric3.binding.ejb.transport.Ejb3StatelessTargetInterceptor;
import org.fabric3.binding.ejb.transport.EjbHomeServiceHandler;
import org.fabric3.binding.ejb.transport.EjbServiceHandler;
import org.fabric3.binding.ejb.spi.EjbLinkResolver;
import org.fabric3.binding.ejb.spi.EjbLinkException;
import org.fabric3.pojo.instancefactory.Signature;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.WireAttachException;
import org.fabric3.spi.builder.component.WireAttacher;
import org.fabric3.spi.builder.component.WireAttacherRegistry;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * Wire attacher for EJB binding.
 *
 * @version $Revision: 1 $ $Date: 2007-05-14 10:40:37 -0700 (Mon, 14 May 2007) $
 */
@EagerInit
public class EjbWireAttacher implements WireAttacher<EjbWireSourceDefinition, EjbWireTargetDefinition> {

    private final EjbLinkResolver ejbLinkResolver;
    private final ClassLoaderRegistry registry;

    /**
     * Injects the wire attacher registry and servlet host.
     *
     * @param wireAttacherRegistry Wire attacher rehistry.
     *
     */
    public EjbWireAttacher(@Reference WireAttacherRegistry wireAttacherRegistry,
                           @Reference ClassLoaderRegistry registry,
                           @Reference EjbLinkResolver ejbLinkResolver) {
        wireAttacherRegistry.register(EjbWireSourceDefinition.class, this);
        wireAttacherRegistry.register(EjbWireTargetDefinition.class, this);
        this.ejbLinkResolver = ejbLinkResolver;
        this.registry = registry;
    }

    /**
     * @see org.fabric3.spi.builder.component.WireAttacher#attachToSource(
     *      org.fabric3.spi.model.physical.PhysicalWireSourceDefinition,
     *      org.fabric3.spi.model.physical.PhysicalWireTargetDefinition, org.fabric3.spi.wire.Wire)
     */
    public void attachToSource(EjbWireSourceDefinition sourceDefinition,
                               PhysicalWireTargetDefinition targetDefinition,
                               Wire wire) throws WiringException {
        
        
        Class interfaceClass = loadClass(sourceDefinition.getInterfaceName(), sourceDefinition.getClassLoaderURI());

        Map<Method, Map.Entry<PhysicalOperationDefinition, InvocationChain>> ops =
                new HashMap<Method, Map.Entry<PhysicalOperationDefinition, InvocationChain>>();

        for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry : wire.getInvocationChains().entrySet()) {
            Signature signature = new Signature(entry.getKey().getName(), entry.getKey().getParameters());
            try {
                ops.put(signature.getMethod(interfaceClass), entry);
            } catch (ClassNotFoundException cnfe) {
                throw new WireAttachException("Error attaching EJB binding source",
                                          sourceDefinition.getUri(), targetDefinition.getUri(), cnfe);
            } catch (NoSuchMethodException nsme) {
                throw new WireAttachException("Error attaching EJB binding source",
                                          sourceDefinition.getUri(), targetDefinition.getUri(), nsme);
            }
        }

        EjbServiceHandler handler = new EjbServiceHandler(wire, ops);

        //TODO: We need to piggy back off of the binding.rmi logic when it's written.  In the meantime, this
        // will only work for local objects
        Object proxy = Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                                              new Class[] {interfaceClass}, handler);

        String homeInterface = sourceDefinition.getBindingDefinition().getHomeInterface();
        if(homeInterface != null) {
            EjbHomeServiceHandler homeHandler = new EjbHomeServiceHandler(proxy);
            try {
                Class homeInterfaceClass = Class.forName(homeInterface, true, interfaceClass.getClassLoader());
                proxy = Proxy.newProxyInstance(homeInterfaceClass.getClassLoader(),
                                               new Class[] {homeInterfaceClass}, homeHandler);
            } catch(ClassNotFoundException cnfe) {
                throw new WireAttachException("Error attaching EJB binding source",
                                              sourceDefinition.getUri(), targetDefinition.getUri(), cnfe);  
            }
        }

        String jndiName = sourceDefinition.getBindingDefinition().getJndiName();
        if (jndiName != null) {
            try {
                InitialContext ic = new InitialContext();
                ic.bind(jndiName, proxy);
            } catch (NamingException ne) {
                throw new WireAttachException("Error binding EJB binding to JNDI name: " + jndiName,
                                              sourceDefinition.getUri(), targetDefinition.getUri(), ne);
            }
        }

        String ejbLinkName = sourceDefinition.getBindingDefinition().getEjbLink();
        if(ejbLinkName != null) {
            try {
                ejbLinkResolver.registerEjbLink(ejbLinkName, proxy);
            } catch(EjbLinkException ele) {
                throw new WireAttachException("Error registering ejb-link-name: " + ejbLinkName,
                                              sourceDefinition.getUri(), targetDefinition.getUri(), ele);
            }
        }

    }

    /**
     * @see org.fabric3.spi.builder.component.WireAttacher#attachToTarget(
     *      org.fabric3.spi.model.physical.PhysicalWireSourceDefinition,
     *      org.fabric3.spi.model.physical.PhysicalWireTargetDefinition, org.fabric3.spi.wire.Wire)
     */
    public void attachToTarget(PhysicalWireSourceDefinition sourceDefinition,
                               EjbWireTargetDefinition targetDefinition,
                               Wire wire) throws WiringException {
        EjbReferenceFactory referenceFactory = new EjbReferenceFactory(targetDefinition, ejbLinkResolver);
        for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry : wire.getInvocationChains().entrySet()) {
            PhysicalOperationDefinition op = entry.getKey();
            Ejb3StatelessTargetInterceptor eti =
                    new Ejb3StatelessTargetInterceptor(op.getName(), referenceFactory);
            InvocationChain chain = entry.getValue();
            chain.addInterceptor(eti);
        }


    }

    private Class loadClass(String name, URI classLoaderURI)
            throws WiringException {

        try {
            ClassLoader cl = registry.getClassLoader(classLoaderURI);
            return cl.loadClass(name);
        } catch(ClassNotFoundException cnfe) {
            throw new WiringException(cnfe);
        }

    }


}
