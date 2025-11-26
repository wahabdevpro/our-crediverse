package cs.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import cs.config.RestServerConfiguration;
import cs.dto.GuiUssdConfig;
import cs.service.batch.BatchConfigService;
import cs.service.workflow.WorkFlowService;
import cs.template.CsRestTemplate;
import hxc.ecds.protocol.rest.config.AdjudicationConfig;
import hxc.ecds.protocol.rest.config.AdjustmentsConfig;
import hxc.ecds.protocol.rest.config.AgentsConfig;
import hxc.ecds.protocol.rest.config.AnalyticsConfig;
import hxc.ecds.protocol.rest.config.BalanceEnquiriesConfig;
import hxc.ecds.protocol.rest.config.BatchConfig;
import hxc.ecds.protocol.rest.config.BundleSalesConfig;
import hxc.ecds.protocol.rest.config.ChangePinsConfig;
import hxc.ecds.protocol.rest.config.DepositsQueryConfig;
import hxc.ecds.protocol.rest.config.GeneralConfig;
import hxc.ecds.protocol.rest.config.LastTransactionEnquiriesConfig;
import hxc.ecds.protocol.rest.config.Phrase;
import hxc.ecds.protocol.rest.config.RegisterPinsConfig;
import hxc.ecds.protocol.rest.config.ReplenishConfig;
import hxc.ecds.protocol.rest.config.ReportingConfig;
import hxc.ecds.protocol.rest.config.ReversalsConfig;
import hxc.ecds.protocol.rest.config.RewardsConfig;
import hxc.ecds.protocol.rest.config.SalesConfig;
import hxc.ecds.protocol.rest.config.SalesQueryConfig;
import hxc.ecds.protocol.rest.config.SelfTopUpsConfig;
import hxc.ecds.protocol.rest.config.TransactionStatusEnquiriesConfig;
import hxc.ecds.protocol.rest.config.TransactionsConfig;
import hxc.ecds.protocol.rest.config.TransfersConfig;
import hxc.ecds.protocol.rest.config.UssdConfig;
import hxc.ecds.protocol.rest.config.UssdMenu;
import hxc.ecds.protocol.rest.config.WebUsersConfig;
import hxc.ecds.protocol.rest.config.WorkflowConfig;

@Service
public class ConfigurationService {

	final static Logger logger = LoggerFactory.getLogger(ConfigurationService.class);
	
	@Autowired
	AgentService agentService;

	@Autowired
	AgentUserService agentUserService;

	@Autowired
	AnalyticsService analyticsService;
	
	@Autowired
	WebUserService webUserService;

	@Autowired
	BatchConfigService batchConfigService;

	@Autowired
	WorkFlowService workflowService;

	@Autowired //ask @Configuration-marked class for this
	private CsRestTemplate restTemplate;

	@Autowired //ask @Configuration-marked class for this
	private RestServerConfiguration restServerConfig;

	@Autowired
	private UssdMenuConfigService ussdMenuConfigService;

	private boolean configured = false;
	private String restServerUrl;
	private String restServerUrlReporting;

	@PostConstruct
	public void configure()
	{
		if (!configured)
		{
			this.restServerUrl = restServerConfig.getRestServer() + restServerConfig.getTransactionsConfigUrl();
			this.restServerUrlReporting = restServerConfig.getRestServer() + restServerConfig.getReportsUrl();
			configured = true;
		}
	}

	// -------------------------------------------------------------------------
	// Utility Methods
	// -------------------------------------------------------------------------

	private GuiUssdConfig getUssdMenuConfig(List<UssdMenu> menus, Map<String, Map<String, String[]>> variables) throws Exception
	{
		UssdConfig ussdCfg = new UssdConfig();
		ussdCfg.setMenus(menus);
		GuiUssdConfig convertedConfig = ussdMenuConfigService.convertConfig(ussdCfg);
		convertedConfig.setVariables(variables);
		return convertedConfig;
	}

	private String getConfigurationRESTUrl(String path)
	{
		return String.format("%s/%s", restServerUrl, path);
	}

	/**
	 * Extract Notification variables in an easy to use format, also sort out variables per language
	 * result = {
	 * 	en: ['{var1}','{var2}','{var3}'],
	 * 	fr: ['{var1}','{var2}','{var3}']
	 * }
	 */
	private Map<String, String []> extractConfigVariables(Phrase[] variables) {
		String langs = (restServerConfig.getNotificationLanguages() != null)? restServerConfig.getNotificationLanguages() : "en";
		String [] langList = langs.split(",");

		Map<String, String[]> result = new HashMap<>();
		for(String lang : langList)
		{
			result.put(lang, new String [variables.length]);
		}

		for(int i=0; i<variables.length; i++)
		{
			for(String lang : langList)
			{
				result.get(lang)[i] = variables[i].get(lang);
				if (result.get(lang)[i] == null || "".equals(result.get(lang)[i]))
				{
					result.get(lang)[i] = variables[i].get("en");
				}
			}
		}

		return result;
	}
	// -------------------------------------------------------------------------
	// Agents Configuration
	// -------------------------------------------------------------------------

