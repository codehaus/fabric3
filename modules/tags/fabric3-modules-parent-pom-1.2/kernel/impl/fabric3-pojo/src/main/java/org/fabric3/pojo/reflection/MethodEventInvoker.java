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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.pojo.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Performs an wire on a method of a given instance
 *
 * @version $Rev$ $Date$
 */
public class MethodEventInvoker<T> implements EventInvoker<T> {
    private final Method method;

    /**
     * Intantiates an  invoker for the given method
     *
     * @param method the method to invoke on
     */
    public MethodEventInvoker(Method method) {
        assert method != null;
        this.method = method;
        this.method.setAccessible(true);
    }

    public void invokeEvent(T instance) throws ObjectCallbackException {
        try {
            method.invoke(instance);
        } catch (IllegalArgumentException e) {
            String name = method.toString();
            throw new ObjectCallbackException("Exception thrown by method: " + name, e.getCause());
        } catch (IllegalAccessException e) {
            String name = method.getName();
            throw new AssertionError("Method is not accessible: " + name);
        } catch (InvocationTargetException e) {
            String name = method.getName();
            throw new ObjectCallbackException("Exception thrown by callback method:" + name, e.getCause());
        }
    }

}
