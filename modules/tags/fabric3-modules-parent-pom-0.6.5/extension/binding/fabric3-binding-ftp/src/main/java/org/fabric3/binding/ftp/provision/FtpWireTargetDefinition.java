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
package org.fabric3.binding.ftp.provision;

import java.net.URI;

import org.fabric3.binding.ftp.common.Constants;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;

/**
 * @version $Revision$ $Date$
 */
public class FtpWireTargetDefinition extends PhysicalWireTargetDefinition {
    private final URI classLoaderId;
    private final boolean active;
    private final FtpSecurity security;
    private int connectTimeout;
    private int socketTimeout;

    /**
     * Initializes the classloader id and transfer mode.
     *
     * @param classLoaderId the classloader id to deserialize parameters in
     * @param active        FTP transfer mode
     * @param security      Security parameters
     */
    public FtpWireTargetDefinition(URI classLoaderId, boolean active, FtpSecurity security) {
        this(classLoaderId, active, security, Constants.NO_TIMEOUT, Constants.NO_TIMEOUT);
    }

    /**
     * Initializes the classloader id, transfer mode, and timeout.
     *
     * @param classLoaderId  the classloader id to deserialize parameters in
     * @param active         FTP transfer mode
     * @param security       Security parameters
     * @param connectTimeout the timeout to use for opening socket connections
     * @param socketTimeout  the timeout to use for blocking connection operations
     */
    public FtpWireTargetDefinition(URI classLoaderId, boolean active, FtpSecurity security, int connectTimeout, int socketTimeout) {
        this.classLoaderId = classLoaderId;
        this.active = active;
        this.security = security;
        this.connectTimeout = connectTimeout;
        this.socketTimeout = socketTimeout;
    }

    /**
     * Returns the classloader id to deserialize parameters in.
     *
     * @return the classloader id to deserialize parameters in
     */
    public URI getClassLoaderId() {
        return classLoaderId;
    }

    /**
     * Gets the FTP transfer mode.
     *
     * @return True if user wants active transfer mode.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Get the security parameters.
     *
     * @return Get the security parameters.
     */
    public FtpSecurity getSecurity() {
        return security;
    }

    /**
     * Returns the timeout value to use for opening connections or {@link Constants#NO_TIMEOUT} if none is set.
     *
     * @return the timeout value to use for opening connections or {@link Constants#NO_TIMEOUT} if none is set
     */
    public int getConectTimeout() {
        return connectTimeout;
    }

    /**
     * Sets the timeout value to use for opening connections.
     *
     * @param socketTimeout the timeout value
     */
    public void setConnectTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    /**
     * Returns the timeout value to use for blocking operations or {@link Constants#NO_TIMEOUT} if none is set.
     *
     * @return the timeout value to use for blocking operations or {@link Constants#NO_TIMEOUT} if none is set
     */
    public int getSocketTimeout() {
        return socketTimeout;
    }

    /**
     * Sets the timeout value to use for blocking operations.
     *
     * @param socketTimeout the timeout value
     */
    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

}
