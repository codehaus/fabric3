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
package org.fabric3.rs.runtime;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.rs.provision.RsWireSourceDefinition;
import org.fabric3.rs.runtime.rs.RsWebApplication;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.builder.component.WireAttachException;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.host.ServletHost;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class RsSourceWireAttacher implements SourceWireAttacher<RsWireSourceDefinition> {

    private final ClassLoaderRegistry classLoaderRegistry;
    private RsWireAttacherMonitor monitor;
    private final ServletHost servletHost;
    private final Map<URI, RsWebApplication> webApplications = new ConcurrentHashMap<URI, RsWebApplication>();

    public RsSourceWireAttacher(@Reference ServletHost servletHost, @Reference ClassLoaderRegistry registry, @Monitor RsWireAttacherMonitor monitor) {
        this.servletHost = servletHost;
        this.classLoaderRegistry = registry;
        this.monitor = monitor;
    }

    @Init
    public void init() {
        monitor.extensionStarted();
    }

    @Destroy
    public void destroy() {
        monitor.extensionStopped();
    }

    public void attachToSource(RsWireSourceDefinition sourceDefinition, PhysicalWireTargetDefinition targetDefinition, Wire wire)
            throws WireAttachException {

        URI sourceUri = sourceDefinition.getUri();

        RsWebApplication application = webApplications.get(sourceUri);
        if (application == null) {
            application = new RsWebApplication(getClass().getClassLoader());
            webApplications.put(sourceUri, application);
            String mapping = creatingMappingUri(sourceUri);
            if (servletHost.isMappingRegistered(mapping)) {
                // wire reprovisioned
                servletHost.unregisterMapping(mapping);
            }
            servletHost.registerMapping(mapping, application);
        }

        try {
            provision(sourceDefinition, wire, application);
            monitor.provisionedEndpoint(sourceUri);
        } catch (ClassNotFoundException e) {
            String name = sourceDefinition.getInterfaceName();
            throw new WireAttachException("Unable to load interface class " + name, sourceUri, null, e);
        }

    }

    public void detachFromSource(RsWireSourceDefinition source, PhysicalWireTargetDefinition target) throws WiringException {
        URI uri = source.getUri();
        String mapping = creatingMappingUri(uri);
        servletHost.unregisterMapping(mapping);
        monitor.removedEndpoint(source.getUri());
    }

    public void attachObjectFactory(RsWireSourceDefinition source, ObjectFactory<?> objectFactory, PhysicalWireTargetDefinition target)
            throws WiringException {
        throw new AssertionError();
    }

    public void detachObjectFactory(RsWireSourceDefinition source, PhysicalWireTargetDefinition target) throws WiringException {
        throw new AssertionError();
    }

    private String creatingMappingUri(URI sourceUri) {
        String servletMapping = sourceUri.getPath();
        if (!servletMapping.endsWith("/*")) {
            servletMapping = servletMapping + "/*";
        }
        return servletMapping;
    }

    private void provision(RsWireSourceDefinition sourceDefinition, Wire wire, RsWebApplication application) throws ClassNotFoundException {

        ClassLoader classLoader = classLoaderRegistry.getClassLoader(sourceDefinition.getClassLoaderId());

        Map<String, InvocationChain> invocationChains = new HashMap<String, InvocationChain>();
        for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry : wire.getInvocationChains().entrySet()) {
            invocationChains.put(entry.getKey().getName(), entry.getValue());
        }

        MethodInterceptor methodInterceptor = new RsMethodInterceptor(invocationChains);

        Class<?> interfaze = classLoader.loadClass(sourceDefinition.getInterfaceName());
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(interfaze);
        enhancer.setCallback(methodInterceptor);

        // CGLib requires a classloader with access to the application classloader and this extension classloader
        MultiParentClassLoader rsClassLoader = new MultiParentClassLoader(URI.create("RESTclassloader"), getClass().getClassLoader());
        rsClassLoader.addParent(classLoader);
        enhancer.setClassLoader(rsClassLoader);

        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            // set the TCCL as Jeresy uses it to dynmically load classes
            Thread.currentThread().setContextClassLoader(rsClassLoader);
            Object instance = enhancer.create();
            application.addServiceHandler(interfaze, instance);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    private class RsMethodInterceptor implements MethodInterceptor {

        private Map<String, InvocationChain> invocationChains;

        private RsMethodInterceptor(Map<String, InvocationChain> invocationChains) {
            this.invocationChains = invocationChains;
        }

        public Object intercept(Object object, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            Message message = new MessageImpl(args, false, new WorkContext());
            InvocationChain invocationChain = invocationChains.get(method.getName());
            if (invocationChain != null) {
                Interceptor headInterceptor = invocationChain.getHeadInterceptor();
                Message ret = headInterceptor.invoke(message);
                if (ret.isFault()) {
                    throw (Throwable) ret.getBody();
                } else {
                    return ret.getBody();
                }
            } else {
                return null;
            }
        }

    }

}
