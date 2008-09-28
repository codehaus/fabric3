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
package org.fabric3.transform.dom2java.generics.list;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.xml.namespace.QName;

import org.w3c.dom.Node;

import org.fabric3.scdl.DataType;
import org.fabric3.spi.model.type.JavaParameterizedType;
import org.fabric3.transform.AbstractPullTransformer;
import org.fabric3.transform.TransformContext;
import org.fabric3.transform.TransformationException;

/**
 * Converts a String value to a list of QNames. Expects the property to be defined in the format,
 * <p/>
 * <code> value1, value2, value3 </code>
 * <p/>
 * where values correspond to the format specified by {@link QName#valueOf(String)}.
 *
 * @version $Rev: 1570 $ $Date: 2007-10-20 14:24:19 +0100 (Sat, 20 Oct 2007) $
 */
public class String2ListOfQName extends AbstractPullTransformer<Node, List<QName>> {

    private static List<QName> FIELD = null;
    private static JavaParameterizedType TARGET = null;

    static {
        try {
            ParameterizedType parameterizedType = (ParameterizedType) String2ListOfQName.class.getDeclaredField("FIELD").getGenericType();
            TARGET = new JavaParameterizedType(parameterizedType);
        } catch (NoSuchFieldException ignore) {
        }
    }

    public DataType<?> getTargetType() {
        return TARGET;
    }

    public List<QName> transform(final Node node, final TransformContext context) throws TransformationException {

        final List<QName> list = new ArrayList<QName>();
        final StringTokenizer tokenizer = new StringTokenizer(node.getTextContent(), " \t\n\r\f,");

        while (tokenizer.hasMoreElements()) {
            list.add(QName.valueOf(tokenizer.nextToken()));
        }

        return list;

    }

}