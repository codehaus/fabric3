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

import java.util.Collections;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.fabric3.host.contribution.ContributionException;
import org.fabric3.scdl.ValidationContext;
import org.fabric3.spi.services.contribution.ContributionManifest;
import org.fabric3.spi.services.contribution.XmlElementManifestProcessor;
import org.fabric3.spi.services.contribution.XmlManifestProcessorRegistry;
import org.fabric3.spi.services.contribution.MavenExport;
import org.fabric3.transform.TransformationException;
import org.fabric3.transform.xml.Stream2Document;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Element;

/**
 * Loads Maven export entries in a contribution manifest by parsing a pom.xml file contained in a contribution.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class MavenPOMProcessor implements XmlElementManifestProcessor {

    private Stream2Document transformer = new Stream2Document();
    private static final XPathFactory XPATH_Factory = XPathFactory.newInstance();
    
    public static final String NS = "http://maven.apache.org/POM/4.0.0";
    private static final QName PROJECT = new QName(NS, "project");

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
        
        try {
            
            XPath xpath = XPATH_Factory.newXPath();            
            xpath.setNamespaceContext(new NamespaceContext() {
                public String getNamespaceURI(String prefix) {
                    return NS;
                }
                public String getPrefix(String namespaceURI) {
                    return "mvn";
                }
                public Iterator getPrefixes(String namespaceURI) {
                    return Collections.singletonList("").iterator();
                }
                
            });
            
            Element project = transformer.transform(reader, null).getDocumentElement();
            
            String parentVersion = xpath.evaluate("mvn:parent/mvn:version", project);
            String groupId = xpath.evaluate("mvn:groupId", project);
            String artifactId = xpath.evaluate("mvn:artifactId", project);
            String version = xpath.evaluate("mvn:version", project);
            if (version == null || "".equals(version)) {
                version = parentVersion;
            }
            String packaging = xpath.evaluate("mvn:package", project);
            
            if (groupId == null || "".equals(groupId)) {
                context.addError(new InvalidPOM("Group id not specified", "groupId", reader));
            }
            if (artifactId == null || "".equals(artifactId)) {
                context.addError(new InvalidPOM("Artifact id not specified", "artifactId", reader));
            }
            if (version == null || "".equals(version)) {
                context.addError(new InvalidPOM("Version not specified", "version", reader));
            }

            MavenExport export = new MavenExport();
            export.setGroupId(groupId);
            export.setArtifactId(artifactId);
            export.setVersion(version);

            if (packaging != null && "".equals(packaging)) {
                export.setClassifier(packaging);
            }
            
            manifest.addExport(export);
            
        } catch (TransformationException ex) {
            throw new ContributionException(ex);
        } catch (XPathExpressionException ex) {
            throw new ContributionException(ex);
        }
        
    }
}