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
package org.fabric3.admin.interpreter.command;

import java.net.URL;

/**
 * @version $Revision$ $Date$
 */
public class CommandHelper {

    private CommandHelper() {
    }

    /**
     * Derives a contribution name from a URL by selecting the path part following the last '/'.
     *
     * @param contribution the contribution URL
     * @return the contribution name
     */
    public static String parseContributionName(URL contribution) {
        String contributionName;
        String path = contribution.getPath();
        int pos = path.lastIndexOf('/');
        if (pos < 0) {
            contributionName = path;
        } else if (pos == path.length() - 1) {
            String substr = path.substring(0, pos);
            pos = substr.lastIndexOf('/');
            if (pos < 0) {
                contributionName = substr;
            } else {
                contributionName = path.substring(pos + 1, path.length() - 1);
            }
        } else {
            contributionName = path.substring(pos + 1);
        }
        return contributionName;
    }
}
