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
package org.fabric3.federation.shoal;

import org.fabric3.api.annotation.logging.Config;
import org.fabric3.api.annotation.logging.Fine;
import org.fabric3.api.annotation.logging.Finer;
import org.fabric3.api.annotation.logging.Finest;
import org.fabric3.api.annotation.logging.Info;
import org.fabric3.api.annotation.logging.Severe;
import org.fabric3.api.annotation.logging.Warning;

/**
 * Monitor for federation services.
 *
 * @version $Revision$ $Date$
 */
public interface FederationServiceMonitor {

    /**
     * Callback invoked when the runtime joins a group.
     *
     * @param groupName   the group name.
     * @param runtimeName the runtime name
     */
    @Info
    void joined(String groupName, String runtimeName);

    /**
     * Callback invoked when the runtime exits a group.
     *
     * @param groupName   the domain.
     */
    @Info
    void exited(String groupName);

    /**
     * Logged when an exception occurs.
     *
     * @param description the error description
     * @param domainName  the domain name
     * @param throwable   Exception that occured.
     */
    @Severe
    void onException(String description, String domainName, Throwable throwable);

    /**
     * Logged when a general exception occurs.
     *
     * @param description the error description
     * @param throwable   Exception that occured.
     */
    @Severe
    void onSignalException(String description, Throwable throwable);

    /**
     * Logged when an error occurs.
     *
     * @param description the error description
     * @param domainName  the domain name
     */
    @Severe
    void onError(String description, String domainName);

    /**
     * Callback for when a signal is received.
     *
     * @param message a message describing the signal
     */
    @Fine
    void onSignal(String message);

    /**
     * Used for recording Shoal config-level log messages.
     *
     * @param message the message
     */
    @Config
    void onConfig(String message);

    /**
     * Used for recording Shoal config-level log messages.
     *
     * @param message the message
     */
    @Info
    void onInfo(String message);

    /**
     * Used for recording Shoal config-level log messages.
     *
     * @param message the message
     */
    @Finer
    void onFiner(String message);

    /**
     * Used for recording Shoal config-level log messages.
     *
     * @param message the message
     */
    @Finest
    void onFinest(String message);

    /**
     * Used for recording Shoal config-level log messages.
     *
     * @param message the message
     */
    @Fine
    void onFine(String message);

    /**
     * Used for recording Shoal config-level log messages.
     *
     * @param message the message
     */
    @Warning
    void onWarning(String message);

    /**
     * Used for recording Shoal config-level log messages.
     *
     * @param message the message
     */
    @Severe
    void onSevere(String message);

}
