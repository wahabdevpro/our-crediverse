package hxc.ecds.protocol.rest.non_airtime;

public class RefundRequest extends Request {
    private String debitCrediverseTransactionId;
    private String debitClientTransactionId;
	private String originChannel;

    public String getDebitCrediverseTransactionId() {
        return debitCrediverseTransactionId;
    }

    public void setDebitCrediverseTransactionId(String debitCrediverseTransactionId) {
        this.debitCrediverseTransactionId = debitCrediverseTransactionId;
    }

    public String getDebitClientTransactionId() {
        return debitClientTransactionId;
    }

    public void setDebitClientTransactionId(String debitClientTransactionId) {
        this.debitClientTransactionId = debitClientTransactionId;
    }

	public String getOriginChannel() {
        return originChannel;
    }

    public void setOriginChannel(String originChannel) {
        this.originChannel = originChannel;
    }

}
