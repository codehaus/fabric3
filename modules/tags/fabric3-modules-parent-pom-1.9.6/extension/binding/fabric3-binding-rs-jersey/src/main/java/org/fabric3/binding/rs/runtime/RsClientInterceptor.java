/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.binding.rs.runtime;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.oasisopen.sca.ServiceRuntimeException;

import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.wire.Interceptor;

/**
 * @version $Rev$ $Date$
 */
public class RsClientInterceptor implements Interceptor {
    private RsClientResponse response;

    public RsClientInterceptor(String operName, Class<?> interfaze, URI uri, Class<?>... classes) throws Exception {
        response = createResponseConfiguration(uri, interfaze, operName, classes);
    }

    public Message invoke(Message m) {
        Object[] args = (Object[]) m.getBody();
        MessageImpl result;
        try {
            result = new MessageImpl();
            result.setBody(response.build(args));
        } catch (RuntimeException e) {
            throw new ServiceRuntimeException(e);
        }
        return result;
    }

    public void setNext(Interceptor interceptor) {
        throw new IllegalStateException("This interceptor must be the last one in an target interceptor chain");
    }

    public Interceptor getNext() {
        return null;
    }

    private RsClientResponse createResponseConfiguration(URI uri, Class<?> interfaze, String operation, Class<?>... args) throws Exception {
        Method m = interfaze.getMethod(operation, args);
        RsClientResponse cfg = new RsClientResponse(m.getReturnType(), uri);
        cfg = cfg.
                // Class level
                withPath(interfaze.getAnnotation(Path.class)).
                withProduces(interfaze.getAnnotation(Produces.class)).
                withConsumes(interfaze.getAnnotation(Consumes.class)).
                // Method level overriding
                withAction(m.getAnnotation(PUT.class)).
                withAction(m.getAnnotation(POST.class)).
                withAction(m.getAnnotation(GET.class)).
                withPath(m.getAnnotation(Path.class)).
                withProduces(m.getAnnotation(Produces.class)).
                withConsumes(m.getAnnotation(Consumes.class))
        ;
        Annotation[][] parameterAnnotations = m.getParameterAnnotations();
        for (int i = 0; i < parameterAnnotations.length; i++) {
            cfg.withParam(i, parameterAnnotations[i]);
        }
        return cfg;
    }

}
