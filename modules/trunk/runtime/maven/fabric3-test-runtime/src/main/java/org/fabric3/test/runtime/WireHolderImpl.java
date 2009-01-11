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
package org.fabric3.test.runtime;

import java.util.LinkedHashMap;
import java.util.Map;

import org.fabric3.test.spi.TestWireHolder;
import org.fabric3.spi.wire.Wire;

/**
 * TestWireHolder implementation for the Maven runtime.
 *
 * @version $Revision$ $Date$
 */
public class WireHolderImpl implements TestWireHolder {
    Map<String, Wire> wires = new LinkedHashMap<String, Wire>();

    public Map<String, Wire> getWires() {
        return wires;
    }

    public void add(String testName, Wire wire) {
        wires.put(testName, wire);
    }
}
