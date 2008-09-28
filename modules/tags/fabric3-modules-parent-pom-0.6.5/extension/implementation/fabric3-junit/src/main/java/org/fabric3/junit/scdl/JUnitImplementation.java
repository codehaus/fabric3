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
package org.fabric3.junit.scdl;

import javax.xml.namespace.QName;

import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.Implementation;
import org.fabric3.spi.Constants;

/**
 * @version $Rev$ $Date$
 */
public class JUnitImplementation extends Implementation<PojoComponentType> {
    public static final QName IMPLEMENTATION_JUNIT = new QName(Constants.FABRIC3_NS, "junit");
    private static final long serialVersionUID = -5048471724313487914L;
    private String implementationClass;

    /**
     * Constructor supplying the name of the JUnit test class
     *
     * @param className the name of the JUnit test class
     */
    public JUnitImplementation(String className) {
        this.implementationClass = className;
    }

    public QName getType() {
        return IMPLEMENTATION_JUNIT;
    }

    /**
     * Returns the name of the JUnit test class.
     *
     * @return the name of the JUnit test class
     */
    public String getImplementationClass() {
        return implementationClass;
    }

}
