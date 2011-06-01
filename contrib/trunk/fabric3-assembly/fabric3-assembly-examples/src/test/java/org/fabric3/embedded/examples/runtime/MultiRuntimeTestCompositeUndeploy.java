package org.fabric3.embedded.examples.runtime;

import org.fabric3.assembly.configuration.AssemblyConfig;
import org.fabric3.assembly.modifier.AssemblyModifier;

import java.io.IOException;

/**
 * @author Michal Capo
 */
public class MultiRuntimeTestCompositeUndeploy {

    public static void main(String[] args) throws IOException, InterruptedException {
        AssemblyConfig config = MultiRuntimeConfiguration.create();
        AssemblyModifier modifier = new AssemblyModifier(config);

        modifier.archive("comp").undeployFromServer("server1");
    }

}
