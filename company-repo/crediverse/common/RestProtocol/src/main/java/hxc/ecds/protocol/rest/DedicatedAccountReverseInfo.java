package hxc.ecds.protocol.rest;

import java.io.Serializable;
import java.math.BigDecimal;

public class DedicatedAccountReverseInfo implements Serializable {

    private String dedicatedAccountID;
    private String dedicatedAccountUnitType;
    private BigDecimal reverseAmount;

    public DedicatedAccountReverseInfo() {
    }

    public DedicatedAccountReverseInfo(String dedicatedAccountID, String dedicatedAccountUnitType, BigDecimal reverseAmount) {
        this.dedicatedAccountID = dedicatedAccountID;
        this.dedicatedAccountUnitType = dedicatedAccountUnitType;
        this.reverseAmount = reverseAmount;
    }

    public String getDedicatedAccountID() {
        return dedicatedAccountID;
    }

    public void setDedicatedAccountID(String dedicatedAccountID) {
        this.dedicatedAccountID = dedicatedAccountID;
    }

    public String getDedicatedAccountUnitType() {
        return dedicatedAccountUnitType;
    }

    public void setDedicatedAccountUnitType(String dedicatedAccountUnitType) {
        this.dedicatedAccountUnitType = dedicatedAccountUnitType;
    }

    public BigDecimal getReverseAmount() {
        return reverseAmount;
    }

    public void setReverseAmount(BigDecimal reverseAmount) {
        this.reverseAmount = reverseAmount;
    }
}
