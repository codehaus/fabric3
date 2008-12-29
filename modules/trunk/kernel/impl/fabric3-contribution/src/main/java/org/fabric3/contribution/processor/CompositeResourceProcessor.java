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
package org.fabric3.contribution.processor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.contribution.Constants;
import org.fabric3.host.contribution.InstallException;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.ValidationContext;
import org.fabric3.spi.introspection.xml.Loader;
import org.fabric3.spi.introspection.xml.LoaderException;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.model.type.component.Composite;
import org.fabric3.spi.xml.XMLFactory;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.ResourceElementNotFoundException;
import org.fabric3.spi.contribution.ResourceProcessor;

/**
 * Introspects a composite SCDL file in a contribution and produces a Composite type. This implementation assumes the CCL has all necessary artifacts
 * to perform introspection on its classpath.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class CompositeResourceProcessor implements ResourceProcessor {
    private Loader loader;
    private final XMLInputFactory xmlFactory;

    public CompositeResourceProcessor(@Reference ProcessorRegistry processorRegistry,
                                      @Reference Loader loader,
                                      @Reference XMLFactory xmlFactory) {
        processorRegistry.register(this);
        this.loader = loader;
        this.xmlFactory = xmlFactory.newInputFactoryInstance();
    }

    public String getContentType() {
        return Constants.COMPOSITE_CONTENT_TYPE;
    }

    public void index(Contribution contribution, URL url, ValidationContext context) throws InstallException {
        XMLStreamReader reader = null;
        InputStream stream = null;
        try {
            stream = url.openStream();
            reader = xmlFactory.createXMLStreamReader(stream);
            reader.nextTag();
            String name = reader.getAttributeValue(null, "name");
            if (name == null) {
                context.addError(new MissingAttribute("Composite name not specified", "name", reader));
                return;
            }
            Resource resource = new Resource(url, Constants.COMPOSITE_CONTENT_TYPE);
            String targetNamespace = reader.getAttributeValue(null, "targetNamespace");
            QName compositeName = new QName(targetNamespace, name);
            QNameSymbol symbol = new QNameSymbol(compositeName);
            ResourceElement<QNameSymbol, Composite> element = new ResourceElement<QNameSymbol, Composite>(symbol);
            resource.addResourceElement(element);
            contribution.addResource(resource);
        } catch (XMLStreamException e) {
            throw new InstallException(e);
        } catch (IOException e) {
            throw new InstallException(e);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    public void process(URI contributionUri, Resource resource, ValidationContext context, ClassLoader classLoader) throws InstallException {
        URL url = resource.getUrl();
        IntrospectionContext childContext = new DefaultIntrospectionContext(classLoader, contributionUri, url);
        Composite composite;
        try {
            // check to see if the resoruce has already been evaluated
            composite = loader.load(url, Composite.class, childContext);
        } catch (LoaderException e) {
            throw new InstallException(e);
        }
        boolean found = false;
        for (ResourceElement element : resource.getResourceElements()) {
            if (element.getSymbol().getKey().equals(composite.getName())) {
                element.setValue(composite);
                found = true;
                break;
            }
        }
        if (!found) {
            String identifier = composite.getName().toString();
            throw new ResourceElementNotFoundException("Resource element not found: " + identifier, identifier);
        }
        if (childContext.hasErrors()) {
            context.addErrors(childContext.getErrors());
        }
        if (childContext.hasWarnings()) {
            context.addWarnings(childContext.getWarnings());
        }
        resource.setProcessed(true);

    }


}
