package loanapp.message;

/**
 * @version $Revision$ $Date$
 */
public class PropertyLocation {
    private Address address;
    private double value;

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
