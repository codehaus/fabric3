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
package org.fabric3.scdl;

/**
 * Represents an operation definition in the SCDL. 
 * 
 * This is mainly used for declaring operation level intents and policy 
 * sets in the SCDL. The SCA specification currently doesn't support 
 * overloaded operations to be differentiated in the SCDL.
 * 
 * @version $Revision$ $Date$
 */
public class OperationDefinition extends AbstractPolicyAware {
    
    private String name;

    /**
     * @return Name of the operation as defined in the SCDL.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name Name of the operation as defined in the SCDL.
     */
    public void setName(String name) {
        this.name = name;
    }

}
