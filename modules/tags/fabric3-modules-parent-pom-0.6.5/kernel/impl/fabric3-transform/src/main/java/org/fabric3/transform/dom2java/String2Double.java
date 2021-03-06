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

import org.fabric3.scdl.DataType;
import org.fabric3.spi.model.type.JavaClass;
import org.fabric3.transform.TransformContext;
import org.fabric3.transform.TransformationException;
import org.fabric3.transform.AbstractPullTransformer;

/**
 * @version $Rev$ $Date$
 */
public class String2Double extends AbstractPullTransformer<Node, Double> {
    private static final JavaClass<Double> TARGET = new JavaClass<Double>(Double.class);

    public DataType<?> getTargetType() {
        return TARGET;
    }

    public Double transform(Node node, TransformContext context) throws TransformationException {
        try {
            return Double.valueOf(node.getTextContent());
        } catch (NumberFormatException ex) {
            throw new TransformationException("Unsupportable double " + node.getTextContent(), ex);
        }
    }
}
