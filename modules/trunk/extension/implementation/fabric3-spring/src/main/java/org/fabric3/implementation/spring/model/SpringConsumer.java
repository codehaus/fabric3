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
package org.fabric3.implementation.spring.model;

import java.util.Collections;

import org.fabric3.model.type.component.ConsumerDefinition;
import org.fabric3.model.type.contract.DataType;
import org.fabric3.spi.model.type.java.JavaType;

/**
 * An SCA consumer definition in a Spring application context.
 *
 * @version $Rev$ $Date$
 */
public class SpringConsumer extends ConsumerDefinition {
    private static final long serialVersionUID = 204519855340684340L;
    private String beanName;
    private JavaType<?> type;
    private String methodName;

    /**
     * Constructor.
     *
     * @param name       the consumer name
     * @param type       the consumer type
     * @param targetBean the target bean name to dispatch events to
     * @param methodName the target method name on the bean to dispatch events to
     */
    public SpringConsumer(String name, JavaType<?> type, String targetBean, String methodName) {
        super(name, Collections.<DataType<?>>singletonList(type));
        this.type = type;
        this.beanName = targetBean;
        this.methodName = methodName;
    }

    /**
     * Returns the bean name to dispatch events to.
     *
     * @return the bean name
     */
    public String getBeanName() {
        return beanName;
    }

    /**
     * Returns the name of the bean method to dispatch events to.
     *
     * @return the bean method name
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Returns the event type
     *
     * @return the event type
     */
    public JavaType<?> getType() {
        return type;
    }
}
