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
package org.fabric3.scanner.impl;

import org.fabric3.api.annotation.logging.Fine;
import org.fabric3.api.annotation.logging.Info;
import org.fabric3.api.annotation.logging.Severe;

/**
 * Monitoring interface for the DirectoryScanner
 *
 * @version $Rev$ $Date$
 */
public interface ScannerMonitor {

    /**                
     * Called when a destination is notified of a new resource
     *
     * @param name the name of the resource
     */
    @Info
    void add(String name);

    /**
     * Called when a destination is notified of a resource removal
     *
     * @param name the name of the resource
     */
    @Fine
    void remove(String name);

    /**
     * Called when a destination is notified of a resource update
     *
     * @param name the name of the resource
     */
    @Fine
    void update(String name);

    /**
     * Called when a general error is encountered processing an entry
     *
     * @param e the error
     */
    @Severe
    void error(Throwable e);

    /**
     * Called when an error is encountered during recovery
     *
     * @param e the error
     */
    @Severe
    void recoveryError(Throwable e);

    /**
     * Called when an error is encountered removing an entry
     *
     * @param filename the file being removed
     * @param e        the error
     */
    @Severe
    void removalError(String filename, Throwable e);

    /**
     * Called when errors are encountered processing contributions
     *
     * @param description a description of the errors
     */
    @Severe
    void contributionErrors(String description);

    /**
     * Called when errors are encountered during deployments
     *
     * @param description a description of the errors
     */
    @Severe
    void deploymentErrors(String description);

}
