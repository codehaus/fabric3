/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
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
package org.fabric3.spi.wire;

import org.fabric3.spi.model.physical.PhysicalOperationDefinition;

/**
 * A wire consists of 1..n invocation chains associated with the operations of its source service contract.
 * <p/>
 * Invocation chains contain <code>Interceptors</code> that process invocations in an around-style manner.
 *
 * @version $Rev$ $Date$
 */
public interface InvocationChain {

    /**
     * Returns the target physical operation for this invocation chain.
     *
     * @return the target physical operation for this invocation chain
     */
    PhysicalOperationDefinition getPhysicalOperation();

    /**
     * Adds an interceptor to the chain
     *
     * @param interceptor the interceptor to add
     */
    void addInterceptor(Interceptor interceptor);

    /**
     * Adds an interceptor at the given position in the interceptor stack
     *
     * @param index       the position in the interceptor stack to add the interceptor
     * @param interceptor the interceptor to add
     */
    void addInterceptor(int index, Interceptor interceptor);

    /**
     * Returns the first interceptor in the chain.
     *
     * @return the first interceptor in the chain
     */
    Interceptor getHeadInterceptor();

    /**
     * Returns the last interceptor in the chain.
     *
     * @return the last interceptor in the chain
     */
    Interceptor getTailInterceptor();

}
