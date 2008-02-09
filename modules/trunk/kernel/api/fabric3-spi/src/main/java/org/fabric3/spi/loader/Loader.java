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
package org.fabric3.spi.loader;

import java.net.URL;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.introspection.IntrospectionContext;

/**
 * System service for loading configuration artifacts from an XML source.
 *
 * @version $Rev$ $Date$
 */
public interface Loader {
    /**
     * Parse the supplied XML stream, dispatching to the appropriate registered loader for each element encountered in
     * the stream.
     * <p/>
     * This method must be called with the XML cursor positioned on a START_ELEMENT event. When this method returns, the
     * stream will be positioned on the corresponding END_ELEMENT event.
     *
     * @param reader  the XML stream to parse
     * @param type    the type of Java object that should be returned
     * @param context the current loader context
     * @return the model object obtained by parsing the current element on the stream
     * @throws LoaderException    if there was a problem loading the document
     * @throws XMLStreamException if there was a problem reading the stream
     * @throws ClassCastException if the XML type cannot be cast to the expected output type
     */
    <OUTPUT> OUTPUT load(XMLStreamReader reader, Class<OUTPUT> type, IntrospectionContext context)
            throws XMLStreamException, LoaderException;

    /**
     * Load a model object from a specified location.
     *
     * @param url     the location of an XML document to be loaded
     * @param type    the type of Java Object that should be returned
     * @param context the current loader context
     * @return the model ojbect loaded from the document
     * @throws LoaderException    if there was a problem loading the document
     * @throws ClassCastException if the XML type cannot be cast to the expected output type
     */
    <OUTPUT> OUTPUT load(URL url, Class<OUTPUT> type, IntrospectionContext context) throws LoaderException;
}
