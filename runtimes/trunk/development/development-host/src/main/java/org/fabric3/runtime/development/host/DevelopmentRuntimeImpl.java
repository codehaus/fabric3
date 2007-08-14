package org.fabric3.runtime.development.host;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.Map;

import org.osoa.sca.ServiceUnavailableException;

import org.fabric3.api.annotation.LogLevel;
import org.fabric3.extension.component.SimpleWorkContext;
import org.fabric3.spi.assembly.BindException;
import org.fabric3.fabric.assembly.DistributedAssembly;
import org.fabric3.fabric.monitor.JavaLoggingMonitorFactory;
import org.fabric3.fabric.runtime.AbstractRuntime;
import static org.fabric3.fabric.runtime.ComponentNames.DISTRIBUTED_ASSEMBLY_URI;
import static org.fabric3.fabric.runtime.ComponentNames.LOADER_URI;
import static org.fabric3.fabric.runtime.ComponentNames.RUNTIME_NAME;
import org.fabric3.fabric.util.JavaIntrospectionHelper;
import org.fabric3.fabric.wire.WireUtils;
import org.fabric3.host.runtime.StartException;
import org.fabric3.loader.common.LoaderContextImpl;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.Scope;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.component.WorkContext;
import org.fabric3.spi.loader.Loader;
import org.fabric3.spi.loader.LoaderContext;
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
    private static final URI WIRE_CACHE_URI = URI.create(RUNTIME_NAME + "/ClientWireCache");
    private static final URI MOCK_CACHE_URI = URI.create(RUNTIME_NAME + "/MockObjectCache");
    private static final URI PROXY_SERVICE_URI = URI.create(RUNTIME_NAME + "/proxyService");
    private DevelopmentMonitor monitor;
    private ScopeContainer<URI> scopeContainer;
    private boolean started;
    private DistributedAssembly applicationAssembly;
    private ClientWireCache wireCache;
    private ProxyService proxyService;
    private MockObjectCache mockCache;

    public DevelopmentRuntimeImpl() {
        super(DevelopmentHostInfo.class);
        JavaLoggingMonitorFactory monitorFactory = new JavaLoggingMonitorFactory();
        setMonitorFactory(monitorFactory);
        monitor = monitorFactory.getMonitor(DevelopmentMonitor.class);
    }


    public void start() throws StartException {
        applicationAssembly = getSystemComponent(DistributedAssembly.class, DISTRIBUTED_ASSEMBLY_URI);
        wireCache = getSystemComponent(ClientWireCache.class, WIRE_CACHE_URI);
        mockCache = getSystemComponent(MockObjectCache.class, MOCK_CACHE_URI);
        proxyService = getSystemComponent(ProxyService.class, PROXY_SERVICE_URI);
        ScopeRegistry scopeRegistry = getScopeRegistry();
        scopeContainer = scopeRegistry.getScopeContainer(Scope.COMPOSITE);
        super.start();
    }

    public void activate(URL compositeFile) {
        try {
            Loader loader = getSystemComponent(Loader.class, LOADER_URI);
            LoaderContext loaderContext = new LoaderContextImpl(getHostClassLoader(), compositeFile);
            Composite composite = loader.load(compositeFile, Composite.class, loaderContext);
            applicationAssembly.includeInDomain(composite);
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
            applicationAssembly = null;
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
                applicationAssembly.bindService(uri, definition);
            }
            wire = wireCache.getWire(uri);
            Map<Method, InvocationChain> mappings = WireUtils.createInterfaceToWireMapping(interfaze, wire);
            return proxyService.createProxy(interfaze, false, wire, mappings);
        } catch (BindException e) {
            throw new ServiceUnavailableException(e);
        }
    }

    public <T> void registerMockReference(String name, Class<T> interfaze, T mock) {
        mockCache.putMockDefinition(name, new MockDefinition<T>(interfaze, mock));
    }

    public interface DevelopmentMonitor {
        @LogLevel("SEVERE")
        void runError(Exception e);
    }


}