	public AgentsConfig getAgentsConfiguration() throws Exception
	{
		return agentService.getConfiguration();
	}

	public Map<String, Map<String, String []>> getAgentConfigurationVariables()
	{
		Map<String, Phrase[]> agentVariables = agentService.getConfigurationVariables();
		Map<String, Map<String, String []>> result = new HashMap<>();
		for(String comp : agentVariables.keySet())
		{
			result.put(comp, extractConfigVariables(agentVariables.get(comp)));
		}

		return result;
	}


	public void updateAgentsConfiguration(AgentsConfig updatedAgentsConfig) throws Exception
	{
		agentService.updateConfiguration(updatedAgentsConfig);
	}

	// -------------------------------------------------------------------------
	// Adjustments Configuration
	// -------------------------------------------------------------------------

	public AdjustmentsConfig getAdjustmentsConfig() throws Exception
	{
		return restTemplate.execute(getConfigurationRESTUrl("adjust/config"), HttpMethod.GET, AdjustmentsConfig.class);
	}

	public Map<String, Map<String, String []>> getAdjustmentsConfigVariables()
	{
		Map<String, Map<String, String []>> result = new HashMap<>();

		AdjustmentsConfig config = new AdjustmentsConfig();

		result.put("notification", extractConfigVariables( config.listNotificationFields() ));
		result.put("agentNotification", extractConfigVariables( config.listAgentNotificationFields() ));
		result.put("batchNotification", extractConfigVariables( config.listBatchNotificationFields() ));

		return result;
	}

	public void updateAdjustmentsConfig(AdjustmentsConfig updatedAdjustmentsConfig) throws Exception
	{
		restTemplate.execute(getConfigurationRESTUrl("adjust/config"), HttpMethod.PUT, updatedAdjustmentsConfig, Void.class);
	}

	// -------------------------------------------------------------------------
	// Balance Enquiries Configuration
	// -------------------------------------------------------------------------

	public BalanceEnquiriesConfig getBalanceEnquiriesConfig() throws Exception
	{
		return restTemplate.execute(getConfigurationRESTUrl("balance_enquiry/config"), HttpMethod.GET, BalanceEnquiriesConfig.class);
	}

	public Map<String, Map<String, String []>> getBalanceEnquiriesConfigVariables()
	{
		Map<String, Map<String, String []>> result = new HashMap<>();

		BalanceEnquiriesConfig config = new BalanceEnquiriesConfig();
		result.put("notification", extractConfigVariables( config.listNotificationFields() ));
		result.put("notificationForOther", extractConfigVariables( config.listNotificationFields() ));
		result.put("ussdCommand", extractConfigVariables( config.listCommandFields() ));
		result.put("smsCommand", extractConfigVariables( config.listCommandFields() ));
		result.put("ussdForOthersCommand", extractConfigVariables( config.listForOthersCommandFields() ));
		result.put("smsForOthersCommand", extractConfigVariables( config.listForOthersCommandFields() ));

		return result;
	}

	public void updateBalanceEnquiriesConfig(BalanceEnquiriesConfig config) throws Exception
	{
		restTemplate.execute(getConfigurationRESTUrl("balance_enquiry/config"), HttpMethod.PUT, config, Void.class);
	}

	// -------------------------------------------------------------------------
	// Change Pins Configuration
	// -------------------------------------------------------------------------

	public ChangePinsConfig getChangePinsConfig() throws Exception
	{
		return restTemplate.execute(getConfigurationRESTUrl("change_pin/config"), HttpMethod.GET, ChangePinsConfig.class);
	}

	public Map<String, Map<String, String []>> getChangePinsConfigVariables()
	{
		Map<String, Map<String, String []>> result = new HashMap<>();

		ChangePinsConfig config = new ChangePinsConfig();
		result.put("notification", extractConfigVariables( config.listNotificationFields() ));
		result.put("ussdCommand", extractConfigVariables( config.listCommandFields() ));
		result.put("smsCommand", extractConfigVariables( config.listCommandFields() ));

		return result;
	}

	public void updateChangePinsConfig(ChangePinsConfig config) throws Exception
	{
		restTemplate.execute(getConfigurationRESTUrl("change_pin/config"), HttpMethod.PUT, config, Void.class);
	}

	// -------------------------------------------------------------------------
	// Deposits Query Configuration
	// -------------------------------------------------------------------------

	public DepositsQueryConfig getDepositsQueryConfig() throws Exception
	{
		try {
			return restTemplate.execute(getConfigurationRESTUrl("deposits_query/config"), HttpMethod.GET, DepositsQueryConfig.class);
		} catch(Exception e) {
			throw e;
		}

	}

	public Map<String, Map<String, String []>> getDepositsQueryConfigVariables()
	{
		Map<String, Map<String, String []>> result = new HashMap<>();

		DepositsQueryConfig config = new DepositsQueryConfig();
		result.put("notification", extractConfigVariables( config.listNotificationFields() ));
		result.put("response", extractConfigVariables( config.listNotificationFields() ));
		result.put("ussdCommand", extractConfigVariables( config.listCommandFields() ));
		result.put("smsCommand", extractConfigVariables( config.listCommandFields() ));

		return result;
	}

