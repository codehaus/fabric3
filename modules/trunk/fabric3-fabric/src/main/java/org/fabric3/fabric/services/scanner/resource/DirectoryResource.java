package org.fabric3.fabric.services.scanner.resource;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.extension.scanner.FileSystemResource;
import org.fabric3.extension.scanner.AbstractResource;

/**
 * Represents a directory that is to be contributed to a domain
 *
 * @version $Rev$ $Date$
 */
public class DirectoryResource extends AbstractResource {
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

    public void addResource(FileSystemResource resource) {
        resources.add(resource);
    }

    public void reset() throws IOException {
        for (FileSystemResource resource : resources) {
            resource.reset();
        }
        checksumValue = checksum();
    }

    protected byte[] checksum() {
        try {
            MessageDigest checksum = MessageDigest.getInstance("MD5");
            for (FileSystemResource resource : resources) {
                checksum.update(resource.getChecksum());
            }
            return checksum.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError();
        }
    }

}
