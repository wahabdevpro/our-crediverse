package cs.controller;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cs.dto.GuiDataTable;
import cs.service.AccountService;
import cs.utility.Common;
import hxc.ecds.protocol.rest.Account;

@RestController
@RequestMapping("/api/accounts")
@Profile(Common.CONST_ADMIN_PROFILE)
public class AccountController
{
	@Autowired //ask @Configuration-marked class for this
	private AccountService accountService;

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable list() throws Exception
	{
		Account[] accounts = accountService.listAccounts();

		return new GuiDataTable(accounts);
	}

	@RequestMapping(value="{account}", method = RequestMethod.GET)
	public Account get(@PathVariable("account") String accountId) throws Exception
	{
		return accountService.getAccount(accountId);
	}

	@RequestMapping(value="all", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Account[] serverList() throws Exception
	{
		Account[] accounts = accountService.listAccounts();
		return accounts;
	}

	@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public Account create(@RequestBody(required = true) Account newAccount, Locale locale) throws Exception
	{
		accountService.create(newAccount);
		return newAccount;
	}

	@RequestMapping(value="{account}", method = RequestMethod.DELETE)
	public String delete(@PathVariable("account") String accountId) throws Exception
	{
		accountService.delete(accountId);
		return "{}";
	}

	@RequestMapping(value="{account}", method = RequestMethod.PUT)
	public String update(@PathVariable("account") String accountId, @RequestBody(required = true) /*Gui*/Account newAccount, Locale locale) throws Exception
	{
		accountService.update(/*typeConvertorService.getAccountFromGuiAccount(newAccount)*/newAccount);
		return "{}";
	}
}
