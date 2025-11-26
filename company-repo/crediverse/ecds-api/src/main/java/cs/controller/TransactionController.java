package cs.controller;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
//import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cs.dto.GuiTransaction;
import cs.service.TransactionService;
import hxc.ecds.protocol.rest.SellRequest;
import hxc.ecds.protocol.rest.TransactionNotificationCallback;
import hxc.ecds.protocol.rest.TransferRequest;
import hxc.ecds.protocol.rest.SellBundleRequest;

@RestController
@RequestMapping("/api/account/transaction")
public class TransactionController 
{
	@Autowired
	private TransactionService transactionService;

	/* Unused ?
	@Autowired
	private AuthenticationManager authenticationManager;
	*/
	
	@Resource(name="tokenStore")
	private TokenStore tokenStore;

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiTransaction[] getTransactionList(@RequestParam Map<String, String> params) throws Exception
	{
		GuiTransaction[] transactionList = transactionService.listTransactions(params);
		return transactionList;
	}

	@RequestMapping(value="/last", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiTransaction getLastTransaction(@RequestParam Map<String, String> params) throws Exception {
		params.put("limit", "1");
		params.put("sort", "transactionEnded-");
		GuiTransaction[] transactionList = transactionService.listTransactions(params);
		if (transactionList.length > 0) {
			return transactionList[0];
		} else {
			return null;
		}
	}
	
	@RequestMapping(value="{transactionNo}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiTransaction getTransaction(@PathVariable("transactionNo") String transactionNo) throws Exception
	{
		GuiTransaction transaction = transactionService.getTransactionByNumber(transactionNo);
		return transaction;
	}
	
	@RequestMapping(value="/bundle/sale", method = RequestMethod.POST)
	public GuiTransaction saleBundle(@RequestBody SellBundleRequest sellRequest) throws Exception {
		return transactionService.saleBundle(sellRequest);
	}
	
	@RequestMapping(value="/airtime/sale", method = RequestMethod.POST)
	public GuiTransaction sellAirtime(@RequestBody(required = true) SellRequest sellRequest) throws Exception
	{
		return transactionService.sellAirtime(sellRequest);
	}
	
	@RequestMapping(value="/transfer", method = RequestMethod.POST)
	public GuiTransaction transferCredit(@RequestBody(required = true) TransferRequest transferRequest) throws Exception
	{
		return transactionService.transferCredit(transferRequest);
	}
	
	@RequestMapping(value="/inbox/{transactionID}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiTransaction[] getSubsequentTransactions(@PathVariable("transactionID") String transactionNo, @RequestParam Map<String, String> params) throws Exception
	{
		GuiTransaction[] transactions = transactionService.getSubsequentTransactions(transactionNo, params);
		return transactions;
	}
	
	@RequestMapping(value="/notify", method = RequestMethod.PUT)
	public void notifySession(@RequestBody(required = true) TransactionNotificationCallback callback ) throws Exception
	{
		transactionService.receiveLatestTransactions(callback);
	}
}