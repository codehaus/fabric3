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
package org.fabric3.tests.binding.axis2;

import org.apache.axiom.om.OMElement;

/**
 * @version $Rev$ $Date$
 */
public class Axis2EchoServiceImpl implements Axis2EchoService {

    public OMElement echoWs(OMElement operation) {
        return operation.getFirstElement();
    }

    public OMElement echoRampart(OMElement operation) {
        return operation.getFirstElement();
    }

    public OMElement echoNoSecurity(OMElement operation) {
        return operation.getFirstElement();
    }

}
