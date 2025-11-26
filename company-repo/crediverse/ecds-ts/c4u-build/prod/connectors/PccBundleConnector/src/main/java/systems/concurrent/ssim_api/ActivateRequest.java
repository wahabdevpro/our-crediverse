
package systems.concurrent.ssim_api;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ActivateRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ActivateRequest">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;all>
 *         &lt;element name="chargedSubscriberId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="benefitingSubscriberId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="serviceDefinitionId" type="{urn:concurrent-systems:ssim-api:1.0:wsdl:1.0}CompoundServiceDefinitionId"/>
 *         &lt;element name="options" type="{urn:concurrent-systems:ssim-api:1.0:wsdl:1.0}Tags" minOccurs="0"/>
 *       &lt;/all>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ActivateRequest", propOrder = {

})
public class ActivateRequest {

    @XmlElement(required = true)
    protected String chargedSubscriberId;
    @XmlElement(required = true)
    protected String benefitingSubscriberId;
    @XmlElement(required = true)
    protected CompoundServiceDefinitionId serviceDefinitionId;
    protected Tags options;

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
     * Gets the value of the serviceDefinitionId property.
     * 
     * @return
     *     possible object is
     *     {@link CompoundServiceDefinitionId }
     *     
     */
    public CompoundServiceDefinitionId getServiceDefinitionId() {
        return serviceDefinitionId;
    }

    /**
     * Sets the value of the serviceDefinitionId property.
     * 
     * @param value
     *     allowed object is
     *     {@link CompoundServiceDefinitionId }
     *     
     */
    public void setServiceDefinitionId(CompoundServiceDefinitionId value) {
        this.serviceDefinitionId = value;
    }

    /**
     * Gets the value of the options property.
     * 
     * @return
     *     possible object is
     *     {@link Tags }
     *     
     */
    public Tags getOptions() {
        return options;
    }

    /**
     * Sets the value of the options property.
     * 
     * @param value
     *     allowed object is
     *     {@link Tags }
     *     
     */
    public void setOptions(Tags value) {
        this.options = value;
    }

}
