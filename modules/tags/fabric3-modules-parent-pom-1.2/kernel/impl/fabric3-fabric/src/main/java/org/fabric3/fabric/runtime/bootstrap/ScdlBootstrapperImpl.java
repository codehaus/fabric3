/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
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

import org.fabric3.fabric.documentloader.DocumentLoader;
import org.fabric3.fabric.documentloader.DocumentLoaderImpl;
import org.fabric3.fabric.xml.XMLFactoryImpl;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ValidationFailure;
import org.fabric3.host.monitor.MonitorFactory;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.ScdlBootstrapper;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.Composite;
import org.fabric3.model.type.component.CompositeImplementation;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.annotation.ImplementationProcessor;
import org.fabric3.spi.introspection.validation.InvalidCompositeException;
import org.fabric3.spi.introspection.xml.Loader;
import org.fabric3.spi.introspection.xml.LoaderException;
import org.fabric3.spi.xml.XMLFactory;
import org.fabric3.system.model.SystemImplementation;

/**
 * Bootstrapper that initializes a runtime by reading a system SCDL file.
 *
 * @version $Rev$ $Date$
 */
public class ScdlBootstrapperImpl extends AbstractBootstrapper implements ScdlBootstrapper {

    private static final String USER_CONFIG = System.getProperty("user.home") + "/.fabric3/config.xml";

    private final DocumentLoader documentLoader;

    private URL scdlLocation;
    private URL systemConfig;
    private InputSource systemConfigDocument;

    public ScdlBootstrapperImpl() {
        this(new XMLFactoryImpl());
    }

    private ScdlBootstrapperImpl(XMLFactory xmlFactory) {
        super(xmlFactory);
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
                                            ImplementationProcessor<SystemImplementation> processor,
                                            MonitorFactory monitorFactory) throws InitializationException {
        try {
            Loader loader = BootstrapLoaderFactory.createLoader(processor, monitorFactory, getXmlFactory());

            // load the system composite
            IntrospectionContext introspectionContext = new DefaultIntrospectionContext(bootClassLoader, contributionUri, scdlLocation);
            Composite composite = loader.load(scdlLocation, Composite.class, introspectionContext);
            if (introspectionContext.hasErrors()) {
                QName name = composite.getName();
                List<ValidationFailure> errors = introspectionContext.getErrors();
                List<ValidationFailure> warnings = introspectionContext.getWarnings();
                throw new InvalidCompositeException(name, errors, warnings);
            }
            addContributionUri(contributionUri, composite);
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

    /**
     * Adds the contibution URI to a component and its children if it is a composite.
     *
     * @param contributionUri the contribution URI
     * @param composite       the composite
     */
    private void addContributionUri(URI contributionUri, Composite composite) {
        for (ComponentDefinition<?> definition : composite.getComponents().values()) {
            Implementation<?> implementation = definition.getImplementation();
            if (CompositeImplementation.class.isInstance(implementation)) {
                CompositeImplementation compositeImplementation = CompositeImplementation.class.cast(implementation);
                Composite componentType = compositeImplementation.getComponentType();
                addContributionUri(contributionUri, componentType);
            } else {
                definition.setContributionUri(contributionUri);
            }
        }
    }

}
