package org.fabric3.embedded.examples;

import org.fabric3.embedded.examples.test1.Test1;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * @author Michal Capo
 */
public class Test1Archive {

    public static JavaArchive create() {
        return ShrinkWrap.create(JavaArchive.class, "test1.jar")
                .addClass(Test1.class)
                .addAsResource("composites/test1/compositeTest.composite", "compositeTest.composite")

                .addAsManifestResource("composites/test1/META-INF/plan.xml", "plan.xml")
                .addAsManifestResource("composites/test1/META-INF/sca-contribution.xml", "sca-contribution.xml");
    }

}
