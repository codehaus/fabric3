package org.fabric3.spi.services.contribution;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
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
    private List<Export> exports = new ArrayList<Export>();
    private List<Import> imports = new ArrayList<Import>();
    private List<Deployable> deployables = new ArrayList<Deployable>();

    public List<Export> getExports() {
        return Collections.unmodifiableList(exports);
    }

    public void addExport(Export export) {
        exports.add(export);
    }

    public List<Import> getImports() {
        return Collections.unmodifiableList(imports);
    }

    public void addImport(Import artifactImport) {
        imports.add(artifactImport);
    }

    public List<Deployable> getDeployables() {
        return Collections.unmodifiableList(deployables);
    }

    public void addDeployable(Deployable deployale) {
        deployables.add(deployale);
    }

}
