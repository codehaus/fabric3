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
package org.fabric3.java.control;

import javax.xml.namespace.QName;

import org.osoa.sca.Constants;

import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.model.type.component.Implementation;

/**
 * Represents a Java component implementation type.
 *
 * @version $$Rev$$ $$Date$$
 */
public class JavaImplementation extends Implementation<PojoComponentType> {
    public static final QName IMPLEMENTATION_JAVA = new QName(Constants.SCA_NS, "implementation.java");
    private static final long serialVersionUID = 8922589166061811190L;
    private String implementationClass;

    public JavaImplementation() {
    }

    public QName getType() {
        return IMPLEMENTATION_JAVA;
    }

    public String getImplementationClass() {
        return implementationClass;
    }

    public void setImplementationClass(String implementationClass) {
        this.implementationClass = implementationClass;
    }
}
