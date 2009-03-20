/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the ñLicenseî), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an ñas isî basis,
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
package org.fabric3.test.runtime;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.surefire.suite.SurefireTestSuite;

import org.fabric3.fabric.runtime.AbstractRuntime;
import org.fabric3.fabric.runtime.DefaultCoordinator;
import org.fabric3.fabric.runtime.bootstrap.ScdlBootstrapperImpl;
import org.fabric3.host.Names;
import static org.fabric3.host.Names.APPLICATION_DOMAIN_URI;
import static org.fabric3.host.Names.CONTRIBUTION_SERVICE_URI;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.FileContributionSource;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.host.domain.Domain;
import org.fabric3.host.runtime.BootConfiguration;
import org.fabric3.host.runtime.RuntimeLifecycleCoordinator;
import org.fabric3.host.runtime.ScdlBootstrapper;
import org.fabric3.spi.wire.Wire;
import org.fabric3.test.runtime.api.DeployException;
import org.fabric3.test.runtime.api.MavenHostInfo;
import org.fabric3.test.runtime.api.MavenRuntime;
import org.fabric3.test.runtime.api.StartException;
import org.fabric3.test.spi.TestWireHolder;

/**
 * Maven runtime implementation.
 *
 */
public class MavenRuntimeImpl extends AbstractRuntime<MavenHostInfo> implements MavenRuntime {

    /**
     * Initiates the host information.
     */
    public MavenRuntimeImpl() {
        super(MavenHostInfo.class);
    }
    
    /**
     * Starts the runtime.
     * 
     * @param hostProperties Host properties.
     * @param extensions Extensions to activate on the runtime.
     * @throws StartException If unable to start the runtime.
     */
    public void start(Properties hostProperties, List<ContributionSource> extensions) throws StartException {
        
        BootConfiguration bootConfiguration = getBootConfiguration(extensions);
        
        RuntimeLifecycleCoordinator coordinator = new DefaultCoordinator();
        coordinator.setConfiguration(bootConfiguration);
        
        MavenHostInfo mavenHostInfo = new MavenHostInfoImpl(hostProperties);
        setHostInfo(mavenHostInfo);
        
        boot(coordinator);
        
    }
    
    /**
     * Deploys a list contributions.
     * 
     * @param contributions List of contributions.
     */
    public void deploy(List<ContributionSource> contributions) {
        
        try {
            
            ContributionService contributionService = getSystemComponent(ContributionService.class, CONTRIBUTION_SERVICE_URI);
            Domain domain = getSystemComponent(Domain.class, APPLICATION_DOMAIN_URI);
            
            List<URI> uris = contributionService.contribute(contributions);
            domain.include(uris, false);
            
        } catch (DeploymentException e) {
            throw new DeployException(e.getMessage(), e);
        } catch (ContributionException e) {
            throw new DeployException(e.getMessage(), e);
        }
        
    }
    
    /**
     * Gets the test suite from the SCA contribution.
     * 
     * @return SCA test suite.
     */
    public SurefireTestSuite getTestSuite() {
        
        TestWireHolder testWireHolder = getSystemComponent(TestWireHolder.class, TestWireHolder.COMPONENT_URI);
        SCATestSuite suite = new SCATestSuite();
        for (Map.Entry<String, Wire> entry : testWireHolder.getWires().entrySet()) {
            SCATestSet testSet = new SCATestSet(entry.getKey(), entry.getValue());
            suite.add(testSet);
        }
        return suite;
        
    }

    /*
     * Boots the runtime.
     */
    private void boot(RuntimeLifecycleCoordinator coordinator) {
        
        try {
            coordinator.bootPrimordial();
            coordinator.initialize();
            coordinator.recover();
            coordinator.joinDomain(-1);
            coordinator.start();
        } catch (Exception e) {
            throw new StartException(e.getMessage(), e);
        }
        
    }

    /*
     * Create the boot configuration.
     */
    private BootConfiguration getBootConfiguration(List<ContributionSource> extensions) {
        
        BootConfiguration bootConfiguration = new BootConfiguration();
        
        bootConfiguration.setExtensions(extensions);
        bootConfiguration.setRuntime(this);
        bootConfiguration.setBootClassLoader(getClass().getClassLoader());
        
        setIntents(bootConfiguration);
        setExportedPackages(bootConfiguration);
        setBootstrapper(bootConfiguration);
        
        return bootConfiguration;
        
    }

    /*
     * Set the system intents.
     */
    private void setIntents(BootConfiguration bootConfiguration) {
        
        URL intentsLocation = getClass().getClassLoader().getResource("/META-INF/fabric3/intents.xml");
        ContributionSource source = new FileContributionSource(Names.CORE_INTENTS_CONTRIBUTION, intentsLocation, -1, new byte[0]);
        bootConfiguration.setIntents(source);
        
    }

    /*
     * Set the bootstrapper.
     */
    private void setBootstrapper(BootConfiguration bootConfiguration) {
        
        ScdlBootstrapper bootstrapper = new ScdlBootstrapperImpl();
        URL systemScdl = getClass().getClassLoader().getResource("META-INF/fabric3/embeddedMaven.composite");
        bootstrapper.setScdlLocation(systemScdl);
        bootConfiguration.setBootstrapper(bootstrapper);
        
    }

    /*
     * Set the packages to be exported.
     */
    private void setExportedPackages(BootConfiguration bootConfiguration) {
        
        Map<String, String> exportedPackages = new HashMap<String, String>();
        exportedPackages.put("org.fabric3.test.spi", Names.VERSION);
        exportedPackages.put("org.fabric3.maven", Names.VERSION);
        
        bootConfiguration.setExportedPackages(exportedPackages);
        
    }

}
