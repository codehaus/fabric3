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
package org.fabric3.transform.dom2java.generics.map;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.model.type.service.DataType;
import org.fabric3.spi.model.type.JavaParameterizedType;
import org.fabric3.spi.transform.TransformationException;
import org.fabric3.spi.transform.TransformContext;
import org.fabric3.transform.AbstractPullTransformer;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Expects the property to be dfined in the format,
 * <p/>
 * <code> <key1>value1</key1> <key2>value2</key2> </code>
 *
 * @version $Rev: 1570 $ $Date: 2007-10-20 14:24:19 +0100 (Sat, 20 Oct 2007) $
 */
public class String2MapOfString2String extends AbstractPullTransformer<Node, Map<String, String>> {
    
    private static Map<String, String> FIELD = null;
    private static JavaParameterizedType TARGET = null;
    
    static {
        try {
            ParameterizedType parameterizedType = (ParameterizedType) String2MapOfString2String.class.getDeclaredField("FIELD").getGenericType();
            TARGET = new JavaParameterizedType(parameterizedType);
        } catch (NoSuchFieldException ignore) {
        }
    }

    /**
     * @see org.fabric3.spi.transform.Transformer#getTargetType()
     */
    public DataType<?> getTargetType() {
        return TARGET;
    }

    /**
     * @see org.fabric3.spi.transform.PullTransformer#transform(java.lang.Object, org.fabric3.spi.transform.TransformContext)
     */
    public Map<String, String> transform(final Node node, final TransformContext context)
            throws TransformationException {

        final Map<String, String> map = new HashMap<String, String>();
        final NodeList nodeList = node.getChildNodes();

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            if (child instanceof Element) {
                Element element = (Element) child;
                map.put(element.getTagName(), child.getTextContent());
            }
        }
        return map;
    }
    
    
}
