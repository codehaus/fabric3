package org.fabric3.services.xmlfactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

public interface XMLFactory {

    /**
     * Return the runtime's XMLInputFactory implementation.
     *
     * @return the factory
     * @throws XMLFactoryInstantiationException if an error occurs loading the factory
     */
    XMLInputFactory newInputFactoryInstance() throws XMLFactoryInstantiationException;

    /**
     * Return the runtime's XMLOutputFactory implementation.
     *
     * @return the factory
     * @throws XMLFactoryInstantiationException if an error occurs loading the factory
     */
    XMLOutputFactory newOutputFactoryInstance() throws XMLFactoryInstantiationException;

}
