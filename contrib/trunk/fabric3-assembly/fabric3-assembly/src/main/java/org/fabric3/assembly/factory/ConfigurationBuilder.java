package org.fabric3.assembly.factory;

import org.fabric3.assembly.configuration.AssemblyConfiguration;
import org.fabric3.assembly.configuration.RuntimeConfiguration;
import org.fabric3.assembly.configuration.RuntimeMode;
import org.fabric3.assembly.configuration.ServerConfiguration;
import org.fabric3.assembly.dependency.UpdatePolicy;
import org.fabric3.assembly.dependency.Version;
import org.fabric3.assembly.dependency.profile.Profile;
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

    private AssemblyConfiguration configuration = new AssemblyConfiguration();

    public static ConfigurationBuilder getBuilder() {
        return new ConfigurationBuilder();
    }

    public AssemblyConfiguration createConfiguration() {
        final List<String> serverNames = new ArrayList<String>();

        // collect all server names
        ClosureUtils.each(configuration.getServers(), new Closure<ServerConfiguration>() {
            @Override
            public void exec(ServerConfiguration pParam) {
                serverNames.add(pParam.getServerName());
            }
        });

        // remove all bound server names
        ClosureUtils.each(configuration.getRuntimes(), new Closure<RuntimeConfiguration>() {
            @Override
            public void exec(RuntimeConfiguration pParam) {
                serverNames.remove(pParam.getServerName());
            }
        });

        if (!serverNames.isEmpty()) {
            throw new ValidationException("At least one runtime is needed per server. These servers doesn't have any:" + serverNames);
        }

        return configuration;
    }

    /*
    *
    *
    * Server
    *
    *
    */

    public ConfigurationBuilder addServer(String pPath) {
        return addServer(ServerConfiguration.SERVER_DEFAULT_NAME, pPath);
    }

    public ConfigurationBuilder addServer(String pPath, Profile... pProfiles) {
        return addServer(ServerConfiguration.SERVER_DEFAULT_NAME, pPath, pProfiles);
    }

    public ConfigurationBuilder addServer(String pName, String pPath) {
        return addServer(pName, pPath, (Profile[]) null);
    }

    public ConfigurationBuilder addServer(String pName, String pPath, Version pVersion) {
        return addServer(pName, pPath, null, (Profile[]) null);
    }

    public ConfigurationBuilder addServer(String pName, String pPath, Profile... pProfiles) {
        return addServer(pName, pPath, null, pProfiles);
    }

    public ConfigurationBuilder addServer(String pName, String pPath, Version pVersion, Profile... pProfiles) {
        if (StringUtils.isBlank(pName)) {
            throw new NameNotGivenException("You didn't specified any server name.");
        }

        // check for same server name
        if (!configuration.getConfigurationHelper().getServersByName(pName).isEmpty()) {
            throw new ServerAlreadyExistsException(MessageFormat.format("Server with name ''{0}'' already exists.", pName));
        }

        configuration.addServer(new ServerConfiguration(pName, new File(pPath), pVersion, pProfiles));
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
        return addRuntime(RuntimeConfiguration.RUNTIME_DEFAULT_NAME, pMode, pConfigFile, pProfiles);
    }

    public ConfigurationBuilder addRuntime(String pRuntimeName) {
        return addRuntime(pRuntimeName, (Profile[]) null);
    }

    public ConfigurationBuilder addRuntime(String pRuntimeName, RuntimeMode pMode, Profile... pProfiles) {
        return addRuntime(ServerConfiguration.SERVER_DEFAULT_NAME, pRuntimeName, pMode, null, pProfiles);
    }

    public ConfigurationBuilder addRuntime(String pRuntimeName, RuntimeMode pMode, String pConfigFile, Profile... pProfiles) {
        return addRuntime(ServerConfiguration.SERVER_DEFAULT_NAME, pRuntimeName, pMode, pConfigFile, pProfiles);
    }

    public ConfigurationBuilder addRuntime(String pServerName, String pRuntimeName) {
        return addRuntime(pServerName, pRuntimeName, RuntimeMode.VM, (Profile[]) null);
    }

    public ConfigurationBuilder addRuntime(String pServerName, Profile... pProfiles) {
        return addRuntime(pServerName, RuntimeConfiguration.RUNTIME_DEFAULT_NAME, RuntimeMode.VM, null, pProfiles);
    }

    public ConfigurationBuilder addRuntime(String pServerName, String pRuntimeName, RuntimeMode pMode, Profile... pProfiles) {
        return addRuntime(pServerName, pRuntimeName, pMode, null, pProfiles);
    }

    public ConfigurationBuilder addRuntime(String pServerName, String pRuntimeName, RuntimeMode pMode, String pConfigFile, Profile... pProfiles) {
        if (StringUtils.isBlank(pRuntimeName)) {
            throw new NameNotGivenException("Runtime name doesn't exists. Please provide one.");
        }

        List<RuntimeConfiguration> runtimesByServerName = configuration.getConfigurationHelper().getRuntimesByServerName(pServerName);
        if (RuntimeMode.VM == pMode && !runtimesByServerName.isEmpty()) {
            throw new AssemblyException("You are trying to add VM runtime to server which already has some other runtimes. This won't work.");
        }

        for (RuntimeConfiguration runtime : runtimesByServerName) {
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

        configuration.addRuntime(new RuntimeConfiguration(pServerName, pRuntimeName, pMode, null == pConfigFile ? null : new File(pConfigFile), pProfiles));
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
        configuration.setUpdatePolicy(pPolicy.name());
        return this;
    }

    public ConfigurationBuilder setUpdatePolicy(String pPolicy) {
        configuration.setUpdatePolicy(UpdatePolicy.valueOf(pPolicy).name());
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
        configuration.setVersion(new Version(pVersion));
        return this;
    }
}
