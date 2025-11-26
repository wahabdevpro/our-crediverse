
package systems.concurrent.ssim_api;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Disallowed complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Disallowed">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="reasons" type="{urn:concurrent-systems:ssim-api:1.0:wsdl:1.0}DisallowedReasons"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Disallowed", propOrder = {
    "reasons"
})
public class Disallowed {

    @XmlElement(required = true)
    protected DisallowedReasons reasons;

    /**
     * Gets the value of the reasons property.
     * 
     * @return
     *     possible object is
     *     {@link DisallowedReasons }
     *     
     */
    public DisallowedReasons getReasons() {
        return reasons;
    }

    /**
     * Sets the value of the reasons property.
     * 
     * @param value
     *     allowed object is
     *     {@link DisallowedReasons }
     *     
     */
    public void setReasons(DisallowedReasons value) {
        this.reasons = value;
    }

}
