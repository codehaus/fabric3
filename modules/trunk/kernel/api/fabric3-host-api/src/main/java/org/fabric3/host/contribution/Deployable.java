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
package org.fabric3.host.contribution;

import java.io.Serializable;
import javax.xml.namespace.QName;

import org.fabric3.host.runtime.RuntimeMode;

/**
 * Represents a deployable artifact in a contribution
 *
 * @version $Rev$ $Date$
 */
@SuppressWarnings({"SerializableHasSerializationMethods"})
public class Deployable implements Serializable {
    private static final long serialVersionUID = -710863113841788110L;
    private QName name;
    private QName type;
    private RuntimeMode runtimeMode;

    public Deployable(QName name, QName type) {
        this(name, type, RuntimeMode.VM);
    }

    public Deployable(QName name, QName type, RuntimeMode runtimeMode) {
        this.name = name;
        this.type = type;
        this.runtimeMode = runtimeMode;
    }

    /**
     * The QName of the deployable component.
     *
     * @return QName of the deployable component
     */
    public QName getName() {
        return name;
    }

    /**
     * Returns the deployable type.
     *
     * @return the deployable type
     */
    public QName getType() {
        return type;
    }

    /**
     * Returns the runtime mode the deployable should be activated in.
     *
     * @return the runtime mode the deployable should be activated in.
     */
    public RuntimeMode getRuntimeMode() {
        return runtimeMode;
    }
}
