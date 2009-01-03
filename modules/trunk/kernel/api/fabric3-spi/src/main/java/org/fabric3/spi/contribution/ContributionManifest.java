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
package org.fabric3.spi.contribution;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.host.contribution.Deployable;

/**
 * Represents a contribution manifest
 *
 * @version $Rev$ $Date$
 */
@SuppressWarnings({"SerializableHasSerializationMethods"})
public class ContributionManifest implements Serializable {
    private static final long serialVersionUID = -4968254313720890686L;
    private boolean extension;
    private List<Export> exports = new ArrayList<Export>();
    private List<Import> imports = new ArrayList<Import>();
    private List<Deployable> deployables = new ArrayList<Deployable>();

    /**
     * Rturns true if the contribution is an extension.
     *
     * @return true if the contribution is an extension
     */
    public boolean isExtension() {
        return extension;
    }

    /**
     * Sets if the contribution is an extension.
     *
     * @param extension true if the contribution is an extension
     */
    public void setExtension(boolean extension) {
        this.extension = extension;
    }

    /**
     * Returns the contribution exports.
     *
     * @return the contribution exports
     */
    public List<Export> getExports() {
        return exports;
    }

    /**
     * Adds a contribution export.
     *
     * @param export the contribution export
     */
    public void addExport(Export export) {
        exports.add(export);
    }

    /**
     * Returns the contribution imports.
     *
     * @return the contribution imports
     */
    public List<Import> getImports() {
        return imports;
    }

    /**
     * Adds a contribution import.
     *
     * @param imprt the contribution import
     */
    public void addImport(Import imprt) {
        imports.add(imprt);
    }

    /**
     * Returns the contribution deployables.
     *
     * @return the contribution deployables
     */

    public List<Deployable> getDeployables() {
        return deployables;
    }

    /**
     * Adds a contribution deployable.
     *
     * @param deployale the contribution deployable
     */
    public void addDeployable(Deployable deployale) {
        deployables.add(deployale);
    }

}
