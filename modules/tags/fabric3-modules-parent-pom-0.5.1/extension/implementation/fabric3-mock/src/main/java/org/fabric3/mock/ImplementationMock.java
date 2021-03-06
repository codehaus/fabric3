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

import javax.xml.namespace.QName;

import org.fabric3.scdl.Implementation;
import org.fabric3.spi.Constants;

/**
 * Implementation type for mock components.
 * 
 * @version $Revision$ $Date$
 */
public class ImplementationMock extends Implementation<MockComponentType> {
    
    static final QName IMPLEMENTATION_MOCK = new QName(Constants.FABRIC3_NS, "implementation.mock");
    
    private final List<String> mockedInterfaces;
    
    /**
     * Initializes the mocked interfaces.
     * 
     * @param mockedInterfaces Mocked interfaces.
     */
    public ImplementationMock(List<String> mockedInterfaces, MockComponentType componentType) {
        super(componentType);
        this.mockedInterfaces = mockedInterfaces;
    }
    
    /**
     * Gets the interfaces that are mocked.
     * 
     * @return Interfaces that are mocked.
     */
    public List<String> getMockedInterfaces() {
        return mockedInterfaces;
    }
    
    /**
     * Gets the component type qualified name.
     */
    @Override
    public QName getType() {
        return IMPLEMENTATION_MOCK;
    }

}
