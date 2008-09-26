/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
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
import java.util.List;
import java.util.Map;

import org.fabric3.binding.codegen.ProxyGenerator;
import org.fabric3.binding.ws.jaxws.provision.JaxWsWireTargetDefinition;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;
import org.osoa.sca.annotations.Reference;

public class JaxWsTargetWireAttacher implements TargetWireAttacher<JaxWsWireTargetDefinition> {

    private static final Map<String, Class<?>> PRIMITIVES_TYPES;

    static {
        PRIMITIVES_TYPES = new HashMap<String, Class<?>>();
        PRIMITIVES_TYPES.put("boolean", Boolean.TYPE);
        PRIMITIVES_TYPES.put("byte", Byte.TYPE);
        PRIMITIVES_TYPES.put("short", Short.TYPE);
        PRIMITIVES_TYPES.put("int", Integer.TYPE);
        PRIMITIVES_TYPES.put("long", Long.TYPE);
        PRIMITIVES_TYPES.put("float", Float.TYPE);
        PRIMITIVES_TYPES.put("double", Double.TYPE);
    }

    private final ClassLoaderRegistry registry;
    private final ProxyGenerator generator;

    public JaxWsTargetWireAttacher(@Reference ClassLoaderRegistry registry, @Reference ProxyGenerator generator) {
        this.registry = registry;
        this.generator = generator;
    }

    public void attachToTarget(PhysicalWireSourceDefinition source, JaxWsWireTargetDefinition target, Wire wire) throws WiringException {

        String wsdlLocation = target.getWsdlLocation();
        String targetNamespace = target.getNamespaceURI();
        String serviceName = target.getServiceName();
        String portName = target.getPortName();
        URI classLoaderURI = target.getClassloaderURI();
        ClassLoader cl = classLoaderURI != null ? registry.getClassLoader(classLoaderURI) : null;
        assert targetNamespace != null && serviceName != null && portName != null;
        try {
            Class<?> clazz = loadClass(cl, target.getReferenceInterface());
            clazz = generator.getWrapperInterface(clazz, targetNamespace, wsdlLocation, serviceName, portName);
            for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry : wire.getInvocationChains().entrySet()) {
                PhysicalOperationDefinition op = entry.getKey();
                WsTargetInterceptor wti = new WsTargetInterceptor(locateMethod(cl, op, clazz), clazz, wsdlLocation, serviceName, portName,
                        targetNamespace);
                InvocationChain chain = entry.getValue();
                chain.addInterceptor(wti);
            }

        } catch (ClassNotFoundException cnfe) {
            AssertionError ae = new AssertionError("Unexpected exception");
            ae.initCause(cnfe);
            throw ae;
        }

    }

    public ObjectFactory<?> createObjectFactory(JaxWsWireTargetDefinition definition) {
        throw new AssertionError();
    }

    private static Method locateMethod(ClassLoader cl, PhysicalOperationDefinition operation, Class<?> clazz) {
        try {
            assert clazz.isInterface();
            List<String> paramsAsString = operation.getParameters();
            Class<?>[] params = new Class[paramsAsString.size()];
            int i = 0;
            for (String str : paramsAsString) {
                params[i++] = loadClass(cl, str);
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

    private static Class<?> loadClass(ClassLoader loader, String className) throws ClassNotFoundException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (loader != null) {
            cl = loader;
        }
        try {
            Class<?> clazz = PRIMITIVES_TYPES.get(className);
            if (clazz != null) {
                return clazz;
            }
            return cl.loadClass(className);
        } catch (ClassNotFoundException cnfe) {
            cl = Thread.currentThread().getContextClassLoader();
            return cl.loadClass(className);
        }

    }
}