	public void updateDepositsQueryConfig(DepositsQueryConfig config) throws Exception
	{
		restTemplate.execute(getConfigurationRESTUrl("deposits_query/config"), HttpMethod.PUT, config, Void.class);
	}
	
	// -------------------------------------------------------------------------
	// General Configuration
	// -------------------------------------------------------------------------

	public GeneralConfig getGeneralConfig() throws Exception
	{
		try {
			return restTemplate.execute(getConfigurationRESTUrl("general_config"), HttpMethod.GET, GeneralConfig.class);
		} catch(Exception e) {
			throw e;
		}

	}

	public Map<String, Map<String, String []>> getGeneralConfigVariables()
	{
		Map<String, Map<String, String []>> result = new HashMap<>();
		return result;
	}

	public void updateGeneralConfig(GeneralConfig config) throws Exception
	{
		restTemplate.execute(getConfigurationRESTUrl("general_config"), HttpMethod.PUT, config, Void.class);
	}

	// -------------------------------------------------------------------------
	// Last Transaction Enquiries Configuration
	// -------------------------------------------------------------------------

	public LastTransactionEnquiriesConfig getLastTransactionEnquiriesConfig() throws Exception
	{
		return restTemplate.execute(getConfigurationRESTUrl("last_transaction_enquiry/config"), HttpMethod.GET, LastTransactionEnquiriesConfig.class);
	}

	public Map<String, Map<String, String []>> getLastTransactionEnquiriesConfigVariables()
	{
		Map<String, Map<String, String []>> result = new HashMap<>();

		LastTransactionEnquiriesConfig config = new LastTransactionEnquiriesConfig();
		result.put("notification", extractConfigVariables( config.listNotificationFields() ));
		result.put("ussdCommand", extractConfigVariables( config.listCommandFields() ));
		result.put("smsCommand", extractConfigVariables( config.listCommandFields() ));
		result.put("listNotification", extractConfigVariables( config.listListNotificationFields() ));
		result.put("transactionLine", extractConfigVariables( config.listNotificationFields() ));
		result.put("successful", extractConfigVariables( new Phrase[]{} ));
		result.put("failed", extractConfigVariables( new Phrase[]{} ));
		result.put("pending", extractConfigVariables( new Phrase[]{} ));

		return result;
	}

	public void updateLastTransactionEnquiriesConfig(LastTransactionEnquiriesConfig config) throws Exception
	{
		restTemplate.execute(getConfigurationRESTUrl("last_transaction_enquiry/config"), HttpMethod.PUT, config, Void.class);
	}

	// -------------------------------------------------------------------------
	// Register PinsConfig Configuration
	// -------------------------------------------------------------------------

	public RegisterPinsConfig getRegisterPinsConfig() throws Exception
	{
		return restTemplate.execute(getConfigurationRESTUrl("register_pin/config"), HttpMethod.GET, RegisterPinsConfig.class);
	}

	public Map<String, Map<String, String []>> getRegisterPinsConfigVariables()
	{
		Map<String, Map<String, String []>> result = new HashMap<>();

		RegisterPinsConfig config = new RegisterPinsConfig();
		result.put("notification", extractConfigVariables( config.listNotificationFields() ));
		result.put("ussdCommand", extractConfigVariables( config.listCommandFields() ));
		result.put("smsCommand", extractConfigVariables( config.listCommandFields() ));

		return result;
	}

	public void updateRegisterPinsConfig(RegisterPinsConfig config) throws Exception
	{
		restTemplate.execute(getConfigurationRESTUrl("register_pin/config"), HttpMethod.PUT, config, Void.class);
	}

	// -------------------------------------------------------------------------
	// Replenish Configuration
	// -------------------------------------------------------------------------

	public ReplenishConfig getReplenishConfig() throws Exception
	{
		return restTemplate.execute(getConfigurationRESTUrl("replenish/config"), HttpMethod.GET, ReplenishConfig.class);
	}

	public Map<String, Map<String, String []>> getReplenishConfigVariables()
	{
		Map<String, Map<String, String []>> result = new HashMap<>();

		ReplenishConfig config = new ReplenishConfig();
		result.put("notification", extractConfigVariables( config.listNotificationFields() ));

		return result;
	}

	public void updateReplenishConfig(ReplenishConfig config) throws Exception
	{
		restTemplate.execute(getConfigurationRESTUrl("replenish/config"), HttpMethod.PUT, config, Void.class);
	}

	// -------------------------------------------------------------------------
	// Sales Configuration
	// -------------------------------------------------------------------------

	public GuiUssdConfig getSalesUssdMenuConfig() throws Exception
	{
		SalesConfig config = getSalesConfig();
		return getUssdMenuConfig(config.getConfirmationMenus(), getSalesConfigVariables());
	}

	public void updateSalesUssdMenuConfig(UssdConfig guiMenu) throws Exception
	{
		SalesConfig config = getSalesConfig();
		config.setConfirmationMenus(guiMenu.getMenus());
		restTemplate.execute(getConfigurationRESTUrl("sell/config"), HttpMethod.PUT, config, Void.class);
	}

