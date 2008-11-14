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

import javax.xml.namespace.QName;

import org.fabric3.scdl.DataType;
import org.fabric3.spi.model.type.JavaParameterizedType;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.transform.TransformationException;
import org.fabric3.spi.transform.TransformContext;
import org.fabric3.transform.AbstractPullTransformer;

import org.osoa.sca.annotations.Reference;
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
public class String2MapOfQName2Class extends AbstractPullTransformer<Node, Map<QName, Class<?>>> {
    
    private static Map<QName, Class<?>> FIELD = null;
    private static JavaParameterizedType TARGET = null;
    
    private ClassLoaderRegistry classLoaderRegistry;
    
    static {
        try {
            ParameterizedType parameterizedType = (ParameterizedType) String2MapOfQName2Class.class.getDeclaredField("FIELD").getGenericType();
            TARGET = new JavaParameterizedType(parameterizedType);
        } catch (NoSuchFieldException ignore) {
            throw new AssertionError();
        }
    }
    
    public String2MapOfQName2Class(@Reference ClassLoaderRegistry classLoaderRegistry) {
        this.classLoaderRegistry = classLoaderRegistry;
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
    public Map<QName, Class<?>> transform(final Node node, final TransformContext context) throws TransformationException {

        final Map<QName, Class<?>> map = new HashMap<QName, Class<?>>();
        final NodeList nodeList = node.getChildNodes();

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            if (child instanceof Element) {
                Element element = (Element) child;
                
                String localPart = element.getTagName();
                String namespaceUri = element.getNamespaceURI();
                QName qname = new QName(namespaceUri, localPart);
                String classText = element.getTextContent();

                try {
                    Class<?> clazz = classLoaderRegistry.loadClass(context.getTargetClassLoader(), classText);
                    map.put(qname, clazz);
                } catch (ClassNotFoundException e) {
                    throw new TransformationException(e);
                }
            }
        }
        return map;
    }
    
    
}
