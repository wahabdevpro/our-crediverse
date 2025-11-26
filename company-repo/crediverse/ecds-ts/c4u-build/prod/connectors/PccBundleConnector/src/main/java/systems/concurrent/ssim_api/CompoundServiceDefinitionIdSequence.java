
package systems.concurrent.ssim_api;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CompoundServiceDefinitionIdSequence complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CompoundServiceDefinitionIdSequence">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="definitionId" type="{urn:concurrent-systems:ssim-api:1.0:wsdl:1.0}CompoundServiceDefinitionId" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CompoundServiceDefinitionIdSequence", propOrder = {
    "definitionId"
})
public class CompoundServiceDefinitionIdSequence {

    @XmlElement(nillable = true)
    protected List<CompoundServiceDefinitionId> definitionId;

    /**
     * Gets the value of the definitionId property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the definitionId property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDefinitionId().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CompoundServiceDefinitionId }
     * 
     * 
     */
    public List<CompoundServiceDefinitionId> getDefinitionId() {
        if (definitionId == null) {
            definitionId = new ArrayList<CompoundServiceDefinitionId>();
        }
        return this.definitionId;
    }

}
