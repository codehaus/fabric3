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
package org.fabric3.ftp.server.passive;

import java.util.Stack;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;

/**
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class PassiveConnectionServiceImpl implements PassiveConnectionService {
    
    private int minPort = 6000;
    private int maxPort = 7000;
    private Stack<Integer> ports = new Stack<Integer>();
    
    /**
     * Sets the minimum passive port.
     * @param minPort Minimum passive port.
     */
    @Property
    public void setMinPort(int minPort) {
        this.minPort = minPort;
    }

    /**
     * Sets the maximum passive port.
     * @param maxPort Maximum passive port.
     */
    @Property
    public void setMaxPort(int maxPort) {
        this.maxPort = maxPort;
    }
    
    /**
     * Initializes the port.
     */
    @Init
    public void init() {
        for (int i = minPort;i <= maxPort;i++) {
            ports.push(i);
        }
    }
    
    /**
     * Acquires the next available pasive port.
     * 
     * @return Next available passive port.
     * @throws InterruptedException 
     */
    public synchronized int acquire() throws InterruptedException {
        while (ports.empty()) {
            wait();
        }
        return ports.pop();
    }
    
    /**
     * Release a passive port.
     * @param port Port to be released.
     */
    public synchronized void release(int port) {
        ports.push(port);
        notifyAll();
    }

}
