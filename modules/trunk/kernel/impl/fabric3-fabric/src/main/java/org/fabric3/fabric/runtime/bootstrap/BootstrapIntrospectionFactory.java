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
import org.fabric3.fabric.monitor.MonitorProcessor;
import org.fabric3.introspection.impl.DefaultClassWalker;
import org.fabric3.introspection.impl.DefaultIntrospectionHelper;
import org.fabric3.introspection.impl.annotation.DestroyProcessor;
import org.fabric3.introspection.impl.annotation.EagerInitProcessor;
import org.fabric3.introspection.impl.annotation.InitProcessor;
import org.fabric3.introspection.impl.annotation.OASISDestroyProcessor;
import org.fabric3.introspection.impl.annotation.OASISEagerInitProcessor;
import org.fabric3.introspection.impl.annotation.OASISInitProcessor;
import org.fabric3.introspection.impl.annotation.OASISPropertyProcessor;
import org.fabric3.introspection.impl.annotation.OASISReferenceProcessor;
import org.fabric3.introspection.impl.annotation.PropertyProcessor;
import org.fabric3.introspection.impl.annotation.ReferenceProcessor;
import org.fabric3.introspection.impl.contract.DefaultContractProcessor;
import org.fabric3.spi.introspection.IntrospectionHelper;
import org.fabric3.spi.introspection.contract.ContractProcessor;
import org.fabric3.spi.introspection.java.AnnotationProcessor;
import org.fabric3.spi.introspection.java.ClassWalker;
import org.fabric3.spi.introspection.java.ImplementationProcessor;
import org.fabric3.system.introspection.SystemConstructorHeuristic;
import org.fabric3.system.introspection.SystemHeuristic;
import org.fabric3.system.introspection.SystemImplementationProcessorImpl;
import org.fabric3.system.introspection.SystemServiceHeuristic;
import org.fabric3.system.introspection.SystemUnannotatedHeuristic;
import org.fabric3.system.scdl.SystemImplementation;

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

        // OSOA annotations
        // no constructor processor is needed as that is handled by heuristics
        processors.put(Property.class, new PropertyProcessor<SystemImplementation>(helper));
        processors.put(Reference.class, new ReferenceProcessor<SystemImplementation>(contractProcessor, helper));
        processors.put(EagerInit.class, new EagerInitProcessor<SystemImplementation>());
        processors.put(Init.class, new InitProcessor<SystemImplementation>());
        processors.put(Destroy.class, new DestroyProcessor<SystemImplementation>());

        // OASIS annotations
        processors.put(org.oasisopen.sca.annotation.Property.class, new OASISPropertyProcessor<SystemImplementation>(helper));
        processors.put(org.oasisopen.sca.annotation.Reference.class, new OASISReferenceProcessor<SystemImplementation>(contractProcessor, helper));
        processors.put(org.oasisopen.sca.annotation.EagerInit.class, new OASISEagerInitProcessor<SystemImplementation>());
        processors.put(org.oasisopen.sca.annotation.Init.class, new OASISInitProcessor<SystemImplementation>());
        processors.put(org.oasisopen.sca.annotation.Destroy.class, new OASISDestroyProcessor<SystemImplementation>());

        // F3 annotations
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