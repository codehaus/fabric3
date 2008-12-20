/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the ÒLicenseÓ), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an Òas isÓ basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.scanner.spi;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Represents a file that is to be contributed to a domain
 *
 * @version $Rev$ $Date$
 */
public class FileResource extends AbstractResource {
    private File file;

    public FileResource(File file) {
        this.file = file;
    }

    public String getName() {
        return file.getName();
    }

    public URL getLocation() {
        try {
            return file.toURI().normalize().toURL();
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }

    public long getTimestamp() {
        return file.lastModified();
    }

    public void reset() throws IOException {
        checksumValue = checksum();
    }

    protected byte[] checksum() throws IOException {
        BufferedInputStream is = null;
        try {
            MessageDigest checksum = MessageDigest.getInstance("MD5");
            is = new BufferedInputStream(new FileInputStream(file));
            byte[] bytes = new byte[1024];
            int len;

            while ((len = is.read(bytes)) >= 0) {
                checksum.update(bytes, 0, len);
            }
            return checksum.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        } finally {
            if (is != null) {
                is.close();
            }
        }

    }
}
