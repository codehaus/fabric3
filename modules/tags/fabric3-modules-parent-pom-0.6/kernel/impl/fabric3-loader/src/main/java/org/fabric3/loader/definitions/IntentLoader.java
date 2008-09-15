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
package org.fabric3.loader.definitions;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.InvalidPrefixException;
import org.fabric3.introspection.xml.LoaderHelper;
import org.fabric3.introspection.xml.TypeLoader;
import org.fabric3.introspection.xml.UnrecognizedAttribute;
import org.fabric3.loader.impl.InvalidQNamePrefix;
import org.fabric3.scdl.definitions.Intent;

/**
 * Loader for definitions.
 *
 * @version $Revision$ $Date$
 */
public class IntentLoader implements TypeLoader<Intent> {

    private final LoaderHelper helper;

    public IntentLoader(@Reference LoaderHelper helper) {
        this.helper = helper;
    }

    public Intent load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        validateAttributes(reader, context);
        String name = reader.getAttributeValue(null, "name");
        QName qName = new QName(context.getTargetNamespace(), name);

        String constrainsVal = reader.getAttributeValue(null, "constrains");
        QName constrains = null;
        if (constrainsVal != null) {
            try {
                constrains = helper.createQName(constrainsVal, reader);
            } catch (InvalidPrefixException e) {
                context.addError(new InvalidQNamePrefix(e.getPrefix(), reader));
                return null;
            }
        }

        String description = null;

        String requiresVal = reader.getAttributeValue(null, "requires");
        Set<QName> requires = new HashSet<QName>();
        if (requiresVal != null) {
            StringTokenizer tok = new StringTokenizer(requiresVal);
            while (tok.hasMoreElements()) {
                try {
                    QName id = helper.createQName(tok.nextToken(), reader);
                    requires.add(id);
                } catch (InvalidPrefixException e) {
                    context.addError(new InvalidQNamePrefix(e.getPrefix(), reader));
                    return null;
                }
            }
        }

        while (true) {
            switch (reader.next()) {
            case START_ELEMENT:
                if (DefinitionsLoader.DESCRIPTION.equals(reader.getName())) {
                    description = reader.getElementText();
                }
                break;
            case END_ELEMENT:
                if (DefinitionsLoader.INTENT.equals(reader.getName())) {
                    return new Intent(qName, description, constrains, requires);
                }
            }
        }

    }

    private void validateAttributes(XMLStreamReader reader, IntrospectionContext context) {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (!"name".equals(name) && !"constrains".equals(name) && !"requires".equals(name)) {
                context.addError(new UnrecognizedAttribute(name, reader));
            }
        }
    }

}
