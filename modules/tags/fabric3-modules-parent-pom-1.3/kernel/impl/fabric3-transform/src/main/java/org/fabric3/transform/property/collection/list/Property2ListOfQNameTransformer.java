/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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
package org.fabric3.transform.property.collection.list;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.xml.namespace.QName;

import org.w3c.dom.Node;

import org.fabric3.model.type.contract.DataType;
import org.fabric3.spi.model.type.java.JavaGenericType;
import org.fabric3.spi.model.type.java.JavaTypeInfo;
import org.fabric3.spi.model.type.xsd.XSDConstants;
import org.fabric3.spi.transform.SingleTypeTransformer;
import org.fabric3.spi.transform.TransformationException;

/**
 * Converts a DOM node to a list of QNames. Expects the property to be defined in the format, <p/><code> value1, value2, value3 </code>
 * <p/>
 * where values correspond to the format specified by {@link QName#valueOf(String)}.
 *
 * @version $Rev$ $Date$
 */
public class Property2ListOfQNameTransformer implements SingleTypeTransformer<Node, List<QName>> {

    private static JavaGenericType TARGET = null;

    static {
        JavaTypeInfo qNameInfo = new JavaTypeInfo(QName.class);
        List<JavaTypeInfo> list = new ArrayList<JavaTypeInfo>();
        list.add(qNameInfo);
        JavaTypeInfo listInfo = new JavaTypeInfo(List.class, list);
        TARGET = new JavaGenericType(listInfo);
    }

    public DataType<?> getSourceType() {
        return XSDConstants.PROPERTY_TYPE;
    }

    public DataType<?> getTargetType() {
        return TARGET;
    }

    public List<QName> transform(final Node node, ClassLoader loader) throws TransformationException {

        final List<QName> list = new ArrayList<QName>();
        final StringTokenizer tokenizer = new StringTokenizer(node.getTextContent(), " \t\n\r\f,");

        while (tokenizer.hasMoreElements()) {
            list.add(QName.valueOf(tokenizer.nextToken()));
        }

        return list;

    }

}