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
package org.fabric3.binding.rmi.wire;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.rmi.model.logical.RmiBindingDefinition;
import org.fabric3.binding.rmi.model.physical.RmiWireTargetDefinition;
import org.fabric3.binding.rmi.transport.RmiTargetInterceptor;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.builder.component.WireAttachException;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

public class RmiTargetWireAttacher implements TargetWireAttacher<RmiWireTargetDefinition> {

    static {
        System.setProperty("java.rmi.server.ignoreStubClasses", "true");
    }

    private final ClassLoaderRegistry classLoaderRegistry;
    private final Map<String, CodeGenClassLoader> classLoaderMap = new WeakHashMap<String, CodeGenClassLoader>(11);

    /**
     * Injects the wire attacher classLoaderRegistry and servlet host.
     *
     * @param classLoaderRegistry the classloader registry for loading application classes
     */
    public RmiTargetWireAttacher(@Reference ClassLoaderRegistry classLoaderRegistry) {
        this.classLoaderRegistry = classLoaderRegistry;
    }

    private Class generateRemoteInterface(String name, URI uri)
            throws IOException, ClassNotFoundException {
        String key = uri.toString();
        CodeGenClassLoader cl = classLoaderMap.get(name);
        MultiParentClassLoader multiParentCL;
        if (cl == null) {
            multiParentCL =
                    (MultiParentClassLoader) classLoaderRegistry.getClassLoader(uri);
            ClassLoader ccl = Thread.currentThread().getContextClassLoader();
            if (ccl != null)
                multiParentCL.addParent(ccl);
            cl = new CodeGenClassLoader(key, multiParentCL);
            classLoaderMap.put(name, cl);
        }
        String resourceName = name.replace('.', '/') + ".class";
        return InterfacePreProcessor.generateRemoteInterface(name,
                                                             cl.getResourceAsStream(resourceName), cl);

    }

    public void attachToTarget(PhysicalWireSourceDefinition sourceDefinition,
                               RmiWireTargetDefinition targetDefn,
                               Wire wire) throws WiringException {
        RmiBindingDefinition defn = targetDefn.getBindingDefinition();
        RmiReferenceFactory referenceFactory = new RmiReferenceFactory(defn.getServiceName(), defn.getHost(), defn.getPort());
        try {
            Class clazz = generateRemoteInterface(targetDefn.getInterfaceName(), targetDefn.getClassLoaderURI());
            for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry : wire.getInvocationChains().entrySet()) {
                PhysicalOperationDefinition op = entry.getKey();
                RmiTargetInterceptor eti = new RmiTargetInterceptor(locateMethod(op, clazz), referenceFactory);
                InvocationChain chain = entry.getValue();
                chain.addInterceptor(eti);
            }
        } catch (IOException ioe) {
            StringBuilder sb = new StringBuilder("Error resolving Rmi binding service interface ");
            sb.append(targetDefn.getInterfaceName()).append(" using ");
            sb.append(targetDefn.getClassLoaderURI().toString());
            throw new WireAttachException(sb.toString(), null, null, ioe);
        } catch (ClassNotFoundException e) {
            throw new WireAttachException("Class not found", sourceDefinition.getUri(), targetDefn.getUri(), e);
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
                if (str.equals("double")) {
                    params[i++] = Double.TYPE;
                } else {
                    params[i++] = cl.loadClass(str);
                }
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

    public ObjectFactory<?> createObjectFactory(RmiWireTargetDefinition target) throws WiringException {
        throw new AssertionError();
    }
}