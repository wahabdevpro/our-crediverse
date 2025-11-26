package cs.controller;

import cs.dto.GuiDataTable;
import cs.service.AgentService;
import cs.service.TdrService;
import cs.service.TypeConvertorService;
import cs.utility.Common;
import cs.utility.FilterBuilderUtils;
import hxc.ecds.protocol.rest.Agent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import hxc.ecds.protocol.rest.ExResult;
import hxc.ecds.protocol.rest.Transaction;
import hxc.ecds.protocol.rest.TransactionEx;

@RestController
@Profile(Common.CONST_ADMIN_PROFILE)
@RequestMapping("/api/tdr-agent")
public class TdrAgentController {

  @Autowired
  private TdrService tdrService;

  @Autowired
  private AgentService agentService;

  @Autowired
  private TypeConvertorService typeConvertorService;

  // Endpoint which accepts an MSISDN and:
  // returns either a list of 0 or more sgents representing the agents that have
  // used this msisdn.
  // Including their IDs and names.
  @RequestMapping(value = "agents-by-msisdn/{msisdn}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public Agent[] getAgentsByMsisdn(@PathVariable("msisdn") String msisdn) throws Exception {
    Agent[] agentsWithMsisdn = null;
    // int agentAId = sessionData.getAgentId();

    // TODO verify that agent has permission
    agentsWithMsisdn = agentService.getAllAgentsByMsisdn(msisdn);

    return agentsWithMsisdn;
  }

  // return list of agent transactions. NB. this may include multiple MSISDN's
  // but only the transactions for this specific agent.
  // For now, only last 7 days pof records
  @RequestMapping(value = "agent-tdrs/{agentId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public GuiDataTable getAgentTransactions(@PathVariable("agentId") String agentId) throws Exception {
    ExResult<TransactionEx> transactions = null;
    boolean includeQuery = false;
    String daysFilter = FilterBuilderUtils.getLastXDaysFilter(10, Transaction.RELATION_OWN);
    String search = null;
    Integer offset = null;
    Integer limit = 1000; // As per original code
    String sort = new String("number-"); // Not sure what this does yet.
    Integer withcount = 0;
    boolean advanced = false;
    Integer agentID = Integer.parseInt(agentId);
    String msisdnab = null;

    //logger.info(daysFilter);

    /* public ExResult<TransactionEx> listTransactionsEx(
      boolean includeQuery,
      String filter,
      String search,
      Integer offset,
      Integer limit,
      String sort,
      Integer withcount,
      boolean advanced,
      Integer agentID,
      String msisdnab) throws Exception
    */
    transactions = tdrService.listTransactionsEx(includeQuery, daysFilter, search, offset, limit, sort, withcount, advanced, agentID, msisdnab);
    return new GuiDataTable(typeConvertorService.getGuiTransactionFromTransactionEx(transactions.getInstances()));
  }

}
