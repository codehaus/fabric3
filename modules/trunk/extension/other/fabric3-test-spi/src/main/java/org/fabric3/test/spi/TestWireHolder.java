/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.test.spi;

import java.net.URI;
import java.util.Map;

import org.fabric3.host.Names;
import org.fabric3.spi.wire.Wire;

/**
 * Provides wires to test components used by a integration test runtime for test dispatching.
 *
 * @version $Revision$ $Date$
 */
public interface TestWireHolder {
    
    URI COMPONENT_URI = URI.create(Names.RUNTIME_NAME + "/TestWireHolder");

    /**
     * Adds a wire to a test component keyed by test name.
     *
     * @param testName the test name
     * @param wire     the wire
     */
    void add(String testName, Wire wire);


    /**
     * The wires to test components keyed by test name.
     *
     * @return wires to test components keyed by test name
     */
    Map<String, Wire> getWires();

}
