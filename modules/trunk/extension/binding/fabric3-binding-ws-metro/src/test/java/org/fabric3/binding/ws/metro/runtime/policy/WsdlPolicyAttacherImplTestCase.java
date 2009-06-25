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
package org.fabric3.binding.ws.metro.runtime.policy;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;

import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.model.AbstractSEIModelImpl;
import com.sun.xml.ws.model.RuntimeModeler;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.wsdl.parser.RuntimeWSDLParser;
import com.sun.xml.ws.wsdl.writer.WSDLGenerator;
import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @version $Rev$ $Date$
 */
public class WsdlPolicyAttacherImplTestCase extends TestCase {

    public void testAttach() throws Exception {
        // Verifies a policy expression is properly attached to a WSDL operation definition.
        // The WSDL artifacts are generated from a Java type and the policy expression is attached to the abstract WSDL.
        QName serviceQName = new QName("http://policy.runtime.metro.ws.binding.fabric3.org/", "HelloWorldService");
        RuntimeModeler modeler = new RuntimeModeler(HelloWorldPortType.class, serviceQName, BindingID.SOAP12_HTTP);
        AbstractSEIModelImpl model = modeler.buildRuntimeModel();
        String packageName = HelloWorldPortType.class.getPackage().getName();
        WsdlFileResolver wsdlResolver = new WsdlFileResolver(packageName, new File("."), false);
        WSBinding binding = BindingImpl.create(BindingID.SOAP12_HTTP);
        WSDLGenerator generator = new WSDLGenerator(model, wsdlResolver, binding, null, HelloWorldPortType.class);
        generator.doGeneration();

        DocumentBuilderFactory DOCUMENT_FACTORY = DocumentBuilderFactory.newInstance();
        DOCUMENT_FACTORY.setNamespaceAware(true);
        DocumentBuilder builder = DOCUMENT_FACTORY.newDocumentBuilder();
        ByteArrayInputStream bas = new ByteArrayInputStream(TestPolicy.POLICY.getBytes());
        Document document = builder.parse(bas);
        WsdlPolicyAttacherImpl attacher = new WsdlPolicyAttacherImpl();
//        File portTypeFile = new File(packageName + "." + "HelloWorldPortTypeService.wsdl");
        List<String> operationNames = new ArrayList<String>();
        operationNames.add("sayHello");
        attacher.attach(wsdlResolver.getConcreteWsdl(), operationNames, document.getDocumentElement());

        URL wsdlLocation = new File(packageName + "." + "HelloWorldService.wsdl").toURI().toURL();
        InputStream stream = wsdlLocation.openStream();
        StreamSource source = new StreamSource(stream);

        NullResolver entityResolver = new NullResolver();
        WSDLModel generatedModel = RuntimeWSDLParser.parse(wsdlLocation, source, entityResolver, false, null);

        QName serviceName = new QName("http://policy.runtime.metro.ws.binding.fabric3.org/", "HelloWorldService");
        QName portName = new QName("http://policy.runtime.metro.ws.binding.fabric3.org/", "HelloWorldPortTypePort");
        QName operationName = new QName("http://policy.runtime.metro.ws.binding.fabric3.org/", "sayHello");
        PolicyMapKey key = PolicyMap.createWsdlOperationScopeKey(serviceName, portName, operationName);

        Policy policy = generatedModel.getPolicyMap().getOperationEffectivePolicy(key);
        assertEquals(TestPolicy.POLICY_ID, policy.getId());
    }

    private static class NullResolver implements EntityResolver {

        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            return null;
        }
    }

}
