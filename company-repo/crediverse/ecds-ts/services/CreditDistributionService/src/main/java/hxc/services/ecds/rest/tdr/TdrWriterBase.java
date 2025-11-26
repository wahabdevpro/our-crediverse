package hxc.services.ecds.rest.tdr;

import static hxc.ecds.protocol.rest.Transaction.Type.Code.NON_AIRTIME_DEBIT;
import static hxc.ecds.protocol.rest.Transaction.Type.Code.NON_AIRTIME_REFUND;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.ecds.protocol.rest.Agent;
import hxc.ecds.protocol.rest.Tier;
import hxc.ecds.protocol.rest.config.TransactionsConfig;
import hxc.services.ecds.CompanyInfo;
import hxc.services.ecds.Session;
import hxc.services.ecds.model.Area;
import hxc.services.ecds.model.Cell;
import hxc.services.ecds.model.Group;
import hxc.services.ecds.model.Promotion;
import hxc.services.ecds.model.ServiceClass;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.model.TransactionLocation;
import hxc.services.ecds.rest.ICreditDistribution;
import hxc.services.ecds.rest.batch.CsvExportProcessor;
import hxc.services.ecds.util.EntityManagerEx;

public abstract class TdrWriterBase
{
	final static Logger logger = LoggerFactory.getLogger(TdrWriterBase.class);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private String timeFormat = "yyyyMMdd'T'HHmmss";
	private static final BigDecimal HUNDRED = new BigDecimal(100);

	public static final String[] HEADINGS = new String[] { 
			"hostname", //
			"transaction_no", //
			"transaction_type", //
			"channel", //
			"caller_id", //
			"start_time", //
			"end_time", //
			"inbound_transaction_id", //
			"inbound_session_id", //
			"request_mode", //
			"a_party_id", //
			"a_msisdn", //
			"a_tier", //
			"a_service_class", //
			"a_group", //
			"a_owner", //
			"a_area", //
			"a_imsi", //
			"a_imei", //
			"a_cell_id", //
			"a_balance_before", //
			"a_balance_after", //
			"b_party_id", //
			"b_msisdn", //
			"b_tier", //
			"b_service_class", //
			"b_group", //
			"b_owner", //
			"b_area", //
			"b_imsi", //
			"b_imei", //
			"b_cellid", //
			"b_balance_before", //
			"b_balance_after", //
			"amount", //
			"buyer_trade_bonus", //
			"buyer_bonus_percentage", //
			"buyer_bonus_provision", //
			"gross_sales_amount", //
			"cogs", //
			"future_use_1", //
			"origin_channel", //
			"return_code", //
			"last_external_result_code", //
			"rolled_back", //
			"follow_up", //
			"bundle", //
			"promotion", //
			"requester_msisdn", //
			"requester_type", //
			"a_hold_balance_before", //
			"a_hold_balance_after", //
			"original_tid", //
			"additional_information", //
			"b_transfer_bonus_amount", //
			"b_transfer_bonus_profile", //
			"a_cgi",
			"a_gps",
	};

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected ConcurrentHashMap<Integer, TdrFile> tdrFiles = new ConcurrentHashMap<Integer, TdrFile>();
	protected ICreditDistribution context;
	
	private static String serverName;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	protected TdrWriterBase(ICreditDistribution context)
	{
		this.context = context;
	}
	
