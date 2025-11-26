package hxc.services.airsim.engine;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import hxc.services.airsim.InjectedResponse;
import hxc.services.airsim.SupportedRequest;
import hxc.services.airsim.model.SubscriberEx;
import hxc.services.airsim.protocol.DedicatedAccount;
import hxc.services.airsim.protocol.SubDedicatedAccountInformation;
import hxc.utils.protocol.ucip.DedicatedAccountChangeInformation;
import hxc.utils.protocol.ucip.DedicatedAccountUpdateInformation;
import hxc.utils.protocol.ucip.SubDedicatedAccountChangeInformation;
import hxc.utils.protocol.ucip.UpdateBalanceAndDateRequest;
import hxc.utils.protocol.ucip.UpdateBalanceAndDateResponse;

public class UpdateBalanceAndDate extends SupportedRequest<UpdateBalanceAndDateRequest, UpdateBalanceAndDateResponse>
{

	public UpdateBalanceAndDate()
	{
		super(UpdateBalanceAndDateRequest.class);
	}

	@Override
	protected UpdateBalanceAndDateResponse execute(UpdateBalanceAndDateRequest request, InjectedResponse injectedResponse) throws Exception
	{
		// Create Response
		UpdateBalanceAndDateResponse response = new UpdateBalanceAndDateResponse();
		if (!validate(request.member, response.member, injectedResponse))
			return response;

		// Get the Subscriber
		SubscriberEx subscriber = getSubscriber(request.member);
		if (subscriber == null)
		{
			response.member.setResponseCode(102);
			return response;
		}

		// Effect Changes
		if (request.member.getAdjustmentAmountRelative() != null)
		{
			Long oldValue = subscriber.getAccountValue1();
			if (oldValue == null)
				oldValue = 0L;

			long amount = oldValue + request.member.getAdjustmentAmountRelative();
			if (amount < 0L)
			{
				response.member.setResponseCode(123);
				return response;
			}
			try
			{
				subscriber.setAccountValue1(amount);
			}
			catch (Exception e)
			{
				throw (e);
			}
			subscriber.setAccountValue2(amount);
		}

		if (request.member.getMainAccountValueNew() != null)
		{
			try
			{
				subscriber.setAccountValue1(request.member.getMainAccountValueNew());
			}
			catch (Exception e)
			{
				throw (e);
			}
			subscriber.setAccountValue2(request.member.getMainAccountValueNew());
		}

		if (request.member.getSupervisionExpiryDateRelative() != null)
			subscriber.setSupervisionExpiryDate(addDays(subscriber.getSupervisionExpiryDate(), request.member.getSupervisionExpiryDateRelative()));

		if (request.member.getSupervisionExpiryDate() != null)
			subscriber.setSupervisionExpiryDate(request.member.getSupervisionExpiryDate());

		if (request.member.getServiceFeeExpiryDateRelative() != null)
			subscriber.setServiceFeeExpiryDate(addDays(subscriber.getSupervisionExpiryDate(), request.member.getServiceFeeExpiryDateRelative()));

		if (request.member.getServiceFeeExpiryDate() != null)
			subscriber.setServiceFeeExpiryDate(request.member.getServiceFeeExpiryDate());

		if (request.member.getCreditClearancePeriod() != null)
			subscriber.setCreditClearancePeriod(request.member.getCreditClearancePeriod());

		if (request.member.getServiceRemovalPeriod() != null)
			subscriber.setServiceRemovalPeriod(request.member.getServiceRemovalPeriod());

		if (request.member.getServiceFeeExpiryDateCurrent() != null)
			subscriber.setServiceFeeExpiryDate(pseudoNull(request.member.getServiceFeeExpiryDateCurrent()));

		if (request.member.getSupervisionExpiryDateCurrent() != null)
			subscriber.setSupervisionExpiryDate(pseudoNull(request.member.getSupervisionExpiryDateCurrent()));

		if (request.member.getServiceClassCurrent() != null)
			subscriber.setServiceClassCurrent(request.member.getServiceClassCurrent());

		if (request.member.getCellIdentifier() != null)
			subscriber.setCellIdentifier(request.member.getCellIdentifier());

		// Update Dedicated Accounts
		DedicatedAccountUpdateInformation[] daUpdates = request.member.dedicatedAccountUpdateInformation;
		DedicatedAccountChangeInformation[] dedicatedAccountChangeInformation = null;
		if (daUpdates != null && daUpdates.length > 0)
		{
			int index = 0;
			dedicatedAccountChangeInformation = new DedicatedAccountChangeInformation[daUpdates.length];
			for (DedicatedAccountUpdateInformation daUpdate : daUpdates)
			{
				DedicatedAccount da = subscriber.getDedicatedAccounts().get(daUpdate.dedicatedAccountID);
				if (da == null)
				{
					da = new DedicatedAccount();
					da.setDedicatedAccountID(daUpdate.dedicatedAccountID);
					subscriber.getDedicatedAccounts().put(da.getDedicatedAccountID(), da);
					da.setDedicatedAccountUnitType(daUpdate.dedicatedAccountUnitType);
				}

				// Test Unit Type
				if (da.getDedicatedAccountUnitType() != null && daUpdate.dedicatedAccountUnitType != null && da.getDedicatedAccountUnitType().intValue() != daUpdate.dedicatedAccountUnitType)
				{
					response.member.setResponseCode(167);
					return response;
				}

				// Update Values
				Long newValue = super.getValue(da.getDedicatedAccountValue1(), daUpdate.dedicatedAccountValueNew, daUpdate.adjustmentAmountRelative);
				if (newValue != null && newValue < 0L)
				{
					response.member.setResponseCode(106);
					return response;
				}
				da.setDedicatedAccountValue1(newValue);
				da.setDedicatedAccountValue2(da.getDedicatedAccountValue1());
				da.setExpiryDate(super.getDate(da.getExpiryDate(), daUpdate.expiryDate, daUpdate.adjustmentDateRelative));
				da.setStartDate(super.getDate(da.getStartDate(), daUpdate.startDate, daUpdate.adjustmentStartDateRelative));

				DedicatedAccountChangeInformation daChange = new DedicatedAccountChangeInformation();
				daChange.dedicatedAccountID = da.getDedicatedAccountID();
				daChange.productID = da.getProductID();
				daChange.dedicatedAccountValue1 = da.getDedicatedAccountValue1();
				daChange.dedicatedAccountValue2 = da.getDedicatedAccountValue2();
				daChange.expiryDate = da.getExpiryDate();
				daChange.startDate = da.getStartDate();
				daChange.pamServiceID = da.getPamServiceID();
				daChange.offerID = da.getOfferID();
				daChange.dedicatedAccountRealMoneyFlag = da.getDedicatedAccountRealMoneyFlag();
				daChange.closestExpiryDate = da.getClosestExpiryDate();
				daChange.closestExpiryValue1 = da.getClosestExpiryValue1();
				daChange.closestExpiryValue2 = da.getClosestExpiryValue2();
				daChange.closestAccessibleDate = da.getClosestAccessibleDate();
				daChange.closestAccessibleValue1 = da.getClosestAccessibleValue1();
				daChange.closestAccessibleValue2 = da.getClosestAccessibleValue2();
				daChange.dedicatedAccountActiveValue1 = da.getDedicatedAccountActiveValue1();
				daChange.dedicatedAccountActiveValue2 = da.getDedicatedAccountActiveValue2();

				if (daUpdate.startDate != null)
				{

					SubDedicatedAccountInformation subDa = new SubDedicatedAccountInformation();
					subDa.setDedicatedAccountValue1(daUpdate.dedicatedAccountValueNew);
					subDa.setStartDate(daUpdate.startDate);
					subDa.setStartDate(daUpdate.expiryDate);

					if (da.getSubDedicatedAccountInformation() != null && da.getSubDedicatedAccountInformation().length > 0)
					{

						boolean found = false;

						for (int i = 0; i < da.getSubDedicatedAccountInformation().length; i++)
						{
							SubDedicatedAccountInformation subDaInfo = da.getSubDedicatedAccountInformation()[i];

							if (subDaInfo == null || subDaInfo.getStartDate() == null)
								continue;

							if (daUpdate.startDate.equals(subDaInfo.getStartDate()))
							{
								found = true;

								subDaInfo = subDa;

								break;
							}
						}

						if (!found)
						{

							List<SubDedicatedAccountInformation> subDas = new LinkedList<SubDedicatedAccountInformation>(Arrays.asList(da.getSubDedicatedAccountInformation()));
							subDas.add(subDa);
							da.setSubDedicatedAccountInformation(subDas.toArray(new SubDedicatedAccountInformation[subDas.size()]));

						}

					}
					else
					{

						da.setSubDedicatedAccountInformation(new SubDedicatedAccountInformation[] { subDa });

					}

					SubDedicatedAccountChangeInformation subDaChange = new SubDedicatedAccountChangeInformation();
					subDaChange.changedAmount1 = daUpdate.dedicatedAccountValueNew;
					subDaChange.newStartDate = daUpdate.startDate;
					subDaChange.newExpiryDate = daUpdate.expiryDate;

					daChange.subDedicatedAccountChangeInformation = new SubDedicatedAccountChangeInformation[] { subDaChange };

				}

				daChange.dedicatedAccountUnitType = da.getDedicatedAccountUnitType();
				dedicatedAccountChangeInformation[index++] = daChange;

			}
		}

		// Create Response
		response.member.setCurrency1(subscriber.getCurrency1());
		response.member.setCurrency2(subscriber.getCurrency2());
		response.member.setNegativeBalanceBarringDate(subscriber.getNegativeBalanceBarringDate());
		response.member.setDedicatedAccountChangeInformation(dedicatedAccountChangeInformation);
		// response.member.setAccountFlagsAfter(subscriber.getAccountFlagsAfter());
		// response.member.setAccountFlagsBefore(subscriber.getAccountFlagsBefore());
		response.member.setAccountValue1(subscriber.getAccountValue1());
		response.member.setAccountValue2(subscriber.getAccountValue2());

		return response;
	}

}
