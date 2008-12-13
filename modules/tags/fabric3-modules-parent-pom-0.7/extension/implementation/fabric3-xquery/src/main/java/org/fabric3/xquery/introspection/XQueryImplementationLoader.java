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
package org.fabric3.xquery.introspection;

import javax.xml.stream.XMLStreamException;

import javax.xml.stream.XMLStreamReader;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.fabric3.xquery.scdl.XQueryImplementation;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class XQueryImplementationLoader implements TypeLoader<XQueryImplementation> {

    private XQueryImplementationProcessor introspector;
    private final LoaderHelper loaderHelper;

    public XQueryImplementationLoader(@Reference XQueryImplementationProcessor introspector, @Reference LoaderHelper loaderHelper) {
        this.introspector = introspector;
        this.loaderHelper = loaderHelper;
    }

    public XQueryImplementation load(XMLStreamReader reader, IntrospectionContext introspectionContext) throws XMLStreamException {
        String location = reader.getAttributeValue(null, "location");

        if (location == null) {
            MissingAttribute failure = new MissingAttribute("No XQuery file location specified", "class", reader);
            introspectionContext.addError(failure);
            return null;
        }

        XQueryImplementation impl = new XQueryImplementation();
        impl.setLocation(location);

        //TODO this should probably be a policy instead 
        String context = reader.getAttributeValue(null, "context");
        if (context != null) {
            impl.setContext(context);
        }


        introspector.introspect(impl, introspectionContext);
        loaderHelper.loadPolicySetsAndIntents(impl, reader, introspectionContext);


        LoaderUtil.skipToEndElement(reader);
        return impl;
    }
    
   
}
