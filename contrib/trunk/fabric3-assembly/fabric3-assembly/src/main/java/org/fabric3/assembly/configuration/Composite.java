package org.fabric3.assembly.configuration;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.fabric3.assembly.dependency.UpdatePolicy;

import java.io.File;

/**
 * @author Michal Capo
 */
public class Composite {

    private String mName;

    private String mRuntimeName;

    private File mPath;

    private String mDependency;

    private UpdatePolicy mUpdatePolicy;

    public Composite(String pName, String pRuntimeName, String pDependency, UpdatePolicy pUpdatePolicy) {
        mName = pName;
        if (null == pRuntimeName) {
            mRuntimeName = Runtime.RUNTIME_DEFAULT_NAME;
        } else {
            mRuntimeName = pRuntimeName;
        }
        mDependency = pDependency;
        mUpdatePolicy = pUpdatePolicy;
    }

    public Composite(String pName, String pRuntimeName, File pPath, UpdatePolicy pUpdatePolicy) {
        mName = pName;
        if (null == pRuntimeName) {
            mRuntimeName = Runtime.RUNTIME_DEFAULT_NAME;
        } else {
            mRuntimeName = pRuntimeName;
        }
        mPath = pPath;
        mUpdatePolicy = pUpdatePolicy;
    }

    public String getName() {
        return mName;
    }

    public String getRuntimeName() {
        return mRuntimeName;
    }

    public File getPath() {
        return mPath;
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
                append("runtimeName", mRuntimeName).
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
