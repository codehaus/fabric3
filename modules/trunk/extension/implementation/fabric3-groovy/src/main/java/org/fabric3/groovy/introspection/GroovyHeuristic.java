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
package org.fabric3.groovy.introspection;

import org.fabric3.groovy.scdl.GroovyImplementation;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.HeuristicProcessor;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.model.type.java.Signature;

/**
 * @version $Rev$ $Date$
 */
public class GroovyHeuristic implements HeuristicProcessor<GroovyImplementation> {

    public void applyHeuristics(GroovyImplementation implementation, Class<?> implClass, IntrospectionContext context) {

        PojoComponentType componentType = implementation.getComponentType();

        if (componentType.getConstructor() == null) {
            try {
                componentType.setConstructor(new Signature(implClass.getConstructor()));
            } catch (NoSuchMethodException e) {
                throw new AssertionError();
            }
        }
    }
}
