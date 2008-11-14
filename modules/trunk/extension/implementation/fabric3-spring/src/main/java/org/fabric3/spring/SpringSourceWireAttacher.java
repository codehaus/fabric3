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
package org.fabric3.spring;

import java.net.URI;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.pojo.builder.PojoSourceWireAttacher;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.transform.PullTransformer;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.Component;
import org.fabric3.spi.model.physical.InteractionType;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.services.componentmanager.ComponentManager;
import org.fabric3.spi.services.proxy.ProxyService;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.spi.wire.Wire;
import org.fabric3.spi.transform.TransformerRegistry;

/**
 * The component builder for Spring implementation types. Responsible for creating the Component runtime artifact from a physical component
 * definition
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class SpringSourceWireAttacher extends PojoSourceWireAttacher implements SourceWireAttacher<SpringWireSourceDefinition> {
    private final ComponentManager manager;
    private final ProxyService proxyService;
    private final ClassLoaderRegistry classLoaderRegistry;

    private boolean debug = false;

    public SpringSourceWireAttacher(@Reference ComponentManager manager,
                                    @Reference ProxyService proxyService,
                                    @Reference ClassLoaderRegistry classLoaderRegistry,
                                    @Reference(name = "transformerRegistry")
                                    TransformerRegistry<PullTransformer<?, ?>> transformerRegistry) {
        super(transformerRegistry, classLoaderRegistry);
        this.manager = manager;
        this.proxyService = proxyService;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    public void attachToSource(SpringWireSourceDefinition sourceDefinition,
                               PhysicalWireTargetDefinition targetDefinition,
                               Wire wire) {

        URI sourceUri = sourceDefinition.getUri();
        URI sourceName = UriHelper.getDefragmentedName(sourceDefinition.getUri());
        Component component = manager.getComponent(sourceName);
        assert component instanceof SpringComponent;
        SpringComponent<?> source = (SpringComponent) component;

        Class<?> type = sourceDefinition.getFieldType();
        URI targetUri = targetDefinition.getUri();

        Component target = null;
        if (targetUri != null) {
            URI targetName = UriHelper.getDefragmentedName(targetDefinition.getUri());
            target = manager.getComponent(targetName);
        }

        if (debug)
            System.out.println("##############SpringSourceWireAttacher:attachToSource()" +
                    "; sourceUri=" + sourceUri + "; sourceName=" + sourceName +
                    "; targetUri=" + targetUri + "; targetName=" + UriHelper.getDefragmentedName(targetDefinition.getUri()) +
                    "; sourceUri.getFragment()=" + sourceUri.getFragment());

        assert target instanceof AtomicComponent;
        ObjectFactory<?> factory = null;
        ClassLoader cl = classLoaderRegistry.getClassLoader(sourceDefinition.getClassLoaderId());

        try {
            factory = createWireObjectFactory(cl.loadClass(type.getName()),
                                              sourceDefinition.getInteractionType(),
                                              wire);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        String refName = sourceUri.getFragment();
        source.addRefNameToObjFactory(refName, factory);
    }

    public void detachFromSource(SpringWireSourceDefinition source, PhysicalWireTargetDefinition target) throws WiringException {
        throw new AssertionError();
    }

    public void detachObjectFactory(SpringWireSourceDefinition source, PhysicalWireTargetDefinition target) throws WiringException {
        throw new AssertionError();
    }

    private <T> ObjectFactory<T> createWireObjectFactory(Class<T> type, InteractionType interactionType, Wire wire) {
        return proxyService.createObjectFactory(type, interactionType, wire, null);
    }

    public void attachObjectFactory(SpringWireSourceDefinition source, ObjectFactory<?> objectFactory, PhysicalWireTargetDefinition target)
            throws WiringException {
        throw new AssertionError();
    }
}
