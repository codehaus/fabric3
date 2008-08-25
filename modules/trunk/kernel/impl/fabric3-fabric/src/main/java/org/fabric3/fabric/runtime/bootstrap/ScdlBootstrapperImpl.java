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
package org.fabric3.fabric.runtime.bootstrap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import org.fabric3.fabric.services.documentloader.DocumentLoader;
import org.fabric3.fabric.services.documentloader.DocumentLoaderImpl;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ValidationFailure;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.ScdlBootstrapper;
import org.fabric3.introspection.DefaultIntrospectionContext;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.validation.InvalidCompositeException;
import org.fabric3.introspection.xml.Loader;
import org.fabric3.introspection.xml.LoaderException;
import org.fabric3.monitor.MonitorFactory;
import org.fabric3.scdl.Composite;
import org.fabric3.services.xmlfactory.XMLFactory;
import org.fabric3.services.xmlfactory.impl.XMLFactoryImpl;
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

    protected Document loadUserConfig() {
        // Get the user config location
        File configFile = new File(USER_CONFIG);
        try {
            return documentLoader.load(configFile);
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        } catch (SAXException e) {
            return null;
        }
    }


    protected Document loadSystemConfig() {
        // Get the system config location
        if (systemConfig == null) {
            return null;
        }
        try {
            return documentLoader.load(systemConfig);
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        } catch (SAXException e) {
            return null;
        }
    }
}
