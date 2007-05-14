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

import org.fabric3.spi.model.type.ServiceContract;
import org.fabric3.fabric.wire.IncompatibleServiceContractException;

/**
 * Temporary until this capability moves into the controller infrastructure
 *
 * @version $Rev$ $Date$
 */
public interface ContractCompatibilityService {
    /**
     * Check the compatiblity of the source and the target service contracts.<p> A wire may only connect a source to a
     * target if the target implements an interface that is compatible with the interface required by the source. The
     * source and the target are compatible if:
     * <p/>
     * <ol> <li>the source interface and the target interface MUST either both be remotable or they are both local
     * <li>the methods on the target interface MUST be the same as or be a superset of the methods in the interface
     * specified on the source <li>compatibility for the individual method is defined as compatibility of the signature,
     * that is method name, input types, and output types MUST BE the same. <li>the order of the input and output types
     * also MUST BE the same. <li>the set of Faults and Exceptions expected by the source MUST BE the same or be a
     * superset of those specified by the service. <li>other specified attributes of the two interfaces MUST match,
     * including Scope and Callback interface </ol>
     * <p/>
     * <p>Please note this test is not symetric: the success of checkCompatibility(A, B) does NOT imply
     * checkCompatibility(B, A)
     *
     * @param source         The source service contract
     * @param target         The target service contract
     * @param ignoreCallback Indicate the callback should be checked
     * @param silent         if true, errors will be thrown if the service contracts are not compatible
     * @return true if the service contracts are compatible
     * @throws IncompatibleServiceContractException
     *          If the source service contract is not compatible with the target one
     *          <p/>
     *          TODO JFM this method should be moved from this interface to the allocator phase
     */
    boolean checkCompatibility(ServiceContract<?> source,
                               ServiceContract<?> target,
                               boolean ignoreCallback,
                               boolean silent) throws IncompatibleServiceContractException;


}
