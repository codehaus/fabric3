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
package org.fabric3.fabric.runtime.bootstrap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.host.Names;

/**
 * Returns the packages that should be exported by the boot contribution.
 *
 * @version $Revision$ $Date$
 */
public final class BootExports {
    private static final Map<String, String> BOOT_EXPORTS;

    static {

        Map<String, String> bootMap = new HashMap<String, String>();

        // Fabric3 classes
        bootMap.put("org.fabric3.spi.*", Names.VERSION);
        bootMap.put("org.fabric3.host.*", Names.VERSION);
        bootMap.put("org.fabric3.management.*", Names.VERSION);
        bootMap.put("org.fabric3.model.*", Names.VERSION);
        bootMap.put("org.fabric3.pojo.*", Names.VERSION);
        BOOT_EXPORTS = Collections.unmodifiableMap(bootMap);
    }

    private BootExports() {
    }

    public static Map<String, String> getExports() {
        return BOOT_EXPORTS;
    }

}