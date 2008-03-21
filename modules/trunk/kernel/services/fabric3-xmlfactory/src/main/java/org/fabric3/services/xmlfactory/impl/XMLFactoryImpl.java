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

package org.fabric3.services.xmlfactory.impl;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.Property;

import org.fabric3.services.xmlfactory.XMLFactoryInstantiationException;
import org.fabric3.services.xmlfactory.XMLFactory;

/**
 * An implementation of XMLFactory that uses WoodStox stax parser for input &
 * output factories.
 * Alternately the runtime can be configured to use a different input and ouput factories
 * as properties in the scdl file
 */
public final class XMLFactoryImpl implements XMLFactory {

    private final String inputFactoryName;
    private final String outputFactoryName;
    private final ClassLoader classLoader = getClass().getClassLoader();

    public XMLFactoryImpl() {
        this("com.ctc.wstx.stax.WstxInputFactory", "com.ctc.wstx.stax.WstxOutputFactory");
    }

    @Constructor
    public XMLFactoryImpl(@Property(name = "input")String inputFactoryName,
                          @Property(name = "output")String outputFactoryName) {
        this.inputFactoryName = inputFactoryName;
        this.outputFactoryName = outputFactoryName;
    }

    /**
     * see XMLFactory
     * @return
     * @throws FactoryConfigurationError
     */
    public XMLInputFactory newInputFactoryInstance() throws FactoryConfigurationError {
        ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            return newInputFactoryInstance(inputFactoryName, classLoader);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCL);
        }
    }

    /**
     * see XMLFactory
     * @return
     * @throws FactoryConfigurationError
     */
    public XMLOutputFactory newOutputFactoryInstance() throws FactoryConfigurationError {
        return newOutputFactoryInstance(outputFactoryName, classLoader);
    }

    private XMLInputFactory newInputFactoryInstance(String factoryName, ClassLoader cl)
            throws XMLFactoryInstantiationException {
        try {
            Class clazz = cl.loadClass(factoryName);
            return (XMLInputFactory) clazz.newInstance();
        } catch (InstantiationException ie) {
            throw new XMLFactoryInstantiationException("Error instantiating factory", factoryName, ie);
        } catch (IllegalAccessException iae) {
            throw new XMLFactoryInstantiationException("Error instantiating factory", factoryName, iae);
        } catch (ClassNotFoundException cnfe) {
            throw new XMLFactoryInstantiationException("Error loading factory", factoryName, cnfe);
        }
    }

    private XMLOutputFactory newOutputFactoryInstance(String factoryName, ClassLoader cl)
            throws FactoryConfigurationError {
        try {
            Class clazz = cl.loadClass(factoryName);
            return (XMLOutputFactory) clazz.newInstance();
        } catch (InstantiationException ie) {
            throw new XMLFactoryInstantiationException("Error instantiating factory", factoryName, ie);
        } catch (IllegalAccessException iae) {
            throw new XMLFactoryInstantiationException("Error instantiating factory", factoryName, iae);
        } catch (ClassNotFoundException cnfe) {
            throw new XMLFactoryInstantiationException("Error loading factory", factoryName, cnfe);
        }
    }

}