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
package org.fabric3.fabric.idl.java;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.osoa.sca.annotations.Callback;
import org.osoa.sca.annotations.Conversational;
import org.osoa.sca.annotations.EndsConversation;
import org.osoa.sca.annotations.OneWay;
import org.osoa.sca.annotations.Remotable;

import static org.fabric3.fabric.util.JavaIntrospectionHelper.getBaseName;
import org.fabric3.scdl.DataType;
import org.fabric3.scdl.Operation;
import static org.fabric3.scdl.Operation.CONVERSATION_END;
import static org.fabric3.scdl.Operation.NO_CONVERSATION;
import org.fabric3.spi.idl.InvalidConversationalOperationException;
import org.fabric3.spi.idl.InvalidServiceContractException;
import org.fabric3.spi.idl.OverloadedOperationException;
import org.fabric3.spi.idl.java.JavaInterfaceProcessor;
import org.fabric3.spi.idl.java.JavaInterfaceProcessorRegistry;
import org.fabric3.spi.idl.java.JavaServiceContract;

/**
 * Default implementation of an InterfaceJavaIntrospector.
 *
 * @version $Rev$ $Date$
 */
public class JavaInterfaceProcessorRegistryImpl implements JavaInterfaceProcessorRegistry {
    public static final String IDL_INPUT = "idl:input";

    private static final String UNKNOWN_DATABINDING = null;

    private List<JavaInterfaceProcessor> processors = new ArrayList<JavaInterfaceProcessor>();

    public JavaInterfaceProcessorRegistryImpl() {
    }

    public void registerProcessor(JavaInterfaceProcessor processor) {
        processors.add(processor);
    }

    public void unregisterProcessor(JavaInterfaceProcessor processor) {
        processors.remove(processor);
    }

    public <T> JavaServiceContract introspect(Class<T> type) throws InvalidServiceContractException {
        Class<?> callbackClass = null;
        Callback callback = type.getAnnotation(Callback.class);
        if (callback != null && !Void.class.equals(callback.value())) {
            callbackClass = callback.value();
        } else if (callback != null && Void.class.equals(callback.value())) {
            throw new IllegalCallbackException("No callback interface specified on annotation", type.getName());
        }
        return introspect(type, callbackClass);
    }

    public <I, C> JavaServiceContract introspect(Class<I> type, Class<C> callback)
            throws InvalidServiceContractException {
        JavaServiceContract contract = new JavaServiceContract(type);
        contract.setInterfaceName(getBaseName(type));
        boolean remotable = type.isAnnotationPresent(Remotable.class);
        contract.setRemotable(remotable);
        boolean conversational = type.isAnnotationPresent(Conversational.class);
        contract.setConversational(conversational);
        contract.setOperations(getOperations(type, remotable, conversational, false));

        if (callback != null) {
            contract.setCallbackName(getBaseName(callback));
            contract.setCallbackClass(callback.getName());
            contract.setCallbackOperations(getOperations(callback, remotable, conversational, true));
        }

        for (JavaInterfaceProcessor processor : processors) {
            processor.visitInterface(type, callback, contract);
        }
        return contract;
    }

    private <T> List<Operation<Type>> getOperations(Class<T> type,
                                                    boolean remotable,
                                                    boolean conversational,
                                                    boolean callback)
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
                                                            nonBlocking,
                                                            callback,
                                                            UNKNOWN_DATABINDING,
                                                            conversationSequence);
            operations.add(operation);
        }
        return operations;
    }

}