	public GuiUssdConfig getSalesDeduplicationUssdMenuConfig() throws Exception
	{
		SalesConfig config = getSalesConfig();
		return getUssdMenuConfig(config.getDeDuplicationMenus(), getSalesConfigVariables());
	}

	public void updateSalesDeduplicationUssdMenuConfig(UssdConfig guiMenu) throws Exception
	{
		SalesConfig config = getSalesConfig();
		config.setDeDuplicationMenus(guiMenu.getMenus());
		restTemplate.execute(getConfigurationRESTUrl("sell/config"), HttpMethod.PUT, config, Void.class);
	}

	public SalesConfig getSalesConfig() throws Exception
	{
		SalesConfig config = restTemplate.execute(getConfigurationRESTUrl("sell/config"), HttpMethod.GET, SalesConfig.class);
		return config;
	}

	public Map<String, Map<String, String []>> getSalesConfigVariables()
	{
		Map<String, Map<String, String []>> result = new HashMap<>();

		SalesConfig config = new SalesConfig();
		result.put("senderNotification", extractConfigVariables( config.listNotificationFields() ));
		result.put("senderUnknownNotification", extractConfigVariables( config.listNotificationFields() ));
		result.put("senderNotificationPartialRecovery", extractConfigVariables( config.listNotificationFields() ));
		result.put("senderNotificationFullRecovery", extractConfigVariables( config.listNotificationFields() ));
		result.put("recipientNotification", extractConfigVariables( config.listNotificationFields() ));
		result.put("recipientUnknownNotification", extractConfigVariables( config.listNotificationFields() ));
		result.put("recipientNotificationPartialRecovery", extractConfigVariables( config.listNotificationFields() ));
		result.put("recipientNotificationFullRecovery", extractConfigVariables( config.listNotificationFields() ));
		result.put("requesterNotification", extractConfigVariables( config.listNotificationFields() ));
		result.put("ussdCommand", extractConfigVariables( config.listCommandFields() ));
		result.put("smsCommand", extractConfigVariables( config.listCommandFields() ));
		result.put("refillExternalData1", extractConfigVariables( config.listExternalDataFields() ));
		result.put("refillExternalData2", extractConfigVariables( config.listExternalDataFields() ));
		result.put("refillExternalData3", extractConfigVariables( config.listExternalDataFields() ));
		result.put("refillExternalData4", extractConfigVariables( config.listExternalDataFields() ));
		result.put("numberConfirmMessage", extractConfigVariables( new Phrase[]{ config.getNumberConfirmMessage() }));
		result.put("numberErrorMessage", extractConfigVariables( new Phrase[]{ config.getNumberErrorMessage() }));

		return result;
	}

	public void updateSalesConfig(SalesConfig config) throws Exception
	{
		SalesConfig srvConfig = getSalesConfig();
		BeanUtils.copyProperties(config, srvConfig, "confirmationMenus");
		restTemplate.execute(getConfigurationRESTUrl("sell/config"), HttpMethod.PUT, srvConfig, Void.class);
	}

	// -------------------------------------------------------------------------
	// Sales Query Configuration
	// -------------------------------------------------------------------------

	public SalesQueryConfig getSalesQueryConfig() throws Exception
	{
		return restTemplate.execute(getConfigurationRESTUrl("sales_query/config"), HttpMethod.GET, SalesQueryConfig.class);
	}

	public Map<String, Map<String, String []>> getSalesQueryConfigVariables()
	{
		Map<String, Map<String, String []>> result = new HashMap<>();

		SalesQueryConfig config = new SalesQueryConfig();
		result.put("notification", extractConfigVariables( config.listNotificationFields() ));
		result.put("response", extractConfigVariables( config.listNotificationFields() ));
		result.put("ussdCommand", extractConfigVariables( config.listCommandFields() ));
		result.put("smsCommand", extractConfigVariables( config.listCommandFields() ));

		return result;
	}

	public void updateSalesQueryConfig(SalesQueryConfig config) throws Exception
	{
		restTemplate.execute(getConfigurationRESTUrl("sales_query/config"), HttpMethod.PUT, config, Void.class);
	}

	// -------------------------------------------------------------------------
	// Self TopUps Configuration
	// -------------------------------------------------------------------------

	public GuiUssdConfig getSelfTopUpsUssdMenuConfig() throws Exception
	{
		SelfTopUpsConfig config = getSelfTopUpsConfig();
		return getUssdMenuConfig(config.getConfirmationMenus(), getSelfTopUpsConfigVariables());
	}

	public void updateSelfTopUpsUssdMenuConfig(UssdConfig guiMenu) throws Exception
	{
		SelfTopUpsConfig config = getSelfTopUpsConfig();
		config.setConfirmationMenus(guiMenu.getMenus());
		restTemplate.execute(getConfigurationRESTUrl("self_topup/config"), HttpMethod.PUT, config, Void.class);
	}

