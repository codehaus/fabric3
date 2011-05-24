package org.fabric3.assembly.configuration;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.fabric3.assembly.dependency.UpdatePolicy;

import java.io.File;

/**
 * @author Michal Capo
 */
public class Composite {

    private String name;

    private String runtimeName;

    private File path;

    private String dependency;

    private UpdatePolicy updatePolicy;

    public Composite(String pName, String pRuntimeName, String pDependency, UpdatePolicy pUpdatePolicy) {
        name = pName;
        if (null == pRuntimeName) {
            runtimeName = Runtime.RUNTIME_DEFAULT_NAME;
        } else {
            runtimeName = pRuntimeName;
        }
        dependency = pDependency;
        updatePolicy = pUpdatePolicy;
    }

    public Composite(String pName, String pRuntimeName, File pPath, UpdatePolicy pUpdatePolicy) {
        name = pName;
        if (null == pRuntimeName) {
            runtimeName = Runtime.RUNTIME_DEFAULT_NAME;
        } else {
            runtimeName = pRuntimeName;
        }
        path = pPath;
        updatePolicy = pUpdatePolicy;
    }

    public String getName() {
        return name;
    }

    public String getRuntimeName() {
        return runtimeName;
    }

    public File getPath() {
        return path;
    }

    public UpdatePolicy getUpdatePolicy() {
        return updatePolicy;
    }

    public String getDependency() {
        return dependency;
    }

    public void validate() {
        CompositeValidator.validate(this);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
                append("name", name).
                append("runtimeName", runtimeName).
                append("path", path).
                append("dependency", dependency).
                append("updatePolicy", updatePolicy).
                toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Composite composite = (Composite) o;

        if (name != null ? !name.equals(composite.name) : composite.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
