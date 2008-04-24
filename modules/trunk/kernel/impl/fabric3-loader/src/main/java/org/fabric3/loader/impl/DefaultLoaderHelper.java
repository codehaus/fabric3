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
package org.fabric3.loader.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.fabric3.scdl.PolicyAware;
import org.fabric3.introspection.xml.LoaderException;
import org.fabric3.introspection.xml.LoaderHelper;
import org.fabric3.transform.xml.Stream2Element2;
import org.fabric3.transform.TransformationException;
import org.fabric3.loader.common.InvalidPrefixException;

/**
 * Default implementation of the loader helper.
 * 
 * @version $Revision$ $Date$
 */
public class DefaultLoaderHelper implements LoaderHelper {
    private final Stream2Element2 stream2Element;
    private final DocumentBuilderFactory documentBuilderFactory;

    public DefaultLoaderHelper() {
        stream2Element = new Stream2Element2();
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
    }


    public Document loadValue(XMLStreamReader reader) throws XMLStreamException {
        DocumentBuilder builder;
        try {
            builder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new AssertionError(e);
        }
        Document value = builder.newDocument();
        Element root = value.createElement("value");
        value.appendChild(root);
        try {
            stream2Element.transform(reader, root, null);
        } catch (TransformationException e) {
            throw (XMLStreamException) e.getCause();
        }
        return value;
    }
    public void loadPolicySetsAndIntents(PolicyAware policyAware, XMLStreamReader reader) throws LoaderException {
        
        policyAware.setIntents(parseListOfQNames(reader, "requires"));
        policyAware.setPolicySets(parseListOfQNames(reader, "policySets"));

    }


    public Set<QName> parseListOfQNames(XMLStreamReader reader, String attribute) throws InvalidPrefixException {

        Set<QName> qNames = new HashSet<QName>();

        String val = reader.getAttributeValue(null, attribute);
        if (val != null) {
            StringTokenizer tok = new StringTokenizer(val);
            while (tok.hasMoreElements()) {
                qNames.add(createQName(tok.nextToken(), reader));
            }
        }

        return qNames;

    }

    public QName createQName(String name, XMLStreamReader reader) throws InvalidPrefixException {
        QName qName;
        int index = name.indexOf(':');
        if (index != -1) {
            String prefix = name.substring(0, index);
            String localPart = name.substring(index + 1);
            String ns = reader.getNamespaceContext().getNamespaceURI(prefix);
            if (ns == null) {
                throw new InvalidPrefixException("Invalid prefix: " + prefix, prefix);
            }
            qName = new QName(ns, localPart, prefix);
        } else {
            String prefix = "";
            String ns = reader.getNamespaceURI();
            qName = new QName(ns, name, prefix);
        }
        return qName;
    }

    public URI getURI(String target) {
        int index = target.lastIndexOf('/');
        if (index == -1) {
            return URI.create(target);
        } else {
            String uri = target.substring(0, index);
            String fragment = target.substring(index + 1);
            return URI.create(uri + '#' + fragment);
        }
    }

    public List<URI> parseListOfUris(XMLStreamReader reader, String attribute) throws LoaderException {
        String value = reader.getAttributeValue(null, attribute);
        if (value == null || value.length() == 0) {
            return null;
        } else {
            try {
                String[] uris = value.split(" ");
                List<URI> result = new ArrayList<URI>(uris.length);
                for (String uri : uris) {
                    result.add(new URI(uri));
                }
                return result;
            } catch (URISyntaxException e) {
                throw new LoaderException(e);
            }
        }
    }
}
