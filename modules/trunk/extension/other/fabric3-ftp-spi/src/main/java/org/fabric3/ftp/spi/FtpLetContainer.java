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
package org.fabric3.ftp.spi;

import org.fabric3.ftp.api.FtpLet;

/**
 * SPI for the FTP let container.
 *
 * @version $Revision$ $Date$
 */
public interface FtpLetContainer {

    /**
     * Registers an FTP let for the specified path.
     *
     * @param path   Path on which the FtpLet is listening.
     * @param ftpLet FtpLet listening for the upload request.
     */
    void registerFtpLet(String path, FtpLet ftpLet);

    /**
     * Gets a registered FTP let for the file name.
     *
     * @param fileName Fully qualified name for the file name.
     * @return FTP let that is registered, null if none registered.
     */
    FtpLet getFtpLet(String fileName);

    /**
     * Returns true if an FtpLet is registered for the given path.
     *
     * @param path the path.
     * @return true if an FtpLet is registered for the given path
     */
    public boolean isRegistered(String path);


}
