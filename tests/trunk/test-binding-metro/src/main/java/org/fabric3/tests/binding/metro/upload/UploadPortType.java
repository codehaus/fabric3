
package org.fabric3.tests.binding.metro.upload;

import javax.activation.DataHandler;
import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;


/**
 * This class was generated by the JAXWS SI.
 * JAX-WS RI 2.0_03-b24-fcs
 * Generated source version: 2.0
 * 
 */
@WebService(name = "UploadPortType", targetNamespace = "urn:upload")
public interface UploadPortType {


    /**
     * 
     * @param data
     * @param name
     */
    @WebMethod
    @Oneway
    @RequestWrapper(localName = "upload", targetNamespace = "urn:upload", className = "upload.UploadType")
    public void upload(
        @WebParam(name = "name", targetNamespace = "urn:upload")
        String name,
        @WebParam(name = "data", targetNamespace = "urn:upload")
        DataHandler data);

}
