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

import org.osoa.sca.annotations.Property;

import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.ContributionServiceListener;
import org.fabric3.spi.contribution.Import;
import org.fabric3.spi.contribution.manifest.JavaImport;
import org.fabric3.spi.contribution.manifest.PackageInfo;

/**
 * Adds an implicit import of the Hibernate contribution extension into any contribution using JPA on a runtime configured to use Hibernate. This is
 * necessary as Hibernate's use of CGLIB for generating proxies requires that Hibernate classes (specifically, HibernateDelegate) be visible from the
 * classloader that loaded a particular entity (i.e. the application classloader). If a Hibernate is explicitly imported in a contribution manifest
 * (sca-contribution.xml), it is used instead.
 *
 * @version $Revision$ $Date$
 */
public class HibernateContributionListener implements ContributionServiceListener {
    private JavaImport hibernateImport;
    private boolean noImplicitImport;

    public HibernateContributionListener() {
        PackageInfo hibernateIinfo = new PackageInfo("org.hibernate.*");
        hibernateImport = new JavaImport(hibernateIinfo);
    }

    @Property(required = false)
    public void setNoImplicitImport(boolean noImplicitImport) {
        this.noImplicitImport = noImplicitImport;
    }

    public void onProcessManifest(Contribution contribution) {
        if (noImplicitImport) {
            return;
        }
        ContributionManifest manifest = contribution.getManifest();
        boolean jpaImported = false;
        for (Import imprt : manifest.getImports()) {
            if (imprt instanceof JavaImport) {
                JavaImport contributionImport = (JavaImport) imprt;
                if (contributionImport.getPackageInfo().getName().startsWith("org.hibernate")) {
                    // already explicitly imported, return
                    return;
                } else if (contributionImport.getPackageInfo().getName().startsWith("javax.persistence")) {
                    jpaImported = true;
                }
            }
        }
        if (jpaImported) {
            // JPA is imported, add implicit Hibernate import
            manifest.addImport(hibernateImport);
        }
    }

    public void onStore(Contribution contribution) {
        // no-op
    }

    public void onInstall(Contribution contribution) {
        // no-op
    }

    public void onUpdate(Contribution contribution) {
        // no-op
    }

    public void onUninstall(Contribution contribution) {
        // no-op
    }

    public void onRemove(Contribution contribution) {
        // no-op
    }
}
