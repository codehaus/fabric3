package org.fabric3.assembly.shrinkwrap;

import org.jboss.shrinkwrap.api.ShrinkWrap;

/**
 * @author Michal Capo
 */
public class Fabric3Extensions {

    public static void load() {
        ShrinkWrap.getDefaultDomain().getConfiguration().getExtensionLoader().addOverride(Fabric3Archive.class, Fabric3ArchiveImpl.class);
    }

}
