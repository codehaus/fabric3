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

    private String name;

    private List<String> alternativeNames = new ArrayList<String>();

    private UpdatePolicy updatePolicy;

    private Version version;

    private List<Dependency> dependencies = new ArrayList<Dependency>();

    private List<File> files = new ArrayList<File>();

    public Profile(String pName, UpdatePolicy pUpdatePolicy, Version pVersion, String... pAlternativeNames) {
        name = pName;
        updatePolicy = pUpdatePolicy;
        version = pVersion;
        if (null == pAlternativeNames) {
            alternativeNames = Arrays.asList(pAlternativeNames);
        }
    }

    public void addDependency(Dependency pDependency) {
        if (null != pDependency) {
            dependencies.add(pDependency);
        }
    }

    public void addDependency(String pDependency) {
        if (!StringUtils.isBlank(pDependency)) {
            dependencies.add(DependencyUtils.parseDependency(pDependency));
        }
    }

    public void addPath(String pPath) {
        File temp;
        if (!StringUtils.isBlank(pPath) && (temp = new File(pPath)).exists()) {
            files.add(temp);
        }
    }

    public void addPath(File pFile) {
        if (null != pFile) {
            files.add(pFile);
        }
    }

    public String getName() {
        return name;
    }

    public List<String> getAlternativeNames() {
        return alternativeNames;
    }

    public UpdatePolicy getUpdatePolicy() {
        return updatePolicy;
    }

    public Version getVersion() {
        return version;
    }

    public List<Dependency> getDependencies() {
        return dependencies;
    }

    public List<File> getFiles() {
        return files;
    }

    public void validate() {
        ProfileValidator.validate(this);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
                append("name", name).
                append("alternativeNames", alternativeNames).
                append("updatePolicy", updatePolicy).
                append("version", version).
                append("dependencies", dependencies).
                append("files", files).
                toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Profile that = (Profile) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (version != null ? !version.equals(that.version) : that.version != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }
}
