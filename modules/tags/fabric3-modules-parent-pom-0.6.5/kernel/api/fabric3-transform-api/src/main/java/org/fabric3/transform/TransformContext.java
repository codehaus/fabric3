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
package org.fabric3.transform;

import java.net.URL;

/**
 * Context information applicable during a transformation.
 *
 * @version $Rev$ $Date$
 */
public class TransformContext {
    private final ClassLoader targetClassLoader;
    private final ClassLoader sourceClassLoader;
    private final URL sourceBase;
    private final URL targetBase;

    /**
     * @param sourceClassLoader a ClassLoader that can be used to access resources from the source
     * @param targetClassLoader a ClassLoader that can be used instantiate transformation results
     * @param sourceBase        a URL for resolving locations from the source
     * @param targetBase        a URL for resolving locations for the target
     */
    public TransformContext(ClassLoader sourceClassLoader, ClassLoader targetClassLoader, URL sourceBase, URL targetBase) {
        this.sourceClassLoader = sourceClassLoader;
        this.targetClassLoader = targetClassLoader;
        this.sourceBase = sourceBase;
        this.targetBase = targetBase;
    }

    /**
     * Returns a ClassLoader that can be used instantiate transformation results.
     *
     * @return a ClassLoader that can be used instantiate transformation results
     */
    public ClassLoader getTargetClassLoader() {
        return targetClassLoader;
    }

    /**
     * Returns a ClassLoader that can be used to access resources from the source.
     *
     * @return a ClassLoader that can be used to access resources from the source
     */
    public ClassLoader getSourceClassLoader() {
        return sourceClassLoader;
    }

    /**
     * Returns a URL for resolving locations from the source.
     *
     * @return a URL for resolving locations from the source
     */
    public URL getSourceBase() {
        return sourceBase;
    }

    /**
     * Returns a URL for resolving locations for the target.
     *
     * @return a URL for resolving locations for the target
     */
    public URL getTargetBase() {
        return targetBase;
    }
}
