package org.fabric3.assembly.dependency;

import org.fabric3.assembly.utils.DependencyUtils;

/**
 * @author Michal Capo
 */
public class Dependency extends PartialDependency {

    private Version mVersion;

    public Dependency(String pDependency) {
        super(null, null);

        Dependency temp = DependencyUtils.convertToDependency(pDependency);
        setGroup(temp.getGroup());
        setArtifact(temp.getArtifact());
        setClassifier(temp.getClassifier());
        setType(temp.getType());
        mVersion = temp.getVersion();
    }

    public Dependency(PartialDependency pPartialDependency, Version pVersion) {
        super(pPartialDependency);
        mVersion = pVersion;
    }

    public Dependency(String pGroup, String pArtifact, Version pVersion) {
        super(pGroup, pArtifact);
        mVersion = pVersion;
    }

    public Dependency(String pGroup, String pArtifact, String pClassifier, String pType, Version pVersion) {
        super(pGroup, pArtifact, pClassifier, pType);
        mVersion = pVersion;
    }

    public Version getVersion() {
        return mVersion;
    }

    @Override
    public String toString() {
        return DependencyUtils.convertToString(this);
    }
}
