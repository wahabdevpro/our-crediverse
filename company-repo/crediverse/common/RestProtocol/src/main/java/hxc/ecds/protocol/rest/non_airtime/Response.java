package hxc.ecds.protocol.rest.non_airtime;

import hxc.ecds.protocol.rest.TransactionResponse;

public class Response extends TransactionResponse {
    private String clientTransactionId;
    private String crediverseTransactionId;
    private long transactionEndTimestamp;
    private String status;

    public Response() {}

    public Response(Request request) {
		super(request);
		this.clientTransactionId = request.getClientTransactionId();
    }

    public String getClientTransactionId() {
        return clientTransactionId;
    }

    public void setClientTransactionId(String clientTransactionId) {
        this.clientTransactionId = clientTransactionId;
    }

    public String getCrediverseTransactionId() {
        return crediverseTransactionId;
    }

    public void setCrediverseTransactionId(String crediverseTransactionId) {
        this.crediverseTransactionId = crediverseTransactionId;
    }

    public long getTransactionEndTimestamp() {
        return transactionEndTimestamp;
    }

    public void setTransactionEndTimestamp(long transactionEndTimestamp) {
        this.transactionEndTimestamp = transactionEndTimestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
