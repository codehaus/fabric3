package org.fabric3.assembly.shrinkwrap;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.impl.base.container.ContainerBase;
import org.jboss.shrinkwrap.impl.base.path.BasicPath;

/**
 * @author Michal Capo
 */
public class Fabric3ArchiveImpl extends ContainerBase<Fabric3Archive> implements Fabric3Archive {

    /**
     * Path to the manifests inside of the Archive.
     */
    private static final ArchivePath PATH_MANIFEST = new BasicPath("META-INF");

    /**
     * Path to the resources inside of the Archive.
     */
    private static final ArchivePath PATH_RESOURCE = new BasicPath("/");

    /**
     * Path to the classes inside of the Archive.
     */
    private static final ArchivePath PATH_CLASSES = new BasicPath("/");

    /**
     * Path to the libraries inside of the Archive.
     */
    private static final ArchivePath PATH_LIBRARY = new BasicPath(PATH_MANIFEST, "lib");

    public Fabric3ArchiveImpl(Archive<?> archive) {
        super(Fabric3Archive.class, archive);
    }

    @Override
    protected ArchivePath getManifestPath() {
        return PATH_MANIFEST;
    }

    @Override
    protected ArchivePath getResourcePath() {
        return PATH_RESOURCE;
    }

    @Override
    protected ArchivePath getClassesPath() {
        return PATH_CLASSES;
    }

    @Override
    protected ArchivePath getLibraryPath() {
        return PATH_LIBRARY;
    }
}
