package hxc.connectors.vas;

import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;

import com.concurrent.hxc.AddCreditTransferRequest;
import com.concurrent.hxc.AddMemberRequest;
import com.concurrent.hxc.AddQuotaRequest;
import com.concurrent.hxc.ChangeCreditTransferRequest;
import com.concurrent.hxc.ChangePINRequest;
import com.concurrent.hxc.ChangeQuotaRequest;
import com.concurrent.hxc.ExtendRequest;
import com.concurrent.hxc.GetBalancesRequest;
import com.concurrent.hxc.GetCreditTransfersRequest;
import com.concurrent.hxc.GetLocationRequest;
import com.concurrent.hxc.GetMembersRequest;
import com.concurrent.hxc.GetOwnersRequest;
import com.concurrent.hxc.GetQuotasRequest;
import com.concurrent.hxc.GetReturnCodeTextRequest;
import com.concurrent.hxc.GetReturnCodeTextResponse;
import com.concurrent.hxc.GetStatusRequest;
import com.concurrent.hxc.MigrateRequest;
import com.concurrent.hxc.Number;
import com.concurrent.hxc.ProcessRequest;
import com.concurrent.hxc.RedeemRequest;
import com.concurrent.hxc.RemoveCreditTransfersRequest;
import com.concurrent.hxc.RemoveMemberRequest;
import com.concurrent.hxc.RemoveMembersRequest;
import com.concurrent.hxc.RemoveQuotaRequest;
import com.concurrent.hxc.ReplaceMemberRequest;
import com.concurrent.hxc.RequestHeader;
import com.concurrent.hxc.ResetPINRequest;
import com.concurrent.hxc.ResponseHeader;
import com.concurrent.hxc.ResumeCreditTransferRequest;
import com.concurrent.hxc.ServiceContext;
import com.concurrent.hxc.ServiceQuota;
import com.concurrent.hxc.SubscribeRequest;
import com.concurrent.hxc.SuspendCreditTransferRequest;
import com.concurrent.hxc.SuspendRequest;
import com.concurrent.hxc.TransferRequest;
import com.concurrent.hxc.UnsubscribeRequest;
import com.concurrent.hxc.UnsuspendRequest;
import com.concurrent.hxc.ValidatePINRequest;

import hxc.configuration.ValidationException;
import hxc.connectors.IInteraction;
import hxc.connectors.soap.ISoapConnector;
import hxc.connectors.soap.ISubscriber;
import hxc.connectors.vas.VasCommand.Processes;
import hxc.servicebus.HostInfo;
import hxc.servicebus.ILocale;
import hxc.servicebus.RequestModes;
import hxc.servicebus.ReturnCodes;
import hxc.services.notification.INotificationText;

