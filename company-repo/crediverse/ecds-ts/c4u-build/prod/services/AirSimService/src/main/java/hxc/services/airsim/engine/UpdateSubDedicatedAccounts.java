package hxc.services.airsim.engine;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import hxc.services.airsim.InjectedResponse;
import hxc.services.airsim.SupportedRequest;
import hxc.services.airsim.model.SubscriberEx;
import hxc.services.airsim.protocol.DedicatedAccount;
import hxc.services.airsim.protocol.SubDedicatedAccountInformation;
import hxc.utils.protocol.acip.DedicatedAccountChangeInformation;
import hxc.utils.protocol.acip.SubDedicatedAccountUpdateInformation;
import hxc.utils.protocol.acip.UpdateSubDedicatedAccountsRequest;
import hxc.utils.protocol.acip.UpdateSubDedicatedAccountsResponse;

// 0	- Success
// 100	- Other Error
// 102	- Subscriber not found
// 104	- Temporary blocked
// 105	- Dedicated account not allowed
// 106	- Dedicated account negative
// 123	- Max credit limit exceeded
// 124	- Below minimum balance
// 126	- Account not active
// 136	- Date adjustment error
// 139	- Dedicated account not defined
// 153	- Dedicated account max credit limit exceeded
// 163	- Invalid dedicated account period
// 164	- Invalid dedicated account start date
// 167	- Invalid unit type
// 213	- Sub dedicated account not defined
// 226	- Invalid PAM Period Relative Dates Start PAM Period Indicator
// 227	- Invalid PAM Period Relative Dates Expiry PAM Period Indicator
// 230	- Not allowed to convert to other type of lifetime
// 257	- Operation not allowed since End of Provisioning is set
// 260	- Capability not available
// 999	- Other Error No Retry

public class UpdateSubDedicatedAccounts extends SupportedRequest<UpdateSubDedicatedAccountsRequest, UpdateSubDedicatedAccountsResponse>
{
	public UpdateSubDedicatedAccounts()
	{
		super(UpdateSubDedicatedAccountsRequest.class);
	}

	@Override
	protected UpdateSubDedicatedAccountsResponse execute(UpdateSubDedicatedAccountsRequest request, InjectedResponse injectedResponse)
	{
		// Create Response
		UpdateSubDedicatedAccountsResponse response = new UpdateSubDedicatedAccountsResponse();
		if (!validate(request.member, response.member, injectedResponse))
			return response;

		// Get the Subscriber
		SubscriberEx subscriber = getSubscriber(request.member);
		if (subscriber == null)
		{
			response.member.setResponseCode(102);
			return response;
		}
		if (subscriber.getTemporaryBlockedFlag() != null && subscriber.getTemporaryBlockedFlag())
			return exitWith(response, response.member, 104);

		// Affect the Changes

		SubDedicatedAccountUpdateInformation[] updates = request.member.getSubDedicatedAccountUpdateInformation();
		DedicatedAccountChangeInformation[] changes = null;

		if (updates != null)
		{
			changes = new DedicatedAccountChangeInformation[updates.length];

			int index = 0;
			for (SubDedicatedAccountUpdateInformation update : updates)
			{
				// Get the Dedicated Account
				DedicatedAccount da = subscriber.getDedicatedAccounts().get(update.dedicatedAccountID);
				if (da == null)
				{
					response.member.setResponseCode(139);
					return response;
				}

				// Get the sub-dedicated account
				SubDedicatedAccountInformation targetSubDA = null;
				SubDedicatedAccountInformation[] subDAs = da.getSubDedicatedAccountInformation();
				if (subDAs != null)
				{
					for (SubDedicatedAccountInformation subDA : subDAs)
					{
						if (datesEqualWithoutTimezone(update.subDedicatedAccountIdentifier.startDateCurrent, subDA.getStartDate()))
						{
							targetSubDA = subDA;
							break;
						}
					}
				}

				// Exit if not found
				if (targetSubDA == null)
				{
					response.member.setResponseCode(213);
					return response;
				}

				// Update the Sub-DA
				targetSubDA.setStartDate(super.getDate(targetSubDA.getStartDate(), update.startDate, update.adjustmentStartDateRelative));
				targetSubDA.setExpiryDate(super.getDate(targetSubDA.getExpiryDate(), update.expiryDate, update.adjustmentDateRelative));
				targetSubDA.setDedicatedAccountValue1(super.getValue(targetSubDA.getDedicatedAccountValue1(), update.subDedicatedAccountValueAbsolute, update.adjustmentAmountRelative));

				// Create DA Update Info
				DedicatedAccountChangeInformation change = new DedicatedAccountChangeInformation();
				change.dedicatedAccountID = da.getDedicatedAccountID();
				change.productID = da.getProductID();
				change.dedicatedAccountValue1 = da.getDedicatedAccountValue1();
				change.dedicatedAccountValue2 = da.getDedicatedAccountValue2();
				change.expiryDate = da.getExpiryDate();
				change.startDate = da.getStartDate();
				change.pamServiceID = da.getPamServiceID();
				change.offerID = da.getOfferID();
				change.dedicatedAccountRealMoneyFlag = da.getDedicatedAccountRealMoneyFlag();
				change.closestExpiryDate = da.getClosestExpiryDate();
				change.closestExpiryValue1 = da.getClosestExpiryValue1();
				change.closestExpiryValue2 = da.getClosestExpiryValue2();
				change.closestAccessibleDate = da.getClosestAccessibleDate();
				change.closestAccessibleValue1 = da.getClosestAccessibleValue1();
				change.closestAccessibleValue2 = da.getClosestAccessibleValue2();
				change.dedicatedAccountActiveValue1 = da.getDedicatedAccountActiveValue1();
				change.dedicatedAccountActiveValue2 = da.getDedicatedAccountActiveValue2();
				change.dedicatedAccountUnitType = da.getDedicatedAccountUnitType();
				changes[index++] = change;
			}
		}

		// Create Response
		response.member.currency1 = subscriber.getCurrency1();
		response.member.currency2 = subscriber.getCurrency2();
		response.member.dedicatedAccountChangeInformation = changes;

		return response;
	}

	private boolean datesEqualWithoutTimezone(Date date1, Date date2)
	{
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();

		cal1.setTime(date1);
		cal2.setTime(date2);

		checkTimeZone(date1, cal1);
		checkTimeZone(date2, cal2);

		if (cal1.get(Calendar.DATE) == cal2.get(Calendar.DATE) && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
				&& cal1.get(Calendar.HOUR) == cal2.get(Calendar.HOUR) && cal1.get(Calendar.MINUTE) == cal2.get(Calendar.MINUTE) && cal1.get(Calendar.SECOND) == cal2.get(Calendar.SECOND)
				&& cal1.get(Calendar.MILLISECOND) == cal2.get(Calendar.MILLISECOND))
		{
			return true;
		}

		return false;
	}

	private void checkTimeZone(Date date, Calendar cal)
	{

		SimpleDateFormat sdf = new SimpleDateFormat("Z");
		int relativeAmount = 0;
		try
		{
			relativeAmount = Integer.parseInt(sdf.format(date)) / 100;
		}
		catch (Exception e)
		{

		}

		cal.add(Calendar.HOUR, relativeAmount);

	}

}
