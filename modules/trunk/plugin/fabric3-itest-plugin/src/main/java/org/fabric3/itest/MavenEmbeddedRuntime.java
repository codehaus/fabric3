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
package org.fabric3.itest;

import java.net.URI;

import org.apache.maven.surefire.testset.TestSetFailedException;

import org.fabric3.host.runtime.Fabric3Runtime;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.Operation;

/**
 * @version $Rev$ $Date$
 */
public interface MavenEmbeddedRuntime extends Fabric3Runtime<MavenHostInfo> {
    void deploy(Composite composite) throws Exception;

    void startContext(URI compositeId) throws Exception;

    void destroy();

    void executeTest(URI contextId, String componentName, Operation<?> operation) throws TestSetFailedException;
}
