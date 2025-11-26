package hxc.ecds.protocol.rest;

import java.io.Serializable;
import java.math.BigDecimal;

public class DedicatedAccountInfo implements Serializable {

    private String dedicatedAccountID;
    private String dedicatedAccountUnitType;
    private BigDecimal amount;
    private String offerID;
    private String expiryDateExtended;

    public DedicatedAccountInfo() {
    }

    public DedicatedAccountInfo(String dedicatedAccountID, String dedicatedAccountUnitType, BigDecimal amount) {
        this.dedicatedAccountID = dedicatedAccountID;
        this.dedicatedAccountUnitType = dedicatedAccountUnitType;
        this.amount = amount;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal refillAmount1) {
        this.amount = refillAmount1;
    }

    public String getOfferID() {
        return offerID;
    }

    public void setOfferID(String offerID) {
        this.offerID = offerID;
    }

    public String getExpiryDateExtended() {
        return expiryDateExtended;
    }

    public void setExpiryDateExtended(String expiryDateExtended) {
        this.expiryDateExtended = expiryDateExtended;
    }
}
