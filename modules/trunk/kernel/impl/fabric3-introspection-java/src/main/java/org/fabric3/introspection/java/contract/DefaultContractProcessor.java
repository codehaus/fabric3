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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.introspection.java.contract;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

import org.oasisopen.sca.Constants;
import org.osoa.sca.annotations.Callback;
import org.osoa.sca.annotations.Conversational;
import org.osoa.sca.annotations.EndsConversation;
import org.osoa.sca.annotations.OneWay;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Remotable;

import org.fabric3.model.type.service.DataType;
import org.fabric3.model.type.service.JavaServiceContract;
import org.fabric3.model.type.service.Operation;
import static org.fabric3.model.type.service.Operation.CONVERSATION_END;
import static org.fabric3.model.type.service.Operation.NO_CONVERSATION;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionHelper;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.java.contract.ContractProcessor;
import org.fabric3.spi.introspection.java.contract.InterfaceIntrospector;
import org.fabric3.spi.introspection.java.contract.OperationIntrospector;

/**
 * Default implementation of a ContractProcessor for Java interfaces.
 *
 * @version $Rev$ $Date$
 */
public class DefaultContractProcessor implements ContractProcessor {
    public static final String IDL_INPUT = "idl:input";
    public static final QName ONEWAY_INTENT = new QName(Constants.SCA_NS, "oneWay");

    private final IntrospectionHelper helper;
    private List<InterfaceIntrospector> interfaceIntrospectors;
    private List<OperationIntrospector> operationIntrospectors;


    public DefaultContractProcessor(@Reference IntrospectionHelper helper) {
        this.helper = helper;
        interfaceIntrospectors = new ArrayList<InterfaceIntrospector>();
        operationIntrospectors = new ArrayList<OperationIntrospector>();
    }

    @Reference(required = false)
    public void setInterfaceIntrospectors(List<InterfaceIntrospector> interfaceIntrospectors) {
        this.interfaceIntrospectors = interfaceIntrospectors;
    }

    @Reference(required = false)
    public void setOperationIntrospectors(List<OperationIntrospector> operationIntrospectors) {
        this.operationIntrospectors = operationIntrospectors;
    }

    public ServiceContract<Type> introspect(TypeMapping typeMapping, Type type, IntrospectionContext context) {
        if (type instanceof Class) {
            return introspect(typeMapping, (Class<?>) type, context);
        } else {
            throw new UnsupportedOperationException("Interface introspection is only supported for classes");
        }
    }

    private JavaServiceContract introspect(TypeMapping typeMapping, Class<?> interfaze, IntrospectionContext context) {
        JavaServiceContract contract = introspectInterface(typeMapping, interfaze, context);
        Callback callback = interfaze.getAnnotation(Callback.class);
        if (callback != null) {
            Class<?> callbackClass = callback.value();
            processCallback(typeMapping, interfaze, callbackClass, context, contract);
        } else {
            org.oasisopen.sca.annotation.Callback oasisCallback = interfaze.getAnnotation(org.oasisopen.sca.annotation.Callback.class);
            if (oasisCallback != null) {
                Class<?> callbackClass = oasisCallback.value();
                processCallback(typeMapping, interfaze, callbackClass, context, contract);
            }
        }
        return contract;
    }

    private void processCallback(TypeMapping typeMapping,
                                 Class<?> interfaze,
                                 Class<?> callbackClass,
                                 IntrospectionContext context,
                                 JavaServiceContract contract) {
        if (Void.class.equals(callbackClass)) {
            context.addError(new MissingCallback(interfaze));
            return;
        }
        JavaServiceContract callbackContract = introspectInterface(typeMapping, callbackClass, context);
        contract.setCallbackContract(callbackContract);
    }

    /**
     * Introspects a class, returning its service contract. Errors and warnings are reported in the ValidationContext.
     *
     * @param typeMapping generics mappings
     * @param interfaze   the interface to introspect
     * @param context     the current validation context to report errors
     * @return the service contract
     */
    private JavaServiceContract introspectInterface(TypeMapping typeMapping, Class<?> interfaze, IntrospectionContext context) {
        JavaServiceContract contract = new JavaServiceContract(interfaze);
        contract.setInterfaceName(interfaze.getSimpleName());

        boolean remotable =
                interfaze.isAnnotationPresent(org.oasisopen.sca.annotation.Remotable.class) || interfaze.isAnnotationPresent(Remotable.class);
        contract.setRemotable(remotable);

        boolean conversational = helper.isAnnotationPresent(interfaze, Conversational.class);
        contract.setConversational(conversational);

        List<Operation<Type>> operations = getOperations(typeMapping, interfaze, remotable, conversational, context);
        contract.setOperations(operations);
        for (InterfaceIntrospector introspector : interfaceIntrospectors) {
            introspector.introspect(contract, interfaze, context);
        }
        return contract;
    }

    private <T> List<Operation<Type>> getOperations(TypeMapping typeMapping,
                                                    Class<T> type,
                                                    boolean remotable,
                                                    boolean conversational,
                                                    IntrospectionContext context) {
        Method[] methods = type.getMethods();
        List<Operation<Type>> operations = new ArrayList<Operation<Type>>(methods.length);
        for (Method method : methods) {
            String name = method.getName();
            if (remotable) {
                boolean error = false;
                for (Operation<Type> operation : operations) {
                    if (operation.getName().equals(name)) {
                        context.addError(new OverloadedOperation(method));
                        error = true;
                        break;
                    }
                }
                if (error) {
                    continue;
                }
            }

            Class<?> returnType = method.getReturnType();
            Class<?>[] paramTypes = method.getParameterTypes();
            Class<?>[] faultTypes = method.getExceptionTypes();

            int conversationSequence = NO_CONVERSATION;
            if (method.isAnnotationPresent(EndsConversation.class)) {
                if (!conversational) {
                    context.addError(new InvalidConversationalOperation(method));
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

            DataType<List<DataType<Type>>> inputType = new DataType<List<DataType<Type>>>(Object[].class, paramDataTypes);
            Operation<Type> operation = new Operation<Type>(name, inputType, returnDataType, faultDataTypes, conversationSequence);

            if (method.isAnnotationPresent(org.oasisopen.sca.annotation.OneWay.class) || method.isAnnotationPresent(OneWay.class)) {
                operation.addIntent(ONEWAY_INTENT);
            }
            for (OperationIntrospector introspector : operationIntrospectors) {
                introspector.introspect(operation, method, context);
            }
            operations.add(operation);
        }
        return operations;
    }

}
