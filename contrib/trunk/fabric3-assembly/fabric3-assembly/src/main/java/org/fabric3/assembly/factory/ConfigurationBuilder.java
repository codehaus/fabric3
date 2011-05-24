package org.fabric3.assembly.factory;

import org.fabric3.assembly.configuration.AssemblyConfig;
import org.fabric3.assembly.configuration.Profile;
import org.fabric3.assembly.configuration.Runtime;
import org.fabric3.assembly.configuration.RuntimeMode;
import org.fabric3.assembly.configuration.Server;
import org.fabric3.assembly.dependency.UpdatePolicy;
import org.fabric3.assembly.dependency.Version;
import org.fabric3.assembly.exception.AssemblyException;
import org.fabric3.assembly.exception.NameNotGivenException;
import org.fabric3.assembly.exception.ServerAlreadyExistsException;
import org.fabric3.assembly.exception.ValidationException;
import org.fabric3.assembly.utils.Closure;
import org.fabric3.assembly.utils.ClosureUtils;
import org.fabric3.assembly.utils.StringUtils;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michal Capo
 */
public class ConfigurationBuilder {

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
        if (StringUtils.isBlank(pName)) {
            throw new NameNotGivenException("You didn't specified any server name.");
        }

        // check for same server name
        if (!mConfig.getConfigurationHelper().getServersByName(pName).isEmpty()) {
            throw new ServerAlreadyExistsException(MessageFormat.format("Server with name ''{0}'' already exists.", pName));
        }

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
        if (StringUtils.isBlank(pRuntimeName)) {
            throw new NameNotGivenException("Runtime name doesn't exists. Please provide one.");
        }

        List<Runtime> runtimesByServerName = mConfig.getConfigurationHelper().getRuntimesByServerName(pServerName);
        if (RuntimeMode.VM == pMode && !runtimesByServerName.isEmpty()) {
            throw new AssemblyException("You are trying to add VM runtime to server which already has some other runtimes. This won't work.");
        }

        for (Runtime runtime : runtimesByServerName) {
            if (pRuntimeName.equals(runtime.getRuntimeName())) {
                throw new AssemblyException(MessageFormat.format("Server ''{0}'' already contains ''{1}'' runtime.", pServerName, pRuntimeName));
            }

            if (RuntimeMode.VM == runtime.getRuntimeMode() && RuntimeMode.VM == pMode) {
                throw new AssemblyException("Server already contains VM runtime. You cannot add next VM runtime to this server.");
            }

            if (RuntimeMode.CONTROLLER == runtime.getRuntimeMode() && RuntimeMode.CONTROLLER == pMode) {
                throw new AssemblyException(MessageFormat.format("Server ''{0}'' already contains CONTROLLER runtime. You cannot add next controller runtime to this server.", pServerName));
            }
        }

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
