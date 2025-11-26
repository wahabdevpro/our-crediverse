
package systems.concurrent.ssim_api;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ResponseStatus.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ResponseStatus">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="success"/>
 *     &lt;enumeration value="warning"/>
 *     &lt;enumeration value="rejection"/>
 *     &lt;enumeration value="unchangedStateFailure"/>
 *     &lt;enumeration value="unknownStateFailure"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ResponseStatus")
@XmlEnum
public enum ResponseStatus {

    @XmlEnumValue("success")
    SUCCESS("success"),
    @XmlEnumValue("warning")
    WARNING("warning"),
    @XmlEnumValue("rejection")
    REJECTION("rejection"),
    @XmlEnumValue("unchangedStateFailure")
    UNCHANGED_STATE_FAILURE("unchangedStateFailure"),
    @XmlEnumValue("unknownStateFailure")
    UNKNOWN_STATE_FAILURE("unknownStateFailure");
    private final String value;

    ResponseStatus(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ResponseStatus fromValue(String v) {
        for (ResponseStatus c: ResponseStatus.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
