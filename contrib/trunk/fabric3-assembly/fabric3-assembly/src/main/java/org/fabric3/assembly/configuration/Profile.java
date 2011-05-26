package org.fabric3.assembly.configuration;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.fabric3.assembly.dependency.Dependency;
import org.fabric3.assembly.dependency.UpdatePolicy;
import org.fabric3.assembly.dependency.Version;
import org.fabric3.assembly.utils.DependencyUtils;
import org.fabric3.assembly.utils.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Michal Capo
 */
public class Profile {

    private String mName;

    private List<String> mAlternativeNames = new ArrayList<String>();

    private UpdatePolicy mUpdatePolicy;

    private Version mVersion;

    private List<Dependency> mDependencies = new ArrayList<Dependency>();

    private List<File> mFiles = new ArrayList<File>();

    public Profile(String pName, UpdatePolicy pUpdatePolicy, Version pVersion, String... pAlternativeNames) {
        mName = pName;
        mUpdatePolicy = pUpdatePolicy;
        mVersion = pVersion;
        if (null != pAlternativeNames) {
            mAlternativeNames = Arrays.asList(pAlternativeNames);
        }
    }

    public void addDependency(Dependency pDependency) {
        if (null != pDependency) {
            mDependencies.add(pDependency);
        }
    }

    public void addDependency(String pDependency) {
        if (!StringUtils.isBlank(pDependency)) {
            mDependencies.add(DependencyUtils.parseDependency(pDependency));
        }
    }

    public void addFile(String pPath) {
        File temp;
        if (!StringUtils.isBlank(pPath) && (temp = new File(pPath)).exists()) {
            mFiles.add(temp);
        }
    }

    public void addFile(File pFile) {
        if (null != pFile) {
            mFiles.add(pFile);
        }
    }

    public String getName() {
        return mName;
    }

    public void addAlternativeNames(String... pAlternativeNames) {
        if (null != pAlternativeNames) {
            mAlternativeNames.addAll(Arrays.asList(pAlternativeNames));
        }
    }

    public List<String> getAlternativeNames() {
        return mAlternativeNames;
    }

    public List<String> getAllNames() {
        ArrayList<String> list = new ArrayList<String>(mAlternativeNames);
        list.add(mName);
        return list;
    }

    public void setUpdatePolicy(UpdatePolicy pUpdatePolicy) {
        mUpdatePolicy = pUpdatePolicy;
    }

    public UpdatePolicy getUpdatePolicy() {
        return mUpdatePolicy;
    }

    public void setVersion(Version pVersion) {
        mVersion = pVersion;
    }

    public Version getVersion() {
        return mVersion;
    }

    public List<Dependency> getDependencies() {
        return mDependencies;
    }

    public List<File> getFiles() {
        return mFiles;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
                append("name", mName).
                append("alternativeNames", mAlternativeNames).
                append("updatePolicy", mUpdatePolicy).
                append("version", mVersion).
                append("dependencies", mDependencies).
                append("files", mFiles).
                toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Profile that = (Profile) o;

        if (mName != null ? !mName.equals(that.mName) : that.mName != null) return false;
        if (mVersion != null ? !mVersion.equals(that.mVersion) : that.mVersion != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = mName != null ? mName.hashCode() : 0;
        result = 31 * result + (mVersion != null ? mVersion.hashCode() : 0);
        return result;
    }

    public void addDependencies(String[] pDependencies) {
        if (null != pDependencies) {
            for (String dependency : pDependencies) {
                addDependency(dependency);
            }
        }
    }

    public void addFiles(File[] pFiles) {
        if (null != pFiles) {
            mFiles.addAll(Arrays.asList(pFiles));
        }
    }
}
