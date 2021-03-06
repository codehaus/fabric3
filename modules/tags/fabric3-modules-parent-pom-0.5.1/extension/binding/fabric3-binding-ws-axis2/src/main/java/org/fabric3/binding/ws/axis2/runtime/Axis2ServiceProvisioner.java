/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.binding.ws.axis2.runtime;

import org.fabric3.binding.ws.axis2.provision.Axis2WireSourceDefinition;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.wire.Wire;

/**
 * @version $Revision$ $Date$
 */
public interface Axis2ServiceProvisioner {
    
    /**
     * Provisions an axis service.
     * 
     * @param pwsd Physical wire source definition.
     * @param wire Wire on which the service is provisioned.
     */
    void provision(Axis2WireSourceDefinition pwsd, Wire wire) throws WiringException;

}
