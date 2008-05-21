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
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.contribution.Constants;
import org.fabric3.host.contribution.Deployable;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.LoaderException;
import org.fabric3.introspection.xml.LoaderHelper;
import org.fabric3.introspection.xml.LoaderRegistry;
import org.fabric3.introspection.xml.TypeLoader;
import org.fabric3.spi.services.contribution.ContributionManifest;
import org.fabric3.spi.services.contribution.Export;
import org.fabric3.spi.services.contribution.Import;

/**
 * Loads a contribution manifest from a contribution element
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class ContributionElementLoader implements TypeLoader<ContributionManifest> {
    private static final QName CONTRIBUTION = new QName(SCA_NS, "contribution");
    private static final QName DEPLOYABLE = new QName(SCA_NS, "deployable");

    private final LoaderRegistry registry;
    private final LoaderHelper helper;

    public ContributionElementLoader(@Reference LoaderRegistry registry,
                                     @Reference LoaderHelper helper) {
        this.registry = registry;
        this.helper = helper;
    }

    @Init
    public void start() {
        registry.registerLoader(CONTRIBUTION, this);
    }

    @Destroy
    public void stop() {
        registry.unregisterLoader(CONTRIBUTION);
    }


    public ContributionManifest load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException, LoaderException {
        ContributionManifest contribution = new ContributionManifest();
        while (true) {
            int event = reader.next();
            switch (event) {
            case START_ELEMENT:
                QName element = reader.getName();
                if (CONTRIBUTION.equals(element)) {
                    continue;
                } else if (DEPLOYABLE.equals(element)) {
                    String name = reader.getAttributeValue(null, "composite");
                    if (name == null) {
                        throw new MissingMainifestAttributeException("Composite attribute must be specified", reader);
                    }
                    QName qName = helper.createQName(name, reader);
                    Deployable deployable = new Deployable(qName, Constants.COMPOSITE_TYPE);
                    contribution.addDeployable(deployable);
                } else {
                    Object o = registry.load(reader, Object.class, context);
                    if (o instanceof Export) {
                        contribution.addExport((Export) o);
                    } else if (o instanceof Import) {
                        contribution.addImport((Import) o);
                    } else if (o != null) {
                        String type = o.getClass().getName();
                        throw new InvalidManifestTypeException("Unrecognized type: " + type, reader);
                    }
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
