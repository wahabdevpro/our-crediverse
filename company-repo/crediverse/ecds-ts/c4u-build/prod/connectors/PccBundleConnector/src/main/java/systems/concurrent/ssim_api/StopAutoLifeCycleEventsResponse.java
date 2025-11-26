
package systems.concurrent.ssim_api;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for StopAutoLifeCycleEventsResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="StopAutoLifeCycleEventsResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="serviceInstance" type="{urn:concurrent-systems:ssim-api:1.0:wsdl:1.0}ServiceInstance"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StopAutoLifeCycleEventsResponse", propOrder = {
    "serviceInstance"
})
public class StopAutoLifeCycleEventsResponse {

    @XmlElement(required = true)
    protected ServiceInstance serviceInstance;

    /**
     * Gets the value of the serviceInstance property.
     * 
     * @return
     *     possible object is
     *     {@link ServiceInstance }
     *     
     */
    public ServiceInstance getServiceInstance() {
        return serviceInstance;
    }

    /**
     * Sets the value of the serviceInstance property.
     * 
     * @param value
     *     allowed object is
     *     {@link ServiceInstance }
     *     
     */
    public void setServiceInstance(ServiceInstance value) {
        this.serviceInstance = value;
    }

}
