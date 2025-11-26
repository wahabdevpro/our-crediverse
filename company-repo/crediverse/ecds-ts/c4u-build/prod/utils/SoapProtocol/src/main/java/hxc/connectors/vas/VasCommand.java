package hxc.connectors.vas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import hxc.configuration.Configurable;
import hxc.configuration.ValidationException;

@Configurable
public class VasCommand
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Enumerations
	//
	// /////////////////////////////////
	public enum Processes
	{
		addCreditTransfer, addMember, addQuota, changeCreditTransfer, changePIN, changeQuota, extend, getBalances, getCreditTransfers, //
		getMembers, getOwners, getQuotas, getStatus, migrate, //
		redeem, removeCreditTransfers, removeMember, removeMembers, removeQuota, replaceMember, resetPIN, resumeCreditTransfer, subscribe, //
		suspend, suspendCreditTransfer, transfer, unsubscribe, unsuspend, validatePIN, process, getLocation
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private Processes process;
	private String command;
	private volatile boolean compiled = false;
	private Pattern pattern = null;
	private String[] commandVariables = null;
	private Map<String, String> defaults = new HashMap<String, String>();

	private final static String special = "\\.[]{}()*+-?";
	private final static String wild_card = "{_WildCard_}";
	private final static String eol = "{_eol_}";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public Processes getProcess()
	{
		return process;
	}

	public void setProcess(Processes process)
	{
		this.process = process;
	}

	public String getCommand()
	{
		return command;
	}

	public void setCommand(String command)
	{
		this.command = command;
		this.compiled = false;
	}

	public String[] getCommandVariables()
	{
		return commandVariables;
	}

	public Map<String, String> getDefaults()
	{
		return defaults;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public VasCommand()
	{
	}

	public VasCommand(Processes process, String command)
	{
		this.process = process;
		this.command = command;
	}

	public VasCommand(VasCommand vc)
	{
		this.process = vc.process;
		this.command = vc.command;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	public boolean matches(String command, String[] commandVariables)
	{
		// Create Pattern
		if (!compiled)
			compilePattern(commandVariables);

		return compiled ? pattern.matcher(command).find() : false;
	}

	public Matcher matcher(String command, String[] commandVariables)
	{
		// Create Pattern
		if (!compiled)
			compilePattern(commandVariables);

		return compiled ? pattern.matcher(command) : null;
	}

	public void validate(String[] commandVariables) throws ValidationException
	{
		compiled = false;
		compilePattern(commandVariables);
		if (!compiled)
			throw new ValidationException("Invalid SMS/USSD Command: %s", command);

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Method
	//
	// /////////////////////////////////
	private synchronized void compilePattern(String[] commandVariables)
	{
		if (compiled)
			return;
		pattern = null;
		defaults = new HashMap<String, String>();
		String[] parts = this.command.split("(?=\\{)|(?<=\\})");
		StringBuilder builder = new StringBuilder(this.command.length() << 2);
		builder.append('^');
		List<String> variables = new ArrayList<String>();
		for (String part : parts)
		{
			if (part.startsWith("{") && part.endsWith("}"))
			{
				String variable = part.substring(1, part.length() - 1);
				String value = null;
				int eq = variable.indexOf('=');
				if (eq > 0)
				{
					value = variable.substring(eq + 1);
					variable = variable.substring(0, eq);
				}

				boolean found = false;
				for (String commandVariable : commandVariables)
				{
					if (variable.equalsIgnoreCase(commandVariable))
					{
						if (value == null)
						{
							builder.append("([^\\*\\#\\s]+)");
							variables.add(commandVariable);
						}
						else
						{
							defaults.put(variable, value);
						}
						found = true;
						break;
					}
				}
				if (!found)
					return;
			}
			else
			{
				for (char ch : part.toCharArray())
				{
					if (special.contains("" + ch))
						builder.append("\\");
					builder.append(ch);
				}

			}
		}
		builder.append('$');

		try
		{
			pattern = Pattern.compile(builder.toString(), Pattern.CASE_INSENSITIVE);
		}
		catch (PatternSyntaxException e)
		{
			return;
		}
		this.commandVariables = variables.toArray(new String[variables.size()]);
		compiled = true;
	}

	public String[] tokenize()
	{
		List<String> result = new ArrayList<String>(20);
		StringTokenizer st = new StringTokenizer(command, " *#{}", true);
		String variable = null;
		while (st.hasMoreTokens())
		{
			String token = st.nextToken();
			if (variable == null)
			{
				if (token.equals("{"))
					variable = token;
				else
					result.add(token);
			}
			else if (token.equals("}"))
			{
				if (!variable.contains("="))
					result.add(wild_card);
				variable = null;
			}
			else
			{
				variable += token;
			}
		}

		result.add(eol);

		return result.toArray(new String[result.size()]);
	}

	public static boolean overlaps(String[] tokens1, String[] tokens2)
	{
		for (int index = 0; index < tokens1.length && index < tokens2.length; index++)
		{
			String token1 = tokens1[index];
			String token2 = tokens2[index];

			if (token1.equals(token2))
				continue;
			else if (token1.equals(wild_card) || token2.equals(wild_card))
				continue;
			else
				return false;

		}

		return true;
	}

}
