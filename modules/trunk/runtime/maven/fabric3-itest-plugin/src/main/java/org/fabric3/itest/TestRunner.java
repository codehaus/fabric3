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
package org.fabric3.itest;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.surefire.report.BriefConsoleReporter;
import org.apache.maven.surefire.report.BriefFileReporter;
import org.apache.maven.surefire.report.Reporter;
import org.apache.maven.surefire.report.ReporterException;
import org.apache.maven.surefire.report.ReporterManager;
import org.apache.maven.surefire.report.XMLReporter;
import org.apache.maven.surefire.suite.SurefireTestSuite;
import org.apache.maven.surefire.testset.TestSetFailedException;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ValidationException;
import org.fabric3.host.domain.AssemblyException;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.maven.runtime.ContextStartException;
import org.fabric3.maven.runtime.MavenEmbeddedRuntime;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.Operation;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.spi.Namespaces;

/**
 * Executes integration tests.
 *
 * @version $Revision$ $Date$
 */
public class TestRunner {
    private static final QName IMPLEMENTATION_JUNIT = new QName(Namespaces.IMPLEMENTATION, "junit");
    private String testDomain;
    private String compositeName;
    private String compositeNamespace;
    private File testScdl;
    private URL testScdlURL;

    private File reportsDirectory;
    private boolean trimStackTrace;
    private File buildDirectory;
    private Log log;

