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
package org.fabric3.loader.definitions;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.InvalidPrefixException;
import org.fabric3.introspection.xml.LoaderHelper;
import org.fabric3.introspection.xml.TypeLoader;
import org.fabric3.introspection.xml.UnrecognizedAttribute;
import org.fabric3.loader.impl.InvalidQNamePrefix;
import org.fabric3.scdl.definitions.PolicyPhase;
import org.fabric3.scdl.definitions.PolicySet;
import org.fabric3.spi.Namespaces;
import org.fabric3.transform.TransformationException;
import org.fabric3.transform.xml.Stream2Document;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Loader for definitions.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class PolicySetLoader implements TypeLoader<PolicySet> {

    private final LoaderHelper helper;
    private Stream2Document transformer = new Stream2Document();

    public PolicySetLoader(@Reference LoaderHelper helper) {
        this.helper = helper;
    }

    public PolicySet load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        validateAttributes(reader, context);
        Element policyElement;
        try {
            policyElement = transformer.transform(reader, null).getDocumentElement();
        } catch (TransformationException e) {
            DefinitionProcessingFailure failure = new DefinitionProcessingFailure("Error processing policy set", e, reader);
            context.addError(failure);
            return null;
        }

        String name = policyElement.getAttribute("name");
        QName qName = new QName(context.getTargetNamespace(), name);

        Set<QName> provides = new HashSet<QName>();
        StringTokenizer tok = new StringTokenizer(policyElement.getAttribute("provides"));
        while (tok.hasMoreElements()) {
            try {
                provides.add(helper.createQName(tok.nextToken(), reader));
            } catch (InvalidPrefixException e) {
                context.addError(new InvalidQNamePrefix(e.getPrefix(), reader));
                return null;
            }
        }

        String appliesTo = policyElement.getAttribute("appliesTo");

        String sPhase = policyElement.getAttributeNS(Namespaces.POLICY, "phase");
        PolicyPhase phase = null;
        if (sPhase != null && !"".equals(sPhase.trim())) {
            phase = PolicyPhase.valueOf(sPhase);
        } else {
            phase = PolicyPhase.PROVIDED;
        }

        Element extension = null;
        NodeList children = policyElement.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                extension = (Element) children.item(i);
                break;
            }
        }

        return new PolicySet(qName, provides, appliesTo, extension, phase);

    }

    private void validateAttributes(XMLStreamReader reader, IntrospectionContext context) {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (!"name".equals(name) && !"provides".equals(name) && !"appliesTo".equals(name) && !"phase".equals(name)) {
                context.addError(new UnrecognizedAttribute(name, reader));
            }
        }
    }

}
