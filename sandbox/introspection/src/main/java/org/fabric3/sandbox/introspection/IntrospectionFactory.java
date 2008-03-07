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
package org.fabric3.sandbox.introspection;

import org.fabric3.introspection.xml.Loader;

/**
 * @version $Rev$ $Date$
 */
public interface IntrospectionFactory {

    /**
     * Returns a Loader that can be used to introspect XML definitions such as composite files.
     *
     * @return a Loader that can be used to introspect XML definitions
     */
    Loader getLoader();
}
