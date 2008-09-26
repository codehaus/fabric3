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
package org.fabric3.host.runtime;

import org.fabric3.api.annotation.logging.Info;
import org.fabric3.api.annotation.logging.Severe;

/**
 * Event monitor interface for the bootstrap sequence
 *
 * @version $Rev$ $Date$
 */
public interface CoordinatorMonitor {

    /**
     * Called when the runtime is initialized.
     *
     * @param message a message
     */
    @Info
    void initialized(String message);

    /**
     * Called when the runtime has joined a domain.
     *
     * @param message a message
     */
    @Info
    void joinedDomain(String message);

    /**
     * Called when the runtime has performed recovery.
     *
     * @param message a message
     */
    @Info
    void recovered(String message);

    /**
     * Called when the runtime has started.
     *
     * @param message a message
     */
    @Info
    void started(String message);

    /**
     * Called when an exception was thrown during a boostrap operation
     *
     * @param e the exception
     */
    @Severe
    void error(Throwable e);

    /**
     * Called when errors are encountered processing policy intents
     *
     * @param description a description of the errors
     */
    @Severe
    void intentErrors(String description);


    /**
     * Called when errors are encountered processing extensions
     *
     * @param description a description of the errors
     */
    @Severe
    void extensionErrors(String description);

}
