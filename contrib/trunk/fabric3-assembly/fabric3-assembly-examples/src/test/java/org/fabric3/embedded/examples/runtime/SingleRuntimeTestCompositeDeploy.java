package org.fabric3.embedded.examples.runtime;

import java.io.IOException;

/**
 * @author Michal Capo
 */
public class SingleRuntimeTestCompositeDeploy {

    public static void main(String[] args) throws IOException, InterruptedException {

        SingleRuntimeConfiguration.create().asModifier()
                .getArchive("comp").deployToServer("server1");

    }

}
