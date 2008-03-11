package org.fabric3.services.xmlfactory.impl;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.Property;

import org.fabric3.services.xmlfactory.XMLFactoryInstantiationException;
import org.fabric3.services.xmlfactory.XMLFactory;

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

    public XMLInputFactory newInputFactoryInstance() throws FactoryConfigurationError {
        ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            return newInputFactoryInstance(inputFactoryName, classLoader);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCL);
        }
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

    public XMLOutputFactory newOutputFactoryInstance() throws FactoryConfigurationError {
        return newOutputFactoryInstance(outputFactoryName, classLoader);
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