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
package org.fabric3.binding.ejb.runtime;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.ejb.provision.EjbWireTargetDefinition;
import org.fabric3.binding.ejb.scdl.EjbBindingDefinition;
import org.fabric3.scdl.Signature;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;


/**
 * Wire attacher for EJB binding.
 *
 * @version $Revision: 1 $ $Date: 2007-05-14 10:40:37 -0700 (Mon, 14 May 2007) $
 */
public class EjbTargetWireAttacher implements TargetWireAttacher<EjbWireTargetDefinition> {
    private final EjbRegistry ejbRegistry;
    private final ClassLoaderRegistry classLoaderRegistry;
    private final ScopeRegistry scopeRegistry;
    private ClassLoader cl;

    /**
     * Injects the wire attacher classLoaderRegistry and servlet host.
     */
    public EjbTargetWireAttacher(@Reference ClassLoaderRegistry classLoaderRegistry,
                                 @Reference EjbRegistry ejbRegistry,
                                 @Reference ScopeRegistry scopeRegistry) {
        this.ejbRegistry = ejbRegistry;
        this.classLoaderRegistry = classLoaderRegistry;
        this.scopeRegistry = scopeRegistry;
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
            MultiParentClassLoader multiParentCL =
                    (MultiParentClassLoader) classLoaderRegistry.getClassLoader(classLoaderURI);
            ClassLoader ccl = Thread.currentThread().getContextClassLoader();
            if (ccl != null) {
                multiParentCL.addParent(ccl);
            }
            cl = multiParentCL;
        }

        try {
            return cl.loadClass(name);
        } catch (ClassNotFoundException cnfe) {
            throw new WiringException(cnfe);
        }

    }

    public ObjectFactory<?> createObjectFactory(EjbWireTargetDefinition target) throws WiringException {
        throw new AssertionError();
    }
}