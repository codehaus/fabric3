
package org.fabric3.samples.bigbank.client.ws.loan;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.6 in JDK 6
 * Generated source version: 2.1
 * 
 */
@WebService(name = "LoanService", targetNamespace = "http://loan.api.bigbank.samples.fabric3.org/")
@XmlSeeAlso({
    ObjectFactory.class
})
public interface LoanService {


    /**
     * 
     * @param arg0
     */
    @WebMethod
    @RequestWrapper(localName = "accept", targetNamespace = "http://loan.api.bigbank.samples.fabric3.org/", className = "org.fabric3.samples.bigbank.client.ws.loan.Accept")
    @ResponseWrapper(localName = "acceptResponse", targetNamespace = "http://loan.api.bigbank.samples.fabric3.org/", className = "org.fabric3.samples.bigbank.client.ws.loan.AcceptResponse")
    public void accept(
        @WebParam(name = "arg0", targetNamespace = "")
        OptionSelection arg0);

    /**
     * 
     * @param arg0
     * @return
     *     returns org.fabric3.samples.bigbank.client.ws.loan.LoanApplication
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "retrieve", targetNamespace = "http://loan.api.bigbank.samples.fabric3.org/", className = "org.fabric3.samples.bigbank.client.ws.loan.Retrieve")
    @ResponseWrapper(localName = "retrieveResponse", targetNamespace = "http://loan.api.bigbank.samples.fabric3.org/", className = "org.fabric3.samples.bigbank.client.ws.loan.RetrieveResponse")
    public LoanApplication retrieve(
        @WebParam(name = "arg0", targetNamespace = "")
        long arg0);

    /**
     * 
     * @param arg0
     * @return
     *     returns long
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "apply", targetNamespace = "http://loan.api.bigbank.samples.fabric3.org/", className = "org.fabric3.samples.bigbank.client.ws.loan.Apply")
    @ResponseWrapper(localName = "applyResponse", targetNamespace = "http://loan.api.bigbank.samples.fabric3.org/", className = "org.fabric3.samples.bigbank.client.ws.loan.ApplyResponse")
    public long apply(
        @WebParam(name = "arg0", targetNamespace = "")
        LoanRequest arg0);

    /**
     * 
     * @param arg0
     */
    @WebMethod
    @RequestWrapper(localName = "decline", targetNamespace = "http://loan.api.bigbank.samples.fabric3.org/", className = "org.fabric3.samples.bigbank.client.ws.loan.Decline")
    @ResponseWrapper(localName = "declineResponse", targetNamespace = "http://loan.api.bigbank.samples.fabric3.org/", className = "org.fabric3.samples.bigbank.client.ws.loan.DeclineResponse")
    public void decline(
        @WebParam(name = "arg0", targetNamespace = "")
        long arg0);

}