	static
	{
		try
		{
			InetAddress ip = InetAddress.getLocalHost();
			String name = ip.getHostName();
			int index = name.indexOf('.');
			if (index > 0)
				name = name.substring(0, index);
			serverName = name;
		}
		catch (Exception ex)
		{
			logger.warn("Failed to get hostname, defaulting to 'localhost'", ex);
			serverName = "localhost";
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	public void write(Transaction transaction)
	{
		TransactionsConfig config = getConfig(transaction.getCompanyID());
		// Build Record
		StringBuilder sb = new StringBuilder();

		// 1: Hostname
		sb.append(CsvExportProcessor.toText(transaction.getHostname()));

		// 2: TransactionID
		sb.append(',');
		sb.append(CsvExportProcessor.toText(transaction.getNumber()));

		// 3: TransactionType
		sb.append(',');
		sb.append(CsvExportProcessor.toText(transaction.getType()));

		// 4: Channel
		sb.append(',');
		String channel = transaction.getChannel();
		switch (channel)
		{
			case Session.CHANNEL_WUI:
				channel = "WUI";
				break;

			case Session.CHANNEL_3PP:
				channel = "API";
				break;

			case Session.CHANNEL_BATCH:
				channel = "BATCH";
				break;

			case Session.CHANNEL_SMART_DEVICE:
				channel = "APP";
				break;

			case Session.CHANNEL_SMS:
				channel = "SMS";
				break;

			case Session.CHANNEL_USSD:
				channel = "USSD";
				break;
		}
		sb.append(CsvExportProcessor.toText(channel));

		// 5: CallerID
		sb.append(',');
		sb.append(CsvExportProcessor.toText(transaction.getCallerID()));

		// 6: StartTime
		sb.append(',');
		sb.append(CsvExportProcessor.toText(transaction.getStartTime()));

		// 7: EndTime
		sb.append(',');
		sb.append(CsvExportProcessor.toText(transaction.getEndTime()));

		// 8: InboundTransactionID
		sb.append(',');
		sb.append(CsvExportProcessor.toText(transaction.getInboundTransactionID()));

		// 9: InboundSessionID
		sb.append(',');
		sb.append(CsvExportProcessor.toText(transaction.getInboundSessionID()));

		// 10: RequestMode
		sb.append(',');
		sb.append(CsvExportProcessor.toText(transaction.getRequestMode()));

		// 11: A_PartyID
		sb.append(',');
		Agent agent = transaction.getA_Agent();
		sb.append(agent == null ? CsvExportProcessor.toText(transaction.getA_MSISDN()) : CsvExportProcessor.toText(agent.getAccountNumber()));

		// 12: A_MSISDN
		sb.append(',');
		sb.append(CsvExportProcessor.toText(transaction.getA_MSISDN()));

		// 13: A_Tier
		sb.append(',');
		Tier tier = transaction.getA_Tier();
		if (tier != null)
			sb.append(CsvExportProcessor.toText(tier.getName()));

		// 14: A_ServiceClass
		sb.append(',');
		ServiceClass sc = transaction.getA_ServiceClass();
		if (sc != null)
			sb.append(CsvExportProcessor.toText(sc.getName()));

		// 15: A_Group
		sb.append(',');
		Group group = transaction.getA_Group();
		if (group != null)
			sb.append(CsvExportProcessor.toText(group.getName()));

		// 16: A_Owner
		sb.append(',');
		Agent owner = transaction.getA_Owner();
		if (owner != null)
			sb.append(CsvExportProcessor.toText(owner.getMobileNumber()));

		// 17: A_Area
		sb.append(',');
		Area area = transaction.getA_Area();
		if (area != null)
			sb.append(CsvExportProcessor.toText(area.getName()));

		// 18: A_IMSI
		sb.append(',');
		sb.append(CsvExportProcessor.toText(transaction.getA_IMSI()));

		// 19: A_IMEI
		sb.append(',');
		sb.append(CsvExportProcessor.toText(transaction.getA_IMEI()));

		// 20: A_CellID
		sb.append(',');
		sb.append(CsvExportProcessor.toText(transaction.getA_HlrCellID()));
		
		// 21: A_BalanceBefore
		sb.append(',');
		sb.append(CsvExportProcessor.toText(transaction.getA_BalanceBefore()));

		// 22: A_BalanceAfter
		sb.append(',');
		sb.append(CsvExportProcessor.toText(transaction.getA_BalanceAfter()));

		// 23: B_PartyID
		sb.append(',');
		agent = transaction.getB_Agent();
		sb.append(agent == null ? CsvExportProcessor.toText(transaction.getB_MSISDN()) : CsvExportProcessor.toText(agent.getAccountNumber()));

		// 24: B_MSISDN
		sb.append(',');
		sb.append(CsvExportProcessor.toText(transaction.getB_MSISDN()));

		// 25: B_Tier
		sb.append(',');
		tier = transaction.getB_Tier();
		if (tier != null)
			sb.append(CsvExportProcessor.toText(tier.getName()));

		// 26: B_ServiceClass
		sb.append(',');
		sc = transaction.getB_ServiceClass();
		if (sc != null)
			sb.append(CsvExportProcessor.toText(sc.getName()));

		// 27: B_Group
		sb.append(',');
		group = transaction.getB_Group();
		if (group != null)
			sb.append(CsvExportProcessor.toText(group.getName()));

		// 28: B_Owner
		sb.append(',');
		owner = transaction.getB_Owner();
		if (owner != null)
			sb.append(CsvExportProcessor.toText(owner.getMobileNumber()));

		// 29: B_Area
		sb.append(',');
		area = transaction.getB_Area();
		if (area != null)
			sb.append(CsvExportProcessor.toText(area.getName()));

		// 30: B_IMSI
		sb.append(',');
		sb.append(CsvExportProcessor.toText(transaction.getB_IMSI()));

		// 31: B_IMEI
		sb.append(',');
		sb.append(CsvExportProcessor.toText(transaction.getB_IMEI()));

		// 32: B_CellID
		sb.append(',');
		sb.append(CsvExportProcessor.toText(transaction.getB_HlrCellID()));

		// 33: B_BalanceBefore
		sb.append(',');
		sb.append(CsvExportProcessor.toText(transaction.getB_BalanceBefore()));

		// 34: B_BalanceAfter
		sb.append(',');
		sb.append(CsvExportProcessor.toText(transaction.getB_BalanceAfter()));

		// 35: Amount
		sb.append(',');
		sb.append(CsvExportProcessor.toText(transaction.getAmount()));

		// 36: Buyer Trade Bonus
		sb.append(',');
		sb.append(CsvExportProcessor.toText(transaction.getBuyerTradeBonusAmount()));

		// 37: Buyer Bonus Percentage
		sb.append(',');
		sb.append(CsvExportProcessor.toText(transaction.getBuyerTradeBonusPercentage() == null ? null : transaction.getBuyerTradeBonusPercentage().multiply(HUNDRED)));

		// 38: Buyer Bonus Provision
		sb.append(',');
		sb.append(CsvExportProcessor.toText(transaction.getBuyerTradeBonusProvision()));

		if (config.getTdrStructureVersion().equals(TransactionsConfig.TDR_STRUCTURE_VERSION_2)) {
			// 39: Retail Charge
			sb.append(',');
			sb.append(CsvExportProcessor.toText(transaction.getGrossSalesAmount()));

			// 40: Cost of Goods Sold
			sb.append(',');
			sb.append(CsvExportProcessor.toText(transaction.getCostOfGoodsSold()));

			// 41: Future Use
			sb.append(',');
		}

		// 42: ChargeLevied
		sb.append(',');
		sb.append(CsvExportProcessor.toText(transaction.getChannelType()));

		// 43: ReturnCode
		sb.append(',');
		sb.append(CsvExportProcessor.toText(transaction.getReturnCode()));

		// 44: LastExternalResultCode
		sb.append(',');
		sb.append(CsvExportProcessor.toText(transaction.getLastExternalResultCode()));

		// 45: RolledBack
		sb.append(',');
		sb.append(CsvExportProcessor.toText(transaction.isRolledBack()));

		// 46: FollowUp
		sb.append(',');
		sb.append(CsvExportProcessor.toText(transaction.isFollowUp()));

		// 47: Bundle
		sb.append(',');
		if (NON_AIRTIME_DEBIT.equals(transaction.getType()) || NON_AIRTIME_REFUND.equals(transaction.getType()) ) {
			try (EntityManagerEx em = context.getEntityManager()) {
				sb.append(transaction.getNonAirtimeItemDescription(em));
			}
		} else {
			sb.append(transaction.fullBundleName());
		}

		// 48: Promotion
		sb.append(',');
		Promotion promotion = transaction.getPromotion();
		if (promotion != null)
			sb.append(CsvExportProcessor.toText(promotion.getName()));

		// 49: Requester MSISDN
		sb.append(',');
		sb.append(CsvExportProcessor.toText(transaction.getRequesterMSISDN()));

		// 50: Requester Type
		sb.append(',');
		String requesterType = transaction.getRequesterType();
		switch (requesterType)
		{
			case Transaction.REQUESTER_TYPE_AGENT:
				requesterType = "Agent";
				break;

			case Transaction.REQUESTER_TYPE_AGENT_USER:
				requesterType = "Agent User";
				break;

			case Transaction.REQUESTER_TYPE_WEB_USER:
				requesterType = "Web User";
				break;

			case Transaction.REQUESTER_TYPE_SERVICE_USER:
				requesterType = "Service User";
				break;

		}
		sb.append(CsvExportProcessor.toText(requesterType));

		// 51: OnHoldBalanceBefore
		sb.append(',');
		sb.append(CsvExportProcessor.toText(transaction.getA_OnHoldBalanceBefore()));

		// 52: OnHoldBalanceAfter
		sb.append(',');
		sb.append(CsvExportProcessor.toText(transaction.getA_OnHoldBalanceAfter()));

		// 53: Reversed/Adjudicated Transaction ID
		sb.append(','); 
		Transaction original = transaction.getOriginalTransaction();
		if (original != null)
			sb.append(original.getNumber());

		// 54: AdditionalInformation
		sb.append(',');
		sb.append(CsvExportProcessor.toText(transaction.getAdditionalInformation()));
		
		// 55: TransferBonusAmount
		sb.append(',');
		sb.append(CsvExportProcessor.toText(transaction.getB_TransferBonusAmount()));

		// 56: TransferBonusProfile
		sb.append(',');
		sb.append(CsvExportProcessor.toText(transaction.getB_TransferBonusProfile()));

		// 57: A_CGI
		sb.append(',');
		sb.append(CsvExportProcessor.toText(Cell.formatCgi(transaction.getA_Cell())));

		// 58: A_GPS
		sb.append(',');
		try (EntityManagerEx em = context.getEntityManager()) {
			// Format => latitude|longitude
			sb.append(CsvExportProcessor.toText(TransactionLocation.formatLocation(TransactionLocation.findGPSByTransactionId(em, transaction.getId()))));
		}
		sb.append('\n');

		// Write to file
		try
		{
			byte[] line = sb.toString().getBytes("UTF-8");
			while (true)
			{
				TdrFile tdrFile = getTdrFile(transaction.getCompanyID());
				if (tdrFile == null)
				{
					logger.error("Failed to acquire new TDR file for Transaction {}", transaction.getNumber());
					return;
				}

				synchronized (tdrFile)
				{
					FileOutputStream outputStream = tdrFile.outputStream;
					if (outputStream == null)
					{
						logger.trace("TDR descriptor closed. Retrying... {}", transaction.getNumber());
						continue;
					}
					outputStream.write(line, 0, line.length);
					outputStream.flush();
					break;
				}
			}
		}
		catch (IOException ex)
		{
			logger.error(String.format("Failed to write TDR file for Transaction {}", transaction.getNumber()), ex);
		}

	}
	
	// Get reference to a TDR file
	private TdrFile getTdrFile(int companyID)
	{
		TdrFile tdrFile = tdrFiles.get(companyID);
		if (tdrFile != null)
			return tdrFile;

		// Create a new TDR file if one is not available
		try
		{
			// Get Configuration
			TransactionsConfig config = getConfig(companyID);
			if (config == null)
				return null;

			// Get the TDR Directory
			Date now = new Date();
			String directoryName = expand(config.getTdrDirectory(), companyID, now, null);
			File folder = new File(directoryName);
			folder.mkdirs();

			// Make the filename
			String filename = expand(config.getTdrFilenameFormat(), companyID, now, null);
			File file = new File(folder, filename);

			return createTdrFile(companyID, file);
		}
		catch (IOException ex)
		{
			logger.error("getTdrFile", ex);
		}

		return tdrFile;
	}

	// Create a new Log File for a Company
	private TdrFile createTdrFile(int companyID, File file) throws IOException, FileNotFoundException
	{
		synchronized (tdrFiles)
		{
			// Return Tdr File if it has been acquired
			TdrFile tdrFile = tdrFiles.get(companyID);
			if (tdrFile != null)
				return tdrFile;

			// Create the FileOutputStream
			file.createNewFile();
			tdrFile = new TdrFile();
			tdrFile.filename = file.getAbsolutePath();
			tdrFile.outputStream = new FileOutputStream(file);
			tdrFile.openTime = System.currentTimeMillis();
			tdrFile.companyID = companyID;
			tdrFiles.put(companyID, tdrFile);

			return tdrFile;
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////

	// Get Transaction Configuration for a Company
	protected TransactionsConfig getConfig(int companyID)
	{
		CompanyInfo companyInfo = context.findCompanyInfoByID(companyID);
		TransactionsConfig config = companyInfo.getConfiguration(null, TransactionsConfig.class);
		if (config != null)
			return config;
		try (EntityManagerEx em = context.getEntityManager())
		{
			config = companyInfo.getConfiguration(em, TransactionsConfig.class);
			return config;
		}
		catch (Throwable tr)
		{
			logger.info("getConfig", tr);
			return null;
		}
	}

	protected String expand(String text, int companyID, Date date, String filename)
	{
		return expand(text, companyID, date == null ? (String) null : new SimpleDateFormat(timeFormat).format(date), filename);
	}

	protected String expand(String text, int companyID, String date, String filename)
	{
		String[] parts = text.split("(?=\\{)|(?<=\\})");
		StringBuilder builder = new StringBuilder(text.length() << 2);
		for (String part : parts)
		{

			switch (part)
			{
				case TransactionsConfig.COMPANY_ID:
					builder.append(String.format("%03d", companyID));
					break;

				case TransactionsConfig.TIME_STAMP:
					if (date != null)
						builder.append(date);
					break;
					
				case TransactionsConfig.SERVER_NAME:
					if (date != null)
						builder.append(serverName);					
					break;

				case TransactionsConfig.FILENAME:
					if (filename != null)
						builder.append(filename);
					break;

				default:
					builder.append(part);
			}

		}
		return builder.toString();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Classes
	//
	// /////////////////////////////////
	protected class TdrFile
	{
		int companyID;
		long openTime;
		FileOutputStream outputStream = null;
		String filename;
	}

}
