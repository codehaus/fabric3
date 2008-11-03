/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
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
            URI uri = source.getClassLoaderId();
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

    public void detachFromSource(JaxWsWireSourceDefinition source, PhysicalWireTargetDefinition target) throws WiringException {
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
