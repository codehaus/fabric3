/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.contribution.manifest;

import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.contribution.InstallException;
import org.fabric3.model.type.ValidationContext;
import org.fabric3.spi.services.contribution.ContributionManifest;
import org.fabric3.spi.services.contribution.manifest.MavenExport;
import org.fabric3.spi.services.contribution.manifest.XmlElementManifestProcessor;
import org.fabric3.spi.services.contribution.manifest.XmlManifestProcessorRegistry;

/**
 * Loads Maven export entries in a contribution manifest by parsing a pom.xml file contained in a contribution.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class MavenPOMProcessor implements XmlElementManifestProcessor {

    public static final String NS = "http://maven.apache.org/POM/4.0.0";
    private static final QName PROJECT = new QName(NS, "project");

    private XmlManifestProcessorRegistry registry;

    public MavenPOMProcessor() {
    }

    @Constructor
    public MavenPOMProcessor(@Reference XmlManifestProcessorRegistry registry) {
        this.registry = registry;
    }

    @Init
    public void init() {
        if (registry != null) {
            registry.register(this);
        }
    }

    public QName getType() {
        return PROJECT;
    }

    public void process(ContributionManifest manifest, XMLStreamReader reader, ValidationContext context) throws InstallException {
        String parentVersion = null;
        String groupId = null;
        String artifactId = null;
        String version = null;
        String packaging = null;
        boolean loop = true;
        QName firstChildElement = null;
        try {
            while (loop) {
                switch (reader.next()) {
                case START_ELEMENT:
                    if (reader.getName().getLocalPart().equals("parent")) {
                        parentVersion = parseParent(reader);
                    } else if (firstChildElement == null && reader.getName().getLocalPart().equals("groupId")) {
                        groupId = reader.getElementText();
                    } else if (firstChildElement == null && reader.getName().getLocalPart().equals("artifactId")) {
                        artifactId = reader.getElementText();
                    } else if (firstChildElement == null && reader.getName().getLocalPart().equals("packaging")) {
                        packaging = reader.getElementText();
                    } else if (firstChildElement == null && reader.getName().getLocalPart().equals("version")) {
                        version = reader.getElementText();
                    } else if (firstChildElement == null) {
                        // keep track of child element below <project>. This is used to ignore values in subelements for version, groupid, and
                        // artifact id wich pertain to different contexts, e.g. <dependency>
                        firstChildElement = reader.getName();
                    }
                    break;
                case END_ELEMENT:
                    if (reader.getName().getLocalPart().equals("project")) {
                        loop = false;
                        continue;
                    } else {
                        if (reader.getName().equals(firstChildElement)) {
                            firstChildElement = null;
                        }
                    }
                }
            }
        } catch (XMLStreamException e) {
            throw new InstallException(e);
        }

        if (version == null || "".equals(version)) {
            version = parentVersion;
        }

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


    }

    private String parseParent(XMLStreamReader reader) throws XMLStreamException {
        String version = null;
        while (true) {
            switch (reader.next()) {
            case START_ELEMENT:
                if (reader.getName().getLocalPart().equals("version")) {
                    version = reader.getElementText();
                }
                break;
            case END_ELEMENT:
                if (reader.getName().getLocalPart().equals("parent")) {
                    return version;
                }
            }
        }
    }
}