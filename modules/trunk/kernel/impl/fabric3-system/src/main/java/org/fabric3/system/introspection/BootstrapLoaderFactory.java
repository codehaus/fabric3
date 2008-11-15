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
package org.fabric3.system.introspection;

import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;

import org.fabric3.spi.introspection.xml.Loader;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.fabric3.spi.xml.XMLFactory;
import org.fabric3.loader.common.ComponentReferenceLoader;
import org.fabric3.loader.common.ComponentServiceLoader;
import org.fabric3.loader.common.PropertyLoader;
import org.fabric3.loader.composite.ComponentLoader;
import org.fabric3.loader.composite.CompositeLoader;
import org.fabric3.loader.composite.IncludeLoader;
import org.fabric3.loader.composite.PropertyValueLoader;
import org.fabric3.loader.composite.WireLoader;
import org.fabric3.loader.impl.DefaultLoaderHelper;
import org.fabric3.loader.impl.LoaderRegistryImpl;
import org.fabric3.host.monitor.MonitorFactory;
import org.fabric3.system.scdl.SystemImplementation;

/**
 * Factory class for an implementation of Loader that can handle system SCDL.
 * <p/>
 * This loader can handle a constrained version of SCDL for bootstrapping a runtime. The constraints are:
 * <pre>
 * <ul>
 * <li>The only implementation type allowed is system</li>
 * <li>The only service contract type is a Java interface found through introspection</li>
 * <li>Resolution of SCDL artifacts by QName is not supported; scdlLocation or scdlResource must be used</li>
 * </ul>
 * </pre>
 *
 * @version $Rev$ $Date$
 */
public class BootstrapLoaderFactory {

    public static Loader createLoader(SystemImplementationProcessor processor, MonitorFactory monitorFactory, XMLFactory xmlFactory) {

        LoaderHelper loaderHelper = new DefaultLoaderHelper();

        LoaderRegistryImpl loader = new LoaderRegistryImpl(monitorFactory.getMonitor(LoaderRegistryImpl.Monitor.class), xmlFactory);
        Map<QName, TypeLoader<?>> loaders = new HashMap<QName, TypeLoader<?>>();

        WireLoader wireLoader = new WireLoader(loaderHelper);

        // loader for <composite> document
        loaders.put(CompositeLoader.COMPOSITE, compositeLoader(loader, wireLoader, loaderHelper));

        // loader for <implementation.system> element
        SystemImplementationLoader systemLoader = new SystemImplementationLoader(processor);
        loaders.put(SystemImplementation.IMPLEMENTATION_SYSTEM, systemLoader);

        loaders.put(CompositeLoader.WIRE, wireLoader);

        loader.setLoaders(loaders);
        return loader;
    }

    private static CompositeLoader compositeLoader(Loader loader, WireLoader wireLoader, LoaderHelper loaderHelper) {
        PropertyLoader propertyLoader = new PropertyLoader(loaderHelper);
        PropertyValueLoader propertyValueLoader = new PropertyValueLoader(loaderHelper);

        ComponentReferenceLoader componentReferenceLoader = new ComponentReferenceLoader(loader, loaderHelper);
        ComponentServiceLoader componentServiceLoader = new ComponentServiceLoader(loader, loaderHelper);
        ComponentLoader componentLoader = new ComponentLoader(loader,
                                                              propertyValueLoader,
                                                              componentReferenceLoader,
                                                              componentServiceLoader,
                                                              loaderHelper);

        IncludeLoader includeLoader = new IncludeLoader(loader, null);
        return new CompositeLoader(loader, includeLoader, propertyLoader, componentLoader, wireLoader, loaderHelper);
    }


}
