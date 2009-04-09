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
package org.fabric3.spi.binding;

import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

/**
 * Result for a binding match operation.
 *
 * @version $Revision$ $Date$
 */
public class BindingMatchResult {
    private boolean match;
    private QName type;
    private List<String> reasons = new ArrayList<String>();

    public BindingMatchResult(boolean match, QName type) {
        this.match = match;
        this.type = type;
    }

    public boolean isMatch() {
        return match;
    }

    public QName getType() {
        return type;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public void addReason(String reason) {
        reasons.add(reason);
    }

}
