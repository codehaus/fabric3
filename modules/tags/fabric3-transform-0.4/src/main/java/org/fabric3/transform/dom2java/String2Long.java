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
import org.fabric3.spi.transform.TransformContext;
import org.fabric3.spi.transform.TransformationException;
import org.fabric3.transform.AbstractPullTransformer;

/**
 * @version $Rev$ $Date$
 */
public class String2Long extends AbstractPullTransformer<Node, Long> {
    private static final JavaClass<Long> TARGET = new JavaClass<Long>(Long.class);

    public DataType<?> getTargetType() {
        return TARGET;
    }

    public Long transform(Node node, TransformContext context) throws TransformationException {
        try {
            return Long.valueOf(node.getTextContent());
        } catch (NumberFormatException ex) {
            throw new TransformationException("Unsupportable long " + node.getTextContent(), ex);
        }
    }
}
