package org.fabric3.extension.scanner.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.extension.scanner.FileSystemResource;

/**
 * Represents a directory that is to be contributed to a domain
 *
 * @version $Rev$ $Date$
 */
public class DirectoryResource implements FileSystemResource {
    private final String name;
    // the list of resources to track changes against
    private List<FileSystemResource> resources;

    public DirectoryResource(String name) {
        this.name = name;
        resources = new ArrayList<FileSystemResource>();
    }

    public String getName() {
        return name;
    }

    public boolean isChanged() throws IOException {
        for (FileSystemResource resource : resources) {
            if (resource.isChanged()) {
                return true;
            }
        }
        return false;
    }

    public void reset() throws IOException {
        for (FileSystemResource resource : resources) {
            resource.reset();
        }
    }

    public void addResource(FileSystemResource resource) {
        resources.add(resource);
    }
}
