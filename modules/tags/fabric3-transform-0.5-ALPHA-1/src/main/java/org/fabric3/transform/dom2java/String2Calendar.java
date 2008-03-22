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

import java.util.Calendar;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.w3c.dom.Node;

import org.fabric3.scdl.DataType;
import org.fabric3.spi.model.type.JavaClass;
import org.fabric3.spi.transform.TransformContext;
import org.fabric3.spi.transform.TransformationException;
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