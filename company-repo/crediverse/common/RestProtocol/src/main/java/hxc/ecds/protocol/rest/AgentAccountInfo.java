package hxc.ecds.protocol.rest;


import java.math.BigDecimal;

public class AgentAccountInfo /*implements IValidatable*/{

    protected Integer agentId;
    protected String accountNumber;
    protected String firstName;
    protected String lastName;
    protected String mobileNumber;
    protected BigDecimal balance;
    protected BigDecimal bonusBalance;
    protected BigDecimal onHoldBalance;
    protected String state;
    /*
     *	Functionality on hold MSISDN-RECYCLING - uncomment when re-instated
     */
    //protected boolean msisdnRecycled;
    /*
     *	Functionality on hold MSISDN-RECYCLING - uncomment when re-instated
     */
    //protected boolean recyclable;
    protected boolean matched;

    public Integer getAgentId() {
        return agentId;
    }

    public void setAgentId(Integer agentId) {
        this.agentId = agentId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean isMatched() {
        return matched;
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
    }

    /*
     *	Functionality on hold MSISDN-RECYCLING - uncomment when re-instated
     */
   /* public boolean isRecyclable() {
        return recyclable;
    }

    public void setRecyclable(boolean recyclable) {
        this.recyclable = recyclable;
    }*/

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    /*
     *	Functionality on hold MSISDN-RECYCLING - uncomment when re-instated
     */
    /*public boolean isMsisdnRecycled() {
        return msisdnRecycled;
    }*/

    /*
     *	Functionality on hold MSISDN-RECYCLING - uncomment when re-instated
     */
    /*public void setMsisdnRecycled(boolean msisdnRecycled) {
        this.msisdnRecycled = msisdnRecycled;
    }*/

    /*@Override
    public List<Violation> validate() {
        return new Validator().toList();*/ //
    //}
}
