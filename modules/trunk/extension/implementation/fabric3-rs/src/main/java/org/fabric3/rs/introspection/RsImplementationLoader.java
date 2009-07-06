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
package org.fabric3.rs.introspection;

import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.java.control.JavaImplementation;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.annotation.ImplementationProcessor;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.TypeLoader;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class RsImplementationLoader implements TypeLoader<JavaImplementation> {

    private final LoaderHelper loaderHelper;
    private final ImplementationProcessor processor;
    private final RsHeuristic rsHeuristic;

    public RsImplementationLoader(@Reference(name = "implementationProcessor") ImplementationProcessor processor,
                                  @Reference(name = "RsHeuristic") RsHeuristic rsHeuristic,
                                  @Reference LoaderHelper loaderHelper) {
        this.processor = processor;
        this.loaderHelper = loaderHelper;
        this.rsHeuristic = rsHeuristic;
    }

    public JavaImplementation load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {

        String className = reader.getAttributeValue(null, "class");
        String webApp = reader.getAttributeValue(null, "uri");
        URI webAppURI = null;

        if (className == null) {
            MissingAttribute failure = new MissingAttribute("No class name specified", reader);
            context.addError(failure);
            return null;
        }

        if (webApp == null) {
            MissingAttribute failure = new MissingAttribute("No web application URI specified", reader);
            context.addError(failure);
            return null;
        }
        try {
            webAppURI = new URI(webApp);
        } catch (URISyntaxException ex) {
            InvalidValue failure = new InvalidValue("invalid URI value", reader);
            context.addError(failure);
            return null;
        }

        JavaImplementation impl = new JavaImplementation();
        impl.setImplementationClass(className);
        loaderHelper.loadPolicySetsAndIntents(impl, reader, context);
        processor.introspect(impl, context);
        LoaderUtil.skipToEndElement(reader);
        rsHeuristic.applyHeuristics(impl, webAppURI, context);
        return impl;
    }
}
