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
package org.fabric3.xstream.factory;

import com.thoughtworks.xstream.XStream;

/**
 * Implementations create <code>XStream</code> instances for serializing internal runtime data structures.
 *
 * @version $Rev$ $Date$
 */
public interface XStreamFactory {

    /**
     * Returns a new XStream instance.
     *
     * @return a new XStream instance
     */
    XStream createInstance();

}