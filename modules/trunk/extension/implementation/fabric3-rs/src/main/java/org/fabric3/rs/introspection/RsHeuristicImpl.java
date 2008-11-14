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
package org.fabric3.rs.introspection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Path;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionHelper;
import org.fabric3.spi.introspection.java.ImplementationNotFoundException;
import org.fabric3.java.scdl.JavaImplementation;
import org.fabric3.rs.scdl.RsBindingDefinition;
import org.fabric3.scdl.DataType;
import org.fabric3.scdl.InjectingComponentType;
import org.fabric3.scdl.Operation;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ServiceDefinition;
import org.osoa.sca.annotations.Reference;
import static org.fabric3.scdl.Operation.NO_CONVERSATION;

/**
 * This would better have been implemented as a custom ImplementationProcessor/Heuristic
 * but then it would have limited reuse of the Java Implementation extension
 * without adding much new functionality
 * @version $Rev$ $Date$
 */
public class RsHeuristicImpl implements RsHeuristic {

    private final IntrospectionHelper helper;

    public RsHeuristicImpl(@Reference IntrospectionHelper helper) {
        this.helper = helper;
    }

    public void applyHeuristics(JavaImplementation impl, URI webAppUri, IntrospectionContext context) {
        ServiceDefinition serviceDefinition = addRESTService(impl, webAppUri);
        RsBindingDefinition definition = (RsBindingDefinition) serviceDefinition.getBindings().get(0);
        List<Operation<Type>> operations = new ArrayList<Operation<Type>>();

        ClassLoader cl = context.getTargetClassLoader();

        Class<?> implClass;
        try {
            implClass = helper.loadClass(impl.getImplementationClass(), cl);
        } catch (ImplementationNotFoundException e) {
            //This should have already been recorded
            return;
        }
        Path path = implClass.getAnnotation(Path.class);
        if (path != null) {
            definition.setIsResource(true);
            for (Method m : implClass.getMethods()) {
                for (Annotation a : m.getAnnotations()) {
                    if (a.annotationType().getName().startsWith("javax.ws.rs")) {
                        operations.add(getOperations(m));
                        break;
                    }
                }
            }
        }

        Provider provider = implClass.getAnnotation(Provider.class);
        if (provider != null) {
            definition.setIsProvider(true);
            for (Class interfaze : implClass.getInterfaces()) {
                if (MessageBodyReader.class.equals(interfaze) || MessageBodyWriter.class.equals(interfaze)) {
                    for (Method m : interfaze.getMethods()) {
                        operations.add(getOperations(m));
                    }
                }
            }
        }

        if (!definition.isResource() && !definition.isProvider()) {
            context.addError(new InvalidRsClass(implClass));
        }
        ServiceContract contract = serviceDefinition.getServiceContract();
        contract.setOperations(operations);
    }

    private ServiceDefinition addRESTService(final JavaImplementation impl, URI webAppURI) {
        RsBindingDefinition bindingDefinition = new RsBindingDefinition(webAppURI);
        ServiceDefinition definition = new ServiceDefinition("REST");
        ServiceContract serviceContract = new ServiceContract() {

            @Override
            public boolean isAssignableFrom(ServiceContract contract) {
                return false;
            }

            @Override
            public String getQualifiedInterfaceName() {
                return impl.getImplementationClass();
            }
        };
        serviceContract.setInterfaceName(impl.getImplementationClass());
        definition.setServiceContract(serviceContract);
        definition.addBinding(bindingDefinition);
        InjectingComponentType componentType = impl.getComponentType();
        componentType.add(definition);
        return definition;
    }

    private <T> Operation<Type> getOperations(Method method) {

        Class<?> returnType = method.getReturnType();
        Class<?>[] paramTypes = method.getParameterTypes();
        Class<?>[] faultTypes = method.getExceptionTypes();


        DataType<Type> returnDataType = new DataType<Type>(returnType, returnType);
        List<DataType<Type>> paramDataTypes = new ArrayList<DataType<Type>>(paramTypes.length);
        for (Type paramType : paramTypes) {
            paramDataTypes.add(new DataType<Type>(paramType, paramType));
        }
        List<DataType<Type>> faultDataTypes = new ArrayList<DataType<Type>>(faultTypes.length);
        for (Type faultType : faultTypes) {
            faultDataTypes.add(new DataType<Type>(faultType, faultType));
        }

        DataType<List<DataType<Type>>> inputType = new DataType<List<DataType<Type>>>(Object[].class, paramDataTypes);
        Operation<Type> operation = new Operation<Type>(method.getName(), inputType, returnDataType, faultDataTypes, NO_CONVERSATION);
        return operation;
    }
}
