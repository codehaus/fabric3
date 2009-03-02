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
package org.fabric3.admin.interpreter;

import java.io.IOException;
import java.util.Map;

/**
 * Encapsulates persistent settings for the admin interpreter.
 *
 * @version $Revision$ $Date$
 */
public interface Settings {

    /**
     * Adds a domain and its admin address to the collection of configured domains.
     *
     * @param name    the domain name
     * @param address the domain admin address
     */
    void addDomain(String name, String address);

    /**
     * Returns the domain admin address.
     *
     * @param name the domain name
     * @return the domain admin address
     */
    String getDomainAddress(String name);

    /**
     * Returns a map of all configured domains and their admin addresses.
     *
     * @return the map of domains
     */
    Map<String, String> getDomainAddresses();

    /**
     * Loads settings from persistent storage.
     *
     * @throws IOException if there is an error loading the settings
     */
    void load() throws IOException;

    /**
     * Persists settings.
     *
     * @throws IOException if there is an error persisting the settings
     */
    void save() throws IOException;
}
