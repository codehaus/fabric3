package org.fabric3.assembly.examples;

import org.fabric3.assembly.examples.composite1.PutSomethingToSystemOut;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import java.io.File;

/**
 * @author Michal Capo
 */
public class Composite1Archive {

    public static JavaArchive create() {
        return ShrinkWrap.create(JavaArchive.class, "composite1.jar")
                .addClass(PutSomethingToSystemOut.class)
                .addAsResource("composites/composite1/composite1.composite", "composite1.composite")

                .addAsManifestResource("composites/composite1/META-INF/plan.xml", "plan.xml")
                .addAsManifestResource("composites/composite1/META-INF/sca-contribution.xml", "sca-contribution.xml");
    }

    public static void main(String[] args) {
        create().as(ZipExporter.class).exportTo(new File("/tmp/composite1.jar"), true);
    }

}
