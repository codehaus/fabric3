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
package org.fabric3.fabric.wire;

import java.lang.reflect.Type;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.fabric3.scdl.DataType;
import org.fabric3.scdl.Operation;
import org.fabric3.scdl.ServiceContract;
import static org.fabric3.scdl.Operation.NO_CONVERSATION;

/**
 * TODO some tests commented out due to DataType.equals() needing to be strict
 *
 * @version $Rev$ $Date$
 */
public class ContractCompatibilityServiceImplTestCase extends TestCase {

    private ContractCompatibilityService proxyService = new ContractCompatibilityServiceImpl();

    public void testNoOperation() throws Exception {
        ServiceContract source = new MockContract<Type>();
        ServiceContract target = new MockContract<Type>();
        proxyService.checkCompatibility(source, target, false, false);
    }

    public void testBasic() throws Exception {
        ServiceContract<Type> source = new MockContract<Type>();
        Operation<Type> opSource1 = new Operation<Type>("op1", null, null, null, false, false, null, NO_CONVERSATION);
        List<Operation<Type>> sourceOperations = new ArrayList<Operation<Type>>();
        sourceOperations.add(opSource1);
        source.setOperations(sourceOperations);
        ServiceContract<Type> target = new MockContract<Type>();
        Operation<Type> opSource2 = new Operation<Type>("op1", null, null, null, false, false, null, NO_CONVERSATION);
        List<Operation<Type>> targetOperations = new ArrayList<Operation<Type>>();
        targetOperations.add(opSource2);
        target.setOperations(targetOperations);
        proxyService.checkCompatibility(source, target, false, false);
    }

    public void testBasicIncompatibleOperationNames() throws Exception {
        ServiceContract<Type> source = new MockContract<Type>();
        Operation<Type> opSource1 = new Operation<Type>("op1", null, null, null, false, false, null, NO_CONVERSATION);
        List<Operation<Type>> sourceOperations = new ArrayList<Operation<Type>>();
        sourceOperations.add(opSource1);
        source.setOperations(sourceOperations);
        ServiceContract<Type> target = new MockContract<Type>();
        Operation<Type> opSource2 = new Operation<Type>("op2", null, null, null, false, false, null, NO_CONVERSATION);
        List<Operation<Type>> targetOperations = new ArrayList<Operation<Type>>();
        targetOperations.add(opSource2);
        target.setOperations(targetOperations);
        try {
            proxyService.checkCompatibility(source, target, false, false);
            fail();
        } catch (IncompatibleServiceContractException e) {
            //expected
        }
    }

    public void testInputTypes() throws Exception {
        ServiceContract<Type> source = new MockContract<Type>();
        List<DataType<Type>> sourceInputTypes = new ArrayList<DataType<Type>>();
        sourceInputTypes.add(new DataType<Type>(Object.class, Object.class));
        DataType<List<DataType<Type>>> inputType = new DataType<List<DataType<Type>>>(String.class, sourceInputTypes);
        Operation<Type> opSource1 = new Operation<Type>("op1", inputType, null, null, false,
                                                        false, null, NO_CONVERSATION);
        List<Operation<Type>> sourceOperations = new ArrayList<Operation<Type>>();
        sourceOperations.add(opSource1);
        source.setOperations(sourceOperations);

        ServiceContract<Type> target = new MockContract<Type>();
        List<DataType<Type>> targetInputTypes = new ArrayList<DataType<Type>>();
        targetInputTypes.add(new DataType<Type>(Object.class, Object.class));
        DataType<List<DataType<Type>>> targetInputType =
                new DataType<List<DataType<Type>>>(String.class, targetInputTypes);

        Operation<Type> opTarget =
                new Operation<Type>("op1", targetInputType, null, null, false, false, null, NO_CONVERSATION);
        List<Operation<Type>> targetOperations = new ArrayList<Operation<Type>>();
        targetOperations.add(opTarget);
        target.setOperations(targetOperations);
        proxyService.checkCompatibility(source, target, false, false);
    }


