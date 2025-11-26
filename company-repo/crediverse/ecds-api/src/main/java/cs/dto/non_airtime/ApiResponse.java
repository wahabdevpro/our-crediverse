package cs.dto.non_airtime;

import hxc.ecds.protocol.rest.non_airtime.Response;

public class ApiResponse {
    private String clientTransactionId;
    private String crediverseTransactionId;
    private long transactionEndTimestamp;
    private String status;
    
    public ApiResponse(){}
    
    public ApiResponse(Response response){
        this.clientTransactionId = response.getClientTransactionId();
        this.crediverseTransactionId = response.getCrediverseTransactionId();
        this.transactionEndTimestamp = response.getTransactionEndTimestamp();
        this.status = response.getStatus();
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
