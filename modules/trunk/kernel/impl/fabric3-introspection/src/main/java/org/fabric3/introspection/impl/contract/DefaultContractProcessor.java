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

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

import org.osoa.sca.Constants;
import org.osoa.sca.annotations.Callback;
import org.osoa.sca.annotations.Conversational;
import org.osoa.sca.annotations.EndsConversation;
import org.osoa.sca.annotations.OneWay;
import org.osoa.sca.annotations.Remotable;
import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.contract.ContractProcessor;
import org.fabric3.introspection.contract.InvalidServiceContractException;
import org.fabric3.introspection.helper.IntrospectionHelper;
import org.fabric3.introspection.helper.TypeMapping;
import org.fabric3.scdl.DataType;
import org.fabric3.scdl.Operation;
import static org.fabric3.scdl.Operation.CONVERSATION_END;
import static org.fabric3.scdl.Operation.NO_CONVERSATION;

/**
 * Default implementation of a ContractProcessor for Java interfaces.
 *
 * @version $Rev$ $Date$
 */
public class DefaultContractProcessor implements ContractProcessor {
    public static final String IDL_INPUT = "idl:input";
    public static final QName ONEWAY_INTENT = new QName(Constants.SCA_NS, "oneWay");

    private final IntrospectionHelper helper;

    public DefaultContractProcessor(@Reference IntrospectionHelper helper) {
        this.helper = helper;
    }

    public JavaServiceContract introspect(TypeMapping typeMapping, Type type) throws InvalidServiceContractException {
        if (type instanceof Class) {
            return introspect(typeMapping, (Class<?>) type);
        } else {
            throw new UnsupportedOperationException("Interface introspection is only supported for classes");
        }
    }

    public JavaServiceContract introspect(TypeMapping typeMapping, Class<?> interfaze) throws InvalidServiceContractException {
        JavaServiceContract contract = introspectInterface(typeMapping, interfaze);
        Callback callback = interfaze.getAnnotation(Callback.class);
        if (callback != null) {
            Class<?> callbackClass = callback.value();
            if (Void.class.equals(callbackClass)) {
                throw new MissingCallbackException(interfaze.getName());
            }
            JavaServiceContract callbackContract = introspectInterface(typeMapping, callbackClass);
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
    private JavaServiceContract introspectInterface(TypeMapping typeMapping, Class<?> interfaze) throws InvalidServiceContractException {
        JavaServiceContract contract = new JavaServiceContract(interfaze);
        contract.setInterfaceName(interfaze.getSimpleName());

        // TODO this should be refactored to its own processor
        boolean remotable = interfaze.isAnnotationPresent(Remotable.class);
        contract.setRemotable(remotable);

        // TODO this should be refactored to its own processor
        boolean conversational = helper.isAnnotationPresent(interfaze, Conversational.class);
        contract.setConversational(conversational);

        contract.setOperations(getOperations(typeMapping, interfaze, remotable, conversational));

        return contract;
    }

    private <T> List<Operation<Type>> getOperations(TypeMapping typeMapping,
                                                    Class<T> type,
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

            Class<?> returnType = method.getReturnType();
            Class<?>[] paramTypes = method.getParameterTypes();
            Class<?>[] faultTypes = method.getExceptionTypes();

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

            Type actualReturnType = typeMapping.getActualType(returnType);
            DataType<Type> returnDataType = new DataType<Type>(actualReturnType, actualReturnType);
            List<DataType<Type>> paramDataTypes = new ArrayList<DataType<Type>>(paramTypes.length);
            for (Type paramType : paramTypes) {
                Type actualType = typeMapping.getActualType(paramType);
                paramDataTypes.add(new DataType<Type>(actualType, actualType));
            }
            List<DataType<Type>> faultDataTypes = new ArrayList<DataType<Type>>(faultTypes.length);
            for (Type faultType : faultTypes) {
                Type actualType = typeMapping.getActualType(faultType);
                faultDataTypes.add(new DataType<Type>(actualType, actualType));
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
