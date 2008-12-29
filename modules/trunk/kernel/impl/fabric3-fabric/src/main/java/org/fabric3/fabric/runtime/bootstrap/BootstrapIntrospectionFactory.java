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
package org.fabric3.fabric.runtime.bootstrap;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.introspection.impl.DefaultClassWalker;
import org.fabric3.introspection.impl.DefaultIntrospectionHelper;
import org.fabric3.introspection.impl.annotation.DestroyProcessor;
import org.fabric3.introspection.impl.annotation.EagerInitProcessor;
import org.fabric3.introspection.impl.annotation.InitProcessor;
import org.fabric3.fabric.monitor.MonitorProcessor;
import org.fabric3.introspection.impl.annotation.PropertyProcessor;
import org.fabric3.introspection.impl.annotation.ReferenceProcessor;
import org.fabric3.introspection.impl.contract.DefaultContractProcessor;
import org.fabric3.spi.introspection.IntrospectionHelper;
import org.fabric3.spi.introspection.contract.ContractProcessor;
import org.fabric3.spi.introspection.java.AnnotationProcessor;
import org.fabric3.spi.introspection.java.ClassWalker;
import org.fabric3.spi.introspection.java.ImplementationProcessor;
import org.fabric3.system.scdl.SystemImplementation;
import org.fabric3.system.introspection.SystemServiceHeuristic;
import org.fabric3.system.introspection.SystemConstructorHeuristic;
import org.fabric3.system.introspection.SystemUnannotatedHeuristic;
import org.fabric3.system.introspection.SystemHeuristic;
import org.fabric3.system.introspection.SystemImplementationProcessorImpl;

/**
 * Instantiates an ImplementationProcessor for introspecting system components. System components are composite-scoped and support the standard SCA
 * lifecycle, including @Init, @Destroy, and @EagerInit.
 *
 * @version $Rev$ $Date$
 */
public class BootstrapIntrospectionFactory {

    /**
     * Returns a new ImplementationProcessor for system components.
     *
     * @return a new ImplementationProcessor for system components
     */
    public static ImplementationProcessor<SystemImplementation> createSystemImplementationProcessor() {
        IntrospectionHelper helper = new DefaultIntrospectionHelper();
        ContractProcessor contractProcessor = new DefaultContractProcessor(helper);

        Map<Class<? extends Annotation>, AnnotationProcessor<? extends Annotation, SystemImplementation>> processors =
                new HashMap<Class<? extends Annotation>, AnnotationProcessor<? extends Annotation, SystemImplementation>>();

        // no constructor processor is needed as that is handled by heuristics
        processors.put(Property.class, new PropertyProcessor<SystemImplementation>(helper));
        processors.put(Reference.class, new ReferenceProcessor<SystemImplementation>(contractProcessor, helper));
        processors.put(EagerInit.class, new EagerInitProcessor<SystemImplementation>());
        processors.put(Init.class, new InitProcessor<SystemImplementation>());
        processors.put(Destroy.class, new DestroyProcessor<SystemImplementation>());
        processors.put(Monitor.class, new MonitorProcessor<SystemImplementation>(helper, contractProcessor));

        ClassWalker<SystemImplementation> classWalker = new DefaultClassWalker<SystemImplementation>(processors);

        // heuristics for system components
        SystemServiceHeuristic serviceHeuristic = new SystemServiceHeuristic(contractProcessor, helper);
        SystemConstructorHeuristic constructorHeuristic = new SystemConstructorHeuristic();
        SystemUnannotatedHeuristic unannotatedHeuristic = new SystemUnannotatedHeuristic(helper, contractProcessor);
        SystemHeuristic systemHeuristic = new SystemHeuristic(serviceHeuristic, constructorHeuristic, unannotatedHeuristic);

        return new SystemImplementationProcessorImpl(classWalker, systemHeuristic, helper);
    }

}