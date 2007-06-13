package org.fabric3.runtime.development.host;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.Map;

import org.osoa.sca.ServiceUnavailableException;

import org.fabric3.api.annotation.LogLevel;
import org.fabric3.extension.component.SimpleWorkContext;
import org.fabric3.fabric.assembly.BindException;
import org.fabric3.fabric.assembly.DistributedAssembly;
import org.fabric3.fabric.loader.LoaderContextImpl;
import org.fabric3.fabric.monitor.JavaLoggingMonitorFactory;
import org.fabric3.fabric.runtime.AbstractRuntime;
import static org.fabric3.fabric.runtime.ComponentNames.DISTRIBUTED_ASSEMBLY_URI;
import static org.fabric3.fabric.runtime.ComponentNames.COMPOSITE_LOADER_URI;
import static org.fabric3.fabric.runtime.ComponentNames.RUNTIME_NAME;
import org.fabric3.fabric.util.JavaIntrospectionHelper;
import org.fabric3.fabric.wire.WireUtils;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.component.WorkContext;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.ComponentTypeLoader;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.type.ComponentDefinition;
import org.fabric3.spi.model.type.CompositeImplementation;
import org.fabric3.spi.model.type.Scope;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.ProxyService;
import org.fabric3.spi.wire.Wire;

/**
 * Default implementation of the development runtime. The runtime is booted in a child classloader to the
 * development-api module, which contains the client {@link org.fabric3.runtime.development.Domain} API.
 * <p/>
 * Composite activation via <code>Domain#activate</code> is transient and lasts for the length of the Domain instance.
 * <p/>
 * <code>Domain#connectTo</code> is delegated to {@link #connectTo(Class,String)}, which generates wires to target
 * services. The generated wires are bound to a special client binding and cached in {@link ClientWireCache} by the
 * {@link ClientWireAttacher} for subsequent requests. As a final step, a proxy fronting the wire is created and
 * returned to the client.
 *
 * @version $Rev$ $Date$
 */
public class DevelopmentRuntimeImpl extends AbstractRuntime<DevelopmentHostInfo> implements DevelopmentRuntime {
    public static final URI DOMAIN_URI = URI.create("fabric3://./domain/main/");
    private static final String DOMAIN_STRING = DOMAIN_URI.toString();
    private static final URI WIRE_CACHE_URI = URI.create(RUNTIME_NAME + "/main/ClientWireCache");
    private static final URI PROXY_SERVICE_URI = URI.create(RUNTIME_NAME + "/main/proxyService");
    private DevelopmentMonitor monitor;
    private ScopeContainer<URI> scopeContainer;
    private boolean started;
    private DistributedAssembly assembly;
    private ClientWireCache wireCache;
    private ProxyService proxyService;

    public DevelopmentRuntimeImpl() {
        super(DevelopmentHostInfo.class);
        JavaLoggingMonitorFactory monitorFactory = new JavaLoggingMonitorFactory();
        setMonitorFactory(monitorFactory);
        monitor = monitorFactory.getMonitor(DevelopmentMonitor.class);
    }

    public void activate(URL compositeFile) {
        if (started) {
            throw new IllegalStateException("Composite already activated");
        }

        CompositeImplementation impl = new CompositeImplementation();
        impl.setScdlLocation(compositeFile);
        impl.setClassLoader(getHostClassLoader());

        ComponentDefinition<CompositeImplementation> definition =
                new ComponentDefinition<CompositeImplementation>("main", impl);
        try {
            @SuppressWarnings("unchecked")
            ComponentTypeLoader<CompositeImplementation> loader =
                    getSystemComponent(ComponentTypeLoader.class, COMPOSITE_LOADER_URI);
            assembly = getSystemComponent(DistributedAssembly.class, DISTRIBUTED_ASSEMBLY_URI);
            wireCache = getSystemComponent(ClientWireCache.class, WIRE_CACHE_URI);
            proxyService = getSystemComponent(ProxyService.class, PROXY_SERVICE_URI);
            LoaderContext loaderContext = new LoaderContextImpl(getHostClassLoader(), null);
            loader.load(impl, loaderContext);
            assembly.activate(definition, false);
            ScopeRegistry scopeRegistry = getScopeRegistry();
            scopeContainer = scopeRegistry.getScopeContainer(Scope.COMPOSITE);
            WorkContext workContext = new SimpleWorkContext();
            workContext.setScopeIdentifier(Scope.COMPOSITE, DOMAIN_URI);
            scopeContainer.startContext(workContext, DOMAIN_URI);
            started = true;
        } catch (Exception e) {
            monitor.runError(e);
        }
    }

    public void destroy() {
        if (started) {
            super.destroy();
            WorkContext workContext = new SimpleWorkContext();
            workContext.setScopeIdentifier(Scope.COMPOSITE, DOMAIN_URI);
            scopeContainer.stopContext(workContext);
            scopeContainer = null;
            wireCache = null;
            assembly = null;
            proxyService = null;
            started = false;
        }
    }

    public <T> T connectTo(Class<T> interfaze, String serviceUri) {
        URI uri = URI.create(DOMAIN_STRING + serviceUri);
        if (uri.getFragment() == null) {
            // no service name specified, calculate from the interface
            uri = URI.create(uri.toString() + "#" + JavaIntrospectionHelper.getBaseName(interfaze));
        }
        try {
            Wire wire = wireCache.getWire(uri);
            if (wire == null) {
                ClientBindingDefinition definition = new ClientBindingDefinition();
                LogicalBinding<?> binding = new LogicalBinding<ClientBindingDefinition>(definition);
                assembly.bindService(uri, binding);
            }
            wire = wireCache.getWire(uri);
            Map<Method, InvocationChain> mappings = WireUtils.createInterfaceToWireMapping(interfaze, wire);
            return proxyService.createProxy(interfaze, false, wire, mappings);
        } catch (BindException e) {
            throw new ServiceUnavailableException(e);
        }
    }

    public interface DevelopmentMonitor {
        @LogLevel("SEVERE")
        void runError(Exception e);
    }
}
