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

import java.lang.reflect.Type;
import java.util.List;

import org.easymock.IMocksControl;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionHelper;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.contract.ContractProcessor;
import org.fabric3.scdl.DefaultValidationContext;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.scdl.ValidationContext;
import org.fabric3.scdl.validation.MissingResource;

/**
 * @version $Revision$ $Date$
 */
public class MockComponentTypeLoaderImpl implements MockComponentTypeLoader {
    private final ContractProcessor contractProcessor;
    private final IntrospectionHelper helper;
    private final ServiceDefinition controlService;

    public MockComponentTypeLoaderImpl(@Reference IntrospectionHelper helper, @Reference ContractProcessor contractProcessor) {
        this.helper = helper;
        this.contractProcessor = contractProcessor;
        ValidationContext context = new DefaultValidationContext();
        ServiceContract<Type> controlServiceContract = introspect(IMocksControl.class, context);
        assert !context.hasErrors(); // should not happen
        controlService = new ServiceDefinition("mockControl", controlServiceContract);
    }

    /**
     * Loads the mock component type.
     *
     * @param mockedInterfaces     Interfaces that need to be mocked.
     * @param introspectionContext Loader context.
     * @return Mock component type.
     */
    public MockComponentType load(List<String> mockedInterfaces, IntrospectionContext introspectionContext) {

        MockComponentType componentType = new MockComponentType();

        ClassLoader classLoader = introspectionContext.getTargetClassLoader();
        for (String mockedInterface : mockedInterfaces) {
            Class<?> interfaceClass;
            try {
                interfaceClass = classLoader.loadClass(mockedInterface);
            } catch (ClassNotFoundException e) {
                MissingResource failure = new MissingResource("Mock interface not found: " + mockedInterface, mockedInterface);
                introspectionContext.addError(failure);
                continue;
            }

            ServiceContract<Type> serviceContract = introspect(interfaceClass, introspectionContext);
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

    private ServiceContract<Type> introspect(Class<?> interfaceClass, ValidationContext context) {
        TypeMapping typeMapping = helper.mapTypeParameters(interfaceClass);
        return contractProcessor.introspect(typeMapping, interfaceClass, context);
    }

}
