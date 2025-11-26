package hxc.ecds.protocol.rest;

import java.math.BigDecimal;
import java.util.Date;

public class ExistingAgentInfo {
    private int agentId;
    private String agentState;
    private Date activationDate;
    private BigDecimal balance;
    private BigDecimal bonusBalance;
    private BigDecimal onHoldBalance;
    private Date lastTransactionDate;

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getBonusBalance() {
        return bonusBalance;
    }

    public void setBonusBalance(BigDecimal bonusBalance) {
        this.bonusBalance = bonusBalance;
    }

    public BigDecimal getOnHoldBalance() {
        return onHoldBalance;
    }

    public void setOnHoldBalance(BigDecimal onHoldBalance) {
        this.onHoldBalance = onHoldBalance;
    }

    public int getAgentId() {
        return agentId;
    }

    public void setAgentId(int agentId) {
        this.agentId = agentId;
    }

    public String getAgentState() {
        return agentState;
    }

    public void setAgentState(String agentState) {
        this.agentState = agentState;
    }

    public Date getActivationDate() {
        return activationDate;
    }

    public void setActivationDate(Date activationDate) {
        this.activationDate = activationDate;
    }

    public Date getLastTransactionDate() {
        return lastTransactionDate;
    }

    public void setLastTransactionDate(Date lastTransactionDate) {
        this.lastTransactionDate = lastTransactionDate;
    }
}
