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

package org.fabric3.services.xmlfactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

/**
 * This service has been added as a work around to a problem in JDK stax parser api
 * This allows to get instances of XML input and output factories
 */

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
