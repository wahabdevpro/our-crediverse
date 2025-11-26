package hxc.ecds.protocol.rest;

import java.io.Serializable;
import java.math.BigDecimal;

public class DedicatedAccountRefillInfo implements Serializable {

    private String dedicatedAccountID;
    private String dedicatedAccountUnitType;
    private BigDecimal refillAmount1;
    private String offerID;
    private String expiryDateExtended;

    public DedicatedAccountRefillInfo() {
    }

    public DedicatedAccountRefillInfo(String dedicatedAccountID, String dedicatedAccountUnitType, BigDecimal refillAmount1) {
        this.dedicatedAccountID = dedicatedAccountID;
        this.dedicatedAccountUnitType = dedicatedAccountUnitType;
        this.refillAmount1 = refillAmount1;
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

    public BigDecimal getRefillAmount1() {
        return refillAmount1;
    }

    public void setRefillAmount1(BigDecimal refillAmount1) {
        this.refillAmount1 = refillAmount1;
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
