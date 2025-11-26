package cs.service.portal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cs.dto.portal.GuiAgentAccountSummary;
import cs.dto.portal.GuiPinRules;
import cs.dto.security.LoginSessionData;
import cs.service.AccountService;
import cs.service.ConfigurationService;
import hxc.ecds.protocol.rest.Account;
import hxc.ecds.protocol.rest.DepositsQueryResponse;
import hxc.ecds.protocol.rest.SalesQueryResponse;
import hxc.ecds.protocol.rest.config.AgentsConfig;

@Service
public class PortalConverterService
{
	@Autowired
	private PortalTransactionService transactionService;

	@Autowired
	private LoginSessionData sessionData;

	@Autowired
	private AccountService accountService;

	@Autowired
	private ConfigurationService configService;

	public GuiAgentAccountSummary produceAgentAccountSummary() throws Exception
	{

		DepositsQueryResponse depositsInfo = transactionService.getDepositsQuery();
		SalesQueryResponse salesInfo = transactionService.getSalesQuery();

		GuiAgentAccountSummary summary  = new GuiAgentAccountSummary();

		try
		{
			Account accountInfo = accountService.getAccount(sessionData.getAgentId());
			summary.setBalance( accountInfo.getBalance() );
			summary.setBonusBalance( accountInfo.getBonusBalance() );
		} catch(Exception ex)
		{
			// Currently part of summary though might be used my mobile (needs to be seperated out)
		}

		summary.setDepositsCount( depositsInfo.getCount() );
		summary.setDepositsAmount( depositsInfo.getAmount() );

		summary.setTransferCount( salesInfo.getTransfersCount() );
		summary.setTransferAmount( salesInfo.getTransfersAmount() );

		summary.setSalesCount( salesInfo.getSalesCount() );
		summary.setSalesAmount( salesInfo.getSalesAmount() );

		summary.setSelfTopupsCount( salesInfo.getSelfTopUpsCount() );
		summary.setSelfTopupsAmount( salesInfo.getSelfTopUpsAmount() );

		return summary;
	}

	public GuiPinRules extractPinChangeRules() throws Exception
	{
		AgentsConfig agentsConfig = configService.getAgentsConfiguration();
		GuiPinRules result = GuiPinRules.extractPinRuleSet(agentsConfig);
		return result;
	}
}
