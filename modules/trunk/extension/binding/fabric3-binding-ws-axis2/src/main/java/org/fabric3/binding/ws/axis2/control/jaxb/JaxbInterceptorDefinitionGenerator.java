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
package org.fabric3.binding.ws.axis2.control.jaxb;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.ws.WebFault;

import org.osoa.sca.annotations.EagerInit;
import org.w3c.dom.Element;

import org.fabric3.binding.ws.axis2.provision.jaxb.JaxbInterceptorDefinition;
import org.fabric3.model.type.service.DataType;
import org.fabric3.model.type.service.Operation;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.InterceptorDefinitionGenerator;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalService;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
public class JaxbInterceptorDefinitionGenerator implements InterceptorDefinitionGenerator {

    public JaxbInterceptorDefinition generate(Element policySet, Operation<?> operation, LogicalBinding<?> logicalBinding)
            throws GenerationException {

        boolean service = logicalBinding.getParent() instanceof LogicalService;

        // This assumes a Java interface contract

        List<? extends DataType<?>> inputTypes = operation.getInputType().getLogical();
        List<? extends DataType<?>> faultTypes = operation.getFaultTypes();
        DataType<?> outputType = operation.getOutputType();

        Set<String> classNames = new HashSet<String>(inputTypes.size() + 1);

        // parameter types
        for (DataType<?> inputType : inputTypes) {
            String className = ((Class<?>) inputType.getPhysical()).getName();
            classNames.add(className);
        }

        // fault types
        Set<String> faultNames = new HashSet<String>(faultTypes.size());
        for (DataType<?> faultType : faultTypes) {
            Class<?> webFaultClass = (Class<?>) faultType.getPhysical();

            // in JAX-WS, the fault class is a wrapper for the fault message
            // the actual fault is returned by the getFaultInfo() method
            if (!webFaultClass.isAnnotationPresent(WebFault.class)) {
                throw new InvalidWebFaultException(webFaultClass.getName());
            }
            Method getFaultInfo;
            try {
                getFaultInfo = webFaultClass.getMethod("getFaultInfo");
            } catch (NoSuchMethodException e) {
                throw new MissingFaultInfoException(webFaultClass.getName());
            }
            Class<?> faultClass = getFaultInfo.getReturnType();

            faultNames.add(webFaultClass.getName());
            classNames.add(faultClass.getName());
        }

        // return type
        classNames.add(((Class<?>) outputType.getPhysical()).getName());

        return new JaxbInterceptorDefinition(classNames, faultNames, service);

    }

}
