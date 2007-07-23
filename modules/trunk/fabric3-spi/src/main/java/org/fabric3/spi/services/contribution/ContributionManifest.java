package org.fabric3.spi.services.contribution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.io.Serializable;
import javax.xml.namespace.QName;

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
    private List<QName> deployables = new ArrayList<QName>();

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

    public List<QName> getDeployables() {
        return Collections.unmodifiableList(deployables);
    }

    public void addDeployable(QName deployale) {
        deployables.add(deployale);
    }

}
