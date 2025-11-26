package hxc.services.pin;

import java.sql.SQLException;
import java.text.StringCharacterIterator;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.concurrent.hxc.ChangePINRequest;
import com.concurrent.hxc.ChangePINResponse;
import com.concurrent.hxc.IServiceContext;
import com.concurrent.hxc.Number;
import com.concurrent.hxc.ResetPINRequest;
import com.concurrent.hxc.ResetPINResponse;
import com.concurrent.hxc.ResponseHeader;
import com.concurrent.hxc.ServiceContext;
import com.concurrent.hxc.SubscriptionState;
import com.concurrent.hxc.ValidatePINRequest;
import com.concurrent.hxc.ValidatePINResponse;
import com.concurrent.hxc.VasServiceInfo;

import hxc.configuration.IConfiguration;
import hxc.configuration.ValidationException;
import hxc.connectors.IConnection;
import hxc.connectors.IInteraction;
import hxc.connectors.air.IAirConnector;
import hxc.connectors.air.proxy.Subscriber;
import hxc.connectors.database.IDatabase;
import hxc.connectors.database.IDatabaseConnection;
import hxc.connectors.soap.ISoapConnector;
import hxc.connectors.soap.ISubscriber;
import hxc.connectors.vas.VasCommand;
import hxc.connectors.vas.VasCommandParser;
import hxc.connectors.vas.VasService;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.ReturnCodes;
import hxc.servicebus.Trigger;
import hxc.services.IService;
import hxc.services.notification.INotifications;
import hxc.services.notification.Phrase;
import hxc.services.notification.ReturnCodeTexts;
import hxc.services.numberplan.INumberPlan;
import hxc.services.security.Perm;
import hxc.services.security.Perms;
import hxc.services.transactions.CdrBase;
import hxc.services.transactions.CsvCdr;
import hxc.services.transactions.ITransactionService;
import hxc.services.transactions.Transaction;
import hxc.utils.configuration.ConfigurationBase;
import hxc.utils.instrumentation.IMetric;

