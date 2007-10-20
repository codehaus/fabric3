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

/**
 * Event monitor interface for the bootstrap sequence
 *
 * @version $Rev$ $Date$
 */
public interface CoordinatorMonitor {

    /**
     * Called when the runtime is initialized.
     *
     * @param message a message
     */
    void initialized(String message);

    /**
     * Called when the runtime has joined a domain.
     *
     * @param message a message
     */
    void joinedDomain(String message);

    /**
     * Called when the runtime has performed recovery.
     *
     * @param message a message
     */
    void recovered(String message);

    /**
     * Called when the runtime has started.
     *
     * @param message a message
     */
    void started(String message);

    /**
     * Called when an exception was thrown during a boostrap operation
     *
     * @param e the exception
     */
    void error(Throwable e);
}
