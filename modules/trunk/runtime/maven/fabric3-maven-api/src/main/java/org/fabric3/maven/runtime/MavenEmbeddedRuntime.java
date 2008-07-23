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
package org.fabric3.maven.runtime;

import java.net.URI;
import java.net.URL;
import javax.xml.namespace.QName;

import org.apache.maven.surefire.testset.TestSetFailedException;

import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.host.runtime.Fabric3Runtime;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.Operation;

/**
 * Contract for the Maven runtime.
 *
 * @version $Rev$ $Date$
 */
public interface MavenEmbeddedRuntime extends Fabric3Runtime<MavenHostInfo> {

    /**
     * Activates a composite by qualified name contained in the Maven module the runtime is currently executing for.
     *
     * @param base      the module output directory location
     * @param composite the composite qname to activate
     * @return the activated composite's component type
     * @throws ContributionException if a contribution is thrown. The cause may a ValidationException resulting from  errors in the contribution. In
     *                               this case the errors should be reported back to the user.
     * @throws DeploymentException   if there is an error activating the test composite
     */
    Composite activate(URL base, QName composite) throws ContributionException, DeploymentException;

    /**
     * Activates a composite by qualified name contained in the contribution source.
     *
     * @param source    the source of the contribution
     * @param composite the composite qname to activate
     * @return the activated composite's component type
     * @throws ContributionException if a contribution is thrown. The cause may a ValidationException resulting from  errors in the contribution. In
     *                               this case the errors should be reported back to the user.
     * @throws DeploymentException   if there is an error activating the test composite
     */
    Composite activate(ContributionSource source, QName composite) throws ContributionException, DeploymentException;

    /**
     * Activates a composite pointed to by the SCDL location.
     * <p/>
     * Note this method preserves backward compatibility through specifying the composite by location. When possible, use {@link
     * #activate(java.net.URL, javax.xml.namespace.QName)} instead.
     *
     * @param base         the module output directory location
     * @param scdlLocation the composite file location
     * @return the activated composite's component type
     * @throws DeploymentException   if there is an error activating the test composite
     * @throws ContributionException if a contribution is thrown. The cause may a ValidationException resulting from  errors in the contribution. In
     *                               this case the errors should be reported back to the user.
     */
    Composite activate(URL base, URL scdlLocation) throws ContributionException, DeploymentException;

    /**
     * Starts the component context
     *
     * @param compositeId the context id
     * @throws ContextStartException if an error starting the context is encountered
     */
    void startContext(URI compositeId) throws ContextStartException;

    /**
     * @param contextId     the context id assocated with the test
     * @param componentName the test component name
     * @param operation     the operation to invoke on the test service contract
     * @throws TestSetFailedException if a test case fails
     */
    void executeTest(URI contextId, String componentName, Operation<?> operation) throws TestSetFailedException;
}
