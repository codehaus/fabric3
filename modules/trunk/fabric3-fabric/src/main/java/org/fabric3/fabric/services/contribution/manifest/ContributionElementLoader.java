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
import javax.xml.stream.XMLStreamConstants;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.osoa.sca.Constants.SCA_NS;
import org.osoa.sca.annotations.Reference;

import org.fabric3.extension.loader.LoaderExtension;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.services.contribution.ContributionManifest;
import org.fabric3.spi.services.contribution.Deployable;
import org.fabric3.spi.services.contribution.Export;
import org.fabric3.spi.services.contribution.Import;

/**
 * Loads a contribution manifest from a contribution element
 *
 * @version $Rev$ $Date$
 */
public class ContributionElementLoader extends LoaderExtension<ContributionManifest> {
    private static final QName CONTRIBUTION = new QName(SCA_NS, "contribution");

    private final LoaderRegistry registry;

    public ContributionElementLoader(@Reference LoaderRegistry registry) {
        super(registry);
        this.registry = registry;
    }

    public QName getXMLType() {
        return CONTRIBUTION;
    }

    public ContributionManifest load(XMLStreamReader reader, LoaderContext context)
            throws XMLStreamException, LoaderException {
        ContributionManifest contribution = new ContributionManifest();
        while (true) {
            int event = reader.next();
            switch (event) {
            case START_ELEMENT:
                QName element = reader.getName();
                if (CONTRIBUTION.equals(element)) {
                    continue;
                }
                Object o = registry.load(reader, context);
                if (o instanceof Deployable) {
                    contribution.addDeployable((Deployable) o);
                } else if (o instanceof Export) {
                    contribution.addExport((Export) o);
                } else if (o instanceof Import) {
                    contribution.addImport((Import) o);
                } else if (o != null) {
                    throw new InvalidManifestTypeException("Unrecognized type", o.getClass().getName());
                }
                break;
            case XMLStreamConstants.END_ELEMENT:
                if (CONTRIBUTION.equals(reader.getName())) {
                    return contribution;
                }
                break;
            }
        }
    }

}
