/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.transform.dom2java;

import java.util.Calendar;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.w3c.dom.Node;

import org.fabric3.scdl.DataType;
import org.fabric3.spi.model.type.JavaClass;
import org.fabric3.transform.TransformContext;
import org.fabric3.transform.TransformationException;
import org.fabric3.transform.AbstractPullTransformer;

/**
 * String format for Calendar, expects format of date per XMLSchema (2007-10-31T01:02:03Z)
 *
 * @version $Rev: 566 $ $Date: 2007-07-24 22:07:41 +0100 (Tue, 24 Jul 2007) $
 */
public class String2Calendar extends AbstractPullTransformer<Node, Calendar> {
    private static final JavaClass<Calendar> TARGET = new JavaClass<Calendar>(Calendar.class);

    private final DatatypeFactory factory;

    public String2Calendar() throws DatatypeConfigurationException {
        factory = DatatypeFactory.newInstance();
    }

    public DataType<?> getTargetType() {
        return TARGET;
    }

    public Calendar transform(final Node node, final TransformContext context) throws TransformationException {
        XMLGregorianCalendar xmlCalendar = factory.newXMLGregorianCalendar(node.getTextContent());
        return xmlCalendar.toGregorianCalendar();
    }

}