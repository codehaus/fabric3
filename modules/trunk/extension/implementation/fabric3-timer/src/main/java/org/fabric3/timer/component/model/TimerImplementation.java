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
package org.fabric3.timer.component.model;

import javax.xml.namespace.QName;

import org.fabric3.java.control.JavaImplementation;
import org.fabric3.spi.Namespaces;
import org.fabric3.timer.component.provision.TriggerData;

/**
 * Represents a timer component implementation type.
 *
 * @version $$Rev: 3079 $$ $$Date: 2008-03-13 03:30:59 -0700 (Thu, 13 Mar 2008) $$
 */
public class TimerImplementation extends JavaImplementation {
    public static final QName IMPLEMENTATION_TIMER = new QName(Namespaces.IMPLEMENTATION, "implementation.timer");
    private static final long serialVersionUID = -911919528396189874L;
    private TriggerData triggerData;

    public QName getType() {
        return IMPLEMENTATION_TIMER;
    }

    public TriggerData getTriggerData() {
        return triggerData;
    }

    public void setTriggerData(TriggerData triggerData) {
        this.triggerData = triggerData;
    }
}