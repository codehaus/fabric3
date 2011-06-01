package org.fabric3.assembly.examples;

import org.fabric3.assembly.examples.web.WebComposite;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * @author Michal Capo
 */
public class Web1Archive {

    public static WebArchive create() {
        return ShrinkWrap.create(WebArchive.class, "web1.war")
                .addClass(WebComposite.class)

                .addAsWebInfResource("composites/web/webapp/WEB-INF/plan.xml", "plan.xml")
                .addAsWebInfResource("composites/web/webapp/WEB-INF/sca-contribution.xml", "sca-contribution.xml")
                .addAsWebInfResource("composites/web/webapp/WEB-INF/web.componentType", "web.componentType")
                .addAsWebInfResource("composites/web/webapp/WEB-INF/web.composite", "web.composite")
                .addAsWebInfResource("composites/web/webapp/WEB-INF/web.xml", "web.xml")

                ;
    }

}
