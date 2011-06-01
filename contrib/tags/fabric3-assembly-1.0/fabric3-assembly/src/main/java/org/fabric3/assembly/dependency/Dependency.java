package org.fabric3.assembly.dependency;

import org.fabric3.assembly.utils.DependencyUtils;

/**
 * @author Michal Capo
 */
public class Dependency {

    private String mGroup;

    private String mArtifact;

    private String mClassifier = null;

    private String mType = "jar";

    private Version mVersion;

    public Dependency(String pGroup, String pArtifact) {
        this(pGroup, pArtifact, null);
    }

    public Dependency(String pGroup, String pArtifact, Version pVersion) {
        mGroup = pGroup;
        mArtifact = pArtifact;
        mVersion = pVersion;
    }

    public Dependency(String pGroup, String pArtifact, String pClassifier, String pType) {
        this(pGroup, pArtifact, null, pClassifier, pType);
    }

    public Dependency(String pGroup, String pArtifact, Version pVersion, String pClassifier, String pType) {
        mGroup = pGroup;
        mArtifact = pArtifact;
        mVersion = pVersion;
        mClassifier = pClassifier;
        mType = pType;
    }

    public void setVersion(Version pVersion) {
        mVersion = pVersion;
    }

    public String getGroup() {
        return mGroup;
    }

    public String getArtifact() {
        return mArtifact;
    }

    public String getClassifier() {
        return mClassifier;
    }

    public String getType() {
        return mType;
    }

    public Version getVersion() {
        return mVersion;
    }

    public boolean isVersionLess() {
        return null == mVersion;
    }

    @Override
    public String toString() {
        return DependencyUtils.convertToString(this);
    }

}
