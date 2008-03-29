/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.fabric3.spring;

import java.net.URI;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.pojo.wire.PojoSourceWireAttacher;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.Component;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.runtime.component.ComponentManager;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.transform.PullTransformer;
import org.fabric3.spi.transform.TransformerRegistry;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.spi.wire.ProxyService;
import org.fabric3.spi.wire.Wire;

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
                                              sourceDefinition.isConversational(),
                                              wire);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String refName = sourceUri.getFragment();
        source.addRefNameToObjFactory(refName, factory);
    }

    private <T> ObjectFactory<T> createWireObjectFactory(Class<T> type, boolean isConversational, Wire wire) {
        return proxyService.createObjectFactory(type, isConversational, wire, null);
    }

    public void attachObjectFactory(SpringWireSourceDefinition source, ObjectFactory<?> objectFactory) throws WiringException {
        throw new AssertionError();
    }
}
