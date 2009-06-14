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
package org.fabric3.java.introspection;

import java.awt.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.HeuristicProcessor;
import org.fabric3.java.control.JavaImplementation;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.model.type.java.InjectableAttribute;
import org.fabric3.model.type.java.InjectableAttributeType;
import org.fabric3.model.type.java.InjectionSite;
import org.fabric3.model.type.component.Property;

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

    public void applyHeuristics(JavaImplementation implementation, Class<?> implClass, IntrospectionContext context) {

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
