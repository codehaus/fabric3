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

import java.net.URI;
import java.net.URL;
import javax.xml.namespace.QName;

import org.apache.maven.surefire.suite.SurefireTestSuite;

import org.fabric3.fabric.runtime.AbstractRuntime;
import static org.fabric3.host.Names.APPLICATION_DOMAIN_URI;
import static org.fabric3.host.Names.CONTRIBUTION_SERVICE_URI;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.host.domain.Domain;
import org.fabric3.maven.CompositeQNameService;
import org.fabric3.maven.InvalidResourceException;
import org.fabric3.maven.MavenEmbeddedRuntime;
import org.fabric3.maven.MavenHostInfo;
import org.fabric3.maven.ModuleContributionSource;
import org.fabric3.maven.TestSuiteFactory;

/**
 * Default Maven runtime implementation.
 *
 * @version $Rev$ $Date$
 */
public class MavenEmbeddedRuntimeImpl extends AbstractRuntime<MavenHostInfo> implements MavenEmbeddedRuntime {
    private static final URI CONTRIBUTION_URI = URI.create("iTestContribution");

    public MavenEmbeddedRuntimeImpl() {
        super(MavenHostInfo.class, null);
    }

    public void deploy(URL base, QName qName) throws ContributionException, DeploymentException {
        ModuleContributionSource source = new ModuleContributionSource(CONTRIBUTION_URI, base);
        // contribute the Maven project to the application domain
        ContributionService contributionService =
                getSystemComponent(ContributionService.class, CONTRIBUTION_SERVICE_URI);
        Domain domain = getSystemComponent(Domain.class, APPLICATION_DOMAIN_URI);
        contributionService.contribute(source);
        // activate the deployable composite in the domain
        domain.include(qName);
    }

    public QName deploy(URL base, URL scdlLocation) throws ContributionException, DeploymentException {
        try {
            ModuleContributionSource source = new ModuleContributionSource(CONTRIBUTION_URI, base);

            ContributionService contributionService = getSystemComponent(ContributionService.class, CONTRIBUTION_SERVICE_URI);
            Domain domain = getSystemComponent(Domain.class, APPLICATION_DOMAIN_URI);
            contributionService.contribute(source);
            CompositeQNameService qNameService = getSystemComponent(CompositeQNameService.class, CompositeQNameService.SERVICE_URI);
            QName deployable = qNameService.getQName(CONTRIBUTION_URI, scdlLocation);
            if (deployable == null) {
                throw new DeploymentException("Test composite not found:" + scdlLocation);
            }
            domain.include(deployable);
            return deployable;
        } catch (InvalidResourceException e) {
            throw new DeploymentException("Error activating test contribution", e);
        }

    }

    @SuppressWarnings({"unchecked"})
    public SurefireTestSuite createTestSuite() {
        TestSuiteFactory factory = getSystemComponent(TestSuiteFactory.class, TestSuiteFactory.FACTORY_URI);
        return factory.createTestSuite();
    }
}
