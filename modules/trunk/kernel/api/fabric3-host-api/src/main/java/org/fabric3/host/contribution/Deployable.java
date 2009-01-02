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
import java.util.ArrayList;
import java.util.List;
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
    public static final List<RuntimeMode> DEFAULT_MODES;

    static {
        DEFAULT_MODES = new ArrayList<RuntimeMode>();
        DEFAULT_MODES.add(RuntimeMode.VM);
        DEFAULT_MODES.add(RuntimeMode.CONTROLLER);
        DEFAULT_MODES.add(RuntimeMode.PARTICIPANT);
    }

    private QName name;
    private QName type;
    private List<RuntimeMode> runtimeModes;


    public Deployable(QName name, QName type) {
        this(name, type, DEFAULT_MODES);
    }

    public Deployable(QName name, QName type, List<RuntimeMode> runtimeModes) {
        this.name = name;
        this.type = type;
        this.runtimeModes = runtimeModes;
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
     * Returns the runtime modes the deployable should be activated in.
     *
     * @return the runtime modes the deployable should be activated in.
     */
    public List<RuntimeMode> getRuntimeModes() {
        return runtimeModes;
    }
}
