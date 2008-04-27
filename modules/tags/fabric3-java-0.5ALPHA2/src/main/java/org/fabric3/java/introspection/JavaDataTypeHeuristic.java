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
package org.fabric3.java.introspection;

import java.util.Map;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.net.URI;
import java.awt.*;

import javax.xml.namespace.QName;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionException;
import org.fabric3.introspection.java.HeuristicProcessor;
import org.fabric3.java.scdl.JavaImplementation;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.Property;
import org.fabric3.scdl.InjectionSite;
import org.fabric3.scdl.InjectableAttribute;
import org.fabric3.scdl.InjectableAttributeType;
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.datatype.Duration;
import javax.xml.transform.Source;

/**
 * Heuristic that determines the XML type of Java properties.
 *
 * @version $Rev$ $Date$
 */
public class JavaDataTypeHeuristic implements HeuristicProcessor<JavaImplementation> {
    private static final Map<String, QName> JAXB_MAPPING;
    static {
        JAXB_MAPPING = new ConcurrentHashMap<String, QName>();
        JAXB_MAPPING.put("boolean", new QName(W3C_XML_SCHEMA_NS_URI, "boolean"));
        JAXB_MAPPING.put("byte", new QName(W3C_XML_SCHEMA_NS_URI, "byte"));
        JAXB_MAPPING.put("short", new QName(W3C_XML_SCHEMA_NS_URI, "short"));
        JAXB_MAPPING.put("int", new QName(W3C_XML_SCHEMA_NS_URI, "int"));
        JAXB_MAPPING.put("long", new QName(W3C_XML_SCHEMA_NS_URI, "long"));
        JAXB_MAPPING.put("float", new QName(W3C_XML_SCHEMA_NS_URI, "float"));
        JAXB_MAPPING.put("double", new QName(W3C_XML_SCHEMA_NS_URI, "double"));
        JAXB_MAPPING.put(String.class.getName(), new QName(W3C_XML_SCHEMA_NS_URI, "string"));
        JAXB_MAPPING.put(BigInteger.class.getName(), new QName(W3C_XML_SCHEMA_NS_URI, "integer"));
        JAXB_MAPPING.put(BigDecimal.class.getName(), new QName(W3C_XML_SCHEMA_NS_URI, "decimal"));
        JAXB_MAPPING.put(Calendar.class.getName(), new QName(W3C_XML_SCHEMA_NS_URI, "dateTime"));
        JAXB_MAPPING.put(Date.class.getName(), new QName(W3C_XML_SCHEMA_NS_URI, "dateTime"));
        JAXB_MAPPING.put(QName.class.getName(), new QName(W3C_XML_SCHEMA_NS_URI, "QName"));
        JAXB_MAPPING.put(URI.class.getName(), new QName(W3C_XML_SCHEMA_NS_URI, "string"));
        JAXB_MAPPING.put(XMLGregorianCalendar.class.getName(), new QName(W3C_XML_SCHEMA_NS_URI, "anySimpleType"));
        JAXB_MAPPING.put(Duration.class.getName(), new QName(W3C_XML_SCHEMA_NS_URI, "duration"));
        JAXB_MAPPING.put(Object.class.getName(), new QName(W3C_XML_SCHEMA_NS_URI, "anyType"));
        JAXB_MAPPING.put(Image.class.getName(), new QName(W3C_XML_SCHEMA_NS_URI, "base64Binary"));
        JAXB_MAPPING.put("javax.activation.DataHandler", new QName(W3C_XML_SCHEMA_NS_URI, "base64Binary"));
        JAXB_MAPPING.put(Source.class.getName(), new QName(W3C_XML_SCHEMA_NS_URI, "base64Binary"));
        JAXB_MAPPING.put(UUID.class.getName(), new QName(W3C_XML_SCHEMA_NS_URI, "string"));
        JAXB_MAPPING.put(byte[].class.getName(), new QName(W3C_XML_SCHEMA_NS_URI, "base64Binary"));
    }

    public void applyHeuristics(JavaImplementation implementation, Class<?> implClass, IntrospectionContext context) throws IntrospectionException {

        PojoComponentType componentType = implementation.getComponentType();
        Map<String, Property> properties = componentType.getProperties();
        for (Map.Entry<InjectionSite, InjectableAttribute> entry : componentType.getInjectionSites().entrySet()) {
            InjectionSite site = entry.getKey();
            InjectableAttribute attribute = entry.getValue();
            if (InjectableAttributeType.PROPERTY != attribute.getValueType()) {
                continue;
            }

            Property property = properties.get(attribute.getName());
            if (property.getXmlType() != null) {
                continue;
            }

            property.setXmlType(getXmlType(site.getType()));
        }
    }

    QName getXmlType(String className) {
        return JAXB_MAPPING.get(className);
    }
}
