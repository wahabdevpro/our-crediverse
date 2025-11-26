package hxc.ecds.protocol.rest;

import java.math.BigDecimal;
import java.util.List;

public class PartialReversalRequestWithCoAuth extends PartialReversalRequest implements ICoSignable {
	protected String coSignatorySessionID;
	protected String coSignatoryTransactionID;
	protected String coSignatoryOTP;

    @Override
    public String getCoSignatorySessionID() {
        return coSignatorySessionID;
    }

    @Override
    public PartialReversalRequestWithCoAuth setCoSignatorySessionID(String coSignatorySessionID) {
        this.coSignatorySessionID = coSignatorySessionID;
        return this;
    }

    @Override
    public String getCoSignatoryTransactionID() {
        return this.coSignatoryTransactionID;
    }

    @Override
    public PartialReversalRequestWithCoAuth setCoSignatoryTransactionID(String coSignatoryTransactionID) {
        this.coSignatoryTransactionID = coSignatoryTransactionID;
        return this;
    }

    @Override
    public String getCoSignatoryOTP() {
        return this.coSignatoryOTP;
    }

    @Override
    public PartialReversalRequestWithCoAuth setCoSignatoryOTP(String coSignatoryOTP) {
        this.coSignatoryOTP = coSignatoryOTP;
        return this;
    }

    @Override
    public List<Violation> validate() {
        Validator validator = new Validator(super.validate())
                .notEmpty("transactionNumber", transactionNumber, MAX_TRANSACTION_NUMBER_LENGTH)
                .notEmpty("reason", reason, MIN_REASON_LENGTH, MAX_REASON_LENGTH).isMoney("amount", amount)
                .notLess("amount", amount, BigDecimal.ZERO);
        CoSignableUtils.validate(validator, this, true);
        return validator.toList();
    }
}
