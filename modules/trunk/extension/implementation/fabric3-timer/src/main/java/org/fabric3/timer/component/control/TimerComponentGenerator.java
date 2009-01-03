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
package org.fabric3.timer.component.control;

import javax.xml.namespace.QName;

import org.osoa.sca.Constants;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.java.control.JavaGenerationHelper;
import org.fabric3.java.provision.JavaWireSourceDefinition;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.policy.Policy;
import org.fabric3.timer.component.provision.TimerComponentDefinition;
import org.fabric3.timer.component.provision.TriggerData;
import org.fabric3.timer.component.model.TimerImplementation;

/**
 * Generates a TimerComponentDefinition from a ComponentDefinition corresponding to a timer component implementation
 *
 * @version $Rev: 4833 $ $Date: 2008-06-20 03:41:57 -0700 (Fri, 20 Jun 2008) $
 */
@EagerInit
public class TimerComponentGenerator implements ComponentGenerator<LogicalComponent<TimerImplementation>> {
    private static final QName MANAGED_TRANSACTION = new QName(Constants.SCA_NS, "managedTransaction");
    private final GeneratorRegistry registry;
    private JavaGenerationHelper generationHelper;

    public TimerComponentGenerator(@Reference GeneratorRegistry registry, @Reference JavaGenerationHelper generationHelper) {
        this.registry = registry;
        this.generationHelper = generationHelper;
    }

    @Init
    public void init() {
        registry.register(TimerImplementation.class, this);
    }

    public PhysicalComponentDefinition generate(LogicalComponent<TimerImplementation> component) throws GenerationException {
        TimerComponentDefinition physical = new TimerComponentDefinition();
        generationHelper.generate(component, physical);
        TimerImplementation implementation = component.getDefinition().getImplementation();
        physical.setTransactional(implementation.getIntents().contains(MANAGED_TRANSACTION));
        TriggerData data = implementation.getTriggerData();
        physical.setTriggerData(data);
        return physical;
    }

    public PhysicalWireSourceDefinition generateWireSource(LogicalComponent<TimerImplementation> source, LogicalReference reference, Policy policy)
            throws GenerationException {
        JavaWireSourceDefinition wireDefinition = new JavaWireSourceDefinition();
        return generationHelper.generateWireSource(source, wireDefinition, reference, policy);
    }

    public PhysicalWireSourceDefinition generateCallbackWireSource(LogicalComponent<TimerImplementation> source,
                                                                   ServiceContract<?> serviceContract,
                                                                   Policy policy) throws GenerationException {
        JavaWireSourceDefinition wireDefinition = new JavaWireSourceDefinition();
        return generationHelper.generateCallbackWireSource(source, wireDefinition, serviceContract, policy);
    }

    public PhysicalWireSourceDefinition generateResourceWireSource(LogicalComponent<TimerImplementation> source, LogicalResource<?> resource)
            throws GenerationException {
        JavaWireSourceDefinition wireDefinition = new JavaWireSourceDefinition();
        return generationHelper.generateResourceWireSource(source, resource, wireDefinition);
    }

    public PhysicalWireTargetDefinition generateWireTarget(LogicalService service, LogicalComponent<TimerImplementation> target, Policy policy)
            throws GenerationException {
        throw new UnsupportedOperationException("Cannot wire to timer components");
    }
}