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
package org.fabric3.rs.introspection;

import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.ImplementationProcessor;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.fabric3.java.control.JavaImplementation;

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
