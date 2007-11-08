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

package org.fabric3.binding.ws.cxf.wire;

import java.net.URI;

import org.fabric3.binding.ws.cxf.CXFService;
import org.fabric3.binding.ws.cxf.physical.CxfWireSourceDefinition;
import org.fabric3.binding.ws.cxf.physical.CxfWireTargetDefinition;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.WireAttacher;
import org.fabric3.spi.builder.component.WireAttacherRegistry;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.wire.Wire;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

/**
 * Wire attacher for web services.
 *
 * @version $Revision: 1589 $ $Date: 2007-10-25 23:13:37 +0100 (Thu, 25 Oct 2007) $
 */
@EagerInit
public class CxfWireAttacher implements WireAttacher<CxfWireSourceDefinition, CxfWireTargetDefinition> {

    /*
         Force initialization of CXF's StAXUtil class using our classloader (which should also be the TCCL).
         StAXUtil loads and caches an XmlInputFactory loaded from the TCCL.
    */
    static {

        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        ClassLoader cl = CxfWireAttacher.class.getClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(cl);
            Class.forName("org.apache.cxf.tools.util.StAXUtil", true, cl);
            Class.forName("org.apache.cxf.staxutils.StaxUtils", true, cl);
        } catch (ClassNotFoundException e) {
            throw new AssertionError();
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }

    private ClassLoaderRegistry classLoaderRegistry;
    private CXFService cxfService;

    /**
     * Injects the wire attacher registry and servlet host.
     *
     * @param wireAttacherRegistry Wire attacher registry.
     * @param classLoaderRegistry  the classloader registry
     * @param cxfService           the CXF service
     */
    public CxfWireAttacher(@Reference WireAttacherRegistry wireAttacherRegistry,
                          @Reference ClassLoaderRegistry classLoaderRegistry,
                          @Reference CXFService cxfService) {
        this.classLoaderRegistry = classLoaderRegistry;
        this.cxfService = cxfService;
        wireAttacherRegistry.register(CxfWireSourceDefinition.class, this);
        wireAttacherRegistry.register(CxfWireTargetDefinition.class, this);
    }


    public void attachToSource(CxfWireSourceDefinition sourceDefinition,
                               PhysicalWireTargetDefinition targetDefinition,
                               Wire wire) throws WiringException {

        Thread currentThread = Thread.currentThread();
        ClassLoader oldCl = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(getClass().getClassLoader());
        try {
            URI classLoaderUri = sourceDefinition.getClassloaderURI();
            ClassLoader loader = classLoaderRegistry.getClassLoader(classLoaderUri);
            if (loader == null) {
                throw new ClassLoaderNotFoundException("Classloader not defined", classLoaderUri.toString());
            }
            Class<?> service = loader.loadClass(sourceDefinition.getServiceInterface());
            URI uri = sourceDefinition.getUri();
            // provision the bound service as a Web Service endpoint
            cxfService.provisionEndpoint(uri, service, wire);
        } catch (ClassNotFoundException e) {
            throw new WiringException(e);
        } finally {
            currentThread.setContextClassLoader(oldCl);
        }

    }

    public void attachToTarget(PhysicalWireSourceDefinition sourceDefinition,
                               CxfWireTargetDefinition targetDefinition,
                               Wire wire) throws WiringException {

        Thread currentThread = Thread.currentThread();
        ClassLoader oldCl = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(getClass().getClassLoader());

        try {
            URI classLoaderUri = targetDefinition.getClassloaderURI();
            ClassLoader loader = classLoaderRegistry.getClassLoader(classLoaderUri);
            if (loader == null) {
                throw new ClassLoaderNotFoundException("Classloader not defined", classLoaderUri.toString());
            }

            Class<?> referenceClass = loader.loadClass(targetDefinition.getReferenceInterface());

//            ClientProxyFactoryBean factory = new ClientProxyFactoryBean();
//            factory.setServiceClass(referenceClass);
//            factory.setAddress(targetDefinition.getUri().toString());
//
//
//            Object proxy = factory.create();
            cxfService.bindToTarget(targetDefinition.getUri(), referenceClass, wire);
//            for (Method method : referenceClass.getDeclaredMethods()) {
//                for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry : wire.getInvocationChains().entrySet()) {
//                    PhysicalOperationDefinition op = entry.getKey();
//                    InvocationChain chain = entry.getValue();
//                    if (method.getName().equals(op.getName())) {
//                        chain.addInterceptor(new WsTargetInterceptor(method, proxy));
//                    }
//                }
//            }

        } catch (ClassNotFoundException e) {
            throw new WiringException(e);
        } finally {
            currentThread.setContextClassLoader(oldCl);
        }

    }

}
