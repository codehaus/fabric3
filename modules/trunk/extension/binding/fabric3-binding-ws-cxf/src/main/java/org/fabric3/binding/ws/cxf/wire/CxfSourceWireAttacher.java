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

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.ws.cxf.CXFService;
import org.fabric3.binding.ws.cxf.physical.CxfWireSourceDefinition;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.builder.component.SourceWireAttacherRegistry;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.wire.Wire;

/**
 * Wire attacher for web services.
 *
 * @version $Revision: 1589 $ $Date: 2007-10-25 23:13:37 +0100 (Thu, 25 Oct 2007) $
 */
@EagerInit
public class CxfSourceWireAttacher implements SourceWireAttacher<CxfWireSourceDefinition> {

    private final SourceWireAttacherRegistry sourceWireAttacherRegistry;
    private final ClassLoaderRegistry classLoaderRegistry;
    private final CXFService cxfService;

    /**
     * Injects the wire attacher registry and servlet host.
     *
     * @param sourceWireAttacherRegistry the registry for source wire attachers
     * @param classLoaderRegistry        the classloader registry
     * @param cxfService                 the CXF service
     */
    public CxfSourceWireAttacher(@Reference SourceWireAttacherRegistry sourceWireAttacherRegistry,
                                 @Reference ClassLoaderRegistry classLoaderRegistry,
                                 @Reference CXFService cxfService) {
        this.sourceWireAttacherRegistry = sourceWireAttacherRegistry;
        this.classLoaderRegistry = classLoaderRegistry;
        this.cxfService = cxfService;
    }


    @Init
    public void start() {
        sourceWireAttacherRegistry.register(CxfWireSourceDefinition.class, this);
    }

    @Destroy
    public void stop() {
        sourceWireAttacherRegistry.unregister(CxfWireSourceDefinition.class, this);
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

    public void attachObjectFactory(CxfWireSourceDefinition source, ObjectFactory<?> objectFactory) throws WiringException {
        throw new AssertionError();
    }
}
