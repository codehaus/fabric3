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
package org.fabric3.binding.rmi.wire;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.osoa.sca.ServiceUnavailableException;

public class RmiReferenceFactory {

    private final String host;
    private final int port;
    private final String serviceName;

    /* package */ RmiReferenceFactory(String serviceName, String host, int port) {
        this.serviceName = serviceName;
        this.host = host;
        this.port = port;
    }

    public Object getReference() {
        Registry registry;
        try {
            registry = LocateRegistry.getRegistry(host, port);
            return registry.lookup(serviceName);
        } catch (RemoteException e) {
            throw new ServiceUnavailableException(serviceName, e);
        } catch (NotBoundException e) {
            throw new ServiceUnavailableException(serviceName, e);
        }
    }


}

