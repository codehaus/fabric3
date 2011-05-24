package org.fabric3.assembly.configuration;

import org.fabric3.assembly.dependency.UpdatePolicy;
import org.fabric3.assembly.dependency.Version;
import org.fabric3.assembly.dependency.profile.Profile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Michal Capo
 */
public class RuntimeConfiguration {

    public static final String RUNTIME_DEFAULT_NAME = "vm";

    private String serverName;

    private String runtimeName;

    private RuntimeMode runtimeMode;

    private File systemConfig;

    private List<Profile> profiles = new ArrayList<Profile>();

    private Version version;

    private UpdatePolicy updatePolicy;

    public RuntimeConfiguration(String pServerName, String pRuntimeName, RuntimeMode pRuntimeMode, File pSystemConfig, Profile... pProfiles) {
        if (null == pServerName) {
            serverName = ServerConfiguration.SERVER_DEFAULT_NAME;
        } else {
            serverName = pServerName;
        }
        if (null == pRuntimeName) {
            runtimeName = RuntimeConfiguration.RUNTIME_DEFAULT_NAME;
        } else {
            runtimeName = pRuntimeName;
        }
        runtimeMode = pRuntimeMode;
        systemConfig = pSystemConfig;
        if (null != pProfiles) {
            profiles = Arrays.asList(pProfiles);
        }
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String pServerName) {
        serverName = pServerName;
    }

    public String getRuntimeName() {
        return runtimeName;
    }

    public void setRuntimeName(String pRuntimeName) {
        runtimeName = pRuntimeName;
    }

    public RuntimeMode getRuntimeMode() {
        return runtimeMode;
    }

    public void setRuntimeMode(String pRuntimeMode) {
        runtimeMode = RuntimeMode.valueOf(pRuntimeMode);
    }

    public File getSystemConfig() {
        return systemConfig;
    }

    public void setSystemConfig(String pSystemConfig) {
        systemConfig = new File(pSystemConfig);
    }

    public List<Profile> getProfiles() {
        return profiles;
    }

    public void setProfiles(Profile... pProfiles) {
        profiles.addAll(Arrays.asList(pProfiles));
    }

    public void setVersion(Version pVersion) {
        version = pVersion;
    }

    public Version getVersion() {
        return version;
    }

    public UpdatePolicy getUpdatePolicy() {
        return updatePolicy;
    }

    public void setUpdatePolicy(UpdatePolicy pUpdatePolicy) {
        updatePolicy = pUpdatePolicy;
    }
}
