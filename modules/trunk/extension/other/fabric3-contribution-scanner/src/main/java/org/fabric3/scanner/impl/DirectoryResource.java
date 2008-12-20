/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.scanner.impl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.scanner.spi.AbstractResource;
import org.fabric3.scanner.spi.FileSystemResource;

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
