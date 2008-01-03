/*
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
package org.fabric3.fabric.loader;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.monitor.MonitorFactory;
import org.fabric3.spi.loader.InvalidConfigurationException;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.loader.StAXElementLoader;
import org.fabric3.spi.loader.UnrecognizedElementException;
import org.fabric3.spi.services.factories.xml.XMLFactory;

/**
 * The default implementation of a loader registry
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class LoaderRegistryImpl implements LoaderRegistry {
    private Monitor monitor;
    private final XMLInputFactory xmlFactory;
    private Map<QName, StAXElementLoader<?>> mappedLoaders;
    private final Map<QName, StAXElementLoader<?>> loaders = new HashMap<QName, StAXElementLoader<?>>();

    @Constructor
    public LoaderRegistryImpl(@Reference MonitorFactory monitorFactory, @Reference XMLFactory factory) {
        // JFM FIXME use @Monitor when resources are fixed
        this.monitor = monitorFactory.getMonitor(Monitor.class);
        this.xmlFactory = factory.newInputFactoryInstance();
    }

    public LoaderRegistryImpl(Monitor monitor, XMLInputFactory factory) {
        this.monitor = monitor;
        this.xmlFactory = factory;
    }

    @Reference(required = false)
    public void setLoaders(Map<QName, StAXElementLoader<?>> mappedLoaders) {
        this.mappedLoaders = mappedLoaders;
    }

    public void registerLoader(QName element, StAXElementLoader<?> loader) {
        if (loaders.containsKey(element)) {
            throw new IllegalStateException("Loader already registered for " + element);
        }
        monitor.registeringLoader(element);
        loaders.put(element, loader);
    }

    public void unregisterLoader(QName element) {
        monitor.unregisteringLoader(element);
        loaders.remove(element);
    }

    public <O> O load(XMLStreamReader reader, Class<O> type, LoaderContext loaderContext)
            throws XMLStreamException, LoaderException {
        QName name = reader.getName();
        monitor.elementLoad(name);
        StAXElementLoader<?> loader = loaders.get(name);
        if (loader == null) {
            loader = mappedLoaders.get(name);
        }
        if (loader == null) {
            UnrecognizedElementException e = new UnrecognizedElementException(name);
            Location location = reader.getLocation();
            URI contributionUri = loaderContext.getContributionUri();
            URL sourceBase = loaderContext.getSourceBase();
            if (sourceBase != null) {
                e.setResourceURI(sourceBase.toString());
            }
            if (contributionUri != null) {
                e.setResourceURI(contributionUri.toString());
            }

            e.setLine(location.getLineNumber());
            e.setColumn(location.getColumnNumber());
            throw e;
        }
        return type.cast(loader.load(reader, loaderContext));
    }

    public <O> O load(URL url, Class<O> type, LoaderContext ctx) throws LoaderException {
        XMLStreamReader reader = null;
        try {
            reader = xmlFactory.createXMLStreamReader(url.toString(), url.openStream());
            reader.nextTag();
            return load(reader, type, ctx);
        } catch (IOException e) {
            LoaderException sfe = new LoaderException(e);
            sfe.setResourceURI(url.toString());
            throw sfe;
        } catch (XMLStreamException e) {
            throw new InvalidConfigurationException("Invalid or missing resource", url.toString(), e);
        } catch (LoaderException e) {
            Location location = reader.getLocation();
            e.setResourceURI(location.getSystemId());
            e.setLine(location.getLineNumber());
            e.setColumn(location.getColumnNumber());
            throw e;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                }
            }
        }
    }

    public static interface Monitor {
        /**
         * Event emitted when a StAX element loader is registered.
         *
         * @param xmlType the QName of the element the loader will handle
         */
        void registeringLoader(QName xmlType);

        /**
         * Event emitted when a StAX element loader is unregistered.
         *
         * @param xmlType the QName of the element the loader will handle
         */
        void unregisteringLoader(QName xmlType);

        /**
         * Event emitted when a request is made to load an element.
         *
         * @param xmlType the QName of the element that should be loaded
         */
        void elementLoad(QName xmlType);
    }
}
