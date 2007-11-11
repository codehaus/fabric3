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
package org.fabric3.mock;

import java.util.List;

import org.fabric3.scdl.ModelObject;

/**
 * Instance factory definition for mocked components.
 * 
 * @version $Revision$ $Date$
 */
public class MockInstanceFactoryDefinition extends ModelObject {
    
    private List<String> mockedInterfaces;

    /**
     * @return Gets all the interfaces to be mocked.
     */
    public List<String> getMockedInterfaces() {
        return mockedInterfaces;
    }

    /**
     * @param mockedInterfaces Sets all the interfaces to be mocked.
     */
    public void setMockedInterfaces(List<String> mockedInterfaces) {
        this.mockedInterfaces = mockedInterfaces;
    }

}
