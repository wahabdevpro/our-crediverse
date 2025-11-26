package hxc.ecds.protocol.rest.non_airtime;

import java.math.BigDecimal;

import hxc.ecds.protocol.rest.TransactionRequest;

public abstract class Request extends TransactionRequest {
    private String msisdn;
    private String clientTransactionId;
    private String consumerMsisdn;
    private String itemDescription;
    private BigDecimal amount;
    private Long expiryTimeInMillisecondsSinceUnixEpoch;
    private String imsi;

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getClientTransactionId() {
        return clientTransactionId;
    }

    public void setClientTransactionId(String clientTransactionId) {
        this.clientTransactionId = clientTransactionId;
    }

    public String getConsumerMsisdn() {
        return consumerMsisdn;
    }

    public void setConsumerMsisdn(String consumerMsisdn) {
        this.consumerMsisdn = consumerMsisdn;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getExpiryTimeInMillisecondsSinceUnixEpoch() {
        return expiryTimeInMillisecondsSinceUnixEpoch;
    }

    public void setExpiryTimeInMillisecondsSinceUnixEpoch(Long expiryTimeInMillisecondsSinceUnixEpoch) {
        this.expiryTimeInMillisecondsSinceUnixEpoch = expiryTimeInMillisecondsSinceUnixEpoch;
    }

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    @Override
    public Response createResponse() {
        return new Response(this);
    }
}
