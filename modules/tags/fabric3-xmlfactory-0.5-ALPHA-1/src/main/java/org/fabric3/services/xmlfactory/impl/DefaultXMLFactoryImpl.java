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
package org.fabric3.services.xmlfactory.impl;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import org.fabric3.services.xmlfactory.XMLFactory;
import org.fabric3.services.xmlfactory.XMLFactoryInstantiationException;

/**
 * Implementation of XMLFactory that uses the default factories provided by the StAX API.
 * <p/>
 * In general, this should only be used when it is known that the API and it's default implementation are adequate. For example, this could be used in
 * a Java6 environment when the implementation desired is the one from the JRE.
 *
 * @version $Rev$ $Date$
 */
public class DefaultXMLFactoryImpl implements XMLFactory {
    public XMLInputFactory newInputFactoryInstance() throws XMLFactoryInstantiationException {
        return XMLInputFactory.newInstance();
    }

    public XMLOutputFactory newOutputFactoryInstance() throws XMLFactoryInstantiationException {
        return XMLOutputFactory.newInstance();
    }
}
