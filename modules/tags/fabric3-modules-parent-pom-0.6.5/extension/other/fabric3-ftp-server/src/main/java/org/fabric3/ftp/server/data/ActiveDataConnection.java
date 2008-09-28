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
package org.fabric3.ftp.server.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Active data connection.
 * 
 * @version $Revision$ $Date$
 */
public class ActiveDataConnection implements DataConnection {
    
    /**
     * Initializes a data connection.
     * 
     * @throws IOException If unable to open connection.
     */
    public void initialize() throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Closes the data connection.
     */
    public void close() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Get an input stream to the data connection.
     * 
     * @return Input stream to the data cnnection.
     * @throws IOException If unable to get input stream.
     */
    public InputStream getInputStream() throws IOException {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Get an output stream to the data connection.
     * 
     * @return Output stream to the data connection.
     * @throws IOException If unable to get output stream.
     */
    public OutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Opens the data connection.
     * 
     * @throws IOException If unable to open connection.
     */
    public void open() throws IOException {
        throw new UnsupportedOperationException();
    }

}
