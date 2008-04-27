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
package org.fabric3.transform.dom2java.generics.list;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.fabric3.scdl.DataType;
import org.fabric3.spi.model.type.JavaParameterizedType;
import org.fabric3.transform.TransformContext;
import org.fabric3.transform.TransformationException;
import org.fabric3.transform.AbstractPullTransformer;
import org.w3c.dom.Node;

/**
 * Expects the property to be defined in the format,
 * <p/>
 * <code> value1, value2, value3 </code>
 *
 * @version $Rev: 1570 $ $Date: 2007-10-20 14:24:19 +0100 (Sat, 20 Oct 2007) $
 */
public class String2ListOfString extends AbstractPullTransformer<Node, List<String>> {
    
    private static List<String> FIELD = null;
    private static JavaParameterizedType TARGET = null;
    
    static {
        try {
            ParameterizedType parameterizedType = (ParameterizedType) String2ListOfString.class.getDeclaredField("FIELD").getGenericType();
            TARGET = new JavaParameterizedType(parameterizedType);
        } catch (NoSuchFieldException ignore) {
        }
    }

    /**
     * @see org.fabric3.transform.Transformer#getTargetType()
     */
    public DataType<?> getTargetType() {
        return TARGET;
    }

    /**
     * @see org.fabric3.transform.PullTransformer#transform(java.lang.Object, org.fabric3.transform.TransformContext)
     */
    public List<String> transform(final Node node, final TransformContext context) throws TransformationException {

        final List<String> list = new ArrayList<String>();
        final StringTokenizer tokenizer = new StringTokenizer(node.getTextContent());
        
        while (tokenizer.hasMoreElements()) {
            list.add(tokenizer.nextToken());
        }
        
        return list;
        
    }
    
}
