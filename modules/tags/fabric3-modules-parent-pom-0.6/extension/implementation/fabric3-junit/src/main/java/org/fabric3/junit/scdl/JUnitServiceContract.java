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
package org.fabric3.junit.scdl;

import java.lang.reflect.Type;
import java.util.List;

import org.fabric3.scdl.Operation;
import org.fabric3.scdl.ServiceContract;

/**
 * @version $Rev$ $Date$
 */
public class JUnitServiceContract extends ServiceContract<Type> {
    private static final long serialVersionUID = -2402977196426881023L;

    public JUnitServiceContract(List<Operation<Type>> operations) {
        setOperations(operations);
    }

    public boolean isAssignableFrom(ServiceContract<?> contract) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getQualifiedInterfaceName() {
        // TODO Identify the qualified interface name for JUnit components - test class?
        return null;
    }

}
