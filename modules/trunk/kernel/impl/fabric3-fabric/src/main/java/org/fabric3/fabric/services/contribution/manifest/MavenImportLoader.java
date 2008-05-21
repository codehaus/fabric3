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
import org.fabric3.introspection.xml.LoaderException;
import org.fabric3.introspection.xml.TypeLoader;

/**
 * Loads Maven import entries in a contribution manifest.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class MavenImportLoader implements TypeLoader<MavenImport> {
    //private static final QName IMPORT = new QName(Constants.FABRIC3_MAVEN_NS, "import");

    public MavenImport load(XMLStreamReader reader, IntrospectionContext context)
            throws XMLStreamException, LoaderException {
        String groupId = reader.getAttributeValue(null, "groupId");
        if (groupId == null) {
            throw new MissingMainifestAttributeException("The groupId attribute must be specified", reader);
        }
        String artifactId = reader.getAttributeValue(null, "artifactId");
        if (artifactId == null) {
            throw new MissingMainifestAttributeException("The artifactId attribute must be specified", reader);
        }
        String version = reader.getAttributeValue(null, "version");
        String classifier = reader.getAttributeValue(null, "classifier");

        MavenImport imprt = new MavenImport();
        imprt.setGroupId(groupId);
        imprt.setArtifactId(artifactId);
        if (version != null) {
            imprt.setVersion(version);
        }
        if (classifier != null) {
            imprt.setClassifier(classifier);
        }
        return imprt;
    }
}
