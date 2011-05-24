package org.fabric3.assembly.factory;

import org.fabric3.assembly.configuration.AssemblyConfig;
import org.fabric3.assembly.configuration.Profile;
import org.fabric3.assembly.configuration.Runtime;
import org.fabric3.assembly.configuration.RuntimeMode;
import org.fabric3.assembly.configuration.Server;
import org.fabric3.assembly.dependency.UpdatePolicy;
import org.fabric3.assembly.dependency.Version;
import org.fabric3.assembly.exception.ValidationException;
import org.fabric3.assembly.utils.Closure;
import org.fabric3.assembly.utils.ClosureUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michal Capo
 */
public class ConfigurationBuilder {

    //TODO <capo> change builder to fluent builder

    private AssemblyConfig mConfig = new AssemblyConfig();

    public static ConfigurationBuilder getBuilder() {
        return new ConfigurationBuilder();
    }

    public AssemblyConfig createConfiguration() {
        final List<String> serverNames = new ArrayList<String>();

        // collect all server names
        ClosureUtils.each(mConfig.getServers(), new Closure<Server>() {
            @Override
            public void exec(Server pParam) {
                serverNames.add(pParam.getServerName());
            }
        });

        // remove all bound server names
        ClosureUtils.each(mConfig.getRuntimes(), new Closure<Runtime>() {
            @Override
            public void exec(Runtime pParam) {
                serverNames.remove(pParam.getServerName());
            }
        });

        if (!serverNames.isEmpty()) {
            throw new ValidationException("At least one runtime is needed per server. These servers doesn't have any:" + serverNames);
        }

        return mConfig;
    }

    /*
    *
    *
    * Server
    *
    *
    */

    public ConfigurationBuilder addServer(String pPath) {
        return addServer(Server.SERVER_DEFAULT_NAME, pPath);
    }

    public ConfigurationBuilder addServer(String pPath, Profile... pProfiles) {
        return addServer(Server.SERVER_DEFAULT_NAME, pPath, pProfiles);
    }

    public ConfigurationBuilder addServer(String pName, String pPath) {
        return addServer(pName, pPath, (Profile[]) null);
    }

    public ConfigurationBuilder addServer(String pName, String pPath, Profile... pProfiles) {
        return addServer(pName, pPath, null, null, pProfiles);
    }

    public ConfigurationBuilder addServer(String pName, String pPath, Version pVersion, Profile... pProfiles) {
        return addServer(pName, pPath, pVersion, null, pProfiles);
    }

    public ConfigurationBuilder addServer(String pName, String pPath, Version pVersion, UpdatePolicy pUpdatePolicy, Profile... pProfiles) {
        mConfig.addServer(new Server(pName, new File(pPath), pVersion, pUpdatePolicy, pProfiles));
        return this;
    }

    /*
    *
    *
    * Runtime
    *
    *
    */

    public ConfigurationBuilder addRuntime(Profile... pProfiles) {
        return addRuntime(RuntimeMode.VM, null, pProfiles);
    }

    public ConfigurationBuilder addRuntime(RuntimeMode pMode, Profile... pProfiles) {
        return addRuntime(pMode, null, pProfiles);
    }

    public ConfigurationBuilder addRuntime(RuntimeMode pMode, String pConfigFile, Profile... pProfiles) {
        return addRuntime(Runtime.RUNTIME_DEFAULT_NAME, pMode, pConfigFile, pProfiles);
    }

    public ConfigurationBuilder addRuntime(String pRuntimeName) {
        return addRuntime(pRuntimeName, (Profile[]) null);
    }

    public ConfigurationBuilder addRuntime(String pRuntimeName, RuntimeMode pMode, Profile... pProfiles) {
        return addRuntime(Server.SERVER_DEFAULT_NAME, pRuntimeName, pMode, null, null, pProfiles);
    }

    public ConfigurationBuilder addRuntime(String pRuntimeName, RuntimeMode pMode, String pConfigFile, Profile... pProfiles) {
        return addRuntime(Server.SERVER_DEFAULT_NAME, pRuntimeName, pMode, null, pConfigFile, pProfiles);
    }

    public ConfigurationBuilder addRuntime(String pServerName, String pRuntimeName) {
        return addRuntime(pServerName, pRuntimeName, RuntimeMode.VM, (Profile[]) null);
    }

    public ConfigurationBuilder addRuntime(String pServerName, Profile... pProfiles) {
        return addRuntime(pServerName, Runtime.RUNTIME_DEFAULT_NAME, RuntimeMode.VM, null, null, pProfiles);
    }

    public ConfigurationBuilder addRuntime(String pServerName, String pRuntimeName, RuntimeMode pMode, Profile... pProfiles) {
        return addRuntime(pServerName, pRuntimeName, pMode, null, null, pProfiles);
    }

    public ConfigurationBuilder addRuntime(String pServerName, String pRuntimeName, RuntimeMode pMode, UpdatePolicy pUpdatePolicy, String pConfigFile, Profile... pProfiles) {

        mConfig.addRuntime(new Runtime(pServerName, pRuntimeName, pMode, pUpdatePolicy, null == pConfigFile ? null : new File(pConfigFile), pProfiles));
        return this;
    }

    /*
    *
    *
    * Update policy
    *
    *
    */

    public ConfigurationBuilder setUpdatePolicy(UpdatePolicy pPolicy) {
        mConfig.setUpdatePolicy(pPolicy.name());
        return this;
    }

    public ConfigurationBuilder setUpdatePolicy(String pPolicy) {
        mConfig.setUpdatePolicy(UpdatePolicy.valueOf(pPolicy).name());
        return this;
    }

    /*
     *
     *
     * Version
     *
     *
     */

    public ConfigurationBuilder setVersion(String pVersion) {
        mConfig.setVersion(new Version(pVersion));
        return this;
    }
}
