package org.fabric3.assembly.configuration;

import org.fabric3.assembly.assembly.Assembly;
import org.fabric3.assembly.profile.UpdatePolicy;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michal Capo
 */
public class AssemblyConfiguration {

    private UpdatePolicy updatePolicy = UpdatePolicy.DAILY;

    private List<ServerConfiguration> servers = new ArrayList<ServerConfiguration>();

    private List<RuntimeConfiguration> runtimes = new ArrayList<RuntimeConfiguration>();

    private List<CompositeConfiguration> composites = new ArrayList<CompositeConfiguration>();

    private ConfigurationServices mConfigurationServices = new ConfigurationServices() {
        @Override
        public List<ServerConfiguration> getServerConfigurations() {
            return servers;
        }

        @Override
        public List<RuntimeConfiguration> getRuntimeConfigurations() {
            return runtimes;
        }
    };

    public void setUpdatePolicy(String pUpdatePolicy) {
        updatePolicy = UpdatePolicy.valueOf(pUpdatePolicy);
    }

    public UpdatePolicy getUpdatePolicy() {
        return updatePolicy;
    }

    public void addServer(ServerConfiguration server) {
        servers.add(server);
    }

    public List<ServerConfiguration> getServers() {
        return servers;
    }

    public void addRuntime(RuntimeConfiguration runtime) {
        runtimes.add(runtime);
    }

    public List<RuntimeConfiguration> getRuntimes() {
        return runtimes;
    }

    public void addComposite(CompositeConfiguration composite) {
        composites.add(composite);
    }

    public List<CompositeConfiguration> getComposites() {
        return composites;
    }

    public ConfigurationServices getConfigurationServices() {
        return mConfigurationServices;
    }

    public void doAssembly() {
        new Assembly().doAssembly(this);
    }
}
