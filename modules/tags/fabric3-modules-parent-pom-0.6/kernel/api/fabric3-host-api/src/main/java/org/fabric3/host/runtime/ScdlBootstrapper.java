/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.fabric3.host.runtime;

import java.net.URL;

import org.xml.sax.InputSource;

/**
 * A bootstrapper subtype that instantiates a runtime from a system composite definition.
 *
 * @version $Rev$ $Date$
 */
public interface ScdlBootstrapper extends Bootstrapper {

    /**
     * Sets the location of the SCDL used to boot this runtime.
     *
     * @param scdlLocation the location of the SCDL used to boot this runtime
     */
    void setScdlLocation(URL scdlLocation);

    /**
     * Sets the system configuration for the host.
     *
     * @param systemConfig System configuration.
     */
    void setSystemConfig(URL systemConfig);

    /**
     * Sets the system configuration for the host.
     *
     * @param document System configuration as an InputSource.
     */
    void setSystemConfig(InputSource document);

}
