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
package org.fabric3.jaxb.provision;

import java.util.Set;

import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;

/**
 * Base definition for an interceptor that performs a data transformation to or from JAXB objects.
 *
 * @version $Revision$ $Date$
 */
public abstract class AbstractTransformingInterceptorDefinition extends PhysicalInterceptorDefinition {
    private static final long serialVersionUID = -2187418436391138272L;
    private String encoding;
    private Set<String> classNames;

    /**
     * Cosntructor.
     *
     * @param encoding   the encoding transformer must convert to and from
     * @param classNames set of parameter and fault types the transformer must be able to convert
     */
    public AbstractTransformingInterceptorDefinition(String encoding, Set<String> classNames) {
        this.encoding = encoding;
        this.classNames = classNames;
    }

    /**
     * The set of parameter and fault types the transformer must be able to convert.
     *
     * @return the parameter and fault types
     */
    public Set<String> getClassNames() {
        return classNames;
    }

    /**
     * Returns the encoding the transformer must convert to and from.
     *
     * @return the data type
     */
    public String getEncoding() {
        return encoding;
    }
}
