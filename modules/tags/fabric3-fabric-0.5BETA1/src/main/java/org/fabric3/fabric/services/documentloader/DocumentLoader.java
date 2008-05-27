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
package org.fabric3.fabric.services.documentloader;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Service interface for loading XML documents from a file as DOM objects.
 *
 * @version $Rev$ $Date$
 */
public interface DocumentLoader {
    /**
     * Loads a Document from a local file.
     *
     * @param file the file containing the XML document
     * @return the content of the file as a Document
     * @throws IOException  if there was a problem reading the file
     * @throws SAXException if there was a problem with the document
     */
    Document load(File file) throws IOException, SAXException;

    /**
     * Loads a Document from a physical resource.
     *
     * @param url the location of the resource
     * @return the content of the resource as a Document
     * @throws IOException  if there was a problem reading the resource
     * @throws SAXException if there was a problem with the document
     */
    Document load(URL url) throws IOException, SAXException;

    /**
     * Loads a Document from a logical resource.
     * <p/>
     * How the resource is converted to a physical location is implementation defined.
     *
     * @param uri the logical location of the resource
     * @return the content of the resource as a Document
     * @throws IOException  if there was a problem reading the resource
     * @throws SAXException if there was a problem with the document
     */
    Document load(URI uri) throws IOException, SAXException;

    /**
     * Loads a Document from a logical source.
     *
     * @param source the source of the document text
     * @return the content as a Document
     * @throws IOException  if there was a problem reading the content
     * @throws SAXException if there was a problem with the document
     */
    Document load(InputSource source) throws IOException, SAXException;
}