	public GuiUssdConfig getSelfTopUpsDeduplicationUssdMenuConfig() throws Exception
	{
		SelfTopUpsConfig config = getSelfTopUpsConfig();
		return getUssdMenuConfig(config.getDeDuplicationMenus(), getSelfTopUpsConfigVariables());
	}

	public void updateSelfTopUpsDeduplicationUssdMenuConfig(UssdConfig guiMenu) throws Exception
	{
		SelfTopUpsConfig config = getSelfTopUpsConfig();
		config.setDeDuplicationMenus(guiMenu.getMenus());
		restTemplate.execute(getConfigurationRESTUrl("self_topup/config"), HttpMethod.PUT, config, Void.class);
	}

	public SelfTopUpsConfig getSelfTopUpsConfig() throws Exception
	{
		return restTemplate.execute(getConfigurationRESTUrl("self_topup/config"), HttpMethod.GET, SelfTopUpsConfig.class);
	}

	public Map<String, Map<String, String []>> getSelfTopUpsConfigVariables()
	{
		Map<String, Map<String, String []>> result = new HashMap<>();

		SelfTopUpsConfig config = new SelfTopUpsConfig();
		result.put("senderNotification", extractConfigVariables( config.listNotificationFields() ));
		result.put("senderUnknownNotification", extractConfigVariables( config.listNotificationFields() ));
		result.put("ussdCommand", extractConfigVariables( config.listCommandFields() ));
		result.put("smsCommand", extractConfigVariables( config.listCommandFields() ));
		result.put("refillExternalData1", extractConfigVariables( config.listExternalDataFields() ));
		result.put("refillExternalData2", extractConfigVariables( config.listExternalDataFields() ));
		result.put("refillExternalData3", extractConfigVariables( config.listExternalDataFields() ));
		result.put("refillExternalData4", extractConfigVariables( config.listExternalDataFields() ));

		return result;
	}

	public void updateSelfTopUpsConfig(SelfTopUpsConfig config) throws Exception
	{
		SelfTopUpsConfig srvConfig = getSelfTopUpsConfig();
		BeanUtils.copyProperties(config, srvConfig, "confirmationMenus");
		restTemplate.execute(getConfigurationRESTUrl("self_topup/config"), HttpMethod.PUT, srvConfig, Void.class);
	}

	// -------------------------------------------------------------------------
	// Transactions Configuration
	// -------------------------------------------------------------------------

	public TransactionsConfig getTransactionsConfig() throws Exception
	{
		return restTemplate.execute(getConfigurationRESTUrl("config"), HttpMethod.GET, TransactionsConfig.class);
	}

	public Map<String, Map<String, String []>> getTransactionsConfigVariables()
	{
		Map<String, Map<String, String []>> result = new HashMap<>();
		TransactionsConfig config = new TransactionsConfig();
		result.put("errorMessages", extractConfigVariables( new Phrase[]{} ));
		result.put("tdrDirectory", extractConfigVariables( config.listTdrDirectoryFields() ));
		result.put("tdrFilenameFormat", extractConfigVariables( config.listTdrFilenameFormatFields() ));
		result.put("zipFilenameFormat", extractConfigVariables( config.listZipFilenameFormatFields() ));
		return result;
	}

	public void updateTransactionsConfig(TransactionsConfig config) throws Exception
	{
		// Ensure that empty Phrases are removed
		for (String key : config.getErrorMessages().keySet())
		{
			Phrase message = config.getErrorMessages().get(key);
			Set<String> toRemove = null;
			for(String lang : message.getTexts().keySet())
			{
				if (message.get(lang) != null && message.get(lang).trim().length()==0)
				{
					if (toRemove == null) toRemove = new HashSet<>();
					toRemove.add(lang);
				}
			}

			if (toRemove != null)
			{
				for(String lang : toRemove)
				{
					message.getTexts().remove(lang);
				}
			}
		}

		restTemplate.execute(getConfigurationRESTUrl("config"), HttpMethod.PUT, config, Void.class);
	}

	// -------------------------------------------------------------------------
	// Transaction Status Enquiries Configuration
	// -------------------------------------------------------------------------

	public TransactionStatusEnquiriesConfig getTransactionStatusEnquiriesConfig() throws Exception
	{
		return restTemplate.execute(getConfigurationRESTUrl("transaction_status_enquiry/config"), HttpMethod.GET, TransactionStatusEnquiriesConfig.class);
	}

	public Map<String, Map<String, String []>> getTransactionStatusEnquiriesConfigVariables()
	{
		Map<String, Map<String, String []>> result = new HashMap<>();

		TransactionStatusEnquiriesConfig config = new TransactionStatusEnquiriesConfig();
		result.put("notification", extractConfigVariables( config.listNotificationFields() ));
		result.put("ussdCommand", extractConfigVariables( config.listCommandFields() ));
		result.put("smsCommand", extractConfigVariables( config.listCommandFields() ));

		return result;
	}

	public void updateTransactionStatusEnquiriesConfig(TransactionStatusEnquiriesConfig config) throws Exception
	{
		restTemplate.execute(getConfigurationRESTUrl("transaction_status_enquiry/config"), HttpMethod.PUT, config, Void.class);
	}

