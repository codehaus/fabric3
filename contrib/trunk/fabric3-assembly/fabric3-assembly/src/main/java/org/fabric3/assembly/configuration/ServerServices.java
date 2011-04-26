package org.fabric3.assembly.configuration;

import org.fabric3.assembly.exception.ServerNotFoundException;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;

/**
 * @author Michal Capo
 */
public abstract class ServerServices {

    public abstract List<ServerConfiguration> getServerConfigurations();

    public File findServerPathByRuntime(RuntimeConfiguration pRuntime) {
        String serverLookupName = pRuntime.getServerName();

        for (ServerConfiguration server : getServerConfigurations()) {
            if (serverLookupName.equals(server.getServerName())) {
                return server.getServerPath();
            }
        }

        throw new ServerNotFoundException(MessageFormat.format("You specified that runtime ''{0}'' should be added to ''{1}'' server. But such server doesn''t exists.", pRuntime.getRuntimeName(), serverLookupName));
    }

}
