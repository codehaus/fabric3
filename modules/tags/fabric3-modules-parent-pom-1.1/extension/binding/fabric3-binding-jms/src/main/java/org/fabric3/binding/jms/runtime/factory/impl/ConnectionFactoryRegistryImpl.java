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
package org.fabric3.binding.jms.runtime.factory.impl;

import java.util.HashMap;
import java.util.Map;
import javax.jms.ConnectionFactory;

import org.osoa.sca.annotations.EagerInit;

import org.fabric3.binding.jms.runtime.factory.ConnectionFactoryRegistry;

/**
 * Default ConnectionFactoryRegistry implementaton.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class ConnectionFactoryRegistryImpl implements ConnectionFactoryRegistry {
    private Map<String, ConnectionFactory> factories = new HashMap<String, ConnectionFactory>();

    public ConnectionFactory get(String name) {
        return factories.get(name);
    }

    public void register(String name, ConnectionFactory factory) {
        factories.put(name, factory);
    }

    public void unregister(String name) {
        factories.remove(name);
    }
}
