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
package org.fabric3.scanner.spi;

import java.io.IOException;

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
