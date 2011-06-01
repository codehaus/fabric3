package org.fabric3.embedded.examples.runtime;

import java.io.IOException;

/**
 * @author Michal Capo
 */
public class MultiRuntimeStart {

    public static void main(String[] args) throws IOException, InterruptedException {

        MultiRuntimeConfiguration.create().asModifier()
                .startServer("server1");

    }

}
