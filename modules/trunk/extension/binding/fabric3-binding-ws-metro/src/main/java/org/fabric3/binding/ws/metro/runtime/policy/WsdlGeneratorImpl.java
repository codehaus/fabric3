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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.model.AbstractSEIModelImpl;
import com.sun.xml.ws.model.RuntimeModeler;
import com.sun.xml.ws.wsdl.writer.WSDLGenerator;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.runtime.HostInfo;

/**
 * default implementation of WsdlGenerator.
 *
 * @version $Rev$ $Date$
 */
public class WsdlGeneratorImpl implements WsdlGenerator {
    private File tempDir;

    public WsdlGeneratorImpl(@Reference HostInfo info) {
        tempDir = new File(info.getTempDir(), "wsdl");
        tempDir.mkdir();
        tempDir.deleteOnExit();
    }


    public GeneratedArtifacts generate(Class<?> seiClass, QName serviceQName, boolean client) throws WsdlGenerationException {
        RuntimeModeler modeler = new RuntimeModeler(seiClass, serviceQName, BindingID.SOAP11_HTTP);
        AbstractSEIModelImpl model = modeler.buildRuntimeModel();
        String packageName = seiClass.getPackage().getName();
        WsdlFileResolver wsdlResolver = new WsdlFileResolver(packageName, tempDir, client);
        // only support SOAP 1.1
        WSBinding binding = BindingImpl.create(BindingID.SOAP11_HTTP);
        WSDLGenerator generator = new WSDLGenerator(model, wsdlResolver, binding, null, seiClass);

        // generate the WSDL and schemas
        generator.doGeneration();

        // resolve the generated XSD files
        // TODO support multiple schema artifacts
        List<File> schemas = new ArrayList<File>();
        schemas.add(wsdlResolver.getSchema());

        return new GeneratedArtifacts(wsdlResolver.getConcreteWsdl(), schemas);
    }
}
