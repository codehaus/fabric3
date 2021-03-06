package org.fabric3.tests.wsdl.matching.singleelementsimpletype;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.7-hudson-48-
 * Generated source version: 2.1
 * 
 */
@WebService(name = "HelloWorldPortType", targetNamespace = "urn:helloworld:sest")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
@XmlSeeAlso({
    ObjectFactory.class
})
public interface HelloWorldPortType {


    /**
     * 
     * @param name
     * @return
     *     returns java.lang.String
     */
    @WebMethod
    @WebResult(name = "sayHelloResponse", targetNamespace = "urn:helloworld", partName = "result")
    public String sayHello(
        @WebParam(name = "sayHelloRequest", targetNamespace = "urn:helloworld", partName = "name")
        String name);

}