    public void testIncompatibleInputTypes() throws Exception {
        ServiceContract<Type> source = new MockContract<Type>();
        List<DataType<Type>> sourceInputTypes = new ArrayList<DataType<Type>>();
        sourceInputTypes.add(new DataType<Type>(Integer.class, Integer.class));
        DataType<List<DataType<Type>>> inputType = new DataType<List<DataType<Type>>>(String.class, sourceInputTypes);
        Operation<Type> opSource1 = new Operation<Type>("op1", inputType, null, null, false,
                                                        false, null, NO_CONVERSATION);
        List<Operation<Type>> sourceOperations = new ArrayList<Operation<Type>>();
        sourceOperations.add(opSource1);
        source.setOperations(sourceOperations);

        ServiceContract<Type> target = new MockContract<Type>();
        List<DataType<Type>> targetInputTypes = new ArrayList<DataType<Type>>();
        targetInputTypes.add(new DataType<Type>(String.class, String.class));
        DataType<List<DataType<Type>>> targetInputType =
                new DataType<List<DataType<Type>>>(String.class, targetInputTypes);

        Operation<Type> opTarget =
                new Operation<Type>("op1", targetInputType, null, null, false, false, null, NO_CONVERSATION);
        List<Operation<Type>> targetOperations = new ArrayList<Operation<Type>>();
        targetOperations.add(opTarget);
        target.setOperations(targetOperations);
        try {
            proxyService.checkCompatibility(source, target, false, false);
            fail();
        } catch (IncompatibleServiceContractException e) {
            //expected
        }
    }

    /**
     * Verfies source input types can be super types of the target
     */
    public void testSourceSuperTypeInputCompatibility() throws Exception {
//        ServiceContract<Type> source = new MockContract<Type>();
//        List<DataType<Type>> sourceInputTypes = new ArrayList<DataType<Type>>();
//        sourceInputTypes.add(new DataType<Type>(Object.class, Object.class));
//        DataType<List<DataType<Type>>> inputType = new DataType<List<DataType<Type>>>(String.class, sourceInputTypes);
//        Operation<Type> opSource1 = new Operation<Type>("op1", inputType, null, null, false, null);
//        Map<String, Operation<Type>> sourceOperations = new HashMap<String, Operation<Type>>();
//        sourceOperations.put("op1", opSource1);
//        source.setOperations(sourceOperations);
//
//        ServiceContract<Type> target = new MockContract<Type>();
//        List<DataType<Type>> targetInputTypes = new ArrayList<DataType<Type>>();
//        targetInputTypes.add(new DataType<Type>(String.class, String.class));
//        DataType<List<DataType<Type>>> targetInputType =
//            new DataType<List<DataType<Type>>>(String.class, targetInputTypes);
//
//        Operation<Type> opTarget = new Operation<Type>("op1", targetInputType, null, null, false, null);
//        Map<String, Operation<Type>> targetOperations = new HashMap<String, Operation<Type>>();
//        targetOperations.put("op1", opTarget);
//        target.setOperations(targetOperations);
//        wireService.checkCompatibility(source, target, false);
    }

    public void testOutputTypes() throws Exception {
        ServiceContract<Type> source = new MockContract<Type>();
        DataType<Type> sourceOutputType = new DataType<Type>(String.class, String.class);
        Operation<Type> opSource1 =
                new Operation<Type>("op1", null, sourceOutputType, null, false, false, null, NO_CONVERSATION);
        List<Operation<Type>> sourceOperations = new ArrayList<Operation<Type>>();
        sourceOperations.add(opSource1);
        source.setOperations(sourceOperations);

        ServiceContract<Type> target = new MockContract<Type>();
        DataType<Type> targetOutputType = new DataType<Type>(String.class, String.class);
        Operation<Type> opTarget =
                new Operation<Type>("op1", null, targetOutputType, null, false, false, null, NO_CONVERSATION);
        List<Operation<Type>> targetOperations = new ArrayList<Operation<Type>>();
        targetOperations.add(opTarget);
        target.setOperations(targetOperations);
        proxyService.checkCompatibility(source, target, false, false);
    }

