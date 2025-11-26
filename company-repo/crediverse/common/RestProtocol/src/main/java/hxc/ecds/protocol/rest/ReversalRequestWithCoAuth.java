package hxc.ecds.protocol.rest;

import java.util.List;

public class ReversalRequestWithCoAuth extends ReversalRequest implements ICoSignable {
    protected String coSignatorySessionID;
    protected String coSignatoryTransactionID;
    protected String coSignatoryOTP;

    @Override
    public String getCoSignatorySessionID() {
        return coSignatorySessionID;
    }

    @Override
    public ReversalRequestWithCoAuth setCoSignatorySessionID(String coSignatorySessionID) {
        this.coSignatorySessionID = coSignatorySessionID;
        return this;
    }

    @Override
    public String getCoSignatoryTransactionID() {
        return this.coSignatoryTransactionID;
    }

    @Override
    public ReversalRequestWithCoAuth setCoSignatoryTransactionID(String coSignatoryTransactionID) {
        this.coSignatoryTransactionID = coSignatoryTransactionID;
        return this;
    }

    @Override
    public String getCoSignatoryOTP() {
        return this.coSignatoryOTP;
    }

    public ReversalRequestWithCoAuth setCoSignatoryOTP(String coSignatoryOTP) {
        this.coSignatoryOTP = coSignatoryOTP;
        return this;
    }

    @Override
    public List<Violation> validate() {
        Validator validator = new Validator(super.validate())
                .notEmpty("transactionNumber", transactionNumber, MAX_TRANSACTION_NUMBER_LENGTH)
                .notEmpty("reason", reason, MIN_REASON_LENGTH, MAX_REASON_LENGTH);
        CoSignableUtils.validate(validator, this, true);
        return validator.toList();
    }
}
