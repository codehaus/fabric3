/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
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
package org.fabric3.maven.runtime;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

import org.apache.maven.surefire.suite.SurefireTestSuite;

import org.fabric3.fabric.runtime.AbstractRuntime;
import org.fabric3.util.io.FileHelper;
import org.fabric3.host.Names;
import static org.fabric3.host.Names.APPLICATION_DOMAIN_URI;
import static org.fabric3.host.Names.CONTRIBUTION_SERVICE_URI;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.contribution.Deployable;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.host.domain.Domain;
import org.fabric3.maven.contribution.ModuleContributionSource;
import org.fabric3.maven.MavenEmbeddedRuntime;
import org.fabric3.maven.MavenHostInfo;
import org.fabric3.spi.wire.Wire;

/**
 * Default Maven runtime implementation.
 *
 * @version $Rev$ $Date$
 */
public class MavenEmbeddedRuntimeImpl extends AbstractRuntime<MavenHostInfo> implements MavenEmbeddedRuntime {
    public MavenEmbeddedRuntimeImpl() {
        super(MavenHostInfo.class, null);
    }

    public void deploy(URL url, QName qName) throws ContributionException, DeploymentException {
        try {
            URI contributionUri = url.toURI();
            ModuleContributionSource source =
                    new ModuleContributionSource(contributionUri, FileHelper.toFile(url).toString());
            // contribute the Maven project to the application domain
            ContributionService contributionService =
                    getSystemComponent(ContributionService.class, CONTRIBUTION_SERVICE_URI);
            Domain domain = getSystemComponent(Domain.class, APPLICATION_DOMAIN_URI);
            contributionService.contribute(source);
            // activate the deployable composite in the domain
            domain.include(qName);
        } catch (MalformedURLException e) {
            String identifier = url.toString();
            throw new DeploymentException("Invalid project directory: " + identifier, identifier, e);
        } catch (URISyntaxException e) {
            throw new DeploymentException("Error activating test contribution", e);
        }
    }

    public List<Deployable> deploy(URL url, URL scdlLocation) throws ContributionException, DeploymentException {
        try {
            URI contributionUri = url.toURI();
            ModuleContributionSource source =
                    new ModuleContributionSource(contributionUri, FileHelper.toFile(url).toString());

            ContributionService contributionService =
                    getSystemComponent(ContributionService.class, CONTRIBUTION_SERVICE_URI);
            Domain domain = getSystemComponent(Domain.class, APPLICATION_DOMAIN_URI);
            contributionService.contribute(source);
            List<Deployable> deployables = contributionService.getDeployables(contributionUri);
            assert !deployables.isEmpty();
            for (Deployable deployable : deployables) {
                domain.include(deployable.getName());
            }
            return deployables;
        } catch (MalformedURLException e) {
            String identifier = url.toString();
            throw new DeploymentException("Invalid project directory: " + identifier, identifier, e);
        } catch (URISyntaxException e) {
            throw new DeploymentException("Error activating test contribution", e);
        }

    }

    @SuppressWarnings({"unchecked"})
    public SurefireTestSuite createTestSuite() {
        // get wires to test operations generated by test extensions
        URI uri = URI.create(Names.RUNTIME_NAME + "/TestWireHolder");
        Map<String, Wire> wires = getSystemComponent(Map.class, uri);
        if (wires == null) {
            throw new AssertionError("TestWireHolder is not configured");
        }
        SCATestSuite suite = new SCATestSuite();
        for (Map.Entry<String, Wire> entry : wires.entrySet()) {
            SCATestSet testSet = new SCATestSet(entry.getKey(), entry.getValue());
            suite.add(testSet);
        }
        return suite;
    }
}
