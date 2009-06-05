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
package org.fabric3.taglib.sca;

import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.VariableInfo;
import javax.servlet.jsp.tagext.TagData;

/**
 * @version $Revision$ $Date$
 */
public class ReferenceTEI extends TagExtraInfo {

    public VariableInfo[] getVariableInfo(TagData data) {
        String name = data.getAttributeString("name");
        String type = data.getAttributeString("type");
        VariableInfo info = new VariableInfo(name, type, true, VariableInfo.AT_END);
        return new VariableInfo[]{info};
    }
}
