
package systems.concurrent.ssim_api;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for StopAutoLifeCycleEventsRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="StopAutoLifeCycleEventsRequest">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;all>
 *         &lt;element name="chargedSubscriberId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="benefitingSubscriberId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="serviceInstanceId" type="{urn:concurrent-systems:ssim-api:1.0:wsdl:1.0}CompoundServiceInstanceId"/>
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
@XmlType(name = "StopAutoLifeCycleEventsRequest", propOrder = {

})
public class StopAutoLifeCycleEventsRequest {

    @XmlElement(required = true)
    protected String chargedSubscriberId;
    @XmlElement(required = true)
    protected String benefitingSubscriberId;
    @XmlElement(required = true)
    protected CompoundServiceInstanceId serviceInstanceId;
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
     * Gets the value of the serviceInstanceId property.
     * 
     * @return
     *     possible object is
     *     {@link CompoundServiceInstanceId }
     *     
     */
    public CompoundServiceInstanceId getServiceInstanceId() {
        return serviceInstanceId;
    }

    /**
     * Sets the value of the serviceInstanceId property.
     * 
     * @param value
     *     allowed object is
     *     {@link CompoundServiceInstanceId }
     *     
     */
    public void setServiceInstanceId(CompoundServiceInstanceId value) {
        this.serviceInstanceId = value;
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
