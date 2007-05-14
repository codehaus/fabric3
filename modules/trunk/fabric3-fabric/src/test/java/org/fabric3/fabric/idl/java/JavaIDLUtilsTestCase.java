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

import junit.framework.TestCase;
import junit.framework.Assert;

import org.fabric3.spi.model.type.DataType;
import org.fabric3.spi.model.type.Operation;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;

/**
 * @version $Rev$ $Date$
 */
public class JavaIDLUtilsTestCase extends TestCase {
    private Method method;
    private Method methodString;
    private PhysicalOperationDefinition physicalOperationDefinition;
    private PhysicalOperationDefinition physicalOperationDefinitionString;
    private List<PhysicalOperationDefinition> physicalOperations;
    private Operation<?> operation;
    private Operation<?> operationString;
    private List<Operation<?>> operations;
    private Method methodOverload;
    private Operation<Type> operationStringInt;

    public void testFindMethod() throws Exception {
        Assert.assertEquals(method, JavaIDLUtils.findMethod(Foo.class, physicalOperationDefinition));
        assertEquals(methodString, JavaIDLUtils.findMethod(Foo.class, physicalOperationDefinitionString));
    }

    public void testMatchPhysicalOperation() throws Exception {
        assertEquals(physicalOperationDefinitionString,
                     JavaIDLUtils.matchPhysicalOperation(methodString, physicalOperations));
        assertEquals(physicalOperationDefinition,
                     JavaIDLUtils.matchPhysicalOperation(method, physicalOperations));
    }

    public void testMatchOperation() throws Exception {
        assertEquals(operationString,
                     JavaIDLUtils.matchOperation(methodString, operations));
        assertEquals(operation,
                     JavaIDLUtils.matchOperation(method, operations));
        assertEquals(operationStringInt,
                     JavaIDLUtils.matchOperation(methodOverload, operations));

    }

    protected void setUp() throws Exception {
        super.setUp();
        method = Foo.class.getMethod("operation");
        methodString = Foo.class.getMethod("operation", String.class);
        methodOverload = Foo.class.getMethod("operationOverload", String.class, Integer.TYPE);
        physicalOperationDefinition = new PhysicalOperationDefinition();
        physicalOperationDefinition.setName("operation");
        physicalOperationDefinitionString = new PhysicalOperationDefinition();
        physicalOperationDefinitionString.setName("operation");
        physicalOperationDefinitionString.addParameter(String.class.getName());
        physicalOperations = new ArrayList<PhysicalOperationDefinition>();
        physicalOperations.add(physicalOperationDefinition);
        physicalOperations.add(physicalOperationDefinitionString);

        List<DataType<Type>> params = new ArrayList<DataType<Type>>();
        DataType<List<DataType<Type>>> paramType = new DataType<List<DataType<Type>>>(String.class, params);
        operation = new Operation<Type>("operation", paramType, null, null);

        DataType<Type> param = new DataType<Type>(String.class, String.class);
        params = new ArrayList<DataType<Type>>();
        params.add(param);
        paramType = new DataType<List<DataType<Type>>>(String.class, params);
        operationString = new Operation<Type>("operation", paramType, null, null);


        DataType<Type> param1 = new DataType<Type>(String.class, String.class);
        DataType<Type> param2 = new DataType<Type>(Integer.TYPE, Integer.TYPE);
        List<DataType<Type>> paramsStringInt = new ArrayList<DataType<Type>>();
        paramsStringInt.add(param1);
        paramsStringInt.add(param2);
        DataType<List<DataType<Type>>> paramTypeStringInt = new DataType<List<DataType<Type>>>(String.class, paramsStringInt);
        operationStringInt = new Operation<Type>("operationOverload", paramTypeStringInt, null, null);


        operations = new ArrayList<Operation<?>>();
        operations.add(operation);
        operations.add(operationString);
        operations.add(operationStringInt);
    }

    private interface Foo {
        void operation();

        void operation(String param);

        void operationOverload(String param1, String param2);

        void operationOverload(String param1, int param2);

    }
}