	// -------------------------------------------------------------------------
	// Transfers Configuration
	// -------------------------------------------------------------------------

	public TransfersConfig getTransfersConfig() throws Exception
	{
		TransfersConfig config = restTemplate.execute(getConfigurationRESTUrl("transfer/config"), HttpMethod.GET, TransfersConfig.class);
		return config;
	}

	public GuiUssdConfig getTransfersUssdConfig() throws Exception
	{
		TransfersConfig config = getTransfersConfig();
		return getUssdMenuConfig(config.getConfirmationMenus(), getTransfersConfigVariables());
	}

	public void updateTransfersUssdMenuConfig(UssdConfig guiMenu) throws Exception
	{
		TransfersConfig config = getTransfersConfig();
		config.setConfirmationMenus(guiMenu.getMenus());
		restTemplate.execute(getConfigurationRESTUrl("transfer/config"), HttpMethod.PUT, config, Void.class);
	}

	public GuiUssdConfig getTransfersDeduplicationUssdConfig() throws Exception
	{
		TransfersConfig config = getTransfersConfig();
		return getUssdMenuConfig(config.getDeDuplicationMenus(), getTransfersConfigVariables());
	}

	public void updateTransfersDeduplicationUssdMenuConfig(UssdConfig guiMenu) throws Exception
	{
		TransfersConfig config = getTransfersConfig();
		config.setDeDuplicationMenus(guiMenu.getMenus());
		restTemplate.execute(getConfigurationRESTUrl("transfer/config"), HttpMethod.PUT, config, Void.class);
	}

	public Map<String, Map<String, String []>> getTransfersConfigVariables()
	{
		Map<String, Map<String, String []>> result = new HashMap<>();

		TransfersConfig config = new TransfersConfig();

		result.put("senderNotification", extractConfigVariables( config.listNotificationFields() ));
		result.put("recipientNotification", extractConfigVariables( config.listNotificationFields() ));
		result.put("requesterNotification", extractConfigVariables( config.listNotificationFields() ));
		result.put("ussdCommand", extractConfigVariables( config.listCommandFields() ));
		result.put("smsCommand", extractConfigVariables( config.listCommandFields() ));
		result.put("refillExternalData1", extractConfigVariables( config.listExternalDataFields() ));
		result.put("refillExternalData2", extractConfigVariables( config.listExternalDataFields() ));
		result.put("refillExternalData3", extractConfigVariables( config.listExternalDataFields() ));
		result.put("refillExternalData4", extractConfigVariables( config.listExternalDataFields() ));
		result.put("numberConfirmMessage", extractConfigVariables( new Phrase[]{ config.getNumberConfirmMessage() }));
		result.put("numberErrorMessage", extractConfigVariables( new Phrase[]{ config.getNumberErrorMessage() }));

		return result;
	}

	public void updateTransfersConfig(TransfersConfig config) throws Exception
	{
		TransfersConfig svrConfig = getTransfersConfig();
		BeanUtils.copyProperties(config, svrConfig, "confirmationMenus");
		restTemplate.execute(getConfigurationRESTUrl("transfer/config"), HttpMethod.PUT, svrConfig, Void.class);
	}

	// -------------------------------------------------------------------------
	// Batch Configuration
	// -------------------------------------------------------------------------
	public BatchConfig getBatchConfig() throws Exception
	{
		return batchConfigService.getConfiguration();
	}

	public Map<String, Map<String, String []>> getBatchConfigVariables()
	{
		Map<String, Map<String, String []>> result = new HashMap<>();

		return result;
	}

	public void updateBatchConfig(BatchConfig updatedBatchConfig) throws Exception
	{
		batchConfigService.updateConfiguration(updatedBatchConfig);
	}

	// -------------------------------------------------------------------------
	// Reversals Configuration
	// -------------------------------------------------------------------------

	public ReversalsConfig getReversalsConfig() throws Exception
	{
		return restTemplate.execute(getConfigurationRESTUrl("reverse/config"), HttpMethod.GET, ReversalsConfig.class);
	}

	public Boolean isEnabledMobileMoney()
	{
	    return agentUserService.isEnabledMobileMoney(); 
	}

	public Map<String, Map<String, String []>> getReversalsConfigVariables()
	{
		Map<String, Map<String, String []>> result = new HashMap<>();

		ReversalsConfig config = new ReversalsConfig();

		result.put("senderNotification", extractConfigVariables( config.listNotificationFields() ));
		result.put("recipientNotification", extractConfigVariables( config.listNotificationFields() ));
		result.put("requesterNotification", extractConfigVariables( config.listNotificationFields() ));
		result.put("ubadExternalData1", extractConfigVariables( config.listExternalDataFields() ));
		result.put("ubadExternalData2", extractConfigVariables( config.listExternalDataFields() ));
		result.put("enableCoAuthReversal", extractConfigVariables( new Phrase[] {Phrase.en(String.valueOf(config.isEnableCoAuthReversal()))}));
		result.put("enableDedicatedAccountReversal", extractConfigVariables( new Phrase[] {Phrase.en(String.valueOf(config.isEnableDedicatedAccountReversal()))}));

		return result;
	}

