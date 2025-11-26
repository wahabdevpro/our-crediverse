package hxc.connectors.air;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.configuration.ValidationException;
import hxc.connectors.air.AirConnector.AirConnectionConfig;
import hxc.connectors.air.AirConnector.AirConnectorConfig;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.ServiceBus;
import hxc.services.transactions.CsvCdr;
import hxc.services.transactions.ITransactionService;
import hxc.services.transactions.Transaction;
import hxc.services.transactions.TransactionService;
import hxc.testsuite.RunAllTestsBase;
import hxc.utils.protocol.ucip.DedicatedAccountChangeInformation;
import hxc.utils.protocol.ucip.DedicatedAccountInformation;
import hxc.utils.protocol.ucip.DedicatedAccountUpdateInformation;
import hxc.utils.protocol.ucip.GetBalanceAndDateRequest;
import hxc.utils.protocol.ucip.GetBalanceAndDateResponse;
import hxc.utils.protocol.ucip.UpdateBalanceAndDateRequest;
import hxc.utils.protocol.ucip.UpdateBalanceAndDateResponse;
import hxc.utils.xmlrpc.XmlRpcRequest;
import hxc.utils.xmlrpc.XmlRpcServer;

public class ProxyTest
{
	final static Logger logger = LoggerFactory.getLogger(ProxyTest.class);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Internal Properties
	//
	// /////////////////////////////////
	private static IServiceBus esb;
	private Date timeNow = new Date();
	private static int TEST_PORT = 10012;
	private static XmlRpcServer server = null;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Setup
	//
	// /////////////////////////////////
	@BeforeClass
	public static void setup() throws ValidationException
	{
		// Create Transaction Service
		esb = ServiceBus.getInstance();
		esb.stop();
		RunAllTestsBase.configureLogging(esb);
		esb.registerService(new TransactionService());
		AirConnector air = new AirConnector();
		AirConnectorConfig airConfig = (AirConnectorConfig) air.getConfiguration();
		AirConnectionConfig conConfig = (AirConnectionConfig) airConfig.getConfigurations().iterator().next();
		conConfig.setUri("http://127.0.0.1:" + TEST_PORT + "/Air");
		esb.registerConnector(air);
		esb.start(null);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Test Add Member API
	//
	// /////////////////////////////////
	@Test
	public void testAddMember() throws ValidationException, IOException, AirException
	{
		// Mock Air Server
		server = new XmlRpcServer(GetBalanceAndDateRequest.class, UpdateBalanceAndDateRequest.class)
		{
			@Override
			protected void uponXmlRpcRequest(XmlRpcRequest request)
			{
				if (request.isTypeOf(GetBalanceAndDateRequest.class))
				{
					GetBalanceAndDateRequest gbd = request.getMethodCall();

					GetBalanceAndDateResponse response = new GetBalanceAndDateResponse();
					// response.member.negotiatedCapabilities = gbd.member.negotiatedCapabilities;
					response.member.originTransactionID = gbd.member.originTransactionID;
					response.member.responseCode = 0;

					response.member.accountFlagsAfter = null;
					response.member.accountFlagsBefore = null;
					response.member.accountPrepaidEmptyLimit1 = null;
					response.member.accountPrepaidEmptyLimit2 = null;
					response.member.accountValue1 = null;
					response.member.accountValue2 = null;
					response.member.aggregatedBalance1 = null;
					response.member.aggregatedBalance2 = null;
					response.member.aggregatedBalanceInformation = null;
					response.member.availableServerCapabilities = null;
					response.member.chargingResultInformation = null;
					response.member.creditClearanceDate = null;
					response.member.currency1 = null;
					response.member.currency2 = null;
					response.member.languageIDCurrent = 1;
					response.member.offerInformationList = null;
					response.member.serviceClassCurrent = 1234;
					response.member.serviceFeeExpiryDate = null;
					response.member.serviceRemovalDate = null;
					response.member.supervisionExpiryDate = null;
					response.member.temporaryBlockedFlag = null;

					DedicatedAccountInformation dai = new DedicatedAccountInformation();
					dai.closestAccessibleDate = null;
					dai.closestAccessibleValue1 = null;
					dai.closestAccessibleValue2 = null;
					dai.closestExpiryDate = null;
					dai.closestExpiryValue1 = null;
					dai.closestExpiryValue2 = null;
					dai.compositeDedicatedAccountFlag = null;
					dai.dedicatedAccountActiveValue1 = null;
					dai.dedicatedAccountActiveValue2 = null;
					dai.dedicatedAccountID = 7;
					dai.dedicatedAccountRealMoneyFlag = null;
					dai.dedicatedAccountUnitType = null;
					dai.dedicatedAccountValue1 = 500L;
					dai.dedicatedAccountValue2 = 500L;
					dai.expiryDate = timeNow;
					dai.offerID = null;
					dai.pamServiceID = null;
					dai.productID = null;
					dai.startDate = null;
					dai.subDedicatedAccountInformation = null;

					response.member.dedicatedAccountInformation = new DedicatedAccountInformation[] { dai };

					try
					{
						request.respond(response);
					}
					catch (IOException e)
					{
					}

				}

				else if (request.isTypeOf(UpdateBalanceAndDateRequest.class))
				{
					UpdateBalanceAndDateRequest ubd = request.getMethodCall();

					UpdateBalanceAndDateResponse response = new UpdateBalanceAndDateResponse();
					// response.member.negotiatedCapabilities = ubd.member.negotiatedCapabilities;
					response.member.originTransactionID = ubd.member.originTransactionID;
					response.member.originOperatorID = ubd.member.originOperatorID;
					response.member.responseCode = 0;

					response.member.accountFlagsAfter = null;
					response.member.accountFlagsBefore = null;
					response.member.accountValue1 = null;
					response.member.accountValue2 = null;
					response.member.availableServerCapabilities = null;
					response.member.currency1 = null;
					response.member.currency2 = null;
					response.member.negativeBalanceBarringDate = null;

					DedicatedAccountChangeInformation daci = new DedicatedAccountChangeInformation();
					DedicatedAccountUpdateInformation daui = ubd.member.dedicatedAccountUpdateInformation[0];

					daci.closestAccessibleDate = null;
					daci.closestAccessibleValue1 = null;
					daci.closestAccessibleValue2 = null;
					daci.closestExpiryDate = null;
					daci.closestExpiryValue1 = null;
					daci.closestExpiryValue2 = null;
					daci.dedicatedAccountActiveValue1 = null;
					daci.dedicatedAccountActiveValue2 = null;
					daci.dedicatedAccountID = daui.dedicatedAccountID;
					daci.dedicatedAccountRealMoneyFlag = null;
					daci.dedicatedAccountUnitType = daui.dedicatedAccountUnitType;
					daci.dedicatedAccountValue1 = daui.adjustmentAmountRelative != null ? 500L + daui.adjustmentAmountRelative : daui.dedicatedAccountValueNew;
					daci.dedicatedAccountValue2 = daci.dedicatedAccountValue1;
					daci.expiryDate = daui.expiryDate;
					if (daui.adjustmentDateRelative != null)
					{
						Calendar cal = Calendar.getInstance();
						cal.setTime(timeNow);
						cal.add(Calendar.DATE, daui.adjustmentDateRelative);
						daci.expiryDate = cal.getTime();
					}
					daci.offerID = null;
					daci.pamServiceID = daci.pamServiceID;
					daci.productID = daci.productID;
					daci.startDate = daci.startDate;
					daci.subDedicatedAccountChangeInformation = null;

					response.member.dedicatedAccountChangeInformation = new DedicatedAccountChangeInformation[] { daci };

					try
					{
						request.respond(response);
					}
					catch (IOException e)
					{
					}
				}

				return;
			}

		};
		server.start(TEST_PORT, "/Air");

		CsvCdr cdr = new CsvCdr();
		ITransactionService tservice = esb.getFirstService(ITransactionService.class);
		try (Transaction<?> transaction = tservice.create(cdr, null))
		{
			// Subscriber subscriber = new Subscriber("923075000113", air, transaction);
			// DedicatedAccount da7 = subscriber.getDedicatedAccount(7);
			// Long value1 = da7.getDedicatedAccountValue1();
			// Date expiryDate1 = da7.getExpiryDate();
			// da7.adjust(null, 100L, null, 2, null, null);
			// Long value2 = da7.getDedicatedAccountValue1();
			// Date expiryDate2 = da7.getExpiryDate();
			// assertEquals(100, value2 - value1);
			//
			// Calendar cal = Calendar.getInstance();
			// cal.setTime(expiryDate1);
			// cal.add(Calendar.DATE, 2);
			// Date expected = cal.getTime();
			//
			// assertTrue("Dates Mismatch", expected.equals(expiryDate2));
			// transaction.complete();
		}

	}

	// //////////////////////////////////////////////////////////////////
	//
	// Teardown
	//
	// /////////////////////////////////
	@AfterClass
	public static void teardown()
	{
		esb.stop();
		if (server != null)
		{
			try
			{
				server.stop();
				server = null;
			}
			catch (Exception e)
			{
			}

		}
	}

}
