package org.fabric3.assembly.factory;

import org.fabric3.assembly.configuration.AssemblyConfig;
import org.fabric3.assembly.configuration.Composite;
import org.fabric3.assembly.configuration.Profile;
import org.fabric3.assembly.configuration.Runtime;
import org.fabric3.assembly.configuration.RuntimeMode;
import org.fabric3.assembly.configuration.Server;
import org.fabric3.assembly.dependency.Dependency;
import org.fabric3.assembly.dependency.UpdatePolicy;
import org.fabric3.assembly.dependency.Version;
import org.fabric3.assembly.exception.ValidationException;
import org.fabric3.assembly.utils.Closure;
import org.fabric3.assembly.utils.ClosureUtils;
import org.fabric3.assembly.utils.ConfigUtils;
import org.jboss.shrinkwrap.api.Archive;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michal Capo
 */
public class AssemblyConfigBuilder {

    protected AssemblyConfig mConfig;

    protected AssemblyConfigBuilder(AssemblyConfig pConfig) {
        mConfig = pConfig;
    }

    public static AssemblyConfigBuilder getBuilder() {
        return new AssemblyConfigBuilder(new AssemblyConfig());
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

    public ServerBuilder addServer() {
        return addServer(Server.SERVER_DEFAULT_NAME, null);
    }

    public ServerBuilder addServer(String pPath) {
        return addServer(Server.SERVER_DEFAULT_NAME, pPath);
    }

    public ServerBuilder addServer(String pName, String pPath) {
        return addServer(pName, pPath, null);
    }

    public ServerBuilder addServer(String pName, String pPath, Version pVersion) {
        return addServer(pName, pPath, pVersion, null);
    }

    public ServerBuilder addServer(String pName, String pPath, Version pVersion, UpdatePolicy pUpdatePolicy) {
        return new ServerBuilder(mConfig, new Server(pName, new File(pPath), pVersion, pUpdatePolicy));
    }

    public static class ServerBuilder extends AssemblyConfigBuilder {

        private Server mServer;

        public ServerBuilder(AssemblyConfig pConfig, Server pServer) {
            super(pConfig);
            mServer = pServer;

            mConfig.addServer(pServer);
        }

        public ServerBuilder withName(String pName) {
            mServer.setServerName(pName);
            return this;
        }

        public ServerBuilder atPath(String pPath) {
            mServer.setServerPath(new File(pPath));
            return this;
        }

        public ServerBuilder atPath(File pPath) {
            mServer.setServerPath(pPath);
            return this;
        }

        public ServerBuilder withVersion(Version pVersion) {
            mServer.setVersion(pVersion);
            return this;
        }

        public ServerBuilder withVersion(String pVersion) {
            mServer.setVersion(new Version(pVersion));
            return this;
        }

        public ServerBuilder withUpdatePolicy(UpdatePolicy pUpdatePolicy) {
            mServer.setUpdatePolicy(pUpdatePolicy);
            return this;
        }

        public ServerBuilder withUpdatePolicy(String pUpdatePolicy) {
            mServer.setUpdatePolicy(UpdatePolicy.valueOf(pUpdatePolicy.toUpperCase()));
            return this;
        }

        public ServerBuilder withProfiles(String... pProfiles) {
            mServer.addProfileNames(pProfiles);
            return this;
        }

        public ServerBuilder withProfiles(Profile... pProfiles) {
            mServer.addProfiles(pProfiles);
            return this;
        }

        public ServerBuilder deployComposite(String pComposite) {
            mServer.addComposite(pComposite);
            return this;
        }

    }

    /*
    *
    *
    * Runtime
    *
    *
    */

    public RuntimeBuilder addRuntime() {
        return addRuntime(RuntimeMode.VM, null);
    }

    public RuntimeBuilder addRuntime(String pRuntimeName) {
        return addRuntime(Server.SERVER_DEFAULT_NAME, pRuntimeName, RuntimeMode.VM, null, null);
    }

    public RuntimeBuilder addRuntime(RuntimeMode pMode) {
        return addRuntime(pMode, null);
    }

    public RuntimeBuilder addRuntime(RuntimeMode pMode, String pConfigFile) {
        return addRuntime(Runtime.RUNTIME_DEFAULT_NAME, pMode, pConfigFile);
    }

    public RuntimeBuilder addRuntime(String pRuntimeName, RuntimeMode pMode) {
        return addRuntime(Server.SERVER_DEFAULT_NAME, pRuntimeName, pMode, null, null);
    }

    public RuntimeBuilder addRuntime(String pRuntimeName, RuntimeMode pMode, String pConfigFile) {
        return addRuntime(Server.SERVER_DEFAULT_NAME, pRuntimeName, pMode, null, pConfigFile);
    }

    public RuntimeBuilder addRuntime(String pServerName, String pRuntimeName) {
        return addRuntime(pServerName, pRuntimeName, RuntimeMode.VM);
    }

    public RuntimeBuilder addRuntime(String pServerName, String pRuntimeName, RuntimeMode pMode) {
        return addRuntime(pServerName, pRuntimeName, pMode, null, null);
    }

    public RuntimeBuilder addRuntime(String pServerName, String pRuntimeName, RuntimeMode pMode, UpdatePolicy pUpdatePolicy, String pConfigFile) {
        return new RuntimeBuilder(mConfig, new Runtime(pServerName, pRuntimeName, pMode, pUpdatePolicy, null == pConfigFile ? null : new File(pConfigFile)));
    }

    public static class RuntimeBuilder extends AssemblyConfigBuilder {

        private Runtime mRuntime;

        public RuntimeBuilder(AssemblyConfig pConfig, Runtime pRuntime) {
            super(pConfig);
            mRuntime = pRuntime;

            mConfig.addRuntime(pRuntime);
        }

        public RuntimeBuilder toServer(String pServerName) {
            mRuntime.setServerName(pServerName);
            return this;
        }

        public RuntimeBuilder withProfiles(String... pProfiles) {
            mRuntime.addProfileNames(pProfiles);
            return this;
        }

        public RuntimeBuilder withProfiles(Profile... pProfiles) {
            mRuntime.addProfiles(pProfiles);
            return this;
        }

        public RuntimeBuilder asType(RuntimeMode pRuntimeMode) {
            mRuntime.setRuntimeMode(pRuntimeMode);
            return this;
        }

        public RuntimeBuilder asType(String pRuntimeMode) {
            mRuntime.setRuntimeMode(RuntimeMode.valueOf(pRuntimeMode.toUpperCase()));
            return this;
        }

        public RuntimeBuilder configFile(File pConfigFile) {
            mRuntime.setSystemConfig(pConfigFile);
            return this;
        }

        public RuntimeBuilder configFile(String pConfigFile) {
            mRuntime.setSystemConfig(new File(pConfigFile));
            return this;
        }
    }

    /*
    *
    *
    * Update policy
    *
    *
    */

    public AssemblyConfigBuilder setUpdatePolicy(UpdatePolicy pPolicy) {
        mConfig.setUpdatePolicy(pPolicy.name());
        return this;
    }

    public AssemblyConfigBuilder setUpdatePolicy(String pPolicy) {
        mConfig.setUpdatePolicy(pPolicy);
        return this;
    }

    /*
     *
     *
     * Version
     *
     *
     */

    public AssemblyConfigBuilder setVersion(String pVersion) {
        mConfig.setVersion(new Version(pVersion));
        return this;
    }

    public AssemblyConfigBuilder setVersion(Version pVersion) {
        mConfig.setVersion(pVersion);
        return this;
    }

    /*
     *
     *
     * Profile
     *
     *
     */

    public AssemblyConfigBuilder setProfilesUpdatePolicy(UpdatePolicy pPolicy) {
        mConfig.getProfileConfig().setUpdatePolicy(pPolicy);
        return this;
    }

    public AssemblyConfigBuilder setProfilesUpdatePolicy(String pPolicy) {
        mConfig.getProfileConfig().setUpdatePolicy(UpdatePolicy.valueOf(pPolicy));
        return this;
    }

    public AssemblyConfigBuilder setProfilesVersion(String pVersion) {
        mConfig.getProfileConfig().setVersion(new Version(pVersion));
        return this;
    }

    public AssemblyConfigBuilder setProfilesVersion(Version pVersion) {
        mConfig.getProfileConfig().setVersion(pVersion);
        return this;
    }


    public ProfileBuilder addProfile(String pName) {
        return new ProfileBuilder(mConfig, new Profile(pName, null, null));
    }

    public ProfileBuilder addProfile(String pName, String... pAlternativeNames) {
        return new ProfileBuilder(mConfig, new Profile(pName, null, null, pAlternativeNames));
    }

    public ProfileBuilder addProfile(String pName, UpdatePolicy pUpdatePolicy, String... pAlternativeNames) {
        return new ProfileBuilder(mConfig, new Profile(pName, pUpdatePolicy, null, pAlternativeNames));
    }

    public ProfileBuilder addProfile(String pName, Version pVersion, String... pAlternativeNames) {
        return new ProfileBuilder(mConfig, new Profile(pName, null, pVersion, pAlternativeNames));
    }

    public ProfileBuilder addProfile(String pName, UpdatePolicy pUpdatePolicy, Version pVersion, String... pAlternativeNames) {
        return new ProfileBuilder(mConfig, new Profile(pName, pUpdatePolicy, pVersion, pAlternativeNames));
    }

    public static class ProfileBuilder extends AssemblyConfigBuilder {

        private Profile mProfile;

        public ProfileBuilder(AssemblyConfig pConfig, Profile pProfile) {
            super(pConfig);
            mProfile = pProfile;

            mConfig.addProfile(pProfile);
        }

        public ProfileBuilder alternativeNames(String... pAlternativeNames) {
            mProfile.addAlternativeNames(pAlternativeNames);
            return this;
        }

        public ProfileBuilder dependency(String pDependency) {
            mProfile.addDependency(pDependency);
            return this;
        }

        public ProfileBuilder dependency(Dependency pDependency) {
            mProfile.addDependency(pDependency);
            return this;
        }

        public ProfileBuilder dependencies(String... pDependencies) {
            mProfile.addDependencies(pDependencies);
            return this;
        }

        public ProfileBuilder file(String pDependencyPath) {
            mProfile.addFile(pDependencyPath);
            return this;
        }

        public ProfileBuilder file(File pDependencyPath) {
            mProfile.addFile(pDependencyPath);
            return this;
        }

        public ProfileBuilder files(File... pFiles) {
            mProfile.addFiles(pFiles);
            return this;
        }

        public ProfileBuilder withVersion(Version pVersion) {
            mProfile.setVersion(pVersion);
            return this;
        }

        public ProfileBuilder withVersion(String pVersion) {
            mProfile.setVersion(new Version(pVersion));
            return this;
        }

        public ProfileBuilder withUpdatePolicy(UpdatePolicy pUpdatePolicy) {
            mProfile.setUpdatePolicy(pUpdatePolicy);
            return this;
        }

        public ProfileBuilder withUpdatePolicy(String pUpdatePolicy) {
            mProfile.setUpdatePolicy(UpdatePolicy.valueOf(pUpdatePolicy.toUpperCase()));
            return this;
        }

    }

    /*
     *
     *
     * Composites
     *
     *
     */

    public CompositeBuilder addComposite(String pName, String pDependency) {
        return new CompositeBuilder(mConfig, new Composite(pName, pDependency));
    }

    public CompositeBuilder addComposite(String pName, File pFile) {
        return new CompositeBuilder(mConfig, new Composite(pName, pFile));
    }

    public ArchiveBuilder addArchive(Archive pArchive) {
        return new ArchiveBuilder(mConfig, pArchive);
    }

    public ArchiveBuilder addArchive(String pName, Archive pArchive) {
        return new ArchiveBuilder(mConfig, pName, pArchive);
    }

    public static class ArchiveBuilder extends AssemblyConfigBuilder {

        protected Archive mArchive;

        public ArchiveBuilder(AssemblyConfig pConfig, Archive pArchive) {
            super(pConfig);
            mArchive = pArchive;

            mConfig.addArchive(pArchive);
        }

        public ArchiveBuilder(AssemblyConfig pConfig, String pName) {
            super(pConfig);
            mArchive = ConfigUtils.findArchiveByName(pConfig, pName);
        }

        public ArchiveBuilder(AssemblyConfig pConfig, String pName, Archive pArchive) {
            super(pConfig);
            mArchive = pArchive;

            mConfig.addArchive(pName, pArchive);
        }

        public ArchiveBuilder addToServer(String pServerName) {
            Server server = ConfigUtils.findServerByName(mConfig, pServerName);
            server.addArchive(mArchive.getName());

            return this;
        }

        public ArchiveBuilder removeFromServer() {
            mConfig.getArchives().remove(mArchive);

            return this;
        }

    }

    public static class CompositeBuilder extends AssemblyConfigBuilder {

        protected Composite mComposite;

        public CompositeBuilder(AssemblyConfig pConfig, String pName) {
            super(pConfig);
            mComposite = ConfigUtils.findCompositeByName(pConfig, pName);
        }

        public CompositeBuilder(AssemblyConfig pConfig, Composite pComposite) {
            super(pConfig);
            mComposite = pComposite;

            mConfig.addComposite(pComposite);
        }

        public CompositeBuilder addToServer(String pServerName) {
            Server server = ConfigUtils.findServerByName(mConfig, pServerName);
            server.addComposite(mComposite.getName());

            return this;
        }

        public CompositeBuilder removeFromServer() {
            Server server = ConfigUtils.findServerByComposite(mConfig, mComposite);
            server.getComposites().remove(mComposite);

            return this;
        }

    }

}
