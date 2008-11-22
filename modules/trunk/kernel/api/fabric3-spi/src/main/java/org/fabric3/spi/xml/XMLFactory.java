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
 */
package org.fabric3.spi.xml;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

/**
 * Provides instances of XMLInputFactory and XMLOutputFactory. This service provides classloading semantics and works around a bug in the JDK StAX
 * parser API (StAX 1.0) which returns an XMLInputFactory for XMLOutputFactory.newInstance(String,ClassLoader).
 */

public interface XMLFactory {

    /**
     * Return the runtime's XMLInputFactory implementation.
     *
     * @return the factory
     * @throws XMLFactoryInstantiationException
     *          if an error occurs loading the factory
     */
    XMLInputFactory newInputFactoryInstance() throws XMLFactoryInstantiationException;

    /**
     * Return the runtime's XMLOutputFactory implementation.
     *
     * @return the factory
     * @throws XMLFactoryInstantiationException
     *          if an error occurs loading the factory
     */
    XMLOutputFactory newOutputFactoryInstance() throws XMLFactoryInstantiationException;

}
