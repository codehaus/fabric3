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
package org.fabric3.management.contribution;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates errors for a contribution artifact.
 *
 * @version $Revision$ $Date$
 */
public class ArtifactErrorInfo extends ErrorInfo {
    private static final long serialVersionUID = 1620259064648312693L;
    private String name;
    private List<ErrorInfo> errors = new ArrayList<ErrorInfo>();

    public ArtifactErrorInfo(String name) {
        super(null);
        this.name = name;
    }

    public List<ErrorInfo> getErrors() {
        return errors;
    }

    public void addError(ErrorInfo error) {
        errors.add(error);
    }

    public String getName() {
        return name;
    }
}
