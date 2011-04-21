package org.fabric3.assembly.configuration;

import org.fabric3.host.RuntimeMode;

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

    private List<String> profiles = new ArrayList<String>();

    public RuntimeConfiguration(RuntimeMode pRuntimeMode, File pSystemConfig, String... pProfiles) {
        this(ServerConfiguration.SERVER_DEFAULT_NAME, RUNTIME_DEFAULT_NAME, pRuntimeMode, pSystemConfig, pProfiles);
    }

    public RuntimeConfiguration(String pServerName, String pRuntimeName, RuntimeMode pRuntimeMode, File pSystemConfig, String... pProfiles) {
        if (null == pServerName) {
            serverName = ServerConfiguration.SERVER_DEFAULT_NAME;
        } else {
            serverName = pServerName;
        }
        runtimeName = pRuntimeName;
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

    public List<String> getProfiles() {
        return profiles;
    }

    public void setProfiles(String... pProfiles) {
        profiles.addAll(Arrays.asList(pProfiles));
    }
}
