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
package org.fabric3.transform.xml;

import java.io.ByteArrayOutputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.fabric3.scdl.DataType;
import org.fabric3.spi.transform.TransformContext;
import org.fabric3.transform.AbstractPullTransformer;
import org.w3c.dom.Node;

/**
 * Serializes an element.
 * 
 * @version $Revision$ $Date$
 */
public class NodeToString extends AbstractPullTransformer<Node, String>{

    public String transform(Node source, TransformContext context) throws Exception {
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(new DOMSource(source), new StreamResult(out));
        
        return new String(out.toByteArray());
        
    }

    public DataType<?> getTargetType() {
        // TODO Auto-generated method stub
        return null;
    }

}
