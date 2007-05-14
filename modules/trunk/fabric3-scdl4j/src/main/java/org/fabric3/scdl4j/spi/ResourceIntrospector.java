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
package org.fabric3.scdl4j.spi;

import java.io.IOException;
import java.net.URL;

import org.fabric3.scdl4j.ImplementationImpl;

/**
 * Extension interface for the provider that introspects resources.
 *
 * @version $Rev$ $Date$
 */
public interface ResourceIntrospector {
    /**
     * Introspect the supplied resource and return the implementation it defines.
     *
     * @param resource the resource whose content should be inspected
     * @return the implementation defined by the content
     * @throws IOException            if there was a problem accessing the resource
     * @throws IntrospectionException if there was a problem introspecting the content
     */
    ImplementationImpl introspect(URL resource) throws IOException, IntrospectionException;
}
