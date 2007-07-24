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

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.fabric3.transform.AbstractPullTransformer;
import org.fabric3.spi.model.type.XSDSimpleType;
import org.fabric3.spi.model.type.JavaClass;
import org.fabric3.scdl.DataType;
import org.fabric3.spi.transform.TransformationException;
import org.fabric3.spi.transform.TransformContext;

/**
 * Expects the property to be dfined in the format,
 * 
 * <code>
 *   <key1>value1</key1>
 *   <key2>value2</key2>
 * </code>
 * @version $Rev$ $Date$
 */
public class String2Map extends AbstractPullTransformer<Node, Map<String, String>> {
    private static final XSDSimpleType SOURCE = new XSDSimpleType(Node.class, XSDSimpleType.STRING);
    private static final JavaClass<Map> TARGET = new JavaClass<Map>(Map.class);

    public DataType<?> getSourceType() {
        return SOURCE;
    }

    public DataType<?> getTargetType() {
        return TARGET;
    }

    public Map<String, String> transform(Node node, TransformContext context) throws TransformationException {
        
        Map<String, String> map = new HashMap<String, String>();
        NodeList nodeList = node.getChildNodes();
        for(int i = 0;i < nodeList.getLength();i++) {
            Node child = nodeList.item(i);
            if(child instanceof Element) {
                Element element = (Element) child;
                map.put(element.getTagName(), child.getTextContent());
            }
        }
        return map;
        
    }
    
}
