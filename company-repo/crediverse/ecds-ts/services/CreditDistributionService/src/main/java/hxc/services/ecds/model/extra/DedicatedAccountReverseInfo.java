package hxc.services.ecds.model.extra;

import java.math.BigDecimal;

public class DedicatedAccountReverseInfo {

    private String dedicatedAccountID;
    private String dedicatedAccountUnitType;
    private BigDecimal reversalAmount;


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

    public BigDecimal getReversalAmount() {
        return reversalAmount;
    }

    public void setReversalAmount(BigDecimal reversalAmount) {
        this.reversalAmount = reversalAmount;
    }
}
