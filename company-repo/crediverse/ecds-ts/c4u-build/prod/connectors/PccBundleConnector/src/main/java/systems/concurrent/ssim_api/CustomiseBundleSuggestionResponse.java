
package systems.concurrent.ssim_api;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CustomiseBundleSuggestionResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CustomiseBundleSuggestionResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="serviceInstance" type="{urn:concurrent-systems:ssim-api:1.0:wsdl:1.0}ServiceInstance" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CustomiseBundleSuggestionResponse", propOrder = {
    "serviceInstance"
})
public class CustomiseBundleSuggestionResponse {

    @XmlElement(nillable = true)
    protected List<ServiceInstance> serviceInstance;

    /**
     * Gets the value of the serviceInstance property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the serviceInstance property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getServiceInstance().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ServiceInstance }
     * 
     * 
     */
    public List<ServiceInstance> getServiceInstance() {
        if (serviceInstance == null) {
            serviceInstance = new ArrayList<ServiceInstance>();
        }
        return this.serviceInstance;
    }

}
