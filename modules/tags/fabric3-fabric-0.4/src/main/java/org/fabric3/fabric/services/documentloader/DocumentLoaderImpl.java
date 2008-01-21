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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.fabric3.fabric.util.IOHelper;

/**
 * Default implementation that creates a new DocumentBuilder for every invocation.
 * <p/>
 * URI resolution is handled by the underlying JAXP implementation.
 *
 * @version $Rev$ $Date$
 */
public class DocumentLoaderImpl implements DocumentLoader {
    private static final DocumentBuilderFactory DOCUMENT_FACTORY;

    static {
        DOCUMENT_FACTORY = DocumentBuilderFactory.newInstance();
        DOCUMENT_FACTORY.setNamespaceAware(true);
    }

    public Document load(File file) throws IOException, SAXException {
        DocumentBuilder builder = getBuilder();
        return builder.parse(file);
    }

    public Document load(URL url) throws IOException, SAXException {
        InputStream stream = url.openStream();
        try {
            stream = new BufferedInputStream(stream);
            DocumentBuilder builder = getBuilder();
            return builder.parse(stream);
        } finally {
            IOHelper.closeQueitly(stream);
        }
    }

    public Document load(URI uri) throws IOException, SAXException {
        DocumentBuilder builder = getBuilder();
        return builder.parse(uri.toString());
    }

    public Document load(InputSource source) throws IOException, SAXException {
        DocumentBuilder builder = getBuilder();
        return builder.parse(source);
    }

    private DocumentBuilder getBuilder() {
        try {
            return DOCUMENT_FACTORY.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new AssertionError();
        }
    }
}
