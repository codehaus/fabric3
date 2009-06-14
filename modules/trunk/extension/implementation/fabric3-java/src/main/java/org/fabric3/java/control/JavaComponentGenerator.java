  /*
   * Fabric3
   * Copyright (C) 2009 Metaform Systems
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
package org.fabric3.java.control;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.java.provision.JavaComponentDefinition;
import org.fabric3.java.provision.JavaWireSourceDefinition;
import org.fabric3.java.provision.JavaWireTargetDefinition;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.pojo.control.InstanceFactoryGenerationHelper;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.policy.Policy;

/**
 * Generates a JavaComponentDefinition from a ComponentDefinition corresponding to a Java component implementation
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class JavaComponentGenerator implements ComponentGenerator<LogicalComponent<JavaImplementation>> {
    protected final InstanceFactoryGenerationHelper ifHelper;
    private JavaGenerationHelper generationHelper;

    public JavaComponentGenerator(@Reference JavaGenerationHelper generationHelper, @Reference InstanceFactoryGenerationHelper ifHelper) {
        this.generationHelper = generationHelper;
        this.ifHelper = ifHelper;
    }

    public PhysicalComponentDefinition generate(LogicalComponent<JavaImplementation> component) throws GenerationException {
        JavaComponentDefinition physical = new JavaComponentDefinition();
        return generationHelper.generate(component, physical);
    }

    public PhysicalWireSourceDefinition generateWireSource(LogicalComponent<JavaImplementation> source, LogicalReference reference, Policy policy)
            throws GenerationException {
        JavaWireSourceDefinition wireDefinition = new JavaWireSourceDefinition();
        return generationHelper.generateWireSource(source, wireDefinition, reference, policy);
    }

    public PhysicalWireSourceDefinition generateCallbackWireSource(LogicalComponent<JavaImplementation> source,
                                                                   ServiceContract<?> serviceContract,
                                                                   Policy policy) throws GenerationException {
        JavaWireSourceDefinition wireDefinition = new JavaWireSourceDefinition();
        return generationHelper.generateCallbackWireSource(source, wireDefinition, serviceContract, policy);
    }

    public PhysicalWireSourceDefinition generateResourceWireSource(LogicalComponent<JavaImplementation> source, LogicalResource<?> resource)
            throws GenerationException {
        JavaWireSourceDefinition wireDefinition = new JavaWireSourceDefinition();
        return generationHelper.generateResourceWireSource(source, resource, wireDefinition);
    }

    public PhysicalWireTargetDefinition generateWireTarget(LogicalService service, LogicalComponent<JavaImplementation> target, Policy policy)
            throws GenerationException {
        JavaWireTargetDefinition wireDefinition = new JavaWireTargetDefinition();
        return generationHelper.generateWireTarget(service, target, wireDefinition, policy);
    }

}
