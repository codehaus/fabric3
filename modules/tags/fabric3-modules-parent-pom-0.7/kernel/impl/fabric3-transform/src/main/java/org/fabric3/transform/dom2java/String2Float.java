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
package org.fabric3.transform.dom2java;

import org.w3c.dom.Node;

import org.fabric3.model.type.service.DataType;
import org.fabric3.spi.model.type.JavaClass;
import org.fabric3.spi.transform.TransformationException;
import org.fabric3.spi.transform.TransformContext;
import org.fabric3.transform.AbstractPullTransformer;

/**
 * @version $Rev$ $Date$
 */
public class String2Float extends AbstractPullTransformer<Node, Float> {
    private static final JavaClass<Float> TARGET = new JavaClass<Float>(Float.class);

    public DataType<?> getTargetType() {
        return TARGET;
    }

    public Float transform(Node node, TransformContext context) throws TransformationException {
        try {
            return Float.valueOf(node.getTextContent());
        } catch (NumberFormatException ex) {
            throw new TransformationException("Unsupportable float " + node.getTextContent(), ex);
        }
    }
}
