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
package org.fabric3.fabric.deployer;

import org.fabric3.api.annotation.LogLevel;

/**
 * Monitor interface for the deployer
 *
 * @version $Rev$ $Date$
 */
public interface DeployerMonitor {

    @LogLevel("FINE")
    void receivedChangeSet(String message);

    @LogLevel("FINEST")
    void startComponent(String message, String id);

    @LogLevel("FINEST")
    void provisionResource(String message, String id);

    @LogLevel("FINEST")
    void executeCommand(String message, String id);

    @LogLevel("SEVERE")
    void error(String message, Throwable e);

}
