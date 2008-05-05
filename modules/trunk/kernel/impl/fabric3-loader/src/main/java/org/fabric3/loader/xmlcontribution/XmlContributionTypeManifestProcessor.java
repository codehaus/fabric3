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
package org.fabric3.loader.xmlcontribution;

import java.io.FileNotFoundException;
import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.osoa.sca.Constants.SCA_NS;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.Deployable;
import org.fabric3.introspection.DefaultIntrospectionContext;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.Loader;
import org.fabric3.introspection.xml.LoaderException;
import static org.fabric3.spi.Constants.FABRIC3_NS;
import org.fabric3.spi.services.contribution.ContributionManifest;
import org.fabric3.spi.services.contribution.Export;
import org.fabric3.spi.services.contribution.Import;
import org.fabric3.spi.services.contribution.XmlElementManifestProcessor;
import org.fabric3.spi.services.contribution.XmlManifestProcessorRegistry;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
public class XmlContributionTypeManifestProcessor implements XmlElementManifestProcessor {
    private static final QName XML_CONTRIBUTION = new QName(FABRIC3_NS, "xmlContribution");
    private static final QName SCA_CONTRIBUTION = new QName(SCA_NS, "contribution");
    private XmlManifestProcessorRegistry manifestProcessorRegistry;
    private Loader loader;

    public XmlContributionTypeManifestProcessor(@Reference XmlManifestProcessorRegistry manifestProcessorRegistry, @Reference Loader loader) {
        this.manifestProcessorRegistry = manifestProcessorRegistry;
        this.loader = loader;
    }

    @Init
    public void init() {
        manifestProcessorRegistry.register(this);
    }

    public QName getType() {
        return XML_CONTRIBUTION;
    }

    public void process(ContributionManifest manifest, XMLStreamReader reader) throws ContributionException {
        try {
            while (true) {
                int i = reader.next();
                switch (i) {
                case START_ELEMENT:
                    QName qname = reader.getName();
                    if (SCA_CONTRIBUTION.equals(qname)) {
                        ClassLoader cl = getClass().getClassLoader();
                        IntrospectionContext context = new DefaultIntrospectionContext(cl, null, null);
                        ContributionManifest embeddedManifest = loader.load(reader, ContributionManifest.class, context);
                        // merge the contents
                        for (Deployable deployable : embeddedManifest.getDeployables()) {
                            manifest.addDeployable(deployable);
                        }
                        for (Export export : embeddedManifest.getExports()) {
                            manifest.addExport(export);
                        }
                        for (Import imprt : embeddedManifest.getImports()) {
                            manifest.addImport(imprt);
                        }
                    }
                    break;
                case END_ELEMENT:
                    if (SCA_CONTRIBUTION.equals(reader.getName())) {
                        // if we reached here, version was never specified and there are no dependencies
                        return;
                    }
                    break;
                case END_DOCUMENT:
                    return;
                }

            }

        } catch (LoaderException e) {
            if (e.getCause() instanceof FileNotFoundException) {
                return;
            } else {
                throw new ContributionException(e);
            }
        } catch (XMLStreamException e) {
            throw new ContributionException(e);
        }

    }


}
