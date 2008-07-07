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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.TypeLoader;
import org.fabric3.spi.services.contribution.MavenExport;

/**
 * Loads Maven export entries in a contribution manifest.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class MavenExportLoader implements TypeLoader<MavenExport> {

    public MavenExport load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        String groupId = reader.getAttributeValue(null, "groupId");
        if (groupId == null) {
            MissingMainifestAttribute failure = new MissingMainifestAttribute("The groupId attribute must be specified", "groupId", reader);
            context.addError(failure);
            return null;
        }
        String artifactId = reader.getAttributeValue(null, "artifactId");
        if (artifactId == null) {
            MissingMainifestAttribute failure = new MissingMainifestAttribute("The artifictId attribute must be specified", "artifictId", reader);
            context.addError(failure);
            return null;
        }
        String version = reader.getAttributeValue(null, "version");
        String classifier = reader.getAttributeValue(null, "classifier");

        MavenExport export = new MavenExport();
        export.setGroupId(groupId);
        export.setArtifactId(artifactId);
        if (version != null) {
            export.setVersion(version);
        }
        if (classifier != null) {
            export.setClassifier(classifier);
        }
        return export;
    }
}
