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

import org.osoa.sca.annotations.Reference;

import org.fabric3.java.model.JavaImplementation;
import org.fabric3.model.type.component.ServiceDefinition;
import org.fabric3.model.type.java.InjectingComponentType;
import org.fabric3.model.type.service.DataType;
import org.fabric3.model.type.service.Operation;
import static org.fabric3.model.type.service.Operation.NO_CONVERSATION;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.rs.model.RsBindingDefinition;
import org.fabric3.spi.introspection.ImplementationNotFoundException;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionHelper;

/**
 * This would better have been implemented as a custom ImplementationProcessor/Heuristic but then it would have limited reuse of the Java
 * Implementation extension without adding much new functionality
 *
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
