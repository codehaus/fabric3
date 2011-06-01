package org.fabric3.assembly.examples;

import org.fabric3.assembly.examples.composite1.PutSomethingToSystemOut;
import org.fabric3.assembly.shrinkwrap.Fabric3Archive;
import org.fabric3.assembly.shrinkwrap.Fabric3Extensions;
import org.jboss.shrinkwrap.api.ShrinkWrap;

/**
 * @author Michal Capo
 */
public class Composite1Archive {

    public static Fabric3Archive create() {
        Fabric3Extensions.load();

        return ShrinkWrap.create(Fabric3Archive.class, "composite1.jar")
                .addClass(PutSomethingToSystemOut.class)
                .addAsResource("composites/composite1/composite1.composite", "composite1.composite")

                .addAsManifestResource("composites/composite1/META-INF/plan.xml", "plan.xml")
                .addAsManifestResource("composites/composite1/META-INF/sca-contribution.xml", "sca-contribution.xml")

                ;
    }

}
