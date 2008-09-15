package org.fabric3.spi.scanner;

import java.io.IOException;

import org.fabric3.spi.scanner.FileSystemResource;
                       
/**
 * Base file system resource implementation
 *
 * @version $Rev$ $Date$
 */
public abstract class AbstractResource implements FileSystemResource {
    protected byte[] checksumValue;

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

    public byte[] getChecksum() {
        return checksumValue;
    }

    public void reset() throws IOException {
        checksumValue = checksum();
    }

    protected abstract byte[] checksum() throws IOException;
}
