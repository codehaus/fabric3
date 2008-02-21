/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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
package org.fabric3.introspection.impl.contract;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Callback;
import org.osoa.sca.annotations.Conversational;
import org.osoa.sca.annotations.EndsConversation;
import org.osoa.sca.annotations.OneWay;
import org.osoa.sca.annotations.Remotable;
import org.osoa.sca.Constants;

import org.fabric3.introspection.ContractProcessor;
import org.fabric3.introspection.InvalidServiceContractException;
import org.fabric3.scdl.DataType;
import org.fabric3.scdl.Operation;
import static org.fabric3.scdl.Operation.CONVERSATION_END;
import static org.fabric3.scdl.Operation.NO_CONVERSATION;
import org.fabric3.scdl.ServiceContract;

/**
 * Default implementation of an InterfaceJavaIntrospector.
 *
 * @version $Rev$ $Date$
 */
public class DefaultContractProcessor implements ContractProcessor {
    public static final String IDL_INPUT = "idl:input";
    public static final QName ONEWAY_INTENT = new QName(Constants.SCA_NS, "oneWay");


    private static final String UNKNOWN_DATABINDING = null;

    public DefaultContractProcessor() {
    }

    public ServiceContract<Type> introspect(Type type) throws InvalidServiceContractException {
        if (type instanceof Class) {
            return introspect((Class<?>) type);
        } else {
            throw new UnsupportedOperationException("Interface introspection is only supported for classes");
        }
    }

    public JavaServiceContract introspect(Class<?> interfaze) throws InvalidServiceContractException {
        JavaServiceContract contract = introspectInterface(interfaze);
        Callback callback = interfaze.getAnnotation(Callback.class);
        if (callback != null) {
            Class<?> callbackClass = callback.value();
            if (Void.class.equals(callbackClass)) {
                throw new MissingCallbackException(interfaze.getName());
            }
            JavaServiceContract callbackContract = introspectInterface(callbackClass);
            contract.setCallbackContract(callbackContract);
        }
        return contract;
    }

    /**
     * Introspects a class, returning its service contract.
     *
     * @param interfaze the interface to introspect
     * @return the service contract
     * @throws InvalidServiceContractException
     *          if the class is an invalid service inteface or contains invalid service meatadata
     */
    private JavaServiceContract introspectInterface(Class<?> interfaze) throws InvalidServiceContractException {
        JavaServiceContract contract = new JavaServiceContract(interfaze);
        contract.setInterfaceName(interfaze.getSimpleName());

        // TODO this should be refactored to its own processor
        boolean remotable = interfaze.isAnnotationPresent(Remotable.class);
        contract.setRemotable(remotable);

        // TODO this should be refactored to its own processor
        boolean conversational = isAnnotationPresent(interfaze, Conversational.class);
        contract.setConversational(conversational);

        contract.setOperations(getOperations(interfaze, remotable, conversational));

        return contract;
    }

    /**
     * Determine if an annotation is present on this interface or any superinterface.
     * <p/>
     * This is similar to the use of @Inherited on classes (given @Inherited does not apply to interfaces).
     *
     * This has been deprecated as there is a duplicate in the IntrospectionHelper interface.
     *
     * @param type           the interface to check
     * @param annotationType the annotation to look for
     * @return true if the annotation is present
     */
    @Deprecated
    private boolean isAnnotationPresent(Class<?> type, Class<? extends Annotation> annotationType) {
        if (type.isAnnotationPresent(annotationType)) {
            return true;
        }
        Class<?>[] interfaces = type.getInterfaces();
        for (Class<?> superInterface : interfaces) {
            if (isAnnotationPresent(superInterface, annotationType)) {
                return true;
            }
        }
        return false;
    }

    private <T> List<Operation<Type>> getOperations(Class<T> type,
                                                    boolean remotable,
                                                    boolean conversational)
            throws InvalidServiceContractException {
        Method[] methods = type.getMethods();
        List<Operation<Type>> operations = new ArrayList<Operation<Type>>(methods.length);
        for (Method method : methods) {
            String name = method.getName();
            if (remotable) {
                for (Operation<Type> operation : operations) {
                    if (operation.getName().equals(name)) {
                        throw new OverloadedOperationException(method);
                    }
                }
            }

            Type returnType = method.getGenericReturnType();
            Type[] paramTypes = method.getGenericParameterTypes();
            Type[] faultTypes = method.getGenericExceptionTypes();
            boolean nonBlocking = method.isAnnotationPresent(OneWay.class);
            int conversationSequence = NO_CONVERSATION;
            if (method.isAnnotationPresent(EndsConversation.class)) {
                if (!conversational) {
                    throw new InvalidConversationalOperationException(
                            "Method is marked as end conversation but contract is not conversational",
                            method.getDeclaringClass().getName(),
                            method);
                }
                conversationSequence = CONVERSATION_END;
            } else if (conversational) {
                conversationSequence = Operation.CONVERSATION_CONTINUE;
            }

            DataType<Type> returnDataType = new DataType<Type>(returnType, returnType);
            List<DataType<Type>> paramDataTypes = new ArrayList<DataType<Type>>(paramTypes.length);
            for (Type paramType : paramTypes) {
                paramDataTypes.add(new DataType<Type>(paramType, paramType));
            }
            List<DataType<Type>> faultDataTypes = new ArrayList<DataType<Type>>(faultTypes.length);
            for (Type faultType : faultTypes) {
                faultDataTypes.add(new DataType<Type>(faultType, faultType));
            }

            DataType<List<DataType<Type>>> inputType =
                    new DataType<List<DataType<Type>>>(Object[].class, paramDataTypes);
            Operation<Type> operation = new Operation<Type>(name,
                                                            inputType,
                                                            returnDataType,
                                                            faultDataTypes,
                                                            conversationSequence);

            // TODO this should be refactored to its own processor
            if (method.isAnnotationPresent(OneWay.class)) {
                operation.addIntent(ONEWAY_INTENT);
            }
            operations.add(operation);
        }
        return operations;
    }

}
