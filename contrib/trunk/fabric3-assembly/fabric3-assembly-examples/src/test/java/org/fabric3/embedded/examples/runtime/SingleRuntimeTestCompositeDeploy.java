package org.fabric3.embedded.examples.runtime;

import org.fabric3.assembly.modifier.AssemblyModifier;

import java.io.IOException;

/**
 * @author Michal Capo
 */
public class SingleRuntimeTestCompositeDeploy {

    public static void main(String[] args) throws IOException, InterruptedException {

        AssemblyModifier modifier = new AssemblyModifier(SingleRuntimeConfiguration.create());
        modifier.archive("comp").deployToServer("server1");

    }

}
