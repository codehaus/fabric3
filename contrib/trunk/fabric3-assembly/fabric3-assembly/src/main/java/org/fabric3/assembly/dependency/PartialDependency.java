package org.fabric3.assembly.dependency;

/**
 * @author Michal Capo
 */
public class PartialDependency {

    private String mGroup;

    private String mArtifact;

    private String mClassifier = null;

    private String mType = "jar";

    public PartialDependency(PartialDependency pDependency) {
        this(pDependency.getGroup(), pDependency.getArtifact(), pDependency.getClassifier(), pDependency.getType());
    }

    public PartialDependency(String pGroup, String pArtifact) {
        mGroup = pGroup;
        mArtifact = pArtifact;
    }

    public PartialDependency(String pGroup, String pArtifact, String pClassifier, String pType) {
        mGroup = pGroup;
        mArtifact = pArtifact;
        mClassifier = pClassifier;
        mType = pType;
    }

    public void setGroup(String pGroup) {
        mGroup = pGroup;
    }

    public void setArtifact(String pArtifact) {
        mArtifact = pArtifact;
    }

    public void setClassifier(String pClassifier) {
        mClassifier = pClassifier;
    }

    public void setType(String pType) {
        mType = pType;
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

}
