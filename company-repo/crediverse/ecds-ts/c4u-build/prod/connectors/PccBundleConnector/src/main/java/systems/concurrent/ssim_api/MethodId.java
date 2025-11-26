
package systems.concurrent.ssim_api;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MethodId.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="MethodId">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="activate"/>
 *     &lt;enumeration value="deactivate"/>
 *     &lt;enumeration value="getServiceInstances"/>
 *     &lt;enumeration value="subscribe"/>
 *     &lt;enumeration value="unsubscribe"/>
 *     &lt;enumeration value="migrate"/>
 *     &lt;enumeration value="extendValidity"/>
 *     &lt;enumeration value="topUpBenefits"/>
 *     &lt;enumeration value="stopAutoLifeCycleEvents"/>
 *     &lt;enumeration value="startAutoLifeCycleEvents"/>
 *     &lt;enumeration value="getBundleSuggestions"/>
 *     &lt;enumeration value="customiseBundleSuggestion"/>
 *     &lt;enumeration value="purchaseBundle"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "MethodId")
@XmlEnum
public enum MethodId {

    @XmlEnumValue("activate")
    ACTIVATE("activate"),
    @XmlEnumValue("deactivate")
    DEACTIVATE("deactivate"),
    @XmlEnumValue("getServiceInstances")
    GET_SERVICE_INSTANCES("getServiceInstances"),
    @XmlEnumValue("subscribe")
    SUBSCRIBE("subscribe"),
    @XmlEnumValue("unsubscribe")
    UNSUBSCRIBE("unsubscribe"),
    @XmlEnumValue("migrate")
    MIGRATE("migrate"),
    @XmlEnumValue("extendValidity")
    EXTEND_VALIDITY("extendValidity"),
    @XmlEnumValue("topUpBenefits")
    TOP_UP_BENEFITS("topUpBenefits"),
    @XmlEnumValue("stopAutoLifeCycleEvents")
    STOP_AUTO_LIFE_CYCLE_EVENTS("stopAutoLifeCycleEvents"),
    @XmlEnumValue("startAutoLifeCycleEvents")
    START_AUTO_LIFE_CYCLE_EVENTS("startAutoLifeCycleEvents"),
    @XmlEnumValue("getBundleSuggestions")
    GET_BUNDLE_SUGGESTIONS("getBundleSuggestions"),
    @XmlEnumValue("customiseBundleSuggestion")
    CUSTOMISE_BUNDLE_SUGGESTION("customiseBundleSuggestion"),
    @XmlEnumValue("purchaseBundle")
    PURCHASE_BUNDLE("purchaseBundle");
    private final String value;

    MethodId(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static MethodId fromValue(String v) {
        for (MethodId c: MethodId.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
