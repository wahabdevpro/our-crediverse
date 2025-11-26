
package systems.concurrent.ssim_api;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Method complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Method">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;all>
 *         &lt;element name="id" type="{urn:concurrent-systems:ssim-api:1.0:wsdl:1.0}MethodId"/>
 *         &lt;element name="charge" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="disallowed" type="{urn:concurrent-systems:ssim-api:1.0:wsdl:1.0}Disallowed" minOccurs="0"/>
 *         &lt;element name="tags" type="{urn:concurrent-systems:ssim-api:1.0:wsdl:1.0}Tags" minOccurs="0"/>
 *       &lt;/all>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Method", propOrder = {

})
public class Method {

    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected MethodId id;
    protected Long charge;
    protected Disallowed disallowed;
    protected Tags tags;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link MethodId }
     *     
     */
    public MethodId getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link MethodId }
     *     
     */
    public void setId(MethodId value) {
        this.id = value;
    }

    /**
     * Gets the value of the charge property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getCharge() {
        return charge;
    }

    /**
     * Sets the value of the charge property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setCharge(Long value) {
        this.charge = value;
    }

    /**
     * Gets the value of the disallowed property.
     * 
     * @return
     *     possible object is
     *     {@link Disallowed }
     *     
     */
    public Disallowed getDisallowed() {
        return disallowed;
    }

    /**
     * Sets the value of the disallowed property.
     * 
     * @param value
     *     allowed object is
     *     {@link Disallowed }
     *     
     */
    public void setDisallowed(Disallowed value) {
        this.disallowed = value;
    }

    /**
     * Gets the value of the tags property.
     * 
     * @return
     *     possible object is
     *     {@link Tags }
     *     
     */
    public Tags getTags() {
        return tags;
    }

    /**
     * Sets the value of the tags property.
     * 
     * @param value
     *     allowed object is
     *     {@link Tags }
     *     
     */
    public void setTags(Tags value) {
        this.tags = value;
    }

}
