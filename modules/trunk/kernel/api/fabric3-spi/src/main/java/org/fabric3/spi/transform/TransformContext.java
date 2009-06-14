  /*
   * Fabric3
   * Copyright (c) 2009 Metaform Systems
   *
   * Fabric3 is free software: you can redistribute it and/or modify
   * it under the terms of the GNU General Public License as
   * published by the Free Software Foundation, either version 3 of
   * the License, or (at your option) any later version, with the
   * following exception:
   *
   * Linking this software statically or dynamically with other
   * modules is making a combined work based on this software.
   * Thus, the terms and conditions of the GNU General Public
   * License cover the whole combination.
   *
   * As a special exception, the copyright holders of this software
   * give you permission to link this software with independent
   * modules to produce an executable, regardless of the license
   * terms of these independent modules, and to copy and distribute
   * the resulting executable under terms of your choice, provided
   * that you also meet, for each linked independent module, the
   * terms and conditions of the license of that module. An
   * independent module is a module which is not derived from or
   * based on this software. If you modify this software, you may
   * extend this exception to your version of the software, but
   * you are not obligated to do so. If you do not wish to do so,
   * delete this exception statement from your version.
   *
   * Fabric3 is distributed in the hope that it will be useful,
   * but WITHOUT ANY WARRANTY; without even the implied warranty
   * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
   * See the GNU General Public License for more details.
   *
   * You should have received a copy of the
   * GNU General Public License along with Fabric3.
   * If not, see <http://www.gnu.org/licenses/>.
   */
package org.fabric3.spi.transform;

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
