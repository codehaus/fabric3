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
 */
package org.fabric3.tests.mock;

import org.oasisopen.sca.annotation.Reference;

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
