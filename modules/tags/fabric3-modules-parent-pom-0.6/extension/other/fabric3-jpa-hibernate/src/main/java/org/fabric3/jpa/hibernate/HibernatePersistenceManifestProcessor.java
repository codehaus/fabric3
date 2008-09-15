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
package org.fabric3.jpa.hibernate;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.contribution.ContributionException;
import org.fabric3.scdl.ValidationContext;
import org.fabric3.spi.services.contribution.ContributionManifest;
import org.fabric3.spi.services.contribution.Import;
import org.fabric3.spi.services.contribution.MavenImport;
import org.fabric3.spi.services.contribution.XmlElementManifestProcessor;
import org.fabric3.spi.services.contribution.XmlManifestProcessorRegistry;

/**
 * Adds an implicit import of the Hibernate contribution extension into any contribution using JPA on a runtime cnfigured to use Hibernate. This is
 * necessary as Hibernate's use of CGLIB for generating proxies requires that Hibernate classes (specifically, HibernateDelegate) be visible from the
 * classloader that loaded a particular entity (i.e. the application classloader). If a Hibernate is explicitly imported in a contribution manifest
 * (sca-contribution.xml), it is used instead.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class HibernatePersistenceManifestProcessor implements XmlElementManifestProcessor {
    public static final QName PERSISTENCE = new QName("http://java.sun.com/xml/ns/persistence", "persistence");
    public static final String GROUP_ID = "org.codehaus.fabric3";
    public static final String ARTIFACT_ID = "fabric3-jpa-hibernate";
    private XmlManifestProcessorRegistry registry;

    public HibernatePersistenceManifestProcessor(@Reference XmlManifestProcessorRegistry registry) {
        this.registry = registry;
    }

    public QName getType() {
        return PERSISTENCE;
    }

    @Init
    public void init() {
        registry.register(this);
    }

    public void process(ContributionManifest manifest, XMLStreamReader reader, ValidationContext context) throws ContributionException {
        // TODO this assumes Hibernate is available on the controller, which is not necessary since it is only required at runtime.
        //  An import scope similar to Maven's "runtime" would be a possible solution.
        for (Import imprt : manifest.getImports()) {
            if (imprt instanceof MavenImport) {
                MavenImport mvnImport = (MavenImport) imprt;
                if (ARTIFACT_ID.equals(mvnImport.getArtifactId()) && GROUP_ID.equals(mvnImport.getGroupId())) {
                    // already explicitly imported, return
                    return;
                }
            }
        }
        MavenImport imprt = new MavenImport();
        imprt.setGroupId(GROUP_ID);
        imprt.setArtifactId(ARTIFACT_ID);
        manifest.addImport(imprt);
    }
}
