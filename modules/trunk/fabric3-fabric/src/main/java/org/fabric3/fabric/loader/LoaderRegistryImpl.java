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
import java.io.InputStream;
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
import org.fabric3.spi.loader.ComponentTypeLoader;
import org.fabric3.spi.loader.InvalidConfigurationException;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.loader.StAXElementLoader;
import org.fabric3.spi.loader.UnrecognizedComponentTypeException;
import org.fabric3.spi.loader.UnrecognizedElementException;
import org.fabric3.spi.model.type.Implementation;

/**
 * The default implementation of a loader registry
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class LoaderRegistryImpl implements LoaderRegistry {
    private Monitor monitor;
    private final XMLInputFactory xmlFactory;
    private final Map<QName, StAXElementLoader<?, ?>> loaders = new HashMap<QName, StAXElementLoader<?, ?>>();
    private final Map<Class<? extends Implementation<?>>,
            ComponentTypeLoader<? extends Implementation<?>>> componentTypeLoaders =
            new HashMap<Class<? extends Implementation<?>>, ComponentTypeLoader<? extends Implementation<?>>>();

    @Constructor
    public LoaderRegistryImpl(@Reference MonitorFactory monitorFactory, @Reference XMLInputFactory factory) {
        // JFM FIXME use @Monitor when resources are fixed
        this.monitor = monitorFactory.getMonitor(Monitor.class);
        this.xmlFactory = factory;
    }

    public LoaderRegistryImpl(Monitor monitor, XMLInputFactory factory) {
        this.monitor = monitor;
        this.xmlFactory = factory;
    }

    public void registerLoader(QName element, StAXElementLoader<?, ?> loader) {
        monitor.registeringLoader(element);
        loaders.put(element, loader);
    }

    public void unregisterLoader(QName element, StAXElementLoader<?, ?> loader) {
        monitor.unregisteringLoader(element);
        loaders.remove(element);
    }

    public <I, O> O load(I modelType, XMLStreamReader reader, LoaderContext loaderContext)
            throws XMLStreamException, LoaderException {
        QName name = reader.getName();
        monitor.elementLoad(name);
        // FIXME unsafe cast!
        StAXElementLoader<I, O> loader = (StAXElementLoader<I, O>) loaders.get(name);
        if (loader == null) {
            throw new UnrecognizedElementException(name);
        }
        return loader.load(modelType, reader, loaderContext);
    }

    public <I, O> O load(I originalModelType, URL url, Class<O> type, LoaderContext ctx)
            throws LoaderException {
        try {
            XMLStreamReader reader;
            InputStream is;
            is = url.openStream();
            try {
                reader = xmlFactory.createXMLStreamReader(is);
                try {
                    reader.nextTag();
                    QName name = reader.getName();
                    Object modelType = load(originalModelType, reader, ctx);
                    if (type.isInstance(modelType)) {
                        return type.cast(modelType);
                    } else {
                        UnrecognizedElementException e = new UnrecognizedElementException(name);
                        e.setResourceURI(url.toString());
                        throw e;
                    }
                } catch (LoaderException e) {
                    Location location = reader.getLocation();
                    e.setLine(location.getLineNumber());
                    e.setColumn(location.getColumnNumber());
                    throw e;
                } finally {
                    try {
                        reader.close();
                    } catch (XMLStreamException e) {
                        // ignore
                    }
                }
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        } catch (IOException e) {
            LoaderException sfe = new LoaderException(e);
            sfe.setResourceURI(url.toString());
            throw sfe;
        } catch (XMLStreamException e) {
            throw new InvalidConfigurationException("Invalid or missing resource", url.toString(), e);
        }
    }

    public <I extends Implementation<?>> void registerLoader(Class<I> key, ComponentTypeLoader<I> loader) {
        componentTypeLoaders.put(key, loader);
    }

    public <I extends Implementation<?>> void unregisterLoader(Class<I> key) {
        componentTypeLoaders.remove(key);
    }

    @SuppressWarnings("unchecked")
    public <I extends Implementation<?>> void loadComponentType(I implementation, LoaderContext loaderContext)
            throws LoaderException {
        Class<I> key = (Class<I>) implementation.getClass();
        ComponentTypeLoader<I> loader = (ComponentTypeLoader<I>) componentTypeLoaders.get(key);
        if (loader == null) {
            throw new UnrecognizedComponentTypeException(key);
        }
        loader.load(implementation, loaderContext);
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
