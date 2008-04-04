package org.fabric3.binding.ws.jaxws.runtime;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import javax.xml.ws.Service;
import javax.xml.namespace.QName;

import org.osoa.sca.ServiceRuntimeException;

import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;

/**
 * @author Copyright (c) 2008 by BEA Systems. All Rights Reserved.
 */
public class WsTargetInterceptor implements Interceptor {

  private final Method method;
  private final Service service;
  private final Class interfazz;
  private Interceptor next;
  private Object reference;
  private final QName name;

  public WsTargetInterceptor(Method method, Service service,
                             Class interfazz,
                             QName name) {
    this.method = method;
    this.service = service;
    this.interfazz = interfazz;
    this.name = name;
  }

  public Interceptor getNext() {
    return next;
  }

  public void setNext(Interceptor next) {
    this.next = next;
  }

  public Message invoke(Message message) {
    Object object = getReference();
    Object[] parameters = (Object[]) message.getBody();
    Message result = new MessageImpl();
    try {
      result.setBody(method.invoke(object, parameters));
    } catch (InvocationTargetException ite) {
      reference = null;
      result.setBodyWithFault(ite.getCause());
    } catch (Exception e) {
      reference = null;
      throw new ServiceRuntimeException(e);
    }
    return result;
  }


  private Object getReference() {
    if (reference == null) {
      reference = service.getPort(name, interfazz);
    }
    return reference;
  }

}
