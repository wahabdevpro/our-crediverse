/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package c4uburntester.packages;

import c4uburntester.Database;
import com.concurrent.hxc.RequestHeader;
import com.concurrent.hxc.ServiceQuota;
import hxc.services.airsim.AirSimService;
import hxc.services.airsim.IAirSim;
import hxc.services.airsim.protocol.SubscriberState;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

/**
 *
 * @author justinguedes
 */
public class Packages
{

	private final Random rand = new Random(new Random().nextLong());
	private int count = 0;
	private int sub = 0;
	private int lif = 0;

	public final com.concurrent.hxc.Number numbers[] = Database.getNumbers(rand.nextInt(Database.MAX_NUMBER));
	private com.concurrent.hxc.Number currentSubscriber;
	private com.concurrent.hxc.Number currentNumber;
	private final RequestHeader pkg1[] = { Database.generateSubscribe(randomSubscriber()), Database.generateGetBalances(currentSubscriber, true), Database.generateUnsubscribe(currentSubscriber) };
	private final RequestHeader pkg2[] = { Database.generateSubscribe(randomSubscriber()), Database.generateAddMember(currentSubscriber, randomNumber()),
			Database.generateGetMembers(currentSubscriber), Database.generateRemoveMember(currentSubscriber, currentNumber), Database.generateUnsubscribe(currentSubscriber) };
	private final RequestHeader pkg3[] = { Database.generateSubscribe(randomSubscriber()), Database.generateAddMember(currentSubscriber, randomNumber()),
			Database.generateAddQuota(currentSubscriber, currentNumber, generateServiceQuota("Sms_Offnet_Day", null, "Offnet", 100L, "Sms_Offnet_Day", "Sms", "Anytime", "SMS")),
			Database.generateRemoveMember(currentSubscriber, currentNumber), Database.generateUnsubscribe(currentSubscriber) };
	private final RequestHeader pkg4[] = { Database.generateSubscribe(randomSubscriber()), Database.generateGetBalances(currentSubscriber, true), Database.generateGetMembers(currentSubscriber),
			Database.generateGetOwners(currentSubscriber), Database.generateUnsubscribe(currentSubscriber) };
	private final RequestHeader pkg5[] = { Database.generateSubscribe(randomSubscriber()), Database.generateGetMembers(currentSubscriber),
			Database.generateAddMember(currentSubscriber, randomNumber()), Database.generateGetMembers(currentSubscriber), Database.generateGetBalances(currentSubscriber, true),
			Database.generateRemoveMember(currentSubscriber, currentNumber), Database.generateGetBalances(currentSubscriber, false), Database.generateUnsubscribe(currentSubscriber) };
	private final RequestHeader pkg6[] = { Database.generateSubscribe(randomSubscriber()), Database.generateAddMember(currentSubscriber, randomNumber()),
			Database.generateAddMember(currentSubscriber, randomNumber()), Database.generateAddMember(currentSubscriber, randomNumber()),
			Database.generateAddMember(currentSubscriber, randomNumber()), Database.generateGetMembers(currentSubscriber), Database.generateRemoveMembers(currentSubscriber),
			Database.generateUnsubscribe(currentSubscriber) };
	private final RequestHeader pkg7[] = { Database.generateSubscribe(currentSubscriber), Database.generateGetServices(currentSubscriber, true),
			Database.generateAddMember(currentSubscriber, randomNumber()),
			Database.generateAddQuota(currentSubscriber, currentNumber, generateServiceQuota("Sms_Offnet_Day", null, "Offnet", 100L, "Sms_Offnet_Day", "Sms", null, "SMS")),
			Database.generateGetQuotas(currentSubscriber, true, null, "Offnet", currentNumber, "Sms", "Anytime"), Database.generateRemoveMembers(currentSubscriber),
			Database.generateUnsubscribe(currentSubscriber) };
	private final RequestHeader pkg8[] = { Database.generateSubscribe(randomSubscriber()), Database.generateGetBalances(currentSubscriber, true), Database.generateUnsubscribe(currentSubscriber) };
	private final RequestHeader pkg9[] = { Database.generateSubscribe(randomSubscriber()), Database.generateAddMember(currentSubscriber, randomNumber()),
			Database.generateGetMembers(currentSubscriber), Database.generateRemoveMember(currentSubscriber, currentNumber), Database.generateUnsubscribe(currentSubscriber) };
	private final RequestHeader pkg10[] = { Database.generateSubscribe(randomSubscriber()), Database.generateAddMember(currentSubscriber, randomNumber()),
			Database.generateAddQuota(currentSubscriber, currentNumber, generateServiceQuota("Sms_Offnet_Day", null, "Offnet", 100L, "Sms_Offnet_Day", "Sms", "Anytime", "SMS")),
			Database.generateRemoveMember(currentSubscriber, currentNumber), Database.generateUnsubscribe(currentSubscriber) };
	private final RequestHeader pkg11[] = { Database.generateSubscribe(randomSubscriber()), Database.generateGetBalances(currentSubscriber, true), Database.generateGetMembers(currentSubscriber),
			Database.generateGetOwners(currentSubscriber), Database.generateUnsubscribe(currentSubscriber) };
	private final RequestHeader pkg12[] = { Database.generateSubscribe(randomSubscriber()), Database.generateGetMembers(currentSubscriber),
			Database.generateAddMember(currentSubscriber, randomNumber()), Database.generateGetMembers(currentSubscriber), Database.generateGetBalances(currentSubscriber, true),
			Database.generateRemoveMember(currentSubscriber, currentNumber), Database.generateGetBalances(currentSubscriber, false), Database.generateUnsubscribe(currentSubscriber) };
	private final RequestHeader pkg13[] = { Database.generateSubscribe(randomSubscriber()), Database.generateAddMember(currentSubscriber, randomNumber()),
			Database.generateAddMember(currentSubscriber, randomNumber()), Database.generateAddMember(currentSubscriber, randomNumber()),
			Database.generateAddMember(currentSubscriber, randomNumber()), Database.generateGetMembers(currentSubscriber), Database.generateRemoveMembers(currentSubscriber),
			Database.generateUnsubscribe(currentSubscriber) };
	private final RequestHeader pkg14[] = { Database.generateSubscribe(randomSubscriber()), Database.generateGetServices(currentSubscriber, true),
			Database.generateAddMember(currentSubscriber, randomNumber()),
			Database.generateAddQuota(currentSubscriber, currentNumber, generateServiceQuota("Sms_Offnet_Day", null, "Offnet", 100L, "Sms_Offnet_Day", "Sms", "Anytime", "SMS")),
			Database.generateGetQuotas(currentSubscriber, true, null, "Offnet", currentNumber, "Sms", "Anytime"), Database.generateRemoveMembers(currentSubscriber),
			Database.generateUnsubscribe(currentSubscriber) };
	private final RequestHeader pkg15[] = { Database.generateSubscribe(randomSubscriber()) };

