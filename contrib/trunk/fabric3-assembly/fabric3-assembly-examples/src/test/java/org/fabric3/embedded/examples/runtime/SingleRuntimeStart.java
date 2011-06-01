package org.fabric3.embedded.examples.runtime;

import org.fabric3.assembly.modifier.AssemblyModifier;

import java.io.IOException;

/**
 * @author Michal Capo
 */
public class SingleRuntimeStart {

    public static void main(String[] args) throws IOException, InterruptedException {

        AssemblyModifier runner = new AssemblyModifier(SingleRuntimeConfiguration.create());
        runner.startServer("server1");

    }

}
