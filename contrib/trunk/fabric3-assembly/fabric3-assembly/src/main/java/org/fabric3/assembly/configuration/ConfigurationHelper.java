package org.fabric3.assembly.configuration;

import org.fabric3.assembly.dependency.Version;
import org.fabric3.assembly.exception.ServerNotFoundException;
import org.fabric3.assembly.utils.Closure;
import org.fabric3.assembly.utils.ClosureUtils;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michal Capo
 */
public abstract class ConfigurationHelper {

    public abstract List<ServerConfiguration> getServerConfigurations();

    public abstract List<RuntimeConfiguration> getRuntimeConfigurations();

    public abstract Version getVersion();

    /*
     *
     *
     * Service methods.
     *
     *
     */

    public File findServerPathByRuntime(RuntimeConfiguration pRuntime) {
        String serverLookupName = pRuntime.getServerName();

        for (ServerConfiguration server : getServerConfigurations()) {
            if (serverLookupName.equals(server.getServerName())) {
                return server.getServerPath();
            }
        }

        throw new ServerNotFoundException(MessageFormat.format("You specified that runtime ''{0}'' should be added to ''{1}'' server. But such server doesn''t exists.", pRuntime.getRuntimeName(), serverLookupName));
    }

    public List<RuntimeConfiguration> getRuntimesByServerName(final String pServerName) {
        final List<RuntimeConfiguration> runtimes = new ArrayList<RuntimeConfiguration>();

        ClosureUtils.each(getRuntimeConfigurations(), new Closure<RuntimeConfiguration>() {
            @Override
            public void exec(RuntimeConfiguration pParam) {
                if (pServerName.equals(pParam.getServerName())) {
                    runtimes.add(pParam);
                }
            }
        });

        return runtimes;
    }

    public List<ServerConfiguration> getServersByName(final String pServerName) {
        final List<ServerConfiguration> servers = new ArrayList<ServerConfiguration>();

        ClosureUtils.each(getServerConfigurations(), new Closure<ServerConfiguration>() {
            @Override
            public void exec(ServerConfiguration pParam) {
                if (pServerName.equals(pParam.getServerName())) {
                    servers.add(pParam);
                }
            }
        });

        return servers;
    }

}