	public void updateReversalsConfig(ReversalsConfig config) throws Exception
	{
		restTemplate.execute(getConfigurationRESTUrl("reverse/config"), HttpMethod.PUT, config, Void.class);
	}

	// -------------------------------------------------------------------------
	// Web Users Configuration
	// -------------------------------------------------------------------------

	public WebUsersConfig getWebUsersConfig() throws Exception
	{
		return webUserService.getConfiguration();
	}

	public Map<String, Map<String, String []>> getWebUsersConfigVariables()
	{
		Map<String, Phrase[]> userUsersVariables = webUserService.getConfigurationVariables();
		Map<String, Map<String, String []>> result = new HashMap<>();
		for(String comp : userUsersVariables.keySet())
		{
			result.put(comp, extractConfigVariables(userUsersVariables.get(comp)));
		}

		return result;
	}

	public void updateWebUsersConfig(WebUsersConfig updatedWebUsersConfig) throws Exception
	{
		webUserService.updateConfiguration(updatedWebUsersConfig);
	}

	// -------------------------------------------------------------------------
	// Workflow Configuration
	// -------------------------------------------------------------------------

	public WorkflowConfig getWorkflowConfig() throws Exception
	{
//		String url = restServerConfig.getRestServer() + "/ecds/work_items/config";
		WorkflowConfig config = workflowService.getConfiguration();
		return config;
	}

	public Map<String, Map<String, String[]>> getWorkflowConfigVariables() throws Exception
	{
		Map<String, Map<String, String []>> result = new HashMap<>();

		WorkflowConfig config = new WorkflowConfig();
		result.put("actorNotification", extractConfigVariables( config.listNotificationFields() ));
		result.put("actorOtpNotification", extractConfigVariables( config.listActorOtpNotificationFields() ));
		result.put("ownerNotification", extractConfigVariables( config.listNotificationFields() ));
		result.put("recipientNotification", extractConfigVariables( config.listNotificationFields() ));
		result.put("forPermission", extractConfigVariables( config.listNotificationFields() ));
		result.put("actions", extractConfigVariables( new Phrase[]{} ));
		result.put("types", extractConfigVariables( new Phrase[]{} ));
		return result;
	}

	public void updateWorkflowConfig(WorkflowConfig config) throws Exception
	{
		workflowService.updateConfiguration(config);
	}

	// -------------------------------------------------------------------------
	// Bundle Sales Configuration
	// -------------------------------------------------------------------------

	public GuiUssdConfig getBundleSalesUssdMenuConfig() throws Exception
	{
		BundleSalesConfig config = getBundleSalesConfig();
		return getUssdMenuConfig(config.getConfirmationMenus(), getBundleSalesConfigVariables());
	}

	public void updateBundleSalesUssdMenuConfig(UssdConfig guiMenu) throws Exception
	{
		BundleSalesConfig config = getBundleSalesConfig();
		config.setConfirmationMenus(guiMenu.getMenus());
		restTemplate.execute(getConfigurationRESTUrl("sell_bundle/config"), HttpMethod.PUT, config, Void.class);
	}

	public GuiUssdConfig getBundleSalesDeduplicationUssdMenuConfig() throws Exception
	{
		BundleSalesConfig config = getBundleSalesConfig();
		return getUssdMenuConfig(config.getDeDuplicationMenus(), getBundleSalesConfigVariables());
	}

	public void updateBundleSalesDeduplicationUssdMenuConfig(UssdConfig guiMenu) throws Exception
	{
		BundleSalesConfig config = getBundleSalesConfig();
		config.setDeDuplicationMenus(guiMenu.getMenus());
		restTemplate.execute(getConfigurationRESTUrl("sell_bundle/config"), HttpMethod.PUT, config, Void.class);
	}

	public BundleSalesConfig getBundleSalesConfig() throws Exception
	{
		BundleSalesConfig config = restTemplate.execute(getConfigurationRESTUrl("sell_bundle/config"), HttpMethod.GET, BundleSalesConfig.class);
		return config;
	}

	public Map<String, Map<String, String[]>> getBundleSalesConfigVariables() throws Exception
	{
		Map<String, Map<String, String []>> result = new HashMap<>();

		BundleSalesConfig config = new BundleSalesConfig();

		result.put("senderDebitNotification", extractConfigVariables( config.listNotificationFields() ));
		result.put("recipientCompleteNotification", extractConfigVariables( config.listNotificationFields() ));
		result.put("senderDebitBalanceNotification", extractConfigVariables( config.listNotificationFields() ));
		result.put("senderRefundBalanceNotification", extractConfigVariables( config.listNotificationFields() ));
		result.put("senderRefundNotification", extractConfigVariables( config.listNotificationFields() ));
		result.put("recipientFailedNotification", extractConfigVariables( config.listNotificationFields() ));
		result.put("ussdCommand", extractConfigVariables( config.listUssdCommandFields() ));
		result.put("smsCommand", extractConfigVariables( config.listSmsCommandFields() ));
		result.put("numberConfirmMessage", extractConfigVariables( new Phrase[]{ config.getNumberConfirmMessage() }));
		result.put("numberErrorMessage", extractConfigVariables( new Phrase[]{ config.getNumberErrorMessage() }));
		return result;
	}