    /**
     * Verfies a return type that is a supertype of of the target is compatible
     */
    public void testSupertypeOutputTypes() throws Exception {
//        ServiceContract<Type> source = new MockContract<Type>();
//        DataType<Type> sourceOutputType = new DataType<Type>(Object.class, Object.class);
//        Operation<Type> opSource1 = new Operation<Type>("op1", null, sourceOutputType, null, false, null);
//        Map<String, Operation<Type>> sourceOperations = new HashMap<String, Operation<Type>>();
//        sourceOperations.put("op1", opSource1);
//        source.setOperations(sourceOperations);
//
//        ServiceContract<Type> target = new MockContract<Type>();
//        DataType<Type> targetOutputType = new DataType<Type>(String.class, String.class);
//        Operation<Type> opTarget = new Operation<Type>("op1", null, targetOutputType, null, false, null);
//        Map<String, Operation<Type>> targetOperations = new HashMap<String, Operation<Type>>();
//        targetOperations.put("op1", opTarget);
//        target.setOperations(targetOperations);
//        wireService.checkCompatibility(source, target, false);
    }

    public void testIncompatibleOutputTypes() throws Exception {
        ServiceContract<Type> source = new MockContract<Type>();
        DataType<Type> sourceOutputType = new DataType<Type>(String.class, String.class);
        Operation<Type> opSource1 =
                new Operation<Type>("op1", null, sourceOutputType, null, false, false, null, NO_CONVERSATION);
        List<Operation<Type>> sourceOperations = new ArrayList<Operation<Type>>();
        sourceOperations.add(opSource1);
        source.setOperations(sourceOperations);

        ServiceContract<Type> target = new MockContract<Type>();
        DataType<Type> targetOutputType = new DataType<Type>(Integer.class, Integer.class);
        Operation<Type> opTarget =
                new Operation<Type>("op1", null, targetOutputType, null, false, false, null, NO_CONVERSATION);
        List<Operation<Type>> targetOperations = new ArrayList<Operation<Type>>();
        targetOperations.add(opTarget);
        target.setOperations(targetOperations);
        try {
            proxyService.checkCompatibility(source, target, false, false);
            fail();
        } catch (IncompatibleServiceContractException e) {
            //expected
        }
    }

    public void testFaultTypes() throws Exception {
        ServiceContract<Type> source = new MockContract<Type>();
        DataType<Type> sourceFaultType = new DataType<Type>(String.class, String.class);
        List<DataType<Type>> sourceFaultTypes = new ArrayList<DataType<Type>>();
        sourceFaultTypes.add(0, sourceFaultType);
        Operation<Type> opSource1 =
                new Operation<Type>("op1", null, null, sourceFaultTypes, false, false, null, NO_CONVERSATION);
        List<Operation<Type>> sourceOperations = new ArrayList<Operation<Type>>();
        sourceOperations.add(opSource1);
        source.setOperations(sourceOperations);

        ServiceContract<Type> target = new MockContract<Type>();
        DataType<Type> targetFaultType = new DataType<Type>(String.class, String.class);
        List<DataType<Type>> targetFaultTypes = new ArrayList<DataType<Type>>();
        targetFaultTypes.add(0, targetFaultType);

        Operation<Type> opTarget =
                new Operation<Type>("op1", null, null, targetFaultTypes, false, false, null, NO_CONVERSATION);
        List<Operation<Type>> targetOperations = new ArrayList<Operation<Type>>();
        targetOperations.add(opTarget);
        target.setOperations(targetOperations);
        proxyService.checkCompatibility(source, target, false, false);
    }

