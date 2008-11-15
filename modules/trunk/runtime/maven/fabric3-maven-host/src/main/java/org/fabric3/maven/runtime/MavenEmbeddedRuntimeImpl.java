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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.maven.surefire.suite.SurefireTestSuite;

import org.fabric3.fabric.runtime.AbstractRuntime;
import org.fabric3.fabric.runtime.ComponentNames;
import static org.fabric3.fabric.runtime.ComponentNames.APPLICATION_DOMAIN_URI;
import static org.fabric3.fabric.runtime.ComponentNames.CONTRIBUTION_SERVICE_URI;
import static org.fabric3.fabric.runtime.ComponentNames.XML_FACTORY_URI;
import org.fabric3.fabric.util.FileHelper;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.host.domain.Domain;
import org.fabric3.host.xml.XMLFactory;
import org.fabric3.maven.contribution.ModuleContributionSource;
import org.fabric3.scdl.Composite;
import org.fabric3.spi.component.GroupInitializationException;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.services.contribution.QNameSymbol;
import org.fabric3.spi.services.contribution.ResourceElement;
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

    public Composite deploy(URL url, QName qName) throws ContributionException, DeploymentException {
        try {
            URI contributionUri = url.toURI();
            ModuleContributionSource source =
                    new ModuleContributionSource(contributionUri, FileHelper.toFile(url).toString());
            return deploy(source, qName);
        } catch (MalformedURLException e) {
            String identifier = url.toString();
            throw new DeploymentException("Invalid project directory: " + identifier, identifier, e);
        } catch (URISyntaxException e) {
            throw new DeploymentException("Error activating test contribution", e);
        }
    }

    public Composite deploy(URL url, URL scdlLocation) throws ContributionException, DeploymentException {
        QName name;
        try {
            name = parseCompositeQName(scdlLocation);
        } catch (IOException e) {
            throw new ContributionException(e);
        } catch (XMLStreamException e) {
            throw new ContributionException(e);
        }
        return deploy(url, name);
    }

    public void startContext(QName deployable) throws ContextStartException {
        WorkContext workContext = new WorkContext();
        CallFrame frame = new CallFrame(deployable);
        workContext.addCallFrame(frame);
        try {
            scopeContainer.startContext(workContext);
        } catch (GroupInitializationException e) {
            throw new ContextStartException(e);
        }
    }

    @SuppressWarnings({"unchecked"})
    public SurefireTestSuite createTestSuite() {
        // get wires to test operations generated by test extensions
        URI uri = URI.create(ComponentNames.RUNTIME_NAME + "/TestWireHolder");
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

    /**
     * Deploys a deployable in a contribution
     *
     * @param source the source
     * @param qName  the deployable QName
     * @return the Composite deployable
     * @throws ContributionException if an error occurs introspecting the contribution
     * @throws DeploymentException   if a deployment error occurs
     */
    private Composite deploy(ContributionSource source, QName qName) throws ContributionException, DeploymentException {
        // contribute the Maven project to the application domain
        Domain domain = getSystemComponent(Domain.class, APPLICATION_DOMAIN_URI);
        ContributionService contributionService =
                getSystemComponent(ContributionService.class, CONTRIBUTION_SERVICE_URI);
        contributionService.contribute(source);
        // activate the deployable composite in the domain
        domain.include(qName);
        ResourceElement<?, ?> element = getMetaDataStore().resolve(new QNameSymbol(qName));
        assert element != null;
        return (Composite) element.getValue();
    }

    /**
     * Determines a composite's QName.
     * <p/>
     * This method preserves backward compatibility for specifying SCDL location in an iTest plugin configuration.
     *
     * @param url the SCDL location
     * @return the composite QName
     * @throws IOException        if an error occurs opening the composite file
     * @throws XMLStreamException if an error occurs processing the composite
     */
    private QName parseCompositeQName(URL url) throws IOException, XMLStreamException {
        XMLStreamReader reader = null;
        InputStream stream = null;
        try {
            stream = url.openStream();
            XMLFactory xmlFactory = getSystemComponent(XMLFactory.class, XML_FACTORY_URI);
            reader = xmlFactory.newInputFactoryInstance().createXMLStreamReader(stream);
            reader.nextTag();
            String name = reader.getAttributeValue(null, "name");
            String targetNamespace = reader.getAttributeValue(null, "targetNamespace");
            return new QName(targetNamespace, name);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        }

    }

}
