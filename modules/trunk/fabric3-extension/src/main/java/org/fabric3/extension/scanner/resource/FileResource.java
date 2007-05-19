package org.fabric3.extension.scanner.resource;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.fabric3.extension.scanner.FileSystemResource;

/**
 * Represents a file that is to be contributed to a domain
 *
 * @version $Rev$ $Date$
 */
public class FileResource implements FileSystemResource {
    private File file;
    private byte[] checksumValue;

    public FileResource(File file) {
        this.file = file;
    }

    public String getName() {
        return file.getName();
    }

    public boolean isChanged() throws IOException {
        byte[] newValue = checksum();
        if (checksumValue == null || checksumValue.length != newValue.length) {
            checksumValue = newValue;
            return true;
        }
        for (int i = 0; i < newValue.length; i++) {
            if (newValue[i] != checksumValue[i]) {
                checksumValue = newValue;
                return true;
            }
        }
        return false;
    }

    public void reset() throws IOException {
        checksumValue = checksum();
    }

    private byte[] checksum() throws IOException {
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
