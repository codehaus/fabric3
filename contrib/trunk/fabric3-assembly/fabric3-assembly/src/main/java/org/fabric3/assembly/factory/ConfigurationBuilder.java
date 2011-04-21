package org.fabric3.assembly.factory;

import org.apache.commons.lang.StringUtils;
import org.fabric3.assembly.configuration.AssemblyConfiguration;
import org.fabric3.assembly.configuration.RuntimeConfiguration;
import org.fabric3.assembly.configuration.ServerConfiguration;
import org.fabric3.assembly.exception.AssemblyException;
import org.fabric3.assembly.exception.NameNotGivenException;
import org.fabric3.assembly.exception.ServerAlreadyExistsException;
import org.fabric3.assembly.profile.Profile;
import org.fabric3.assembly.profile.UpdatePolicy;
import org.fabric3.host.RuntimeMode;

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
        //TODO "every server have to had at least one runtime"
        return configuration;
    }

    /*
    *
    *
    * Server
    *
    *
    */

    public ConfigurationBuilder addServer(String pPath, String pName) {
        return addServer(pPath, pName, (String[]) null);
    }

    public ConfigurationBuilder addServer(String pPath, String pName, Profile... pProfiles) {
        return addServer(pName, pPath, convertProfiles(pProfiles));
    }

    public ConfigurationBuilder addServer(String pPath, String pName, String... pProfiles) {
        if (StringUtils.isBlank(pName)) {
            throw new NameNotGivenException("You didn't specified a name of server.");
        }

        for (ServerConfiguration serverConfiguration : configuration.getServers()) {
            if (pName.equals(serverConfiguration.getServerName())) {
                throw new ServerAlreadyExistsException(MessageFormat.format("Server with name ''{0}'' already exists.", pName));
            }
        }

        configuration.addServer(new ServerConfiguration(pName, new File(pPath), pProfiles));
        return this;
    }

    public ConfigurationBuilder addServer(String pPath) {
        return addServer(pPath, (String[]) null);
    }

    public ConfigurationBuilder addServer(String pPath, Profile... pProfiles) {
        return addServer(pPath, convertProfiles(pProfiles));
    }

    public ConfigurationBuilder addServer(String pPath, String... pProfiles) {
        for (ServerConfiguration serverConfiguration : configuration.getServers()) {
            if (ServerConfiguration.SERVER_DEFAULT_NAME.equals(serverConfiguration.getServerName())) {
                throw new ServerAlreadyExistsException("You cannot have two 'default' servers. Please specify names to these servers.");
            }
        }

        configuration.addServer(new ServerConfiguration(new File(pPath), pProfiles));
        return this;
    }

    /*
    *
    *
    * Runtime
    *
    *
    */

    public ConfigurationBuilder addRuntime(String pRuntimeName, RuntimeMode pMode, String pConfigFile, Profile... pProfiles) {
        return addRuntime(pRuntimeName, pMode, pConfigFile, convertProfiles(pProfiles));
    }

    public ConfigurationBuilder addRuntime(String pRuntimeName, RuntimeMode pMode, String pConfigFile, String... pProfiles) {
        return addRuntime(ServerConfiguration.SERVER_DEFAULT_NAME, pRuntimeName, pMode, pConfigFile, pProfiles);
    }

    public ConfigurationBuilder addRuntime(String pServerName, String pRuntimeName, RuntimeMode pMode, String pConfigFile, Profile... pProfiles) {
        return addRuntime(pServerName, pRuntimeName, pMode, pConfigFile, convertProfiles(pProfiles));
    }

    public ConfigurationBuilder addRuntime(String pServerName, String pRuntimeName, RuntimeMode pMode, String pConfigFile, String... pProfiles) {
        if (StringUtils.isBlank(pRuntimeName)) {
            throw new NameNotGivenException("Runtime name doesn't exists. Please provide one.");
        }

        for (RuntimeConfiguration runtime : configuration.getRuntimes()) {
            if (pRuntimeName.equals(runtime.getRuntimeName()) && pServerName.equals(runtime.getServerName())) {
                throw new AssemblyException(MessageFormat.format("Server ''{0}'' already contains ''{1}'' runtime.", pServerName, pRuntimeName));
            }
        }

        //TODO "only on controller and one vm is allowed per server"

        configuration.addRuntime(new RuntimeConfiguration(pServerName, pRuntimeName, pMode, new File(pConfigFile), pProfiles));
        return this;
    }

    public ConfigurationBuilder addRuntime(String... pProfiles) {
        return addRuntime(RuntimeMode.VM, null, pProfiles);
    }

    public ConfigurationBuilder addRuntime(Profile... pProfiles) {
        return addRuntime(RuntimeMode.VM, null, pProfiles);
    }

    public ConfigurationBuilder addRuntime(RuntimeMode pMode, String... pProfiles) {
        return addRuntime(pMode, null, pProfiles);
    }

    public ConfigurationBuilder addRuntime(RuntimeMode pMode, Profile... pProfiles) {
        return addRuntime(pMode, null, pProfiles);
    }

    public ConfigurationBuilder addRuntime(RuntimeMode pMode, String pConfigFile, Profile... pProfiles) {
        return addRuntime(pMode, pConfigFile, convertProfiles(pProfiles));
    }

    public ConfigurationBuilder addRuntime(RuntimeMode pMode, String pConfigFile, String... pProfiles) {
        //TODO "only on controller and one vm is allowed per server"

        configuration.addRuntime(new RuntimeConfiguration(pMode, pConfigFile == null ? null : new File(pConfigFile), pProfiles));
        return this;
    }

    private String[] convertProfiles(Profile... pProfiles) {
        List<String> temp = new ArrayList<String>();
        for (Profile profile : pProfiles) {
            temp.add(profile.getName());
        }

        return temp.toArray(new String[temp.size()]);
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

}
