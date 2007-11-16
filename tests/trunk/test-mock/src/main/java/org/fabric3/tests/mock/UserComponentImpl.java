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
package org.fabric3.tests.mock;

import org.osoa.sca.annotations.Reference;

/**
 * @version $Revision$ $Date$
 */
public class UserComponentImpl implements UserComponent {
    
    private MockService1 mockService1;
    private MockService2 mockService2;
    
    @Reference
    public void setMockService1(MockService1 mockService1) {
        this.mockService1 = mockService1;
    }
    
    @Reference
    public void setMockService2(MockService2 mockService2) {
        this.mockService2 = mockService2;
    }
    
    /**
     * @see org.fabric3.tests.mock.UserComponent#userMethod()
     */
    public void userMethod() {
        mockService1.doMock1("test");
        mockService2.doMock2(1);
        mockService2.doMock0(1);
    }

}
