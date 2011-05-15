package org.fabric3.assembly.configuration;

import org.fabric3.assembly.assembly.Assembly;
import org.fabric3.assembly.dependency.UpdatePolicy;
import org.fabric3.assembly.dependency.Version;
import org.fabric3.assembly.exception.AssemblyException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michal Capo
 */
public class AssemblyConfiguration {

    private Version version;

    private UpdatePolicy updatePolicy = UpdatePolicy.DAILY;

    private List<ServerConfiguration> servers = new ArrayList<ServerConfiguration>();

    private List<RuntimeConfiguration> runtimes = new ArrayList<RuntimeConfiguration>();

    private List<CompositeConfiguration> composites = new ArrayList<CompositeConfiguration>();

    private ConfigurationHelper mConfigurationHelper = new ConfigurationHelper() {
        @Override
        public List<ServerConfiguration> getServerConfigurations() {
            return servers;
        }

        @Override
        public List<RuntimeConfiguration> getRuntimeConfigurations() {
            return runtimes;
        }

        @Override
        public Version getVersion() {
            return AssemblyConfiguration.this.getVersion();
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

    public ConfigurationHelper getConfigurationHelper() {
        return mConfigurationHelper;
    }

    public Version getVersion() {
        if (null == version) {
            throw new AssemblyException("No version is specified, please do so.");
        }

        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public void doAssembly() {
        new Assembly().doAssembly(this);
    }
}
