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

import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Node;

import org.fabric3.scdl.DataType;
import org.fabric3.spi.model.type.JavaClass;
import org.fabric3.spi.model.type.XSDSimpleType;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.transform.TransformContext;
import org.fabric3.transform.AbstractPullTransformer;

/**
 * @version $Rev$ $Date$
 */
public class String2Class extends AbstractPullTransformer<Node, Class<?>> {
    private static final XSDSimpleType SOURCE = new XSDSimpleType(Node.class, XSDSimpleType.STRING);
    private static final JavaClass<Class> TARGET = new JavaClass<Class>(Class.class);

    private final ClassLoaderRegistry classLoaderRegistry;

    public String2Class(@Reference ClassLoaderRegistry classLoaderRegistry) {
        this.classLoaderRegistry = classLoaderRegistry;
    }

    public DataType<?> getSourceType() {
        return SOURCE;
    }

    public DataType<?> getTargetType() {
        return TARGET;
    }

    public Class<?> transform(Node node, TransformContext context) throws ClassNotFoundException {
        return classLoaderRegistry.loadClass(context.getTargetClassLoader(), node.getTextContent());
    }
}