	public void updateBundleSalesConfig(BundleSalesConfig config) throws Exception
	{
		BundleSalesConfig srvConfig = getBundleSalesConfig();
		BeanUtils.copyProperties(config, srvConfig, "confirmationMenus");
		restTemplate.execute(getConfigurationRESTUrl("sell_bundle/config"), HttpMethod.PUT, srvConfig, Void.class);
	}

	// -------------------------------------------------------------------------
	// Rewards Configuration
	// -------------------------------------------------------------------------

	public RewardsConfig getRewardsConfig() throws Exception
	{
		return restTemplate.execute(getConfigurationRESTUrl("reward/config"), HttpMethod.GET, RewardsConfig.class);
	}

	public Map<String, Map<String, String []>> getRewardsConfigVariables()
	{
		Map<String, Map<String, String []>> result = new HashMap<>();

		RewardsConfig config = new RewardsConfig();

		result.put("agentNotification", extractConfigVariables( config.listAgentNotificationFields() ));

		return result;
	}

	public void updateRewardsConfig(RewardsConfig updatedRewardsConfig) throws Exception
	{
		restTemplate.execute(getConfigurationRESTUrl("reward/config"), HttpMethod.PUT, updatedRewardsConfig, Void.class);
	}

	// -------------------------------------------------------------------------
	// Analytics Configuration
	// -------------------------------------------------------------------------

	public Map<String, Object> getAnalyticsConfig() throws Exception
	{
		return analyticsService.getConfig();
	}
		
	public void updateAnalyticsConfig(AnalyticsConfig config) throws Exception
	{
		analyticsService.setConfig(config);
	}
	
	// -------------------------------------------------------------------------
	// Reporting Configuration
	// -------------------------------------------------------------------------

	public ReportingConfig getReportingConfig() throws Exception
	{
		return restTemplate.execute(this.restServerUrlReporting + "/config", HttpMethod.GET, ReportingConfig.class);
	}

	public Map<String, Map<String, String []>> getReportingConfigVariables()
	{
		Map<String, Map<String, String []>> result = new HashMap<>();

		ReportingConfig config = new ReportingConfig();

		result.put("salesSummaryReportNotification", extractConfigVariables( config.listSalesSummaryReportNotificationFields() ));
		result.put("mobileMoneyReportNotification", extractConfigVariables( config.listMobileMoneyReportNotificationFields() ));

		if (agentUserService.isEnabledMobileMoney()) {
			result.put("salesSummaryReportEmailSubject", extractConfigVariables( config.listSalesSummaryWithMobileMoneyReportNotificationFields() ));
			result.put("salesSummaryReportEmailBody", extractConfigVariables( config.listSalesSummaryWithMobileMoneyReportNotificationFields() ));
		} else {
			result.put("salesSummaryReportEmailSubject", extractConfigVariables( config.listSalesSummaryReportNotificationFields() ));
			result.put("salesSummaryReportEmailBody", extractConfigVariables( config.listSalesSummaryReportNotificationFields() ));
		}
		result.put("reportEmailSubject", extractConfigVariables( config.listReportEmailSubjectFields() ));
		result.put("reportEmailBody", extractConfigVariables( config.listReportEmailBodyFields() ));

		return result;
	}

	public void updateReportingConfig(ReportingConfig config) throws Exception
	{
		restTemplate.execute(this.restServerUrlReporting + "/config", HttpMethod.PUT, config, Void.class);
	}

	// -------------------------------------------------------------------------
	// Adjudication Configuration
	// -------------------------------------------------------------------------

	public AdjudicationConfig getAdjudicationConfig() throws Exception
	{
		return restTemplate.execute(getConfigurationRESTUrl("adjudicate/config"), HttpMethod.GET, AdjudicationConfig.class);
	}

	public Map<String, Map<String, String []>> getAdjudicationConfigVariables()
	{
		Map<String, Map<String, String []>> result = new HashMap<>();

		AdjudicationConfig config = new AdjudicationConfig();

		result.put("requesterSuccessNotification", extractConfigVariables( config.listNotificationFields() ));
		result.put("requesterFailureNotification", extractConfigVariables( config.listNotificationFields() ));
		result.put("agentSuccessNotification", extractConfigVariables( config.listNotificationFields() ));
		result.put("agentFailureNotification", extractConfigVariables( config.listNotificationFields() ));
		result.put("subscriberSuccessNotification", extractConfigVariables( config.listNotificationFields() ));
		result.put("subscriberFailureNotification", extractConfigVariables( config.listNotificationFields() ));

		return result;
	}

	public void updateAdjudicationConfig(AdjudicationConfig config) throws Exception
	{
		restTemplate.execute(getConfigurationRESTUrl("adjudicate/config"), HttpMethod.PUT, config, Void.class);
	}
}
