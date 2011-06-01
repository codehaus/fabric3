package org.fabric3.assembly.shrinkwrap;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.container.ClassContainer;
import org.jboss.shrinkwrap.api.container.LibraryContainer;
import org.jboss.shrinkwrap.api.container.ManifestContainer;

/**
 * @author Michal Capo
 */
public interface Fabric3Archive
        extends
        Archive<Fabric3Archive>,
        ManifestContainer<Fabric3Archive>,
        ClassContainer<Fabric3Archive>,
        LibraryContainer<Fabric3Archive> {
}
