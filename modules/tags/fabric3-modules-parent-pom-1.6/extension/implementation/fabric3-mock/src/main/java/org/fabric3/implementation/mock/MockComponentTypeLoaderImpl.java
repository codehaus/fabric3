/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.implementation.mock;

import java.util.List;

import org.easymock.IMocksControl;
import org.osoa.sca.annotations.Reference;

import org.fabric3.model.type.component.ServiceDefinition;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.MissingResource;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.model.type.java.InjectingComponentType;

/**
 * @version $Rev$ $Date$
 */
public class MockComponentTypeLoaderImpl implements MockComponentTypeLoader {
    private final JavaContractProcessor contractProcessor;
    private final ServiceDefinition controlService;

    public MockComponentTypeLoaderImpl(@Reference JavaContractProcessor contractProcessor) {
        this.contractProcessor = contractProcessor;
        IntrospectionContext context = new DefaultIntrospectionContext();
        ServiceContract controlServiceContract = contractProcessor.introspect(IMocksControl.class, context);
        assert !context.hasErrors(); // should not happen
        controlService = new ServiceDefinition("mockControl", controlServiceContract);
    }

    /**
     * Loads the mock component type.
     *
     * @param mockedInterfaces Interfaces that need to be mocked.
     * @param context          Loader context.
     * @return Mock component type.
     */
    public InjectingComponentType load(List<String> mockedInterfaces, IntrospectionContext context) {

        InjectingComponentType componentType = new InjectingComponentType();

        ClassLoader classLoader = context.getClassLoader();
        for (String mockedInterface : mockedInterfaces) {
            Class<?> interfaceClass;
            try {
                interfaceClass = classLoader.loadClass(mockedInterface);
            } catch (ClassNotFoundException e) {
                MissingResource failure = new MissingResource("Mock interface not found: " + mockedInterface, mockedInterface);
                context.addError(failure);
                continue;
            }

            ServiceContract serviceContract = contractProcessor.introspect(interfaceClass, context);

            String name = interfaceClass.getName();
            int index = name.lastIndexOf('.');
            if (index != -1) {
                name = name.substring(index + 1);
            }
            componentType.add(new ServiceDefinition(name, serviceContract));
        }
        componentType.add(controlService);
        componentType.setScope("STATELESS");

        return componentType;
    }

}
