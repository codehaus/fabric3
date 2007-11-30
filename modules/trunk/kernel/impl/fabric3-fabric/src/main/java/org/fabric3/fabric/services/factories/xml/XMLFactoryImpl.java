package org.fabric3.fabric.services.factories.xml;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import org.fabric3.spi.services.factories.xml.FactoryInstantiationException;
import org.fabric3.spi.services.factories.xml.XMLFactory;

public final class XMLFactoryImpl implements XMLFactory {

    private final String inputFactoryName;
    private final String outputFactoryName;
    private final ClassLoader classLoader = getClass().getClassLoader();

    public XMLFactoryImpl() {
        this("com.ctc.wstx.stax.WstxInputFactory", "com.ctc.wstx.stax.WstxInputFactory");
    }

    public XMLFactoryImpl(String inputFactoryName, String outputFactoryName) {
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

    public XMLInputFactory newInputFactoryInstance(String factoryName, ClassLoader cl)
            throws FactoryInstantiationException {
        try {
            Class clazz = cl.loadClass(factoryName);
            return (XMLInputFactory) clazz.newInstance();
        } catch (InstantiationException ie) {
            throw new FactoryInstantiationException("Error instantiating factory", factoryName, ie);
        } catch (IllegalAccessException iae) {
            throw new FactoryInstantiationException("Error instantiating factory", factoryName, iae);
        } catch (ClassNotFoundException cnfe) {
            throw new FactoryInstantiationException("Error loading factory", factoryName, cnfe);
        }
    }

    public XMLOutputFactory newOutputFactoryInstance() throws FactoryConfigurationError {
        return newOutputFactoryInstance(outputFactoryName, classLoader);
    }

    public XMLOutputFactory newOutputFactoryInstance(String factoryName, ClassLoader cl)
            throws FactoryConfigurationError {
        try {
            Class clazz = cl.loadClass(factoryName);
            return (XMLOutputFactory) clazz.newInstance();
        } catch (InstantiationException ie) {
            throw new FactoryInstantiationException("Error instantiating factory", factoryName, ie);
        } catch (IllegalAccessException iae) {
            throw new FactoryInstantiationException("Error instantiating factory", factoryName, iae);
        } catch (ClassNotFoundException cnfe) {
            throw new FactoryInstantiationException("Error loading factory", factoryName, cnfe);
        }
    }

}