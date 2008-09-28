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
