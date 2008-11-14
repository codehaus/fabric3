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
package org.fabric3.fabric.runtime.bootstrap;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.fabric3.fabric.services.documentloader.DocumentLoader;
import org.fabric3.fabric.services.documentloader.DocumentLoaderImpl;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ValidationFailure;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.ScdlBootstrapper;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.validation.InvalidCompositeException;
import org.fabric3.spi.introspection.xml.Loader;
import org.fabric3.spi.introspection.xml.LoaderException;
import org.fabric3.host.monitor.MonitorFactory;
import org.fabric3.host.xml.XMLFactory;
import org.fabric3.scdl.Composite;
import org.fabric3.fabric.services.xml.XMLFactoryImpl;
import org.fabric3.system.introspection.BootstrapLoaderFactory;
import org.fabric3.system.introspection.SystemImplementationProcessor;

/**
 * Bootstrapper that initializes a runtime by reading a system SCDL file.
 *
 * @version $Rev$ $Date$
 */
public class ScdlBootstrapperImpl extends AbstractBootstrapper implements ScdlBootstrapper {

    private static final String USER_CONFIG = System.getProperty("user.home") + "/.fabric3/config.xml";

    private final XMLFactory xmlFactory;
    private final DocumentLoader documentLoader;

    private URL scdlLocation;
    private URL systemConfig;
    private InputSource systemConfigDocument;

    public ScdlBootstrapperImpl() {
        this(new XMLFactoryImpl());
    }

    private ScdlBootstrapperImpl(XMLFactory xmlFactory) {
        super();
        this.xmlFactory = xmlFactory;
        this.documentLoader = new DocumentLoaderImpl();
    }

    public void setScdlLocation(URL scdlLocation) {
        this.scdlLocation = scdlLocation;
    }

    public void setSystemConfig(URL systemConfig) {
        this.systemConfig = systemConfig;
    }

    public void setSystemConfig(InputSource source) {
        this.systemConfigDocument = source;
    }

    protected Composite loadSystemComposite(URI contributionUri,
                                            ClassLoader bootClassLoader,
                                            SystemImplementationProcessor processor,
                                            MonitorFactory monitorFactory) throws InitializationException {
        try {
            Loader loader = BootstrapLoaderFactory.createLoader(processor, monitorFactory, xmlFactory);

            // load the system composite
            IntrospectionContext introspectionContext = new DefaultIntrospectionContext(bootClassLoader, contributionUri, scdlLocation);
            Composite composite = loader.load(scdlLocation, Composite.class, introspectionContext);
            composite.validate(introspectionContext);
            if (introspectionContext.hasErrors()) {
                QName name = composite.getName();
                List<ValidationFailure> errors = introspectionContext.getErrors();
                List<ValidationFailure> warnings = introspectionContext.getWarnings();
                throw new InvalidCompositeException(name, errors, warnings);
            }
            return composite;
        } catch (ContributionException e) {
            throw new InitializationException(e);
        } catch (LoaderException e) {
            throw new InitializationException(e);
        }
    }

    protected Document loadUserConfig() throws InitializationException {
        // Get the user config location
        File configFile = new File(USER_CONFIG);
        if (!configFile.exists()) {
            // none found, create a default one
            return createDefaultConfigProperty();
        }
        try {
            return documentLoader.load(configFile);
        } catch (IOException e) {
            throw new InitializationException(e);
        } catch (SAXException e) {
            throw new InitializationException(e);
        }
    }


    protected Document loadSystemConfig() throws InitializationException {
        if (systemConfigDocument != null) {
            try {
                // load from an external URL
                return documentLoader.load(systemConfigDocument);
            } catch (IOException e) {
                throw new InitializationException(e);
            } catch (SAXException e) {
                throw new InitializationException(e);
            }
        }
        if (systemConfig == null) {
            // none specified, create a default one
            return createDefaultConfigProperty();
        }
        try {
            // load from an external URL
            return documentLoader.load(systemConfig);
        } catch (IOException e) {
            throw new InitializationException(e);
        } catch (SAXException e) {
            throw new InitializationException(e);
        }
    }

    /**
     * Creates a default configuration domain property.
     *
     * @return a document representing the configuration domain property
     */
    protected Document createDefaultConfigProperty() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            Document document = factory.newDocumentBuilder().newDocument();
            Element root = document.createElement("config");
            document.appendChild(root);
            return document;
        } catch (ParserConfigurationException e) {
            throw new AssertionError(e);
        }
    }
}
