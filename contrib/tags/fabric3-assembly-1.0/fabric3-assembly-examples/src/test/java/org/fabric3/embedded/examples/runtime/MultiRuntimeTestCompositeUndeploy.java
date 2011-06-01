package org.fabric3.embedded.examples.runtime;

import java.io.IOException;

/**
 * @author Michal Capo
 */
public class MultiRuntimeTestCompositeUndeploy {

    public static void main(String[] args) throws IOException, InterruptedException {

        MultiRuntimeConfiguration.create().asModifier()
                .getArchive("comp").undeployFromServer("server1");

    }

}
