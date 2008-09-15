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
package org.fabric3.fabric.services.contribution.manifest;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.TypeLoader;
import org.fabric3.introspection.xml.UnrecognizedAttribute;
import org.fabric3.spi.services.contribution.QNameExport;

/**
 * Processes a QName-based <code>export</code> element in a contribution manifest
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class QNameExportLoader implements TypeLoader<QNameExport> {
    //private static final QName EXPORT = new QName(SCA_NS, "export");

    public QNameExport load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        validateAttributes(reader, context);
        String ns = reader.getAttributeValue(null, "namespace");
        if (ns == null) {
            MissingMainifestAttribute failure = new MissingMainifestAttribute("The namespace attribute must be specified", "namespace", reader);
            context.addError(failure);
            return null;
        }
        return new QNameExport(new QName(ns));
    }

    private void validateAttributes(XMLStreamReader reader, IntrospectionContext context) {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (!"namespace".equals(name)) {
                context.addError(new UnrecognizedAttribute(name, reader));
            }
        }
    }

}
