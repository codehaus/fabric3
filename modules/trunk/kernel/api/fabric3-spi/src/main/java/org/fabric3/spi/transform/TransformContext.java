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