public class PinService extends VasService implements IService, IPinService
{
	final static Logger logger = LoggerFactory.getLogger(PinService.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Internal Properties
	//
	// /////////////////////////////////
	protected IServiceBus esb;
	protected IDatabase database;
	protected ITransactionService transactions;
	protected INumberPlan numberPlan;
	protected IAirConnector air;
	protected ISoapConnector soapConnector;

	private static final int TRANSACTION_ID_LENGTH = 20;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IService Implementation
	//
	// /////////////////////////////////

	@Override
	public void initialise(IServiceBus esb)
	{
		this.esb = esb;
	}

	@Override
	public boolean start(String[] args)
	{
		// Must have Database
		database = esb.getFirstConnector(IDatabase.class);
		if (database == null)
			return false;

		// Must have Transaction Service
		transactions = esb.getFirstService(ITransactionService.class);
		if (transactions == null)
			return false;

		// Must have Air
		air = esb.getFirstConnector(IAirConnector.class);
		if (air == null)
			return false;

		// Must have Number Plan Service
		numberPlan = esb.getFirstService(INumberPlan.class);
		if (numberPlan == null)
			return false;

		// Must have Soap Connector
		soapConnector = esb.getFirstConnector(ISoapConnector.class);
		if (soapConnector == null)
			return false;

		// Create an SMS/USSD Trigger
		Trigger<IInteraction> ussdTrigger = new Trigger<IInteraction>(IInteraction.class)
		{
			@Override
			public boolean testCondition(IInteraction interaction)
			{
				return interaction.getShortCode().equals(config.shortCode) && commandParser != null && commandParser.canExecute(interaction.getMessage());
			}

			@Override
			public void action(IInteraction interaction, IConnection connection)
			{
				commandParser.execute(interaction, esb.getLocale());
			}
		};
		esb.addTrigger(ussdTrigger);

		// Log Information
		logger.info("PIN Service Started");

		return true;
	}

	@Override
	public void stop()
	{
		// Log Information
		logger.info("PIN Service Stopped");
	}

	@Override
	public IConfiguration getConfiguration()
	{
		return config;
	}

	@Override
	public void setConfiguration(IConfiguration config) throws ValidationException
	{
		this.config = (PinServiceConfig) config;
	}

	@Override
	public boolean canAssume(String serverRole)
	{
		return false;
	}

	@Override
	public boolean isFit()
	{
		return true;
	}

	@Override
	public IMetric[] getMetrics()
	{
		return null;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	public PinService()
	{
		super();
		super.commandParser = new VasCommandParser(this, "OLDPIN", "NEWPIN", "PIN")
		{
			@Override
			protected VasCommand[] getCommands()
			{
				return config.commands;
			}

			@Override
			public boolean parseCommandVariable(VasCommand.Processes process, String commandVariable, String value, CommandArguments arguments)
			{
				switch (commandVariable)
				{
					case "OLDPIN":
						arguments.oldPIN = value;
						return true;

					case "NEWPIN":
						arguments.newPIN = value;
						return true;

					case "PIN":
						arguments.pin = value;
						return true;
				}

				return false;
			}

			@Override
			protected ISubscriber getSubscriberProxy(String msisdn)
			{
				return new Subscriber(msisdn, air, null);
			}

			@Override
			protected ISoapConnector getSoapConnector()
			{
				return soapConnector;
			}

		};
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Configurable Parameters
	//
	// /////////////////////////////////
	@Perms(perms = { @Perm(name = "ViewPinServiceParameters", description = "View PinService Parameters", category = "PinService", supplier = true),
			@Perm(name = "ChangePinServiceParameters", implies = "ViewPinServiceParameters", description = "Change PinService Parameters", category = "PinService", supplier = true),
			@Perm(name = "ViewPinServiceNotifications", description = "View PIN Service Notifications", category = "PinService", supplier = true),
			@Perm(name = "ChangePinServiceNotifications", implies = "ViewPinServiceNotifications", description = "Change PIN Service Notifications", category = "PinService", supplier = true) })
	public class PinServiceConfig extends ConfigurationBase
	{
		protected String shortCode = "154";

		// Variants
		protected Variant[] variants = new Variant[] { //

		new Variant(DEFAULT_VARIANT_ID, Phrase.en("Default"), 5, 4, 6, "1111", false), //

		};

		// Single Shot USSD and SMS Commands
		protected VasCommand[] commands = new VasCommand[] { //
		//
				new VasCommand(VasCommand.Processes.changePIN, "*{OLDPIN}*{NEWPIN}#"), //
				new VasCommand(VasCommand.Processes.validatePIN, "*{PIN}#"), };

		// Service Specific error Mapping
		protected ReturnCodeTexts[] returnCodesTexts = new ReturnCodeTexts[] { new ReturnCodeTexts(ReturnCodes.invalidPin, Phrase.en("Your PIN is invalid.").fre("FR: Invalid PIN")),

		new ReturnCodeTexts(ReturnCodes.pinBlocked, Phrase.en("Your PIN is blocked.").fre("FR: PIN Blocked")),

		new ReturnCodeTexts(ReturnCodes.unregisteredPin, Phrase.en("Your PIN is not registered. Please register a new PIN using *154*1111*PIN#").fre("FR: PIN Not Registered")),

		new ReturnCodeTexts(ReturnCodes.alreadyAdded, Phrase.en("You have already registered a PIN. If you have forgotten your PIN, please call customer service.").fre("FR: PIN Not Registered")),

		new ReturnCodeTexts(ReturnCodes.malformedRequest, Phrase.en("There was an error processing the request.").fre("FR: Malformed request")),

		new ReturnCodeTexts(ReturnCodes.success, Phrase.en("PIN Operation successful.").fre("FR: PIN Operation successful")),

		};

		public ReturnCodeTexts[] getReturnCodesTexts()
		{
			check(esb, "ViewPinServiceParameters");
			return returnCodesTexts;
		}

		public void setReturnCodesTexts(ReturnCodeTexts[] returnCodesTexts)
		{
			check(esb, "ChangePinServiceParameters");
			this.returnCodesTexts = returnCodesTexts;
		}

		public Variant[] getVariants()
		{
			check(esb, "ViewPinServiceParameters");
			return variants;
		}

		public void setVariants(Variant[] variants)
		{
			check(esb, "ChangePinServiceParameters");
			this.variants = variants;
		}

		public VasCommand[] getCommands()
		{
			check(esb, "ViewPinServiceParameters");
			return commands;
		}

		public void setCommands(VasCommand[] commands)
		{
			check(esb, "ChangePinServiceParameters");
			this.commands = commands;
		}

		@Override
		public String getPath(String languageCode)
		{
			return "VAS Services";
		}

		@Override
		public INotifications getNotifications()
		{
			return null;
		}

		@Override
		public long getSerialVersionUID()
		{
			return -3193772336943468429L;
		}

		@Override
		public String getName(String languageCode)
		{
			return getServiceName(languageCode);
		}

		public String getShortCode()
		{
			check(esb, "ViewPinServiceParameters");
			return shortCode;
		}

		public void setShortCode(String shortCode)
		{
			check(esb, "ChangePinServiceParameters");
			this.shortCode = shortCode;
		}

		@Override
		public void validate() throws ValidationException
		{
		}

		@Override
		public void performUpdateNotificationSecurityCheck()
		{
			check(esb, "ChangePinServiceNotifications");
		}

		@Override
		public void performGetNotificationSecurityCheck()
		{
			check(esb, "ViewPinServiceNotifications");
		}

	}

	PinServiceConfig config = new PinServiceConfig();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// VasService Implementation
	//
	// /////////////////////////////////

	@Override
	public String getServiceID()
	{
		return "PIN";
	}

	@Override
	public String getServiceName(String lanuageID)
	{
		return "PIN Service";
	}

	public static String getDefaultVariantID()
	{
		return PinService.DEFAULT_VARIANT_ID;
	}

	@Override
	public VasServiceInfo[] getServiceInfo(IServiceContext context, Number subscriberNumber, String variantID, Integer languageID, boolean activeOnly, boolean suggested) throws Exception
	{
		VasServiceInfo[] result = new VasServiceInfo[config.variants.length];
		String languageCode = esb.getLocale().getLanguage(languageID);
		int index = 0;
		for (Variant variant : config.variants)
		{
			VasServiceInfo serviceInfo = new VasServiceInfo();
			serviceInfo.setServiceID(getServiceID());
			serviceInfo.setVariantID(variant.getVariantID());
			serviceInfo.setServiceName(this.getServiceName(Phrase.ENG));
			serviceInfo.setVariantName(getVariant(variantID).getName().get(languageCode));

			// PIN state is "active" by default
			if (subscriberNumber != null && !subscriberNumber.getAddressDigits().isEmpty())
			{
				// if (hasValidPIN(variantID, subscriberNumber.getAddressDigits()))
				// serviceInfo.setState(SubscriptionState.active);
				// else
				// serviceInfo.setState(SubscriptionState.notActive);

				serviceInfo.setState(SubscriptionState.active);
			}
			else
			{
				serviceInfo.setState(SubscriptionState.unknown);
			}

			result[index++] = serviceInfo;

			if (activeOnly && (serviceInfo.getState() != SubscriptionState.active))
			{
				return null;
			}
		}

		return result;
	}

	@Override
	public ResetPINResponse resetPIN(IServiceContext context, ResetPINRequest request)
	{
		// Prepare an Response
		ResetPINResponse response = super.resetPIN(context, request);

		// Get Language ID / Code
		int languageID = esb.getLocale().getLanguageID(request.getLanguageID());
		String languageCode = esb.getLocale().getLanguage(languageID);

		// Create a CDR
		CdrBase cdr = new CsvCdr(request, esb.getNextTransactionNumber(TRANSACTION_ID_LENGTH));

		// Database Connection Scope
		try (IDatabaseConnection db = database.getConnection(null))
		{
			// Transaction Reversal Scope
			try (Transaction<?> transaction = transactions.create(cdr, db))
			{
				// Validate Request
				String problem = ResetPINRequest.validate(request);
				if (problem != null)
				{
					context.setResultText(getReturnCodeText(languageCode, ReturnCodes.malformedRequest));
					return response.exitWith(cdr, ReturnCodes.malformedRequest, problem);
				}
				// Validate Variant
				Variant variant = getVariant(request.getVariantID());
				if (variant == null)
				{
					context.setResultText(getReturnCodeText(languageCode, ReturnCodes.malformedRequest));
					return response.exitWith(cdr, ReturnCodes.malformedRequest, "Invalid VariantID");
				}
				// Update CDR
				// Uses serviceID of PIN service
				cdr.setServiceID(this.getServiceID());
				cdr.setVariantID(request.getVariantID());
				cdr.setProcessID("ResetPIN");

				// Get the MSISDN
				String msisdn = numberPlan.getInternationalFormat(request.getSubscriberNumber().toMSISDN());

				// Perform the Reset
				if (!resetPIN(response, cdr, db, request.getVariantID(), msisdn))
				{
					context.setResultText(getReturnCodeText(languageCode, response.getReturnCode()));
					return response.exitWith(cdr, response.getReturnCode(), context.getRestultText());
				}
				// Complete
				transaction.complete();
				context.setResultText(getReturnCodeText(languageCode, response.getReturnCode()));
				return response.exitWith(cdr, ReturnCodes.success, "Success");

			}

			catch (Exception e)
			{
				context.setResultText(getReturnCodeText(languageCode, ReturnCodes.technicalProblem));
				return response.exitWith(cdr, ReturnCodes.technicalProblem, e);
			}

		}
		catch (Throwable e)
		{
			context.setResultText(getReturnCodeText(languageCode, ReturnCodes.technicalProblem));
			return response.exitWith(cdr, ReturnCodes.technicalProblem, e);
		}

	}

	@Override
	public boolean resetPIN(ResponseHeader response, CdrBase cdr, IDatabaseConnection db, String variantID, String msisdn)
	{
		// Validate Variant
		Variant variant = getVariant(variantID);
		if (variant == null)
		{
			response.exitWith(cdr, ReturnCodes.malformedRequest, "Invalid VariantID");
			return false;
		}

		Pin dbPin = null;

		// Get pin from DB
		try
		{
			dbPin = db.select(Pin.class, "where msisdn = %s and serviceId = %s and variantID = %s", msisdn, this.getServiceID(), variant.getVariantID());
		}
		catch (Throwable sqle)
		{
			response.exitWith(cdr, ReturnCodes.technicalProblem, sqle.getLocalizedMessage());
			return false;
		}

		// Check if PIN exists
		if (dbPin == null)
		{
			response.exitWith(cdr, ReturnCodes.unregisteredPin, "[%s] PIN not registered", msisdn);
			return false;
		}

		try
		{
			db.delete(dbPin);
			db.commit();
		}
		catch (Throwable sqle)
		{
			response.exitWith(cdr, ReturnCodes.technicalProblem, sqle.getLocalizedMessage());
			return false;
		}

		response.exitWith(cdr, ReturnCodes.success, "[%s] PIN successfully reset", msisdn);
		return true;
	}

	@Override
	public ChangePINResponse changePIN(IServiceContext context, ChangePINRequest request)
	{
		// Prepare an Response
		ChangePINResponse response = super.changePIN(context, request);

		// Get Language Code
		int languageID = esb.getLocale().getLanguageID(request.getLanguageID());
		String languageCode = esb.getLocale().getLanguage(languageID);

		// Create a CDR
		CdrBase cdr = new CsvCdr(request, esb.getNextTransactionNumber(TRANSACTION_ID_LENGTH));

		// Database Connection Scope
		try (IDatabaseConnection db = database.getConnection(null))
		{
			// Transaction Reversal Scope
			try (Transaction<?> transaction = transactions.create(cdr, db))
			{
				// Validate Request
				String problem = ChangePINRequest.validate(request);
				if (problem != null)
				{
					context.setResultText(getReturnCodeText(languageCode, ReturnCodes.malformedRequest));
					return response.exitWith(cdr, ReturnCodes.malformedRequest, problem);
				}
				// Update CDR
				cdr.setServiceID(this.getServiceID());
				cdr.setVariantID(request.getVariantID());
				cdr.setProcessID("ChangePIN");

				// Get the MSISDN
				String msisdn = numberPlan.getInternationalFormat(request.getSubscriberNumber().toMSISDN());

				// Perform the Change
				if (!changePIN(response, cdr, db, request.getVariantID(), msisdn, request.getOldPIN(), request.getNewPIN()))
				{
					context.setResultText(getReturnCodeText(languageCode, response.getReturnCode()));
					return response.exitWith(cdr, response.getReturnCode(), context.getRestultText());
				}
				// Complete
				transaction.complete();
				context.setResultText(getReturnCodeText(languageCode, response.getReturnCode()));
				return response.exitWith(cdr, ReturnCodes.success, "Success");

			}

			catch (Exception e)
			{
				context.setResultText(getReturnCodeText(languageCode, ReturnCodes.technicalProblem));
				return response.exitWith(cdr, ReturnCodes.technicalProblem, e);
			}

		}
		catch (Throwable e)
		{
			context.setResultText(getReturnCodeText(languageCode, ReturnCodes.technicalProblem));
			return response.exitWith(cdr, ReturnCodes.technicalProblem, e);
		}
	}

	@Override
	public boolean changePIN(ResponseHeader response, CdrBase cdr, IDatabaseConnection db, String variantID, String msisdn, String oldPIN, String newPIN)
	{
		// Validate Variant
		Variant variant = getVariant(variantID);
		if (variant == null)
		{
			response.exitWith(cdr, ReturnCodes.malformedRequest, "Invalid VariantID");
			return false;
		}

		// Validation new PIN format
		if (newPIN.length() > variant.getMaxLength())
		{
			response.exitWith(cdr, ReturnCodes.invalidPin, "[%s] New PIN too long.", msisdn);
			return false;
		}

		if (newPIN.length() < variant.getMinLength())
		{
			response.exitWith(cdr, ReturnCodes.invalidPin, "[%s] New PIN too short.", msisdn);
			return false;
		}

		if (!isNumeric(newPIN))
		{
			response.exitWith(cdr, ReturnCodes.invalidPin, "[%s] New PIN has invalid characters", msisdn);
			return false;
		}

		if (newPIN.equals(variant.getDefaultPin()) && !variant.isMayUseDefault())
		{
			response.exitWith(cdr, ReturnCodes.invalidPin, "[%s] New PIN cannot be default PIN", msisdn);
			return false;
		}

		Pin dbPin = null;

		// Get pin from DB
		try
		{
			dbPin = db.select(Pin.class, "where msisdn = %s and serviceId = %s and variantID = %s", msisdn, this.getServiceID(), variant.getVariantID());
		}
		catch (Throwable sqle)
		{
			response.exitWith(cdr, ReturnCodes.technicalProblem, sqle.getLocalizedMessage());
			return false;
		}

		if (dbPin == null) // New pin registration
		{
			if (!oldPIN.equals(variant.getDefaultPin()))
			{
				response.exitWith(cdr, ReturnCodes.unregisteredPin, "[%s] PIN registration failed: incorrect default PIN.", msisdn);
				return false;
			}

			dbPin = new Pin(msisdn, this.getServiceID(), variant.getVariantID(), newPIN, 0, false, new Date());

			try
			{
				db.insert(dbPin);
				db.commit();
			}
			catch (Throwable sqle)
			{
				response.exitWith(cdr, ReturnCodes.technicalProblem, sqle.getLocalizedMessage());
				return false;
			}

			response.exitWith(cdr, ReturnCodes.success, "[%s] PIN successfully registered.", msisdn);

		}
		else
		// Pin Change
		{
			// Check that the user is not trying to register using the default pin
			if (oldPIN.equals(variant.getDefaultPin()))
			{
				response.exitWith(cdr, ReturnCodes.alreadyAdded, "[%s] Old PIN is default pin, registration failed: already registered.", msisdn);
				return false;
			}

			// Validate existing pin, and check if blocked
			if (!this.validatePIN(response, cdr, db, variant.getVariantID(), msisdn, oldPIN))
			{
				response.exitWith(cdr, response.getReturnCode(), response.getMessage(), msisdn);
				return false;
			}

			// Update PIN
			dbPin.setEncryptedPin(Pin.encrypt(newPIN));
			dbPin.setFailedCount(0);
			try
			{
				db.update(dbPin);
				db.commit();
			}
			catch (SQLException sqle)
			{
				response.exitWith(cdr, ReturnCodes.technicalProblem, sqle.getLocalizedMessage());
				return false;
			}

			response.exitWith(cdr, ReturnCodes.success, "[%s] PIN successfully changed.", msisdn);
		}

		return true;
	}

	@Override
	public ValidatePINResponse validatePIN(ServiceContext context, ValidatePINRequest request)
	{
		// Prepare an Response
		ValidatePINResponse response = super.validatePIN(context, request);

		// Get Language ID / Code
		int languageID = esb.getLocale().getLanguageID(request.getLanguageID());
		String languageCode = esb.getLocale().getLanguage(languageID);

		// Create a CDR
		CdrBase cdr = new CsvCdr(request, esb.getNextTransactionNumber(TRANSACTION_ID_LENGTH));

		// Database Connection Scope
		try (IDatabaseConnection db = database.getConnection(null))
		{
			// Transaction Reversal Scope
			try (Transaction<?> transaction = transactions.create(cdr, db))
			{
				// Validate Request
				String problem = ValidatePINRequest.validate(request);
				if (problem != null)
				{
					context.setResultText(getReturnCodeText(languageCode, ReturnCodes.malformedRequest));
					return response.exitWith(cdr, ReturnCodes.malformedRequest, problem);
				}
				// Validate Variant
				Variant variant = getVariant(request.getVariantID());
				if (variant == null)
				{
					context.setResultText(getReturnCodeText(languageCode, ReturnCodes.malformedRequest));
					return response.exitWith(cdr, ReturnCodes.malformedRequest, "Invalid VariantID");
				}
				// Update CDR
				cdr.setServiceID(this.getServiceID());
				cdr.setVariantID(request.getVariantID());
				cdr.setProcessID("validatePIN");

				// Get the MSISDN
				String msisdn = numberPlan.getInternationalFormat(request.getSubscriberNumber().toMSISDN());

				// Perform the Validation
				if (!validatePIN(response, cdr, db, request.getVariantID(), msisdn, request.getPIN()))
				{
					context.setResultText(getReturnCodeText(languageCode, response.getReturnCode()));
					return response.exitWith(cdr, response.getReturnCode(), context.getRestultText());
				}
				// Complete
				transaction.complete();
				context.setResultText(getReturnCodeText(languageCode, response.getReturnCode()));
				return response.exitWith(cdr, ReturnCodes.success, "Success");
			}// transaction

			catch (Exception e)
			{
				context.setResultText(getReturnCodeText(languageCode, ReturnCodes.technicalProblem));
				return response.exitWith(cdr, ReturnCodes.technicalProblem, e);
			}
		}// dbConnection
		catch (Throwable e)
		{
			context.setResultText(getReturnCodeText(languageCode, ReturnCodes.technicalProblem));
			return response.exitWith(cdr, ReturnCodes.technicalProblem, e);
		}
	}

	@Override
	public boolean validatePIN(ResponseHeader response, CdrBase cdr, IDatabaseConnection db, String variantID, String msisdn, String pin)
	{
		// Validate Variant
		Variant variant = getVariant(variantID);
		if (variant == null)
		{
			response.exitWith(cdr, ReturnCodes.malformedRequest, "Invalid VariantID");
			return false;
		}

		// Validation new PIN format
		if (pin == null || pin.length() == 0)
		{
			response.exitWith(cdr, ReturnCodes.invalidPin, "[%s] PIN is required.", msisdn);
			return false;
		}
		/*
		 * // Validation new PIN format if (pin.length() > variant.getMaxLength()) { response.exitWith(cdr, ReturnCodes.invalidPin, "[%s] PIN too long.", msisdn); return false; }
		 * 
		 * if (pin.length() < variant.getMinLength()) { response.exitWith(cdr, ReturnCodes.invalidPin, "[%s] PIN too short.", msisdn); return false; }
		 * 
		 * if (!isNumeric(pin)) { response.exitWith(cdr, ReturnCodes.invalidPin, "[%s] PIN has invalid characters.", msisdn); return false; }
		 */

		logger.trace("[{}] " + validatePinFormat(pin, variant), msisdn);

		Pin dbPin = null;

		// Get pin from DB
		try
		{
			dbPin = db.select(Pin.class, "where msisdn = %s and serviceId = %s and variantID = %s", msisdn, this.getServiceID(), variant.getVariantID());
		}
		catch (Throwable sqle)
		{
			response.exitWith(cdr, ReturnCodes.technicalProblem, sqle.getLocalizedMessage());
			return false;
		}

		// Check if pin not registered
		if (dbPin == null)
		{
			response.exitWith(cdr, ReturnCodes.unregisteredPin, "[%s] PIN not registered.", msisdn);
			return false;
		}

		// Check if pin is blocked
		if (dbPin.isBlocked())
		{
			response.exitWith(cdr, ReturnCodes.pinBlocked, "[%s] PIN is blocked.", msisdn);
			return false;
		}

		// Now do pin authentication
		boolean result = false;

		String incomingPin = Pin.encrypt(pin);

		if (!dbPin.getEncryptedPin().equals(incomingPin))
		{
			int failedCount = dbPin.getFailedCount() + 1;
			dbPin.setFailedCount(failedCount);
			response.exitWith(cdr, ReturnCodes.invalidPin, "[%s] PIN authentication failed.", msisdn);

			if (failedCount >= variant.getMaxRetries())
			{
				dbPin.setBlocked(true);
				response.exitWith(cdr, ReturnCodes.pinBlocked, "[%s] PIN is now blocked.", msisdn);
			}

			result = false;
		}
		else
		{
			response.exitWith(cdr, ReturnCodes.success, "[%s] PIN successfully authenticated.", msisdn);
			dbPin.setFailedCount(0);
			result = true;
		}

		// Update DB entry accordingly
		try
		{
			db.update(dbPin);
			db.commit();
		}
		catch (SQLException sqle)
		{
			response.exitWith(cdr, ReturnCodes.technicalProblem, sqle.getLocalizedMessage());
			return false;
		}

		return result;
	}

	@Override
	public boolean hasValidPIN(ResponseHeader response, CdrBase cdr, IDatabaseConnection db, String variantID, String msisdn)
	{
		Variant variant = getVariant(variantID);
		if (variant == null)
		{
			response.exitWith(cdr, ReturnCodes.malformedRequest, "Invalid VariantID");
			return false;
		}

		Pin dbPin = null;

		// Get pin from DB
		try
		{
			dbPin = db.select(Pin.class, "where msisdn = %s and serviceId = %s and variantID = %s", msisdn, this.getServiceID(), variant.getVariantID());
		}
		catch (Throwable sqle)
		{
			response.exitWith(cdr, ReturnCodes.technicalProblem, sqle.getLocalizedMessage());
			return false;
		}

		// Check if pin exists
		if (dbPin == null)
		{
			response.exitWith(cdr, ReturnCodes.unregisteredPin, "[%s] Valid pin check: PIN not registered.", msisdn);
			return false;
		}

		// Check if pin is blocked
		if (dbPin.isBlocked())
		{
			response.exitWith(cdr, ReturnCodes.pinBlocked, "[%s] Valid pin check: PIN is blocked.", msisdn);
			return false;
		}

		response.exitWith(cdr, ReturnCodes.success, "[%s] Valid pin check: PIN is valid.", msisdn);
		return true;
	}

	public boolean hasValidPIN(String variantID, String msisdn)
	{
		Variant variant = getVariant(variantID);
		if (variant == null)
			return false;

		Pin dbPin = null;

		// Get Pin from DB
		try (IDatabaseConnection db = database.getConnection(null))
		{
			dbPin = db.select(Pin.class, "where msisdn = %s and serviceId = %s and variantID = %s", msisdn, this.getServiceID(), variant.getVariantID());
		}
		catch (Exception e)
		{
			return false;
		}

		// Check if pin exists
		if (dbPin == null)
			return false;

		// Check if pin is blocked
		if (dbPin.isBlocked())
			return false;

		return true;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////

	@Override
	protected ReturnCodeTexts[] getReturnCodeTexts()
	{
		return config.getReturnCodesTexts();
	}

	protected Variant getVariant(String variantID)
	{
		if (variantID == null || variantID.length() == 0)
			variantID = DEFAULT_VARIANT_ID;

		for (Variant variant : config.getVariants())
		{
			if (variantID.equalsIgnoreCase(variant.getVariantID()))
				return variant;
		}

		return null;

	}

	// Used by validatePIN only for logging purposes
	protected String validatePinFormat(String pin, Variant variant)
	{
		if (pin.length() > variant.getMaxLength())
			return "PIN too long.";

		if (pin.length() < variant.getMinLength())
			return "PIN too short.";

		if (!isNumeric(pin))
			return "PIN has invalid characters.";

		return "PIN format ok.";
	}

	protected static boolean isNumeric(String pin)
	{
		String numbers = "0123456789";
		StringCharacterIterator iter = new StringCharacterIterator(pin);

		for (char ch = iter.first(); ch != StringCharacterIterator.DONE; ch = iter.next())
		{
			if (!numbers.contains(Character.toString(ch)))
			{
				return false;
			}
		}

		return true;
	}

}
