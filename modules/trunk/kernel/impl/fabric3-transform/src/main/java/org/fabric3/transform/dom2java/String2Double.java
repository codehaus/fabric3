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