    public TestRunner(String testDomain,
                      String compositeNamespace,
                      String compositeName,
                      File testScdl,
                      File reportsDirectory,
                      boolean trimStackTrace,
                      File buildDirectory,
                      Log log) {
        this.testDomain = testDomain;
        this.compositeName = compositeName;
        this.compositeNamespace = compositeNamespace;
        this.testScdl = testScdl;
        this.reportsDirectory = reportsDirectory;
        this.trimStackTrace = trimStackTrace;
        this.buildDirectory = buildDirectory;
        this.log = log;
        try {
            testScdlURL = testScdl.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }

    public void executeTests(MavenEmbeddedRuntime runtime) throws MojoExecutionException, MojoFailureException {
        SurefireTestSuite testSuite;
        log.info("Deploying test composite from " + testScdl);
        try {
            if (compositeName == null) {
                testSuite = createTestSuite(runtime, testScdlURL);
            } else {
                testSuite = createTestSuite(runtime);
            }
        } catch (MojoExecutionException e) {
            throw e;
        } catch (Exception e) {
            // trap any other exception
            throw new MojoExecutionException("Error deploying test composite: " + testScdl, e);
        }
        log.info("Executing tests...");

        boolean success = runSurefire(testSuite);
        if (!success) {
            String msg = "There were test failures";
            throw new MojoFailureException(msg);
        }
    }

    private boolean runSurefire(SurefireTestSuite testSuite) throws MojoExecutionException {

        try {
            Properties status = new Properties();
            boolean success = run(testSuite, status);
            log.debug("Test results: " + status);
            return success;
        } catch (ReporterException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (TestSetFailedException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

    }

    private boolean run(SurefireTestSuite suite, Properties status) throws ReporterException, TestSetFailedException {
        int totalTests = suite.getNumTests();

        List<Reporter> reports = new ArrayList<Reporter>();
        reports.add(new XMLReporter(reportsDirectory, trimStackTrace));
        reports.add(new BriefFileReporter(reportsDirectory, trimStackTrace));
        reports.add(new BriefConsoleReporter(trimStackTrace));
        ReporterManager reporterManager = new ReporterManager(reports);
        reporterManager.initResultsFromProperties(status);

        reporterManager.runStarting(totalTests);

        if (totalTests == 0) {
            reporterManager.writeMessage("There are no tests to run.");
        } else {
            suite.execute(reporterManager, null);
        }

        reporterManager.runCompleted();
        reporterManager.updateResultsProperties(status);
        return reporterManager.getNumErrors() == 0 && reporterManager.getNumFailures() == 0;
    }

    private SurefireTestSuite createTestSuite(MavenEmbeddedRuntime runtime, URL testScdlURL)
            throws DeploymentException, ContributionException, ContextStartException, MojoExecutionException {
        URI domain = URI.create(testDomain);
        Composite composite;
        try {
            composite = runtime.activate(getBuildDirectoryUrl(), testScdlURL);
        } catch (ValidationException e) {
            // print out the validaiton errors
            reportContributionErrors(e);
            String msg = "Contribution errors were found";
            throw new MojoExecutionException(msg);
        } catch (AssemblyException e) {
            reportDeploymentErrors(e);
            String msg = "Deployment errors were found";
            throw new MojoExecutionException(msg);
        }
        runtime.startContext(domain);
        return createTestSuite(runtime, composite, domain);
    }

    private SurefireTestSuite createTestSuite(MavenEmbeddedRuntime runtime)
            throws ContributionException, DeploymentException, ContextStartException, MojoExecutionException {
        URI domain = URI.create(testDomain);
        QName qName = new QName(compositeNamespace, compositeName);
        try {
            Composite composite;
            composite = runtime.activate(getBuildDirectoryUrl(), qName);
            runtime.startContext(domain);
            return createTestSuite(runtime, composite, domain);
        } catch (ValidationException e) {
            // print out the validation errors
            reportContributionErrors(e);
            String msg = "Contribution errors were found";
            throw new MojoExecutionException(msg);
        } catch (AssemblyException e) {
            reportDeploymentErrors(e);
            String msg = "Deployment errors were found";
            throw new MojoExecutionException(msg);
        }
    }

    private SurefireTestSuite createTestSuite(MavenEmbeddedRuntime runtime, Composite composite, URI uriBase) throws MojoExecutionException {
        SCATestSuite suite = new SCATestSuite();

        Map<String, ComponentDefinition<? extends Implementation<?>>> components = composite.getComponents();
        for (Map.Entry<String, ComponentDefinition<? extends Implementation<?>>> entry : components.entrySet()) {
            String name = entry.getKey();
            ComponentDefinition<? extends Implementation<?>> definition = entry.getValue();
            Implementation<?> implementation = definition.getImplementation();
            if (IMPLEMENTATION_JUNIT.equals(implementation.getType())) {
                SCATestSet testSet = createTestSet(runtime, name, uriBase, definition);
                suite.add(testSet);
            }
        }
        return suite;
    }

    private SCATestSet createTestSet(MavenEmbeddedRuntime runtime,
                                     String name,
                                     URI contextId,
                                     ComponentDefinition<?> definition) throws MojoExecutionException {
        Implementation<?> impl = definition.getImplementation();
        PojoComponentType componentType = (PojoComponentType) impl.getComponentType();
        Map<String, ServiceDefinition> services = componentType.getServices();
        ServiceDefinition testService = services.get("testService");
        if (testService == null) {
            throw new MojoExecutionException("No testService defined on component: " + definition.getName());
        }
        List<? extends Operation<?>> operations = testService.getServiceContract().getOperations();
        return new SCATestSet(runtime, name, contextId, operations);
    }


    private void reportContributionErrors(ValidationException cause) {
        StringBuilder b = new StringBuilder("\n\n");
        b.append("-------------------------------------------------------\n");
        b.append("CONTRIBUTION ERRORS\n");
        b.append("-------------------------------------------------------\n\n");
        b.append(cause.getMessage());
        log.error(b);
    }

    private void reportDeploymentErrors(AssemblyException cause) {
        StringBuilder b = new StringBuilder("\n\n");
        b.append("-------------------------------------------------------\n");
        b.append("DEPLOYMENT ERRORS\n");
        b.append("-------------------------------------------------------\n\n");
        b.append(cause.getMessage());
        log.error(b);
    }

    private URL getBuildDirectoryUrl() {
        try {
            return buildDirectory.toURI().toURL();
        } catch (MalformedURLException e) {
            // this should not happen
            throw new AssertionError();
        }
    }

}
