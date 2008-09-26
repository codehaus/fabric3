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
package org.fabric3.idl.wsdl.version;

import java.net.URL;

import org.fabric3.idl.wsdl.version.WsdlVersionChecker.WsdlVersion;

import junit.framework.TestCase;

/**
 * @version $Revision$ $Date$
 */
public class DefaultWsdlVersionCheckerTest extends TestCase {
    
    private WsdlVersionChecker versionChecker = new DefaultWsdlVersionChecker();

    /**
     * Checks for version 1.1
     */
    public void testGetVersion1_1() {        
        URL url = getClass().getClassLoader().getResource("example_1_1.wsdl");
        assertEquals(WsdlVersion.VERSION_1_1, versionChecker.getVersion(url));
    }

    /**
     * Checks for version 2.0
     *
     */
    public void testGetVersion2_0() {     
        URL url = getClass().getClassLoader().getResource("example_2_0.wsdl");
        assertEquals(WsdlVersion.VERSION_2_0, versionChecker.getVersion(url));
    }

    /**
     * Checks for invalid WSDL
     *
     */
    public void testInvalidWsdl() {     
        URL url = getClass().getClassLoader().getResource("invalid.wsdl");
        try {
            versionChecker.getVersion(url);
            fail("Expected to fail");
        } catch(WsdlVersionCheckerException ignore) {
            //
        }
    }

}
