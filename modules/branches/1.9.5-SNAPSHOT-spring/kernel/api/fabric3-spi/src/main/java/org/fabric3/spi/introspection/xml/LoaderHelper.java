/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.spi.introspection.xml;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Document;

import org.fabric3.model.type.PolicyAware;
import org.fabric3.model.type.component.Multiplicity;
import org.fabric3.model.type.component.Target;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * Helper service for handling XML.
 *
 * @version $Rev$ $Date$
 */
public interface LoaderHelper {
    /**
     * Load the value of the attribute key from the current element.
     *
     * @param reader a stream containing a property value
     * @return the key value
     */
    String loadKey(XMLStreamReader reader);

    /**
     * Loads one or more property values configured in a composite or on a component from a Stax stream. Each property value is returned as child of
     * the document root.
     * <p/>
     * The reader must be positioned at the composite or component &lt;property&gt; element.
     *
     * @param reader the stream reader
     * @return a  document containing the values
     * @throws XMLStreamException if there was a problem reading the stream
     */
    Document loadPropertyValues(XMLStreamReader reader) throws XMLStreamException;

    /**
     * Loads a property value configured in a composite or on a component using the @value attribute from a String.
     *
     * @param content String content
     * @return a document containing the values
     * @throws XMLStreamException if there was a problem reading the stream
     */
    Document loadPropertyValue(String content) throws XMLStreamException;

    /**
     * Loads policy sets and intents. Errors will be collated in the IntrospectionContext.
     *
     * @param policyAware Element against which policy sets and intents are declared.
     * @param reader      XML stream reader from where the attributes are read.
     * @param context     the introspection context.
     */
    void loadPolicySetsAndIntents(PolicyAware policyAware, XMLStreamReader reader, IntrospectionContext context);


    /**
     * Convert a URI from a String form of <code>component/service</code> to a URI form of <code>component/service</code>.
     *
     * @param value the URI to convert
     * @return a URI where the fragment represents the service name
     * @throws URISyntaxException if the value is an invalid URI
     */
    URI parseUri(String value) throws URISyntaxException;

    /**
     * Convert a URI from a String form of <code>component/service/binding</code> to a Target.
     *
     * @param target the URI to convert
     * @param reader the stream reader parsing the XML document where the target is specified
     * @return a target instance
     * @throws InvalidTargetException if the target format is invalid
     */
    Target parseTarget(String target, XMLStreamReader reader) throws InvalidTargetException;

    /**
     * Parses a list of qualified names.
     *
     * @param reader    XML stream reader.
     * @param attribute Attribute that contains the list of qualified names.
     * @return Set containing the qualified names.
     * @throws InvalidPrefixException If the qualified name cannot be resolved.
     */
    Set<QName> parseListOfQNames(XMLStreamReader reader, String attribute) throws InvalidPrefixException;

    /**
     * Constructs a QName from the given name. If a namespace prefix is not specified in the name, the namespace context is used.
     *
     * @param name   the name to parse
     * @param reader the XML stream reader
     * @return the parsed QName
     * @throws InvalidPrefixException if a specified namespace prefix is invalid
     */
    QName createQName(String name, XMLStreamReader reader) throws InvalidPrefixException;

    /**
     * Parses a list of URIs contained in a attribute.
     *
     * @param reader    the XML stream reader
     * @param attribute the attribute to parse
     * @return the list of URIs contained in that attribute, or null if the attribute is not present
     * @throws URISyntaxException if the attribute contains an invalid URI
     */
    List<URI> parseListOfUris(XMLStreamReader reader, String attribute) throws URISyntaxException;

    /**
     * Determines if the first multiplicity setting can narrow the second.
     *
     * @param first  multiplicity setting
     * @param second multiplicity setting
     * @return true if the first can narrow the second
     */
    boolean canNarrow(Multiplicity first, Multiplicity second);

    /**
     * Transforms the XML element to a DOM representation.
     *
     * @param reader the XML stream reader
     * @return the DOM
     * @throws XMLStreamException if a conversion exception is encountered
     */
    Document transform(XMLStreamReader reader) throws XMLStreamException;
}
