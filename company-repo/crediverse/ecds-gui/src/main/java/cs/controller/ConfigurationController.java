package cs.controller;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

import cs.config.ApplicationDetailsConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cs.dto.GuiTransactionsConfig;
import cs.dto.GuiTransfersConfig;
import cs.dto.config.GuiAgentsConfig;
import cs.dto.config.GuiAnalyticsConfig;
import cs.dto.config.GuiRewardsConfig;
import cs.dto.error.GuiValidationException;
import cs.service.ConfigurationService;
import cs.service.TypeConvertorService;
import hxc.ecds.protocol.rest.Violation;
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
import hxc.ecds.protocol.rest.config.WebUsersConfig;
import hxc.ecds.protocol.rest.config.WorkflowConfig;

@RestController
@RequestMapping("/api/config")
public class ConfigurationController
{
	@Autowired
	private ConfigurationService configService;

	@Autowired
	private TypeConvertorService typeConvertorService;

	@Autowired
	private ApplicationDetailsConfiguration appConfig;
	
	final static Logger logger = LoggerFactory.getLogger(ConfigurationController.class);

	// -------------------------------------------------------------------------
	// Adjustments Configuration
	// -------------------------------------------------------------------------

	@RequestMapping(value="adjustments", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public AdjustmentsConfig getAdjustmentsConfig() throws Exception
	{
		AdjustmentsConfig config = configService.getAdjustmentsConfig();
		return config;
	}

	@RequestMapping(value="adjustments/vars", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Map<String, String []>> getAdjustmentsConfigVariables() throws Exception
	{
		return configService.getAdjustmentsConfigVariables();
	}

	@RequestMapping(value="adjustments", method = RequestMethod.POST)
	public AdjustmentsConfig updateAdjustmentsConfig(@RequestBody(required = true) AdjustmentsConfig updatedAdjustmentsConfig, Locale locale) throws Exception
	{
		configService.updateAdjustmentsConfig(updatedAdjustmentsConfig);
		return configService.getAdjustmentsConfig();
	}

	// -------------------------------------------------------------------------
	// Agents Configuration
	// -------------------------------------------------------------------------

	@RequestMapping(value="agents", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiAgentsConfig getAgentConfiguration() throws Exception
	{
		return new GuiAgentsConfig(configService.getAgentsConfiguration());
	}

	@RequestMapping(value="agents/vars", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Map<String, String []>> getAgentConfigVariables() throws Exception
	{
		return configService.getAgentConfigurationVariables();
	}

	@RequestMapping(value="agents", method = RequestMethod.POST)
	public String updateAgentsConfig(@RequestBody(required = true) GuiAgentsConfig updatedAgentsConfig, Locale locale) throws Exception
	{
		AgentsConfig config = updatedAgentsConfig.exportAgentsConfig();
		configService.updateAgentsConfiguration(config);
		return "{}";
	}

	// -------------------------------------------------------------------------
	// Balance Enquiries Configuration
	// -------------------------------------------------------------------------


	@RequestMapping(value="balance_enquiries", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public BalanceEnquiriesConfig getBalanceEnquiriesConfig() throws Exception
	{
		return configService.getBalanceEnquiriesConfig();
	}

	@RequestMapping(value="balance_enquiries/vars", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Map<String, String []>>  getBalanceEnquiriesConfigVariables() throws Exception
	{
		return configService.getBalanceEnquiriesConfigVariables();
	}

	@RequestMapping(value="balance_enquiries", method = RequestMethod.POST)
	public String updateBalanceEnquiriesConfig(@RequestBody(required = true) BalanceEnquiriesConfig config, Locale locale) throws Exception
	{
		configService.updateBalanceEnquiriesConfig(config);
		return "{}";
	}

	// -------------------------------------------------------------------------
	// Batch Configuration
	// -------------------------------------------------------------------------


	@RequestMapping(value="batch", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public BatchConfig getBatchConfig() throws Exception
	{
		return configService.getBatchConfig();
	}

	@RequestMapping(value="batch/vars", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Map<String, String []>>  getBatchConfigVariables() throws Exception
	{
		return configService.getBatchConfigVariables();
	}

	@RequestMapping(value="batch", method = RequestMethod.POST)
	public String upgradeBatchConfig(@RequestBody(required = true) BatchConfig config, Locale locale) throws Exception
	{
		configService.updateBatchConfig(config);
		return "{}";
	}


	// -------------------------------------------------------------------------
	// Change PINs Configuration
	// -------------------------------------------------------------------------

	@RequestMapping(value="change_pins", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ChangePinsConfig getChangePinsConfig() throws Exception
	{
		return configService.getChangePinsConfig();
	}

	@RequestMapping(value="change_pins/vars", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Map<String, String []>> getChangePinsConfigVariables() throws Exception
	{
		return configService.getChangePinsConfigVariables();
	}

	@RequestMapping(value="change_pins", method = RequestMethod.POST)
	public String updateChangePinsConfig(@RequestBody(required = true) ChangePinsConfig config, Locale locale) throws Exception
	{
		configService.updateChangePinsConfig(config);
		return "{}";
	}

	// -------------------------------------------------------------------------
	// Deposits Query Configuration
	// -------------------------------------------------------------------------

	@RequestMapping(value="deposits_query", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public DepositsQueryConfig getDepositsQueryConfig() throws Exception
	{
		return configService.getDepositsQueryConfig();
	}

	@RequestMapping(value="deposits_query/vars", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Map<String, String []>> getDepositsQueryConfigVariables() throws Exception
	{
		return configService.getDepositsQueryConfigVariables();
	}

	@RequestMapping(value="deposits_query", method = RequestMethod.POST)
	public String updateDepositsQueryConfig(@RequestBody(required = true) DepositsQueryConfig config, Locale locale) throws Exception
	{
		configService.updateDepositsQueryConfig(config);
		return "{}";
	}
	
	// -------------------------------------------------------------------------
	// General Configuration
	// -------------------------------------------------------------------------

	@RequestMapping(value="general_config", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GeneralConfig getGeneralConfig() throws Exception
	{
		return configService.getGeneralConfig();
	}

	@RequestMapping(value="general_config/vars", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Map<String, String []>> getGeneralConfigVariables() throws Exception
	{
		return configService.getGeneralConfigVariables();
	}

	@RequestMapping(value="general_config", method = RequestMethod.POST)
	public String updateGeneralConfig(@RequestBody(required = true) GeneralConfig config, Locale locale) throws Exception
	{
		configService.updateGeneralConfig(config);
		return "{}";
	}
	
	// -------------------------------------------------------------------------
	// Last Transaction Enquiries Configuration
	// -------------------------------------------------------------------------

	@RequestMapping(value="last_Transaction_enquiries", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public LastTransactionEnquiriesConfig getLastTransactionEnquiriesConfig() throws Exception
	{
		return configService.getLastTransactionEnquiriesConfig();
	}

	@RequestMapping(value="last_Transaction_enquiries/vars", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Map<String, String []>> getLastTransactionEnquiriesConfigVariables() throws Exception
	{
		return configService.getLastTransactionEnquiriesConfigVariables();
	}

	@RequestMapping(value="last_Transaction_enquiries", method = RequestMethod.POST)
	public String updateLastTransactionEnquiriesConfig(@RequestBody(required = true) LastTransactionEnquiriesConfig config, Locale locale) throws Exception
	{
		configService.updateLastTransactionEnquiriesConfig(config);
		return "{}";
	}

	// -------------------------------------------------------------------------
	// Register Pins Configuration
	// -------------------------------------------------------------------------

	@RequestMapping(value="register_pins", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public RegisterPinsConfig getRegisterPinsConfig() throws Exception
	{
		return configService.getRegisterPinsConfig();
	}

	@RequestMapping(value="register_pins/vars", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Map<String, String []>> getRegisterPinsConfigVariables() throws Exception
	{
		return configService.getRegisterPinsConfigVariables();
	}

	@RequestMapping(value="register_pins", method = RequestMethod.POST)
	public String updateRegisterPinsConfig(@RequestBody(required = true) RegisterPinsConfig config, Locale locale) throws Exception
	{
		configService.updateRegisterPinsConfig(config);
		return "{}";
	}

	// -------------------------------------------------------------------------
	// Replenish Configuration
	// -------------------------------------------------------------------------

	@RequestMapping(value="replenish", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ReplenishConfig getReplenishConfig() throws Exception
	{
		return configService.getReplenishConfig();
	}

	@RequestMapping(value="replenish/vars", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Map<String, String []>> getReplenishConfigVariables() throws Exception
	{
		return configService.getReplenishConfigVariables();
	}

	@RequestMapping(value="replenish", method = RequestMethod.POST)
	public String updateReplenishConfig(@RequestBody(required = true) ReplenishConfig config, Locale locale) throws Exception
	{
		configService.updateReplenishConfig(config);
		return "{}";
	}

	// -------------------------------------------------------------------------
	// Reversal Configuration
	// -------------------------------------------------------------------------

	@RequestMapping(value="reversal", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ReversalsConfig getReversalsConfig() throws Exception
	{
		return configService.getReversalsConfig();
	}

	/**
	 * This endpoint is used to expose some application properties to the front end.
	 * @return Application properties in JSON.
	 */
	@RequestMapping(value = "app_properties", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getAppProperties() {
		return "{\"showDisregardBonusOption\":" + appConfig.isShowDisregardBonusOption() + "}";
	}

	@RequestMapping(value="reversal/vars", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Map<String, String []>> getReversalsConfigVariables() throws Exception
	{
		return configService.getReversalsConfigVariables();
	}

	@RequestMapping(value="reversal", method = RequestMethod.POST)
	public String updateReversalsConfig(@RequestBody(required = true) ReversalsConfig config, Locale locale) throws Exception
	{
		configService.updateReversalsConfig(config);
		return "{}";
	}


	// -------------------------------------------------------------------------
	// Sales Configuration
	// -------------------------------------------------------------------------

	@RequestMapping(value="sales", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public SalesConfig getSalesConfig() throws Exception
	{
		return configService.getSalesConfig();
	}

	@RequestMapping(value="sales/vars", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Map<String, String []>> getSalesConfigVariables() throws Exception
	{
		return configService.getSalesConfigVariables();
	}

	@RequestMapping(value="sales", method = RequestMethod.POST)
	public String updateSalesConfig(@RequestBody(required = true) SalesConfig config, Locale locale) throws Exception
	{
		configService.updateSalesConfig(config);
		return "{}";
	}

	// -------------------------------------------------------------------------
	// Sales Query Configuration
	// -------------------------------------------------------------------------

	@RequestMapping(value="sales_queries", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public SalesQueryConfig getSalesQueryConfig() throws Exception
	{
		return configService.getSalesQueryConfig();
	}

	@RequestMapping(value="sales_queries/vars", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Map<String, String []>> getSalesQueryConfigVariables() throws Exception
	{
		return configService.getSalesQueryConfigVariables();
	}

	@RequestMapping(value="sales_queries", method = RequestMethod.POST)
	public String updateSalesQueryConfig(@RequestBody(required = true) SalesQueryConfig config, Locale locale) throws Exception
	{
		configService.updateSalesQueryConfig(config);
		return "{}";
	}

	// -------------------------------------------------------------------------
	// Self Top Ups Configuration
	// -------------------------------------------------------------------------

	@RequestMapping(value="self_topups", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public SelfTopUpsConfig getSelfTopUpsConfig() throws Exception
	{
		return configService.getSelfTopUpsConfig();
	}

	@RequestMapping(value="self_topups/vars", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Map<String, String []>> getSelfTopUpsConfigVariables() throws Exception
	{
		return configService.getSelfTopUpsConfigVariables();
	}

	@RequestMapping(value="self_topups", method = RequestMethod.POST)
	public String updateSelfTopUpsConfig(@RequestBody(required = true) SelfTopUpsConfig config, Locale locale) throws Exception
	{
		configService.updateSelfTopUpsConfig(config);
		return "{}";
	}

	// -------------------------------------------------------------------------
	// Transactions Configuration
	// -------------------------------------------------------------------------

	@RequestMapping(value="transactions", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiTransactionsConfig getTransactionsConfig() throws Exception
	{
		return typeConvertorService.getGuiTransactionsConfigFromTransactionsConfig( configService.getTransactionsConfig() );
	}

	@RequestMapping(value="transactions/vars", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Map<String, String []>> getTransactionsConfigVariables() throws Exception
	{
		return configService.getTransactionsConfigVariables();
	}

	@RequestMapping(value="transactions", method = RequestMethod.POST)
	public String updateTransactionsConfig(@RequestBody(required = true) GuiTransactionsConfig config, Locale locale) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();
		TransactionsConfig transactionsConfig = typeConvertorService.getTransactionsConfigFromGuiTransactionsConfig(config, violations);
		if (violations.size() > 0)
			throw new GuiValidationException(violations);
		configService.updateTransactionsConfig(transactionsConfig);
		return "{}";
	}

	// -------------------------------------------------------------------------
	// Self Top Ups Configuration
	// -------------------------------------------------------------------------

	@RequestMapping(value="transaction_status_enquiries", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public TransactionStatusEnquiriesConfig getTransactionStatusEnquiriesConfig() throws Exception
	{
		return configService.getTransactionStatusEnquiriesConfig();
	}

	@RequestMapping(value="transaction_status_enquiries/vars", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Map<String, String []>> getTransactionStatusEnquiriesConfigVariables() throws Exception
	{
		return configService.getTransactionStatusEnquiriesConfigVariables();
	}

	@RequestMapping(value="transaction_status_enquiries", method = RequestMethod.POST)
	public String updateTransactionStatusEnquiriesConfig(@RequestBody(required = true) TransactionStatusEnquiriesConfig config, Locale locale) throws Exception
	{
		configService.updateTransactionStatusEnquiriesConfig(config);
		return "{}";
	}

	// -------------------------------------------------------------------------
	// Self Top Ups Configuration
	// -------------------------------------------------------------------------

	@RequestMapping(value="transfers", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public TransfersConfig getTransfersConfig() throws Exception
	{
		return configService.getTransfersConfig();
	}

	@RequestMapping(value="transfers/vars", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Map<String, String []>> getTransfersConfigVariables() throws Exception
	{
		return configService.getTransfersConfigVariables();
	}

	@RequestMapping(value="transfers", method = RequestMethod.POST)
	public String updateTransfersConfig(@RequestBody(required = true) GuiTransfersConfig config, Locale locale) throws Exception
	{
		configService.updateTransfersConfig(config);
		return "{}";
	}

	// -------------------------------------------------------------------------
	// Web Users Configuration
	// -------------------------------------------------------------------------
	@RequestMapping(value="web_users", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public WebUsersConfig getWebUsersConfig() throws Exception
	{
		return configService.getWebUsersConfig();
	}

	@RequestMapping(value="web_users/vars", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Map<String, String []>> getWebUsersConfigVariables() throws Exception
	{
		return configService.getWebUsersConfigVariables();
	}

	@RequestMapping(value="web_users", method = RequestMethod.POST)
	public String updateWebUsersConfig(@RequestBody(required = true) WebUsersConfig config, Locale locale) throws Exception
	{
		configService.updateWebUsersConfig(config);
		return "{}";
	}

	// -------------------------------------------------------------------------
	// Workflow Configuration
	// -------------------------------------------------------------------------

	@RequestMapping(value="workflow", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public WorkflowConfig getWorkflowConfig() throws Exception
	{
		return configService.getWorkflowConfig();
	}

	@RequestMapping(value="workflow/vars", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Map<String, String []>> getWorkflowConfigVariables() throws Exception
	{
		return configService.getWorkflowConfigVariables();
	}

	@RequestMapping(value="workflow", method = RequestMethod.POST)
	public String updateWorkflowConfig(@RequestBody(required = true) WorkflowConfig config, Locale locale) throws Exception
	{
		configService.updateWorkflowConfig(config);
		return "{}";
	}

	// -------------------------------------------------------------------------
	// Bundle Sales Configuration
	// -------------------------------------------------------------------------

	@RequestMapping(value="bundle_sales", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public BundleSalesConfig getBundleSalesConfig() throws Exception
	{
		return configService.getBundleSalesConfig();
	}

	@RequestMapping(value="bundle_sales/vars", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Map<String, String []>> getBundleSalesConfigVariables() throws Exception
	{
		return configService.getBundleSalesConfigVariables();
	}

	@RequestMapping(value="bundle_sales", method = RequestMethod.POST)
	public String updateBundleSalesConfig(@RequestBody(required = true) BundleSalesConfig config, Locale locale) throws Exception
	{
		configService.updateBundleSalesConfig(config);
		return "{}";
	}

	// -------------------------------------------------------------------------
	// Rewards Configuration
	// -------------------------------------------------------------------------

	@RequestMapping(value="rewards", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiRewardsConfig getRewardsConfig() throws Exception
	{
		return new GuiRewardsConfig( configService.getRewardsConfig() );
	}

	@RequestMapping(value="rewards/vars", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Map<String, String []>> getRewardsConfigVariables() throws Exception
	{
		return configService.getRewardsConfigVariables();
	}

	@RequestMapping(value="rewards", method = RequestMethod.POST)
	public String updateRewardsConfig(@RequestBody(required = true) GuiRewardsConfig updatedConfig, Locale locale) throws Exception
	{
		RewardsConfig config = updatedConfig.exportRewardsConfig();
		configService.updateRewardsConfig(config);
		return "{}";
	}

	// -------------------------------------------------------------------------
	// Analytics Configuration
	// -------------------------------------------------------------------------
	
	@RequestMapping(value="analytics", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Object> getGuiAnalyticsConfiguration() throws Exception
	{
		return configService.getAnalyticsConfig();
	}

	@RequestMapping(value="analytics/vars", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getAnalyticsConfigVars() throws Exception
	{
		return "{}";
	}
	
	@RequestMapping(value="analytics", method = RequestMethod.POST)
	public String updateAnalyticsConfig(@RequestBody(required = true) GuiAnalyticsConfig updatedAnalyticsConfig, Locale locale) throws Exception
	{
		AnalyticsConfig config = updatedAnalyticsConfig.exportAnalyticsConfig();
		configService.updateAnalyticsConfig(config);
		return "{ }";
	}
	
	// -------------------------------------------------------------------------
	// Reporting Configuration
	// -------------------------------------------------------------------------

	@RequestMapping(value="reporting", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ReportingConfig getReportingConfig() throws Exception
	{
		return configService.getReportingConfig();
	}

	@RequestMapping(value="reporting/vars", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Map<String, String []>> getReportingConfigVariables() throws Exception
	{
		return configService.getReportingConfigVariables();
	}

	@RequestMapping(value="reporting", method = RequestMethod.POST)
	public String updateReportingConfig(@RequestBody(required = true) ReportingConfig config, Locale locale) throws Exception
	{
		configService.updateReportingConfig(config);
		return "{}";
	}

	// -------------------------------------------------------------------------
	// Adjudication Configuration
	// -------------------------------------------------------------------------

	@RequestMapping(value="adjudication", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public AdjudicationConfig getAdjudicationConfig() throws Exception
	{
		return configService.getAdjudicationConfig();
	}

	@RequestMapping(value="adjudication/vars", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Map<String, String []>> getAdjudicationConfigVariables() throws Exception
	{
		return configService.getAdjudicationConfigVariables();
	}

	@RequestMapping(value="adjudication", method = RequestMethod.POST)
	public String updateAdjudicationConfig(@RequestBody(required = true) AdjudicationConfig config, Locale locale) throws Exception
	{
		configService.updateAdjudicationConfig(config);
		return "{}";
	}

}
