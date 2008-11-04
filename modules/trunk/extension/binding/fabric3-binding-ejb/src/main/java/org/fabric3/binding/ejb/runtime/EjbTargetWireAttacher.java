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

    public void detachFromTarget(PhysicalWireSourceDefinition source, EjbWireTargetDefinition target) throws WiringException {
        throw new AssertionError();
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