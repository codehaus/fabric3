package org.fabric3.binding.ws.jaxws.runtime;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.binding.ws.jaxws.provision.JaxWsWireSourceDefinition;
import org.fabric3.scdl.Signature;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.builder.component.WireAttachException;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;
import org.osoa.sca.annotations.Reference;


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
    private final JaxWsServiceProvisioner provisioner;

    public JaxWsSourceWireAttacher(@Reference ClassLoaderRegistry registry,
                                   @Reference JaxWsServiceProvisioner provisioner) {
        this.registry = registry;
        this.provisioner = provisioner;
    }

    public void attachToSource(JaxWsWireSourceDefinition source,
                               PhysicalWireTargetDefinition target, Wire wire)
            throws WiringException {
        Class<?> clazz = null;
        Map<Method, Map.Entry<PhysicalOperationDefinition, InvocationChain>> ops =
                new HashMap<Method, Map.Entry<PhysicalOperationDefinition, InvocationChain>>();
        try {
            URI uri = source.getClassloaderURI();
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (uri != null) {
                cl = registry.getClassLoader(uri);
            }
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
        provisioner.provision(clazz, handler, source, target.getUri());
    }

    public void detachFromSource(JaxWsWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire) throws WiringException {
        provisioner.unprovision(source, target.getUri());
    }


    public void attachObjectFactory(JaxWsWireSourceDefinition source, ObjectFactory<?> objectFactory, PhysicalWireTargetDefinition definition)
            throws WiringException {
        throw new AssertionError();
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
