
package systems.concurrent.ssim_api;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for GetServiceInstancesRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetServiceInstancesRequest">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;all>
 *         &lt;element name="subscriberId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="partialInstanceId" type="{urn:concurrent-systems:ssim-api:1.0:wsdl:1.0}PartialCompoundServiceInstanceId"/>
 *         &lt;element name="requiredTags" type="{urn:concurrent-systems:ssim-api:1.0:wsdl:1.0}Tags"/>
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
@XmlType(name = "GetServiceInstancesRequest", propOrder = {

})
public class GetServiceInstancesRequest {

    @XmlElement(required = true)
    protected String subscriberId;
    @XmlElement(required = true)
    protected PartialCompoundServiceInstanceId partialInstanceId;
    @XmlElement(required = true)
    protected Tags requiredTags;
    protected Tags options;

    /**
     * Gets the value of the subscriberId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSubscriberId() {
        return subscriberId;
    }

    /**
     * Sets the value of the subscriberId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSubscriberId(String value) {
        this.subscriberId = value;
    }

    /**
     * Gets the value of the partialInstanceId property.
     * 
     * @return
     *     possible object is
     *     {@link PartialCompoundServiceInstanceId }
     *     
     */
    public PartialCompoundServiceInstanceId getPartialInstanceId() {
        return partialInstanceId;
    }

    /**
     * Sets the value of the partialInstanceId property.
     * 
     * @param value
     *     allowed object is
     *     {@link PartialCompoundServiceInstanceId }
     *     
     */
    public void setPartialInstanceId(PartialCompoundServiceInstanceId value) {
        this.partialInstanceId = value;
    }

    /**
     * Gets the value of the requiredTags property.
     * 
     * @return
     *     possible object is
     *     {@link Tags }
     *     
     */
    public Tags getRequiredTags() {
        return requiredTags;
    }

    /**
     * Sets the value of the requiredTags property.
     * 
     * @param value
     *     allowed object is
     *     {@link Tags }
     *     
     */
    public void setRequiredTags(Tags value) {
        this.requiredTags = value;
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
