package org.fabric3.assembly.configuration;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.File;

/**
 * @author Michal Capo
 */
public class Composite {

    private String mName;

    private File mPath;

    private String mDependency;

    public Composite(String pName, String pDependency) {
        mName = pName;
        mDependency = pDependency;
    }

    public Composite(String pName, File pPath) {
        mName = pName;
        mPath = pPath;
    }

    public String getName() {
        return mName;
    }

    public void setPath(File pPath) {
        mPath = pPath;
    }

    public File getPath() {
        return mPath;
    }

    public void setDependency(String pDependency) {
        mDependency = pDependency;
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
                toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Composite composite = (Composite) o;

        return !(mName != null ? !mName.equals(composite.mName) : composite.mName != null);
    }

    @Override
    public int hashCode() {
        return mName != null ? mName.hashCode() : 0;
    }
}
