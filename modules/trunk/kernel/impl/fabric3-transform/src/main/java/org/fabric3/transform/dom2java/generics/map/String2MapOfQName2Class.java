  /*
   * Fabric3
   * Copyright (C) 2009 Metaform Systems
   *
   * Fabric3 is free software: you can redistribute it and/or modify
   * it under the terms of the GNU General Public License as
   * published by the Free Software Foundation, either version 3 of
   * the License, or (at your option) any later version, with the
   * following exception:
   *
   * Linking this software statically or dynamically with other
   * modules is making a combined work based on this software.
   * Thus, the terms and conditions of the GNU General Public
   * License cover the whole combination.
   *
   * As a special exception, the copyright holders of this software
   * give you permission to link this software with independent
   * modules to produce an executable, regardless of the license
   * terms of these independent modules, and to copy and distribute
   * the resulting executable under terms of your choice, provided
   * that you also meet, for each linked independent module, the
   * terms and conditions of the license of that module. An
   * independent module is a module which is not derived from or
   * based on this software. If you modify this software, you may
   * extend this exception to your version of the software, but
   * you are not obligated to do so. If you do not wish to do so,
   * delete this exception statement from your version.
   *
   * Fabric3 is distributed in the hope that it will be useful,
   * but WITHOUT ANY WARRANTY; without even the implied warranty
   * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
   * See the GNU General Public License for more details.
   *
   * You should have received a copy of the
   * GNU General Public License along with Fabric3.
   * If not, see <http://www.gnu.org/licenses/>.
   */
package org.fabric3.transform.dom2java.generics.map;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.fabric3.model.type.service.DataType;
import org.fabric3.spi.model.type.JavaParameterizedType;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.transform.TransformationException;
import org.fabric3.spi.transform.TransformContext;
import org.fabric3.spi.transform.AbstractPullTransformer;

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
