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
package org.fabric3.spi.model.physical;

import java.net.URI;
import java.io.Serializable;

/**
 * Represents a "wire" between two classloaders that defines visibility constraints.
 *
 * @version $Revision$ $Date$
 */
public class PhysicalClassLoaderWireDefinition implements Serializable {
    private static final long serialVersionUID = 4080221327918467451L;
    private URI targetClassLoader;
    private String packageName;

    /**
     * Constructor.
     *
     * @param targetClassLoader the URI of the target classloader
     * @param packageName       the package name that is visibile through the wire.
     */
    public PhysicalClassLoaderWireDefinition(URI targetClassLoader, String packageName) {
        this.targetClassLoader = targetClassLoader;
        this.packageName = packageName;
    }

    /**
     * Constructor that makes the entire contents of the target classloader visible.
     *
     * @param targetClassLoader the URI of the target classloader
     */
    public PhysicalClassLoaderWireDefinition(URI targetClassLoader) {
        this.targetClassLoader = targetClassLoader;
    }

    /**
     * Returns the target  classloader.
     *
     * @return the target  classloader
     */
    public URI getTargetClassLoader() {
        return targetClassLoader;
    }

    /**
     * Returns the package visible through the wire or null if the entire contents are visibile.
     *
     * @return the package visible through the wire or null if the entire contents are visibile.
     */
    public String getPackageName() {
        return packageName;
    }
}
