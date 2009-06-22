package org.fabric3.binding.ws.metro.runtime.policy;

import javax.jws.WebService;

/**
 * @version $Rev$ $Date$
 */
@WebService
public class HelloWorldPortType {

    public String sayHello(String name) {
        return "hello";
    }
}
