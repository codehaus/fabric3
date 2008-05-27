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
package org.fabric3.fabric.services.contribution.manifest;

import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.contribution.ContributionException;
import org.fabric3.scdl.ValidationContext;
import org.fabric3.spi.services.contribution.ContributionManifest;
import org.fabric3.spi.services.contribution.XmlElementManifestProcessor;
import org.fabric3.spi.services.contribution.XmlManifestProcessorRegistry;

/**
 * Loads Maven export entries in a contribution manifest by parsing a pom.xml file contained in a contribution.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class MavenPOMProcessor implements XmlElementManifestProcessor {
    public static final String NS = "http://maven.apache.org/POM/4.0.0";
    private static final QName PROJECT = new QName(NS, "project");
    private static final QName PARENT = new QName(NS, "parent");
    private static final QName GROUP_ID = new QName(NS, "groupId");
    private static final QName ARTIFACT_ID = new QName(NS, "artifactId");
    private static final QName PACKAGING = new QName(NS, "packaging");
    private static final QName VERSION = new QName(NS, "version");
    private static final QName DEPENDENCIES = new QName(NS, "dependencies");

    private XmlManifestProcessorRegistry registry;

    public MavenPOMProcessor(@Reference XmlManifestProcessorRegistry registry) {
        this.registry = registry;
    }

    @Init
    public void init() {
        registry.register(this);
    }

    public QName getType() {
        return PROJECT;
    }

    public void process(ContributionManifest manifest, XMLStreamReader reader, ValidationContext context) throws ContributionException {
        String groupId = null;
        String artifactId = null;
        String version;
        String packaging = null;
        try {
            while (true) {
                switch (reader.next()) {
                case START_ELEMENT:
                    QName qname = reader.getName();
                    if (PARENT.equals(qname)) {
                        // skip parent entries
                        while (true) {
                            if (reader.next() == END_ELEMENT && reader.getName().equals(qname)) {
                                break;
                            }
                        }
                    } else if (GROUP_ID.equals(qname)) {
                        groupId = reader.getElementText();
                    } else if (ARTIFACT_ID.equals(qname)) {
                        artifactId = reader.getElementText();
                    } else if (PACKAGING.equals(qname)) {
                        packaging = reader.getElementText();
                    } else if (VERSION.equals(qname)) {
                        version = reader.getElementText();
                        if (groupId == null) {
                            context.addError(new InvalidPOM("Group id not specified", "groupId", reader));
                        }
                        if (artifactId == null) {
                            context.addError(new InvalidPOM("Artifact id not specified", "artifactId", reader));
                        }
                        if (version == null) {
                            context.addError(new InvalidPOM("Version not specified", "version", reader));
                        }

                        MavenExport export = new MavenExport();
                        export.setGroupId(groupId);
                        export.setArtifactId(artifactId);
                        if (version != null) {
                            export.setVersion(version);
                        }
                        if (packaging != null) {
                            export.setClassifier(packaging);
                        }
                        manifest.addExport(export);
                        return;
                    } else if (DEPENDENCIES.equals(qname)) {
                        // if we reached here, version was never specified
                        context.addError(new InvalidPOM("Version not specified", "version", reader));
                    }
                    break;
                case END_ELEMENT:
                    if (PROJECT.equals(reader.getName())) {
                        // if we reached here, version was never specified and there are no dependencies
                        context.addError(new InvalidPOM("Version not specified", "version", reader));
                    }
                }

            }
        } catch (XMLStreamException e) {
            throw new ContributionException(e);
        }
    }
}