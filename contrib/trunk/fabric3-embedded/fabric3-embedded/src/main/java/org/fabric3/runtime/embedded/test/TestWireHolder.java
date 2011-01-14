package org.fabric3.runtime.embedded.test;

import org.fabric3.spi.wire.Wire;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Michal Capo
 */
public class TestWireHolder implements org.fabric3.test.spi.TestWireHolder {

    Map<String, Wire> wires = new LinkedHashMap<String, Wire>();

    public Map<String, Wire> getWires() {
        return wires;
    }

    public void add(String testName, Wire wire) {
        wires.put(testName, wire);
    }

}
