package hxc.ecds.protocol.rest;

public class TransactionServerResponse {
    private boolean error;
    private String message = "";

    public TransactionServerResponse() {
    }

    public static TransactionServerResponse error(String message) {
        TransactionServerResponse response = new TransactionServerResponse();
        response.setError(true);
        response.setMessage(message);
        return response;
    }

    public static TransactionServerResponse ok() {
        return new TransactionServerResponse();
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
