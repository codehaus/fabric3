package org.fabric3.spi.services.factories.xml;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

public interface XMLFactory {

    /**
     * Return default XMLInputFactory implementation. This need not be the default JVM XMLOutputFactory
     *
     * @return the factory
     * @throws FactoryInstantiationException if an error occurs loading the factory
     */
    XMLInputFactory newInputFactoryInstance() throws FactoryInstantiationException;

    /**
     * Return XMLInputFactory with the given factoryName in the given classloader
     *
     * @param factoryName Name of the XMLInputFactory implementation
     * @param classLoader the classloader to use to load the StAX implementation
     * @return the factory
     * @throws FactoryInstantiationException if an error occurs loading the factory
     */
    XMLInputFactory newInputFactoryInstance(String factoryName, ClassLoader classLoader)
            throws FactoryInstantiationException;

    /**
     * Return default XMLOutputFactory implementation. This need not be the default JVM XMLOutputFactory
     *
     * @return the factory
     * @throws FactoryInstantiationException if an error occurs loading the factory
     */
    XMLOutputFactory newOutputFactoryInstance() throws FactoryInstantiationException;

    /**
     * Return XMLOutputFactory with the given factoryName in the given classloader
     *
     * @param factoryName Name of the XMLOutputFactory implementation
     * @param classLoader the classloader to use to load the StAX implementation
     * @return the factory
     * @throws FactoryInstantiationException if an error occurs loading the factory
     */
    XMLOutputFactory newOutputFactoryInstance(String factoryName, ClassLoader classLoader)
            throws FactoryInstantiationException;

}
