package org.fabric3.embedded.examples.runtime;

import java.io.IOException;

/**
 * @author Michal Capo
 */
public class SingleRuntimeStart {

    public static void main(String[] args) throws IOException, InterruptedException {

        SingleRuntimeConfiguration.create().asModifier()
                .startServer("server1");

    }

}