	private final RequestHeader packages[][] = { pkg1, pkg2, pkg3, pkg4, pkg5, pkg6, pkg7, pkg8, pkg9, pkg10, pkg11, pkg12, pkg13, pkg14 };

	public RequestHeader[] getRandomPackage()
	{
		if (++count > 1000)
		{
			initialiseSubs();
			count = 0;
		}
		return packages[rand.nextInt(packages.length)];
	}

	public RequestHeader[] getPackage(int packageNum)
	{
		if (packageNum > packages.length - 1 || packageNum < 0)
			return null;
		return packages[packageNum];
	}

	public RequestHeader subscribe()
	{
		return Database.generateSubscribe(numbers[sub++]);
	}

	public RequestHeader processLifeCycle()
	{
		return Database.generateProcessLifeCycleEvent(numbers[lif++]);
	}

	private com.concurrent.hxc.Number randomNumber()
	{
		currentNumber = numbers[rand.nextInt(numbers.length)];
		return currentNumber;
	}

	private com.concurrent.hxc.Number randomSubscriber()
	{
		currentSubscriber = numbers[rand.nextInt(numbers.length)];
		return currentSubscriber;
	}

	private ServiceQuota generateServiceQuota(String name, String daysOfWeek, String destination, Long quantity, String quotaID, String service, String timeOfDay, String units)
	{
		ServiceQuota quota = new ServiceQuota();
		quota.setName(name);
		quota.setDaysOfWeek(daysOfWeek);
		quota.setDestination(destination);
		quota.setQuantity(quantity);
		quota.setQuotaID(quotaID);
		quota.setService(service);
		quota.setTimeOfDay(timeOfDay);
		quota.setUnits(units);
		return quota;
	}

	public Packages()
	{
		initialiseSubs();
	}

	public Packages(String url) throws MalformedURLException
	{
		initialiseSubs(url);
	}

	private AirSimService service;

	private void initialiseSubs()
	{
		if (service == null)
			service = new AirSimService();
		IAirSim air = service.getAirSimPort();
		for (com.concurrent.hxc.Number number : numbers)
		{
			air.addSubscriber(number.getAddressDigits(), 2, 76, Long.MAX_VALUE, SubscriberState.ACTIVE);
		}
	}

	private void initialiseSubs(String url) throws MalformedURLException
	{
		if (service == null)
			service = new AirSimService(new URL(url));
		IAirSim air = service.getAirSimPort();
		for (com.concurrent.hxc.Number number : numbers)
		{
			air.addSubscriber(number.getAddressDigits(), 2, 76, Long.MAX_VALUE, SubscriberState.ACTIVE);
		}
	}
}
