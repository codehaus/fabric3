package org.fabric3.assembly.configuration;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.fabric3.assembly.dependency.UpdatePolicy;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Michal Capo
 */
public class Runtime {

    public static final String RUNTIME_DEFAULT_NAME = "vm";

    private String serverName;

    private String runtimeName;

    private RuntimeMode runtimeMode;

    private File systemConfig;

    private List<Profile> profiles = new ArrayList<Profile>();

    private UpdatePolicy updatePolicy;

    public Runtime(String pServerName, String pRuntimeName, RuntimeMode pRuntimeMode, UpdatePolicy pUpdatePolicy, File pSystemConfig, Profile... pProfiles) {
        if (null == pServerName) {
            serverName = Server.SERVER_DEFAULT_NAME;
        } else {
            serverName = pServerName;
        }
        if (null == pRuntimeName) {
            runtimeName = Runtime.RUNTIME_DEFAULT_NAME;
        } else {
            runtimeName = pRuntimeName;
        }
        runtimeMode = pRuntimeMode;
        systemConfig = pSystemConfig;
        updatePolicy = pUpdatePolicy;
        if (null != pProfiles) {
            profiles = Arrays.asList(pProfiles);
        }
    }

    public void setSystemConfig(File pSystemConfig) {
        systemConfig = pSystemConfig;
    }

    public String getServerName() {
        return serverName;
    }

    public String getRuntimeName() {
        return runtimeName;
    }

    public RuntimeMode getRuntimeMode() {
        return runtimeMode;
    }

    public File getSystemConfig() {
        return systemConfig;
    }

    public List<Profile> getProfiles() {
        return profiles;
    }

    public UpdatePolicy getUpdatePolicy() {
        return updatePolicy;
    }

    public void validate() {
        RuntimeValidator.validate(this);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
                append("serverName", serverName).
                append("runtimeName", runtimeName).
                append("runtimeMode", runtimeMode).
                append("systemConfig", systemConfig).
                append("profiles", profiles).
                append("updatePolicy", updatePolicy).
                toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Runtime runtime = (Runtime) o;

        if (runtimeName != null ? !runtimeName.equals(runtime.runtimeName) : runtime.runtimeName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return runtimeName != null ? runtimeName.hashCode() : 0;
    }
}
