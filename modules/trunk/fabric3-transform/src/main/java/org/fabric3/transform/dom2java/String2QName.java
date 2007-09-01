/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.fabric3.transform.dom2java;

import javax.xml.namespace.QName;

import org.w3c.dom.Node;

import org.fabric3.scdl.DataType;
import org.fabric3.spi.model.type.JavaClass;
import org.fabric3.spi.transform.TransformContext;
import org.fabric3.spi.transform.TransformationException;
import org.fabric3.transform.AbstractPullTransformer;

/**
 * @version $Rev: 42 $ $Date: 2007-05-16 18:58:55 +0100 (Wed, 16 May 2007) $
 */
public class String2QName extends AbstractPullTransformer<Node, QName> {
    /**
     * Target Class (Long)
     */
    private static final JavaClass<QName> TARGET = new JavaClass<QName>(QName.class);

    /**
     * @see org.fabric3.spi.transform.Transformer#getTargetType()
     */
    public DataType<?> getTargetType() {
        return TARGET;
    }

    /**
     * @see org.fabric3.spi.transform.PullTransformer#transform(java.lang.Object,org.fabric3.spi.transform.TransformContext)
     *      Applies Transformation for QName
     */
    public QName transform(final Node node, final TransformContext context) throws TransformationException {
        try {
            return QName.valueOf(node.getTextContent());
        } catch (IllegalArgumentException ie) {
            throw new TransformationException("Unable to transform on QName ", ie);
		}
	}
}
