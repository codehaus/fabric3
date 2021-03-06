package org.fabric3.samples.bigbank.client.ws.loan;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for accept complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="accept">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="arg0" type="{http://loan.api.bigbank.samples.fabric3.org/}optionSelection" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "accept", propOrder = {
        "arg0"
})
public class Accept {

    protected OptionSelection arg0;

    /**
     * Gets the value of the arg0 property.
     *
     * @return possible object is {@link OptionSelection }
     */
    public OptionSelection getArg0() {
        return arg0;
    }

    /**
     * Sets the value of the arg0 property.
     *
     * @param value allowed object is {@link OptionSelection }
     */
    public void setArg0(OptionSelection value) {
        this.arg0 = value;
    }

}
