/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.ftp.api;

import java.io.InputStream;

/**
 * Interface for receiving FTP callbacks.
 * <p/>
 * Note: The concept is borrowed from Apache MINA FTP Server.
 *
 * @version $Revision$ $Date$
 */
public interface FtpLet {

    /**
     * Callback when data is uploaded by the remote FTP client.
     *
     * @param fileName    Name of the file being uploaded.
     * @param contentType the type of data (e.g. binary or text) being uploaded
     * @param uploadData  Stream of data that is being uploaded.
     * @return true if the operation completed
     * @throws Exception If unable to handle the data.
     */
    boolean onUpload(String fileName, String contentType, InputStream uploadData) throws Exception;

}
