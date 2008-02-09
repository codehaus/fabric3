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

import java.net.URI;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.osoa.sca.Constants.SCA_NS;
import org.osoa.sca.annotations.Reference;

import org.fabric3.extension.loader.LoaderExtension;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.services.contribution.ManifestLoadException;
import org.fabric3.spi.services.contribution.QNameImport;

/**
 * Processes a QName-based <code>import</code> element in a contribution manifest
 *
 * @version $Rev$ $Date$
 */
public class QNameImportLoader extends LoaderExtension<QNameImport> {
    private static final QName IMPORT = new QName(SCA_NS, "import");

    /**
     * Constructor specifies the registry to register with.
     *
     * @param registry the LoaderRegistry this loader should register with
     */
    public QNameImportLoader(@Reference LoaderRegistry registry) {
        super(registry);
    }

    public QName getXMLType() {
        return IMPORT;
    }

    public QNameImport load(XMLStreamReader reader, IntrospectionContext context)
            throws ManifestLoadException, XMLStreamException {
        String ns = reader.getAttributeValue(null, "namespace");
        if (ns == null) {
            throw new MissingAttributeException("namespace");
        }
        String location = reader.getAttributeValue(null, "location");
        QNameImport contributionImport = new QNameImport(new QName(ns));
        if (location != null) {
            contributionImport.setLocation(URI.create(location));
        }
        return contributionImport;
    }
}
