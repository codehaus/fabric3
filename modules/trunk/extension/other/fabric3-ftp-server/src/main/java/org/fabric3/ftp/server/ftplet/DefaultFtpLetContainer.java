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
package org.fabric3.ftp.server.ftplet;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fabric3.ftp.api.FtpLet;
import org.fabric3.ftp.spi.FtpLetContainer;

/**
 * Default implementation of the FtpLet container.
 *
 * @version $Revision$ $Date$
 */
public class DefaultFtpLetContainer implements FtpLetContainer {

    private Map<String, FtpLet> ftpLets = new ConcurrentHashMap<String, FtpLet>();

    public FtpLet getFtpLet(String fileName) {
        for (Map.Entry<String, FtpLet> entry : ftpLets.entrySet()) {
            if (fileName.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    public void registerFtpLet(String path, FtpLet ftpLet) {
        ftpLets.put(path, ftpLet);
    }

    public boolean isRegistered(String path) {
        return ftpLets.containsKey(path);
    }

}
