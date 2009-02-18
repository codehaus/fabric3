package loanapp.appraisal;

import loanapp.api.message.Address;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @version $Revision$ $Date$
 */
//@XmlRootElement
public class AppraisalRequest {
    private long id;
    private Address address;

    public AppraisalRequest() {
    }

    public AppraisalRequest(long id, Address address) {
        this.id = id;
        this.address = address;
    }

    public long getId() {
        return id;
    }

    public Address getAddress() {
        return address;
    }
}
