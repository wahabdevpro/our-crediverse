
package systems.concurrent.ssim_api;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for GetServiceNamesResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetServiceNamesResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="definitionIds" type="{urn:concurrent-systems:ssim-api:1.0:wsdl:1.0}CompoundServiceDefinitionIdSequence"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetServiceNamesResponse", propOrder = {
    "definitionIds"
})
public class GetServiceNamesResponse {

    @XmlElement(required = true)
    protected CompoundServiceDefinitionIdSequence definitionIds;

    /**
     * Gets the value of the definitionIds property.
     * 
     * @return
     *     possible object is
     *     {@link CompoundServiceDefinitionIdSequence }
     *     
     */
    public CompoundServiceDefinitionIdSequence getDefinitionIds() {
        return definitionIds;
    }

    /**
     * Sets the value of the definitionIds property.
     * 
     * @param value
     *     allowed object is
     *     {@link CompoundServiceDefinitionIdSequence }
     *     
     */
    public void setDefinitionIds(CompoundServiceDefinitionIdSequence value) {
        this.definitionIds = value;
    }

}
