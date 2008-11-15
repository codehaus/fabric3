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

import org.fabric3.scdl.ValidationFailure;
import org.fabric3.scdl.ConstructorInjectionSite;
import org.fabric3.scdl.FieldInjectionSite;
import org.fabric3.scdl.InjectableAttributeType;
import org.fabric3.scdl.InjectionSite;
import org.fabric3.scdl.MethodInjectionSite;

/**
 * Denotes an unknown InjectableAttributeType.
 *
 * @version $Rev$ $Date$
 */
public class UnknownInjectionType extends ValidationFailure<InjectionSite> {
    private InjectableAttributeType type;
    private String clazz;

    public UnknownInjectionType(InjectionSite site, InjectableAttributeType type, String clazz) {
        super(site);
        this.type = type;
        this.clazz = clazz;
    }

    public String getImplementationClass() {
        return clazz;
    }

    public String getMessage() {
        InjectionSite site = getValidatable();
        if (site instanceof FieldInjectionSite) {
            FieldInjectionSite field = (FieldInjectionSite) site;
            return "Unknow injection type " + type + " on field " + field.getName() + " in class " + clazz;
        } else if (site instanceof MethodInjectionSite) {
            MethodInjectionSite method = (MethodInjectionSite) site;
            return "Unknow injection type " + type + " on method " + method.getSignature() + " in class " + clazz;
        } else if (site instanceof ConstructorInjectionSite) {
            ConstructorInjectionSite ctor = (ConstructorInjectionSite) site;
            return "Unknow injection type " + type + " on constructor " + ctor.getSignature() + " in class " + clazz;
        } else {
            return "Unknow injection type " + type + " found in class " + clazz;
        }
    }
}
