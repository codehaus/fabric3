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
package org.fabric3.mock;

import java.util.LinkedList;
import java.util.List;

import org.easymock.IMocksControl;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.component.ComponentBuilder;
import org.fabric3.spi.classloader.ClassLoaderRegistry;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
public class MockComponentBuilder<T> implements ComponentBuilder<MockComponentDefinition, MockComponent<T>> {
    private ClassLoaderRegistry classLoaderRegistry;
    private IMocksControl control;

    public MockComponentBuilder(@Reference ClassLoaderRegistry classLoaderRegistry, @Reference IMocksControl control) {
        this.classLoaderRegistry = classLoaderRegistry;
        this.control = control;
    }

    public MockComponent<T> build(MockComponentDefinition componentDefinition) throws BuilderException {

        List<String> interfaces = componentDefinition.getInterfaces();
        ClassLoader classLoader = classLoaderRegistry.getClassLoader(componentDefinition.getClassLoaderId());

        List<Class<?>> mockedInterfaces = new LinkedList<Class<?>>();
        for (String interfaze : interfaces) {
            try {
                mockedInterfaces.add(classLoader.loadClass(interfaze));
            } catch (ClassNotFoundException ex) {
                throw new AssertionError(ex);
            }
        }

        ObjectFactory<T> objectFactory = new MockObjectFactory<T>(mockedInterfaces, classLoader, control);

        return new MockComponent<T>(componentDefinition.getComponentId(), objectFactory);

    }

}
