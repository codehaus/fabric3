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
package org.fabric3.junit.runtime;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashMap;

import org.fabric3.spi.wire.Wire;

/**
 * Implementation of a WireHolder used to contain wires targetted to JUnit components.
 *
 * @version $Revision$ $Date$
 */
public class JUnitWireHolderImpl implements WireHolder {
    Map<String, Wire> wires = new LinkedHashMap<String, Wire>();

    public int size() {
        return wires.size();
    }

    public boolean isEmpty() {
        return wires.isEmpty();
    }

    public boolean containsKey(Object key) {
        return wires.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return wires.containsValue(value);
    }

    public Wire get(Object key) {
        return wires.get(key);
    }

    public Wire put(String key, Wire value) {
        return wires.put(key, value);
    }

    public Wire remove(Object key) {
        return wires.remove(key);
    }

    public void putAll(Map<? extends String, ? extends Wire> t) {
        wires.putAll(t);
    }

    public void clear() {
        wires.clear();
    }

    public Set<String> keySet() {
        return wires.keySet();
    }

    public Collection<Wire> values() {
        return wires.values();
    }

    public Set<Entry<String, Wire>> entrySet() {
        return wires.entrySet();
    }
}
