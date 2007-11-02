/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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
package org.fabric3.spi.loader;

import javax.xml.namespace.QName;

/**
 * Registry for XML loaders that can parse a StAX input stream and return model objects.
 * <p/>
 * Loaders will typically be contributed to the system by any extension that needs to handle extension specific
 * information contained in some XML configuration file. The loader can be contributed as a system component with an
 * autowire reference to this builderRegistry which is used during initialization to actually register. </p> This
 * builderRegistry can also be used to parse an input stream, dispatching to the appropriate loader for each element
 * accepted. Loaders can call back to the builderRegistry to load sub-elements that they are not able to handle
 * directly.
 *
 * @version $Rev$ $Date$
 */
@Deprecated
public interface LoaderRegistry extends Loader {
    /**
     * Register a loader. This operation will typically be called by a loader during its initialization.
     *
     * @param element the name of the XML global element that this loader can handle
     * @param loader  a loader that is being contributed to the system
     * @throws IllegalStateException if there is already a loader registered for the supplied element
     */
    void registerLoader(QName element, StAXElementLoader<?> loader) throws IllegalStateException;

    /**
     * Unregister the loader for the supplied element. This will typically be called by a loader as it is being
     * destroyed. This method simply returns if no loader is registered for that element.
     *
     * @param element the element whose loader should be unregistered
     */
    void unregisterLoader(QName element);
}