    public void testSourceFaultTargetNoFaultCompatibility() throws Exception {
        ServiceContract<Type> source = new MockContract<Type>();
        DataType<Type> sourceFaultType = new DataType<Type>(String.class, String.class);
        List<DataType<Type>> sourceFaultTypes = new ArrayList<DataType<Type>>();
        sourceFaultTypes.add(0, sourceFaultType);
        Operation<Type> opSource1 =
                new Operation<Type>("op1", null, null, sourceFaultTypes, false, false, null, NO_CONVERSATION);
        List<Operation<Type>> sourceOperations = new ArrayList<Operation<Type>>();
        sourceOperations.add(opSource1);
        source.setOperations(sourceOperations);

        ServiceContract<Type> target = new MockContract<Type>();
        Operation<Type> opTarget = new Operation<Type>("op1", null, null, null, false, false, null, NO_CONVERSATION);
        List<Operation<Type>> targetOperations = new ArrayList<Operation<Type>>();
        targetOperations.add(opTarget);
        target.setOperations(targetOperations);
        proxyService.checkCompatibility(source, target, false, false);
    }

    /**
     * Verifies a source's fault which is a supertype of the target's fault are compatibile
     *
     * @throws Exception
     */
    public void testFaultSuperTypes() throws Exception {
//        ServiceContract<Type> source = new MockContract<Type>();
//        DataType<Type> sourceFaultType = new DataType<Type>(Exception.class, Exception.class);
//        List<DataType<Type>> sourceFaultTypes = new ArrayList<DataType<Type>>();
//        sourceFaultTypes.add(0, sourceFaultType);
//        Operation<Type> opSource1 = new Operation<Type>("op1", null, null, sourceFaultTypes, false, null);
//        Map<String, Operation<Type>> sourceOperations = new HashMap<String, Operation<Type>>();
//        sourceOperations.put("op1", opSource1);
//        source.setOperations(sourceOperations);
//
//        ServiceContract<Type> target = new MockContract<Type>();
//        DataType<Type> targetFaultType = new DataType<Type>(Fabric3Exception.class, Fabric3Exception.class);
//        List<DataType<Type>> targetFaultTypes = new ArrayList<DataType<Type>>();
//        targetFaultTypes.add(0, targetFaultType);
//
//        Operation<Type> opTarget = new Operation<Type>("op1", null, null, targetFaultTypes, false, null);
//        Map<String, Operation<Type>> targetOperations = new HashMap<String, Operation<Type>>();
//        targetOperations.put("op1", opTarget);
//        target.setOperations(targetOperations);
//        wireService.checkCompatibility(source, target, false);
    }

    /**
     * Verifies a source's faults which are supertypes and a superset of the target's faults are compatibile
     */
    public void testFaultSuperTypesAndSuperset() throws Exception {
//        ServiceContract<Type> source = new MockContract<Type>();
//        DataType<Type> sourceFaultType = new DataType<Type>(Exception.class, Exception.class);
//        DataType<Type> sourceFaultType2 = new DataType<Type>(RuntimeException.class, RuntimeException.class);
//        List<DataType<Type>> sourceFaultTypes = new ArrayList<DataType<Type>>();
//        sourceFaultTypes.add(0, sourceFaultType);
//        sourceFaultTypes.add(1, sourceFaultType2);
//        Operation<Type> opSource1 = new Operation<Type>("op1", null, null, sourceFaultTypes, false, null);
//        Map<String, Operation<Type>> sourceOperations = new HashMap<String, Operation<Type>>();
//        sourceOperations.put("op1", opSource1);
//        source.setOperations(sourceOperations);
//
//        ServiceContract<Type> target = new MockContract<Type>();
//        DataType<Type> targetFaultType = new DataType<Type>(Fabric3Exception.class, Fabric3Exception.class);
//        List<DataType<Type>> targetFaultTypes = new ArrayList<DataType<Type>>();
//        targetFaultTypes.add(0, targetFaultType);
//
//        Operation<Type> opTarget = new Operation<Type>("op1", null, null, targetFaultTypes, false, null);
//        Map<String, Operation<Type>> targetOperations = new HashMap<String, Operation<Type>>();
//        targetOperations.put("op1", opTarget);
//        target.setOperations(targetOperations);
//        wireService.checkCompatibility(source, target, false);
    }

    private class MockContract<T> extends ServiceContract<T> {
        public MockContract() {
        }

        public boolean isAssignableFrom(ServiceContract<?> contract) {
            return false;
        }

    }

}
