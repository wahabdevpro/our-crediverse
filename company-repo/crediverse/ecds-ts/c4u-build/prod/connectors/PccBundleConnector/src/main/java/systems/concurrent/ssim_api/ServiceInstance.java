
package systems.concurrent.ssim_api;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ServiceInstance complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ServiceInstance">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;all>
 *         &lt;element name="id" type="{urn:concurrent-systems:ssim-api:1.0:wsdl:1.0}NillableCompoundServiceInstanceId"/>
 *         &lt;element name="chargedSubscriberId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="benefitingSubscriberId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="methods" type="{urn:concurrent-systems:ssim-api:1.0:wsdl:1.0}Methods"/>
 *         &lt;element name="tags" type="{urn:concurrent-systems:ssim-api:1.0:wsdl:1.0}Tags"/>
 *       &lt;/all>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ServiceInstance", propOrder = {

})
public class ServiceInstance {

    @XmlElement(required = true)
    protected NillableCompoundServiceInstanceId id;
    protected String chargedSubscriberId;
    @XmlElement(required = true)
    protected String benefitingSubscriberId;
    @XmlElement(required = true)
    protected Methods methods;
    @XmlElement(required = true)
    protected Tags tags;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link NillableCompoundServiceInstanceId }
     *     
     */
    public NillableCompoundServiceInstanceId getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link NillableCompoundServiceInstanceId }
     *     
     */
    public void setId(NillableCompoundServiceInstanceId value) {
        this.id = value;
    }

    /**
     * Gets the value of the chargedSubscriberId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChargedSubscriberId() {
        return chargedSubscriberId;
    }

    /**
     * Sets the value of the chargedSubscriberId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChargedSubscriberId(String value) {
        this.chargedSubscriberId = value;
    }

    /**
     * Gets the value of the benefitingSubscriberId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBenefitingSubscriberId() {
        return benefitingSubscriberId;
    }

    /**
     * Sets the value of the benefitingSubscriberId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBenefitingSubscriberId(String value) {
        this.benefitingSubscriberId = value;
    }

    /**
     * Gets the value of the methods property.
     * 
     * @return
     *     possible object is
     *     {@link Methods }
     *     
     */
    public Methods getMethods() {
        return methods;
    }

    /**
     * Sets the value of the methods property.
     * 
     * @param value
     *     allowed object is
     *     {@link Methods }
     *     
     */
    public void setMethods(Methods value) {
        this.methods = value;
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
