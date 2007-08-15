package org.fabric3.scanner.scanner.resource;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.scanner.scanner.AbstractResource;
import org.fabric3.spi.scanner.FileSystemResource;

/**
 * Represents a directory that is to be contributed to a domain
 *
 * @version $Rev$ $Date$
 */
public class DirectoryResource extends AbstractResource {
    private final File root;
    // the list of resources to track changes against
    private List<FileSystemResource> resources;

    public DirectoryResource(File root) {
        this.root = root;
        resources = new ArrayList<FileSystemResource>();
    }

    public String getName() {
        return root.getName();
    }

    public URL getLocation() {
        try {
            return root.toURI().normalize().toURL();
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }

    public long getTimestamp() {
        return root.lastModified();
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