public abstract class VasCommandParser
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Class Command Arguments
	//
	// /////////////////////////////////
	public class CommandArguments
	{
		public String serviceID;
		public String variantID;
		public Number subscriberNumber;
		public Number recipientNumber;
		public long amount;
		public String pin;
		public Number memberNumber;
		public String transferMode;
		public Date nextTransferDate;
		public Long transferLimit;
		public Number donorNumber;
		public String newServiceID;
		public String newVariantID;
		public Number oldMemberNumber;
		public Number newMemberNumber;
		public boolean requestSMS;
		public String quotaID;
		public String service;
		public String destination;
		public String timeOfDay;
		public String daysOfWeek;
		public ServiceQuota quota;
		public ServiceQuota oldQuota;
		public ServiceQuota newQuota;
		public String sourceAddress;
		public String message;
		public String oldPIN;
		public String newPIN;
		public String serialNumber;
		public String redemptionID;
		public boolean activeOnly;
		public int processID;
		public Object[] processParameters;
		public boolean requestCoordinates = false;
		public boolean requestAddress = true;

		public void setProcessParameter(int index, Object value)
		{
			if (processParameters == null)
				processParameters = new Object[index + 1];
			else if (processParameters.length <= index)
				processParameters = java.util.Arrays.copyOf(processParameters, index + 1);
			processParameters[index] = value;
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private VasService vasService;
	private String[] commandVariables;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public String[] getCommandVariables()
	{
		return commandVariables;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public VasCommandParser(VasService vasService, String... commandVariables)
	{
		this.vasService = vasService;
		this.commandVariables = commandVariables;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	public boolean canExecute(String command)
	{
		for (VasCommand vasCommand : getCommands())
		{
			if (vasCommand.matches(command, commandVariables))
				return true;
		}

		return false;
	}

	public void execute(IInteraction interaction, final ILocale locale)
	{
		CommandArguments arguments = new CommandArguments();
		arguments.subscriberNumber = new Number(interaction.getMSISDN());
		arguments.serviceID = vasService.getServiceID();
		String command = interaction.getMessage();

		ServiceContext context = new ServiceContext();
		for (VasCommand vasCommand : getCommands())
		{
			Matcher matcher = vasCommand.matcher(command, commandVariables);
			if (matcher != null && matcher.find())
			{
				Map<String, String> defaults = vasCommand.getDefaults();
				if (defaults != null)
				{
					for (String variable : defaults.keySet())
					{
						boolean result = false;
						try
						{
							result = parseCommandVariable(vasCommand.getProcess(), variable, defaults.get(variable), arguments);
						}
						catch (Exception ex)
						{
							result = false;
						}

						if (!result)
						{
							interaction.reply(getErrorNotification(ReturnCodes.malformedRequest, interaction, context, locale));
							return;
						}
					}
				}

				for (int index = 1; index <= matcher.groupCount(); index++)
				{
					boolean result = false;
					try
					{
						result = parseCommandVariable(vasCommand.getProcess(), vasCommand.getCommandVariables()[index - 1], matcher.group(index), arguments);
					}
					catch (Exception ex)
					{
						result = false;
					}

					if (!result)
					{
						interaction.reply(getErrorNotification(ReturnCodes.malformedRequest, interaction, context, locale));
						return;
					}
				}

				ReturnCodes result = onPreExecute(interaction, vasCommand.getProcess(), context, arguments);
				if (result != ReturnCodes.success)
				{
					interaction.reply(getErrorNotification(result, interaction, context, locale));
					return;
				}

				final ResponseHeader response = dispatch(interaction, vasCommand.getProcess(), context, arguments);
				final String text = context.getRestultText();
				if (text == null || text.length() == 0)
				{
					interaction.reply(getErrorNotification(response.getReturnCode(), interaction, context, locale));
					return;
				}

				final int languageID = getLanguageID(interaction, context);
				INotificationText notificationText = new INotificationText()
				{
					@Override
					public String getText()
					{
						return text;
					}

					@Override
					public String getLanguageCode()
					{
						return locale.getLanguage(languageID);
					}

				};
				interaction.reply(notificationText);
				return;
			}
		}

		return;
	}

	protected ReturnCodes onPreExecute(IInteraction interaction, Processes process, ServiceContext context, CommandArguments arguments)
	{
		return ReturnCodes.success;
	}

	public void validate(VasCommand[] commands) throws ValidationException
	{
		ValidationException.notNull(commands, "VAS Commands cannot be Null");
		for (VasCommand command : commands)
		{
			ValidationException.notNull(command, "VAS Commands cannot be Null");
			command.validate(getCommandVariables());
		}

		for (int index1 = 0; index1 < commands.length - 1; index1++)
		{
			String[] tokens1 = commands[index1].tokenize();
			for (int index2 = index1 + 1; index2 < commands.length; index2++)
			{
				String[] tokens2 = commands[index2].tokenize();
				if (VasCommand.overlaps(tokens1, tokens2))
					throw new ValidationException("Ambiguous Command: %s", commands[index2]);
			}
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Abstract Methods
	//
	// /////////////////////////////////
	protected abstract VasCommand[] getCommands();

	protected abstract boolean parseCommandVariable(VasCommand.Processes process, String commandVariable, String value, CommandArguments arguments);

	protected abstract ISubscriber getSubscriberProxy(String msisdn);

	protected abstract ISoapConnector getSoapConnector();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////

	private INotificationText getErrorNotification(ReturnCodes returnCode, IInteraction interaction, ServiceContext context, ILocale locale)
	{
		final int languageID = getLanguageID(interaction, context);
		final String languageCode = locale.getLanguage(languageID);

		// Attempt to get the message from the Vas Service Directly
		String text = vasService.getReturnCodeText(languageCode, returnCode);

		// Else get it via the Soap Interface
		if (text == null || text.length() == 0)
		{
			GetReturnCodeTextRequest request = getRequest(GetReturnCodeTextRequest.class, interaction);
			request.setLanguageID(languageID);
			request.setServiceID(vasService.getServiceID());
			request.setReturnCode(returnCode);
			GetReturnCodeTextResponse response = getSoapConnector().getVasInterface().getReturnCodeText(request);
			text = response.getReturnCodeText();
		}

		final String finalText = text;
		return new INotificationText()
		{
			@Override
			public String getText()
			{
				return finalText;
			}

			@Override
			public String getLanguageCode()
			{
				return languageCode;
			}

		};
	}

	private int getLanguageID(IInteraction interaction, ServiceContext context)
	{
		// Get the Language of the Subscriber
		if (context == null)
			context = new ServiceContext();
		ISubscriber subscriberProxy = context.getSubscriberProxy();
		if (subscriberProxy == null)
			subscriberProxy = getSubscriberProxy(interaction.getMSISDN());
		final int languageID = subscriberProxy.getLanguageID();
		return languageID;
	}

	private <T extends RequestHeader> T getRequest(Class<T> cls, IInteraction interaction)
	{
		try
		{
			T request = (T) cls.newInstance();
			request.setCallerID(interaction.getMSISDN());
			request.setChannel(interaction.getChannel());
			request.setHostName(HostInfo.getNameOrElseHxC());
			request.setTransactionID(interaction.getInboundTransactionID());
			request.setSessionID(interaction.getInboundSessionID());
			request.setVersion(RequestHeader.CURRENT_VERSION);
			request.setMode(RequestModes.normal);
			request.setLanguageID(null);
			return request;
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			return null;
		}
	}

	private ResponseHeader dispatch(IInteraction interaction, VasCommand.Processes process, ServiceContext context, CommandArguments arguments)
	{
		switch (process)
		{
			case suspend:
			{
				SuspendRequest request = getRequest(SuspendRequest.class, interaction);
				return vasService.suspend(context, request);
			}

			case getMembers:
			{
				GetMembersRequest request = getRequest(GetMembersRequest.class, interaction);
				request.setServiceID(arguments.serviceID);
				request.setVariantID(arguments.variantID);
				request.setSubscriberNumber(arguments.subscriberNumber);
				return vasService.getMembers(context, request);
			}

			case transfer:
			{
				TransferRequest request = getRequest(TransferRequest.class, interaction);
				request.setServiceID(arguments.serviceID);
				request.setVariantID(arguments.variantID);
				request.setSubscriberNumber(arguments.subscriberNumber);
				request.setRecipientNumber(arguments.recipientNumber);
				request.setTransferModeID(arguments.transferMode);
				request.setAmount(arguments.amount);
				request.setPin(arguments.pin);
				return vasService.transfer(context, request);
			}

			case subscribe:
			{
				SubscribeRequest request = getRequest(SubscribeRequest.class, interaction);
				request.setServiceID(arguments.serviceID);
				request.setVariantID(arguments.variantID);
				request.setSubscriberNumber(arguments.subscriberNumber);
				return vasService.subscribe(context, request);
			}

			case unsubscribe:
			{
				UnsubscribeRequest request = getRequest(UnsubscribeRequest.class, interaction);
				request.setServiceID(arguments.serviceID);
				request.setVariantID(arguments.variantID);
				request.setSubscriberNumber(arguments.subscriberNumber);
				return vasService.unsubscribe(context, request);
			}

			case getCreditTransfers:
			{
				GetCreditTransfersRequest request = getRequest(GetCreditTransfersRequest.class, interaction);
				request.setServiceID(arguments.serviceID);
				request.setVariantID(arguments.variantID);
				request.setSubscriberNumber(arguments.subscriberNumber);
				request.setMemberNumber(arguments.memberNumber);
				request.setTransferMode(arguments.transferMode);
				request.setActiveOnly(arguments.activeOnly);
				return vasService.getCreditTransfers(context, request);
			}

			case addCreditTransfer:
			{
				AddCreditTransferRequest request = getRequest(AddCreditTransferRequest.class, interaction);
				request.setServiceID(arguments.serviceID);
				request.setSubscriberNumber(arguments.subscriberNumber);
				request.setMemberNumber(arguments.memberNumber);
				request.setTransferMode(arguments.transferMode);
				request.setAmount(arguments.amount);
				request.setNextTransferDate(arguments.nextTransferDate);
				request.setTransferLimit(arguments.transferLimit);
				request.setPin(arguments.pin);
				return vasService.addCreditTransfer(context, request);
			}

			case removeCreditTransfers:
			{
				RemoveCreditTransfersRequest request = getRequest(RemoveCreditTransfersRequest.class, interaction);
				request.setServiceID(arguments.serviceID);
				request.setVariantID(arguments.variantID);
				request.setTransferMode(arguments.transferMode);
				request.setSubscriberNumber(arguments.subscriberNumber);
				request.setMemberNumber(arguments.memberNumber);
				return vasService.removeCreditTransfers(context, request);
			}

			case suspendCreditTransfer:
			{
				SuspendCreditTransferRequest request = getRequest(SuspendCreditTransferRequest.class, interaction);
				request.setServiceID(arguments.serviceID);
				request.setVariantID(arguments.variantID);
				request.setTransferMode(arguments.transferMode);
				request.setSubscriberNumber(arguments.subscriberNumber);
				request.setMemberNumber(arguments.memberNumber);
				return vasService.suspendCreditTransfer(context, request);
			}

			case resumeCreditTransfer:
			{
				ResumeCreditTransferRequest request = getRequest(ResumeCreditTransferRequest.class, interaction);
				request.setServiceID(arguments.serviceID);
				request.setVariantID(arguments.variantID);
				request.setTransferMode(arguments.transferMode);
				request.setSubscriberNumber(arguments.subscriberNumber);
				request.setMemberNumber(arguments.memberNumber);
				return vasService.resumeCreditTransfer(context, request);
			}

			case changeCreditTransfer:
			{
				ChangeCreditTransferRequest request = getRequest(ChangeCreditTransferRequest.class, interaction);
				request.setServiceID(arguments.serviceID);
				request.setDonorNumber(arguments.donorNumber);
				request.setRecipientNumber(arguments.recipientNumber);
				request.setPin(arguments.pin);
				return vasService.changeCreditTransfer(context, request);
			}

			case extend:
			{
				ExtendRequest request = getRequest(ExtendRequest.class, interaction);
				return vasService.extend(context, request);
			}

			case migrate:
			{
				MigrateRequest request = getRequest(MigrateRequest.class, interaction);
				request.setServiceID(arguments.serviceID);
				request.setVariantID(arguments.variantID);
				request.setNewServiceID(arguments.newServiceID);
				request.setNewVariantID(arguments.newVariantID);
				request.setSubscriberNumber(arguments.subscriberNumber);
				return vasService.migrate(context, request);
			}

			case unsuspend:
			{
				UnsuspendRequest request = getRequest(UnsuspendRequest.class, interaction);
				return vasService.unsuspend(context, request);
			}

			case getOwners:
			{
				GetOwnersRequest request = getRequest(GetOwnersRequest.class, interaction);
				request.setServiceID(arguments.serviceID);
				request.setVariantID(arguments.variantID);
				request.setMemberNumber(arguments.memberNumber);
				return vasService.getOwners(context, request);
			}

			case addMember:
			{
				AddMemberRequest request = getRequest(AddMemberRequest.class, interaction);
				request.setServiceID(arguments.serviceID);
				request.setVariantID(arguments.variantID);
				request.setSubscriberNumber(arguments.subscriberNumber);
				request.setMemberNumber(arguments.memberNumber);
				return vasService.addMember(context, request);
			}

			case replaceMember:
			{
				ReplaceMemberRequest request = getRequest(ReplaceMemberRequest.class, interaction);
				request.setServiceID(arguments.serviceID);
				request.setVariantID(arguments.variantID);
				request.setSubscriberNumber(arguments.subscriberNumber);
				request.setOldMemberNumber(arguments.oldMemberNumber);
				request.setNewMemberNumber(arguments.newMemberNumber);
				return vasService.replaceMember(context, request);
			}

			case removeMember:
			{
				RemoveMemberRequest request = getRequest(RemoveMemberRequest.class, interaction);
				request.setServiceID(arguments.serviceID);
				request.setVariantID(arguments.variantID);
				request.setSubscriberNumber(arguments.subscriberNumber);
				request.setMemberNumber(arguments.memberNumber);
				return vasService.removeMember(context, request);
			}

			case removeMembers:
			{
				RemoveMembersRequest request = getRequest(RemoveMembersRequest.class, interaction);
				request.setServiceID(arguments.serviceID);
				request.setVariantID(arguments.variantID);
				request.setSubscriberNumber(arguments.subscriberNumber);
				return vasService.removeMembers(context, request);
			}

			case getBalances:
			{
				GetBalancesRequest request = getRequest(GetBalancesRequest.class, interaction);
				request.setServiceID(arguments.serviceID);
				request.setVariantID(arguments.variantID);
				request.setSubscriberNumber(arguments.subscriberNumber);
				request.setRequestSMS(arguments.requestSMS);
				request.setSerialNumber(arguments.serialNumber);
				return vasService.getBalances(context, request);
			}

			case getStatus:
			{
				GetStatusRequest request = getRequest(GetStatusRequest.class, interaction);
				request.setServiceID(arguments.serviceID);
				request.setVariantID(arguments.variantID);
				request.setSubscriberNumber(arguments.subscriberNumber);
				request.setSubjectNumber(arguments.memberNumber);
				return vasService.getStatus(context, request);
			}

			case getQuotas:
			{
				GetQuotasRequest request = getRequest(GetQuotasRequest.class, interaction);
				request.setServiceID(arguments.serviceID);
				request.setVariantID(arguments.variantID);
				request.setSubscriberNumber(arguments.subscriberNumber);
				request.setMemberNumber(arguments.memberNumber);
				request.setQuotaID(arguments.quotaID);
				request.setService(arguments.service);
				request.setDestination(arguments.destination);
				request.setTimeOfDay(arguments.timeOfDay);
				request.setDaysOfWeek(arguments.daysOfWeek);
				request.setActiveOnly(arguments.activeOnly);
				return vasService.getQuotas(context, request);
			}

			case addQuota:
			{
				AddQuotaRequest request = getRequest(AddQuotaRequest.class, interaction);
				request.setServiceID(arguments.serviceID);
				request.setVariantID(arguments.variantID);
				request.setSubscriberNumber(arguments.subscriberNumber);
				request.setMemberNumber(arguments.memberNumber);
				request.setQuota(arguments.quota);
				return vasService.addQuota(context, request);
			}

			case changeQuota:
			{
				ChangeQuotaRequest request = getRequest(ChangeQuotaRequest.class, interaction);
				request.setServiceID(arguments.serviceID);
				request.setVariantID(arguments.variantID);
				request.setSubscriberNumber(arguments.subscriberNumber);
				request.setMemberNumber(arguments.memberNumber);
				request.setOldQuota(arguments.oldQuota);
				request.setNewQuota(arguments.newQuota);
				return vasService.changeQuota(context, request);
			}

			case removeQuota:
			{
				RemoveQuotaRequest request = getRequest(RemoveQuotaRequest.class, interaction);
				request.setServiceID(arguments.serviceID);
				request.setVariantID(arguments.variantID);
				request.setSubscriberNumber(arguments.subscriberNumber);
				request.setMemberNumber(arguments.memberNumber);
				request.setQuota(arguments.quota);
				return vasService.removeQuota(context, request);
			}

			case resetPIN:
			{
				ResetPINRequest request = getRequest(ResetPINRequest.class, interaction);
				request.setServiceID(arguments.serviceID);
				request.setVariantID(arguments.variantID);
				request.setSubscriberNumber(arguments.subscriberNumber);
				return vasService.resetPIN(context, request);
			}

			case changePIN:
			{
				ChangePINRequest request = getRequest(ChangePINRequest.class, interaction);
				request.setServiceID(arguments.serviceID);
				request.setVariantID(arguments.variantID);
				request.setSubscriberNumber(arguments.subscriberNumber);
				request.setOldPIN(arguments.oldPIN);
				request.setNewPIN(arguments.newPIN);
				return vasService.changePIN(context, request);
			}

			case validatePIN:
			{
				ValidatePINRequest request = getRequest(ValidatePINRequest.class, interaction);
				request.setServiceID(arguments.serviceID);
				request.setVariantID(arguments.variantID);
				request.setSubscriberNumber(arguments.subscriberNumber);
				request.setPIN(arguments.pin);
				return vasService.validatePIN(context, request);
			}

			case redeem:
			{
				RedeemRequest request = getRequest(RedeemRequest.class, interaction);
				request.setServiceID(arguments.serviceID);
				request.setVariantID(arguments.variantID);
				request.setSubscriberNumber(arguments.subscriberNumber);
				request.setRecipientNumber(arguments.recipientNumber);
				request.setRedemptionID(arguments.redemptionID);
				return vasService.redeem(context, request);
			}

			case process:
			{
				ProcessRequest request = getRequest(ProcessRequest.class, interaction);
				request.setServiceID(arguments.serviceID);
				request.setVariantID(arguments.variantID);
				request.setProcessID(arguments.processID);
				request.setSubscriberNumber(arguments.subscriberNumber);
				request.setMemberNumber(arguments.memberNumber);
				request.setProcessParameters(arguments.processParameters);
				return vasService.process(context, request);
			}

			case getLocation:
			{
				GetLocationRequest request = getRequest(GetLocationRequest.class, interaction);
				request.setServiceID(arguments.serviceID);
				request.setVariantID(arguments.variantID);
				request.setSubscriberNumber(arguments.subscriberNumber);
				request.setSubjectNumber(arguments.memberNumber);
				request.setRequestCoordinates(arguments.requestCoordinates);
				request.setRequestAddress(arguments.requestAddress);
				return vasService.getLocation(context, request);
			}

		}

		return null;
	}

	// AdB: Do not delete!
	// Trayan: Why not?!

	// public void reflect()
	// {
	// List<String> ignoreList = new ArrayList<String>();
	// ignoreList.add("getServices");
	// ignoreList.add("getReturnCodeText");
	// ignoreList.add("getHistory");
	// ignoreList.add("sendSMS");
	// ignoreList.add("getLocaleSettings");
	// ignoreList.add("process");
	// ignoreList.add("processLifecycleEvent");
	// ignoreList.add("getLocaleSettings");
	//
	// Class<IHxC> ihxc = IHxC.class;
	// Method[] methods = ihxc.getDeclaredMethods();
	//
	// System.out.printf("Method Names\n");
	// for (Method method : methods)
	// {
	// if (ignoreList.contains(method.getName()))
	// continue;
	//
	// Class<?>[] params = method.getParameterTypes();
	// if (params.length != 1)
	// continue;
	// Class<?> param = params[0];
	// if (!RequestHeader.class.isAssignableFrom(param))
	// continue;
	//
	// System.out.printf("%s,\n", method.getName());
	// }
	//
	// System.out.printf("Variable Names\n");
	// List<String> lines = new ArrayList<String>();
	// for (Method method : methods)
	// {
	// if (ignoreList.contains(method.getName()))
	// continue;
	//
	// Class<?>[] params = method.getParameterTypes();
	// if (params.length != 1)
	// continue;
	// Class<?> param = params[0];
	// if (!RequestHeader.class.isAssignableFrom(param))
	// continue;
	//
	// Field[] fields = param.getDeclaredFields();
	// for (Field field : fields)
	// {
	// String line = String.format("public %s %s;", field.getType().getCanonicalName(), field.getName());
	// if (!lines.contains(line))
	// {
	// lines.add(line);
	// logger.info(line);
	// }
	// }
	// }
	//
	// System.out.printf("Dispatchers\n");
	// for (Method method : methods)
	// {
	// if (ignoreList.contains(method.getName()))
	// continue;
	//
	// Class<?>[] params = method.getParameterTypes();
	// if (params.length != 1)
	// continue;
	// Class<?> param = params[0];
	// if (!RequestHeader.class.isAssignableFrom(param))
	// continue;
	//
	// System.out.printf("case %s:\n", method.getName());
	// System.out.printf("{\n");
	// System.out.printf("%sRequest request = vasRequest.getRequest(%sRequest.class);\n", toPascal(method.getName()), toPascal(method.getName()));
	//
	// Field[] fields = param.getDeclaredFields();
	// for (Field field : fields)
	// {
	// System.out.printf("request.set%s(arguments.%s);\n", toPascal(field.getName()), field.getName());
	// }
	//
	// System.out.printf("return vasService.%s(context, request);\n", method.getName());
	// System.out.printf("}\n");
	// System.out.println();
	// }
	//
	// methods = vasService.getClass().getDeclaredMethods();
	//
	// System.out.printf("Local Variable Names\n");
	// lines = new ArrayList<String>();
	// for (Method method : methods)
	// {
	// if (ignoreList.contains(method.getName()))
	// continue;
	//
	// Class<?>[] params = method.getParameterTypes();
	// if (params.length != 2)
	// continue;
	// Class<?> param = params[1];
	// if (!RequestHeader.class.isAssignableFrom(param))
	// continue;
	//
	// Field[] fields = param.getDeclaredFields();
	// for (Field field : fields)
	// {
	// String line = String.format("public %s %s;", field.getType().getCanonicalName(), field.getName());
	// if (!lines.contains(line))
	// {
	// lines.add(line);
	// logger.info(line);
	// }
	// }
	// }
	//
	// }

	private String toPascal(String text)
	{
		if (text == null || text.length() == 0)
			return text;
		return text.substring(0, 1).toUpperCase() + text.substring(1);
	}

}
