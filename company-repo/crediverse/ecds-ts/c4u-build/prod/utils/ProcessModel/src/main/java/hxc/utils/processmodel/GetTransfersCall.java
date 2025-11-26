package hxc.utils.processmodel;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.concurrent.hxc.CreditTransfer;
import com.concurrent.hxc.CreditTransferType;
import com.concurrent.hxc.GetCreditTransfersRequest;
import com.concurrent.hxc.GetCreditTransfersResponse;
import com.concurrent.hxc.Number;

import hxc.servicebus.ReturnCodes;
import hxc.utils.string.StringUtils;

public class GetTransfersCall extends MethodCall
{
	final static Logger logger = LoggerFactory.getLogger(GetTransfersCall.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private IValueT<Number> subscriber;
	private IValueT<String> serviceID;
	private IValueT<String> variantID;
	private IValueT<Number> member;
	private IValueT<String> transferMode;
	private IValueT<Boolean> activeOnly;
	private static final String HAS_TRANSFERS = "HasTransfers";
	private static final String TRANSFERS_LIST = "TransferList";
	private static final String TRANSFERS_NAMES = "TransferNames";
	private static final String TRANSFERS_AVAILABLE = "TransferAvailable";
	private static final String TRANSFERS_VALUES = "TransferValues";
	private static final String TRANSFERS_NUMERATOR = "TransferNumerator";
	private static final String TRANSFERS_DENOMITATOR = "TransferDenominator";
	private static final String TRANSFERS_PIN_MISSING = "TransferPinMissing";

	private IValueT<Boolean> has_Transfers = new IValueT<Boolean>()
	{
		@Override
		public Boolean getValue(IProcessState state)
		{
			return state.get(GetTransfersCall.this, HAS_TRANSFERS);
		}
	};

	private IValueT<Boolean> pinMissing = new IValueT<Boolean>()
	{
		@Override
		public Boolean getValue(IProcessState state)
		{
			return state.get(GetTransfersCall.this, TRANSFERS_PIN_MISSING);
		}
	};

	private IValueT<CreditTransfer[]> transfersList = new IValueT<CreditTransfer[]>()
	{
		@Override
		public CreditTransfer[] getValue(IProcessState state)
		{
			return state.get(GetTransfersCall.this, TRANSFERS_LIST);
		}
	};

	private IValueT<String[]> transferNames = new IValueT<String[]>()
	{
		@Override
		public String[] getValue(IProcessState state)
		{
			return state.get(GetTransfersCall.this, TRANSFERS_NAMES);
		}
	};

	private IValueT<String[]> availableTransferNames = new IValueT<String[]>()
	{
		@Override
		public String[] getValue(IProcessState state)
		{
			return state.get(GetTransfersCall.this, TRANSFERS_AVAILABLE);
		}
	};

	private IValueT<String[]> transferValues = new IValueT<String[]>()
	{
		@Override
		public String[] getValue(IProcessState state)
		{
			return state.get(GetTransfersCall.this, TRANSFERS_VALUES);
		}
	};

	private IValueT<Long> numerator = new IValueT<Long>()
	{
		@Override
		public Long getValue(IProcessState state)
		{
			return state.get(GetTransfersCall.this, TRANSFERS_NUMERATOR);
		}
	};

	private IValueT<Long> demominator = new IValueT<Long>()
	{
		@Override
		public Long getValue(IProcessState state)
		{
			return state.get(GetTransfersCall.this, TRANSFERS_DENOMITATOR);
		}
	};

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public IValueT<Boolean> hasTransfers()
	{

		return has_Transfers;
	}

	public IValueT<CreditTransfer[]> getTransfersList()
	{
		return transfersList;
	}

	public IValueT<String[]> getTransferNames()
	{
		return transferNames;
	}

	public IValueT<String[]> getTransferValues()
	{
		return transferValues;
	}

	public IValueT<String[]> getAvailableTransferNames()
	{
		return availableTransferNames;
	}

	public IValueT<Long> getNumerator()
	{
		return numerator;
	}

	public IValueT<Long> getDemominator()
	{
		return demominator;
	}

	public IValueT<Boolean> isPinMissing()
	{
		return pinMissing;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public GetTransfersCall(Action afterAction, Action errorAction, IValueT<Number> subscriber, IValueT<String> serviceID, //
			IValueT<String> variantID, IValueT<Number> member, //
			IValueT<String> transferMode, IValueT<Boolean> activeOnly)
	{
		super(afterAction, errorAction);
		this.subscriber = subscriber;
		this.serviceID = serviceID;
		this.variantID = variantID;
		this.member = member;
		this.transferMode = transferMode;
		this.activeOnly = activeOnly;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	@Override
	public Action execute(IProcessState state, String command)
	{
		try
		{
			GetCreditTransfersRequest request = state.getRequest(GetCreditTransfersRequest.class);

			request.setServiceID(serviceID.getValue(state));
			if (variantID != null)
				request.setVariantID(variantID.getValue(state));
			request.setSubscriberNumber(subscriber.getValue(state));
			request.setMemberNumber(member.getValue(state));
			request.setActiveOnly(activeOnly.getValue(state));
			if (transferMode != null)
				request.setTransferMode(transferMode.getValue(state));

			GetCreditTransfersResponse response = state.getVasService().getCreditTransfers(state, request);
			state.setLastReturnCode(response.getReturnCode());
			if (response.getReturnCode() == ReturnCodes.unregisteredPin)
				state.set(this, TRANSFERS_PIN_MISSING, true);
			else if (!response.wasSuccess())
				return errorAction;
			else
				state.set(this, TRANSFERS_PIN_MISSING, false);

			CreditTransfer[] transfers = response.getTransfers();

			state.set(this, HAS_TRANSFERS, transfers != null && transfers.length > 0);
			state.set(this, TRANSFERS_LIST, transfers);
			setConverter(state, null);

			// Save Names
			String[] names = new String[transfers != null ? transfers.length : 0];
			int index = 0;
			if (transfers != null)
			{
				for (CreditTransfer transfer : transfers)
				{
					names[index++] = transfer.getName();
					setConverter(state, transfer);
				}
			}
			state.set(this, TRANSFERS_NAMES, names);

			// Save Available Names
			String[] available = new String[transfers != null ? transfers.length : 0];
			index = 0;
			if (transfers != null)
			{
				for (CreditTransfer transfer : transfers)
				{
					if (!transfer.isActive() && transfer.getTransferType()!= CreditTransferType.OnceOff)
						available[index++] = transfer.getName();
				}
			}
			available = Arrays.copyOf(available, index);
			state.set(this, TRANSFERS_AVAILABLE, available);

			// Save Values
			String[] values = new String[transfers != null ? transfers.length : 0];
			index = 0;
			if (transfers != null)
			{
				for (CreditTransfer transfer : transfers)
				{
					String digits = StringUtils.formatScaled(transfer.getAmount(), transfer.getScaleNumerator(), transfer.getScaleDenominator());
					values[index++] = String.format("%s (%s %s)", transfer.getName(), digits, transfer.getUnits());
					setConverter(state, transfer);
				}
			}
			state.set(this, TRANSFERS_VALUES, values);

			return nextAction;
		}
		catch (Throwable e)
		{
			logger.error("execution error", e);
			state.setLastReturnCode(ReturnCodes.technicalProblem);
			return errorAction;
		}
	}

	private void setConverter(IProcessState state, CreditTransfer transfer)
	{
		state.set(this, TRANSFERS_NUMERATOR, transfer != null ? transfer.getScaleNumerator() : null);
		state.set(this, TRANSFERS_DENOMITATOR, transfer != null ? transfer.getScaleDenominator() : null);
	}

}
