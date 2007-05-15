package org.fabric3.runtime.development.host;

import java.net.URI;
import java.net.URL;

import org.fabric3.api.annotation.LogLevel;
import org.fabric3.extension.component.SimpleWorkContext;
import org.fabric3.fabric.assembly.DistributedAssembly;
import org.fabric3.fabric.loader.LoaderContextImpl;
import org.fabric3.fabric.monitor.JavaLoggingMonitorFactory;
import org.fabric3.fabric.runtime.AbstractRuntime;
import org.fabric3.fabric.runtime.ComponentNames;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.component.WorkContext;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.model.type.ComponentDefinition;
import org.fabric3.spi.model.type.CompositeImplementation;
import org.fabric3.spi.model.type.Scope;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;

/**
 * @version $Rev$ $Date$
 */
public class DevelopmentRuntimeImpl extends AbstractRuntime<DevelopmentHostInfo> implements DevelopmentRuntime {
    public static final URI DOMAIN_URI = URI.create("fabric3://./domain/main/");
    private JavaLoggingMonitorFactory monitorFactory;
    private DevelopmentMonitor monitor;
    private ScopeContainer<URI> scopeContainer;
    boolean started;

    public DevelopmentRuntimeImpl() {
        super(DevelopmentHostInfo.class);
        monitorFactory = new JavaLoggingMonitorFactory();
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
            ClassLoaderRegistry classLoaderRegistry =
                    getSystemComponent(ClassLoaderRegistry.class, ComponentNames.CLASSLOADER_REGISTRY_URI);
            classLoaderRegistry.register(URI.create("sca://./applicationClassLoader"), getHostClassLoader());
            LoaderRegistry loader = getSystemComponent(LoaderRegistry.class, ComponentNames.LOADER_URI);
            DistributedAssembly assembly = getSystemComponent(DistributedAssembly.class,
                                                              ComponentNames.DISTRIBUTED_ASSEMBLY_URI);
            LoaderContext loaderContext = new LoaderContextImpl(getHostClassLoader(), null);
            loader.loadComponentType(impl, loaderContext);
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

    public void stop() {
        WorkContext workContext = new SimpleWorkContext();
        workContext.setScopeIdentifier(Scope.COMPOSITE, DOMAIN_URI);
        scopeContainer.stopContext(workContext);
        started = false;
    }

    public <T> T locateService(Class<T> interfaze, URI compositeURI, String name) {
        throw new UnsupportedOperationException();
    }

    public interface DevelopmentMonitor {
        @LogLevel("SEVERE")
        void runError(Exception e);
    }
}
