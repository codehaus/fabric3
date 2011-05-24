package org.fabric3.assembly.configuration;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.fabric3.assembly.dependency.UpdatePolicy;

import java.io.File;

/**
 * @author Michal Capo
 */
public class Composite {

    private String mName;

    private File mPath;

    private String mDependency;

    private UpdatePolicy mUpdatePolicy;

    //TODO <capo> add constructor without update policy
    public Composite(String pName, String pDependency, UpdatePolicy pUpdatePolicy) {
        mName = pName;
        mDependency = pDependency;
        mUpdatePolicy = pUpdatePolicy;
    }

    public Composite(String pName, File pPath, UpdatePolicy pUpdatePolicy) {
        mName = pName;
        mPath = pPath;
        mUpdatePolicy = pUpdatePolicy;
    }

    public String getName() {
        return mName;
    }

    public File getPath() {
        return mPath;
    }

    public void setUpdatePolicy(UpdatePolicy pUpdatePolicy) {
        mUpdatePolicy = pUpdatePolicy;
    }

    public UpdatePolicy getUpdatePolicy() {
        return mUpdatePolicy;
    }

    public String getDependency() {
        return mDependency;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
                append("name", mName).
                append("path", mPath).
                append("dependency", mDependency).
                append("updatePolicy", mUpdatePolicy).
                toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Composite composite = (Composite) o;

        if (mName != null ? !mName.equals(composite.mName) : composite.mName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return mName != null ? mName.hashCode() : 0;
    }
}
