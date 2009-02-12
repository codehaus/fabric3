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
package org.fabric3.spi.introspection.java;

import java.lang.annotation.Annotation;

import org.fabric3.model.type.PolicyAware;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * Parses intent and policy set annotations (i.e. annotations marked with @Intent, @Requires and @PolicySet) and updates the model object they are
 * attached to.
 *
 * @version $Revision$ $Date$
 */
public interface PolicyAnnotationProcessor {

    /**
     * Process the annotation.
     *
     * @param annotation  the annotation
     * @param modelObject the model object
     * @param context     the current introspection context.
     */
    void process(Annotation annotation, PolicyAware modelObject, IntrospectionContext context);

}
