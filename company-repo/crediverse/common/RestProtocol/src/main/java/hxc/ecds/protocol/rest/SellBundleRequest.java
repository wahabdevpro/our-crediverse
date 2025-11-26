package hxc.ecds.protocol.rest;

import java.util.List;

// REST End-Point: ~/transactions/sell_bundle
/*
 * Deprecated
 */
public class SellBundleRequest extends TransactionRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String targetMSISDN;
	private int bundleID;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public String getTargetMSISDN()
	{
		return targetMSISDN;
	}

	public SellBundleRequest setTargetMSISDN(String targetMSISDN)
	{
		this.targetMSISDN = targetMSISDN;
		return this;
	}

	public int getBundleID()
	{
		return bundleID;
	}

	public SellBundleRequest setBundleID(int bundleID)
	{
		this.bundleID = bundleID;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// TransactionRequest
	//
	// /////////////////////////////////
	@Override
	public List<Violation> validate()
	{
		return new Validator(super.validate()) //
				.notEmpty("targetMSISDN", targetMSISDN, MSISDN_MAX_LENGTH) //
				.notLess("bundleID", bundleID, 1) //
				.toList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public SellBundleResponse createResponse()
	{
		return new SellBundleResponse(this);
	}

}
