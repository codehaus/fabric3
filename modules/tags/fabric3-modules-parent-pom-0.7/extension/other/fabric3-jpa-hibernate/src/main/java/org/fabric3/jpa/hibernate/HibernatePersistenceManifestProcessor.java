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
package org.fabric3.jpa.hibernate;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.contribution.InstallException;
import org.fabric3.model.type.ValidationContext;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.Import;
import org.fabric3.spi.contribution.manifest.MavenImport;
import org.fabric3.spi.contribution.manifest.XmlElementManifestProcessor;
import org.fabric3.spi.contribution.manifest.XmlManifestProcessorRegistry;

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

    public void process(ContributionManifest manifest, XMLStreamReader reader, ValidationContext context) throws InstallException {
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
