/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.fabric.builder.transform;

import org.oasisopen.sca.ServiceRuntimeException;

import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.transform.TransformationException;
import org.fabric3.spi.transform.Transformer;
import org.fabric3.spi.wire.Interceptor;

/**
 * Converts the input parameters of an invocation to a target format and the output parameters from the target format by delegating to underlying
 * transformers.
 *
 * @version $Rev$ $Date$
 */
public class TransformerInterceptor implements Interceptor {
    private Transformer<Object, Object> inTransformer;
    private Transformer<Object, Object> outTransformer;
    private ClassLoader inLoader;
    private ClassLoader outLoader;
    private Interceptor next;

    /**
     * Constructor.
     *
     * @param inTransformer  the input parameter transformer
     * @param outTransformer the output parameter transformer
     * @param inLoader       the input parameter classloader, i.e. the target service contribution classloader
     * @param outLoader      the output parameter classloader, i.e. the source component contribution classloader
     */
    public TransformerInterceptor(Transformer<Object, Object> inTransformer,
                                  Transformer<Object, Object> outTransformer,
                                  ClassLoader inLoader,
                                  ClassLoader outLoader) {
        this.inTransformer = inTransformer;
        this.outTransformer = outTransformer;
        this.inLoader = inLoader;
        this.outLoader = outLoader;
    }

    public Message invoke(Message msg) {
        transformInput(msg);
        Message ret = next.invoke(msg);
        return transformOutput(ret);
    }

    private void transformInput(Message msg) {
        Object params = msg.getBody();
        // TODO handle null types
        if (params != null) {
            try {
                // Operations take 0..n parameters. A single parameter must be unwrapped from the invocation array and passed to a transformer.
                // In contrast, multiple parameter operations are passed as an array to the transformer.
                if (params.getClass().isArray() && ((Object[]) params).length == 1) {
                    Object[] paramArray = (Object[]) params;
                    paramArray[0] = inTransformer.transform(paramArray[0], inLoader);
                } else {
                    // multiple parameters - pass the entire array to transform
                    Object transformed = inTransformer.transform(params, inLoader);
                    msg.setBody(transformed);
                }
            } catch (TransformationException e) {
                throw new ServiceRuntimeException(e);
            }
        }
    }

    private Message transformOutput(Message ret) {
        // FIXME For exception transformation, if it is checked convert as application fault
        Object body = ret.getBody();
        // TODO handle null types
        if (body != null) {
            try {
                Object transformed = outTransformer.transform(body, outLoader);
                if (ret.isFault()) {
                    ret.setBodyWithFault(transformed);
                } else {
                    ret.setBody(transformed);
                }
            } catch (ClassCastException e) {
                // an unexpected type was returned by the target service or an interceptor later in the chain. This is an error in the extension or
                // interceptor and not user code since errors should be trapped and returned in the format expected by the transformer
                if (body instanceof Throwable) {
                    throw new ServiceRuntimeException("Unexpected exception returned", (Throwable) body);
                } else {
                    throw new ServiceRuntimeException("Unexpected type returned: " + body.getClass());
                }
            } catch (TransformationException e) {
                throw new ServiceRuntimeException(e);
            }
        }
        return ret;
    }

    public void setNext(Interceptor next) {
        this.next = next;
    }

    public Interceptor getNext() {
        return next;
    }

}
