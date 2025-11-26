package hxc.services.ecds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import hxc.connectors.IInteraction;
import hxc.ecds.protocol.rest.config.AgentsConfig;
import hxc.ecds.protocol.rest.config.Phrase;
import hxc.servicebus.ILocale;
import hxc.services.ecds.rest.IChannelTarget;

public class ChannelManager
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final List<String> pinParts = new ArrayList<String>();

	static
	{
		pinParts.add("{PIN}");
		pinParts.add("{TemporaryPIN}");
		pinParts.add("{DefaultPIN}");
		pinParts.add("{OldPIN}");
		pinParts.add("{NewPIN}");
		pinParts.add("{ConfirmPIN}");
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private ConcurrentMap<String, Filter> filters = new ConcurrentHashMap<String, Filter>();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	public boolean defineChannelFilter(IChannelTarget target, int companyID, hxc.ecds.protocol.rest.config.Phrase command, hxc.ecds.protocol.rest.config.Phrase[] fields, int tag)
	{
		for (String text : command.getTexts().values())
		{
			Filter filter = compilePattern(text, fields);
			if (filter == null)
				return false;
			filter.target = target;
			filter.tag = tag;
			filter.companyID = companyID;
			String key = String.format("%s/%d/%d", target.getClass().getName(), tag, companyID);
			filters.put(key, filter);
		}

		return true;
	}

	public boolean testCondition(IInteraction interaction)
	{
		String shortCode = interaction.getShortCode();
		String[] parts = splitCommand(interaction.getMessage());
		for (Filter filter : filters.values())
		{
			if (filter.matches(shortCode, parts, null))
				return true;
		}
		return false;
	}

	public boolean execute(IInteraction interaction, ILocale unused)
	{
		String shortCode = interaction.getShortCode();
		String[] parts = splitCommand(interaction.getMessage());
		for (Filter filter : filters.values())
		{
			Map<String, String> values = new HashMap<String, String>();
			if (!filter.matches(shortCode, parts, values))
				continue;

			values.putAll(filter.defaults);

			if (filter.target.processChannelRequest(filter.companyID, interaction, values, filter.tag))
				return true;
		}
		return false;

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helpers
	//
	// /////////////////////////////////
	private String[] splitCommand(String command)
	{
		if (command == null || command.isEmpty())
			return new String[0];
		command = command.trim();
		if (command.isEmpty())
			return new String[0];
		
		int c = command.length();
		int i = 0;
		List<String> parts = new ArrayList<String>();
		while (i < c)
		{
			int l = 1;
			int p = command.indexOf(' ', i);
			int q = command.indexOf('#', i);
			if (p < 0 || q >= 0 && q < p)
				p = q;
			q = command.indexOf('*', i);
			if (p < 0 || q >= 0 && q < p)
				p = q;
			q = command.indexOf("=>", i);
			if (p < 0 || q >= 0 && q < p)
			{
				p = q;
				l = 2;
			}

			if (p < 0)
			{
				parts.add(command.substring(i, c));
				i = c;
			}
			else if (p >= i)
			{
				if (p > i)
					parts.add(command.substring(i, p));
				parts.add(command.substring(p, p + l));
				i = p + l;
			}
		}
		return parts.toArray(new String[parts.size()]);
	}

	private Filter compilePattern(String command, Phrase[] fields)
	{
		Filter filter = new Filter();
		filter.defaults = new HashMap<String, String>();
		filter.variables = new ArrayList<String>();
		String[] parts = splitCommand(command);
		if (parts.length < 2)
			return null;

		boolean isSMS = parts.length >= 2 && "=>".equals(parts[parts.length - 2]);
		if (isSMS)
		{
			filter.shortCode = parts[parts.length - 1];
			filter.parts = java.util.Arrays.copyOfRange(parts, 0, parts.length - 2);
		}
		else
		{
			filter.shortCode = parts[1];
			filter.parts = java.util.Arrays.copyOfRange(parts, 2, parts.length);
		}

		return filter;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Classes
	//
	// /////////////////////////////////
	private class Filter
	{
		private String shortCode;
		private String[] parts = null;
		private Map<String, String> defaults = new HashMap<String, String>();
		private List<String> variables = new ArrayList<String>();
		private int companyID;
		private IChannelTarget target;
		private int tag;

		public boolean matches(String shortCode, String[] parts, Map<String, String> values)
		{
			Map<String, String> vals = values != null ? new HashMap<String, String>() : null;
			if (!shortCode.equals(this.shortCode))
				return false;

			if (parts.length != this.parts.length)
				return false;

			for (int index = 0; index < parts.length; index++)
			{
				String part = this.parts[index];
				if (part.startsWith("{"))
				{
					String supplied = parts[index];

					// Pin are at least AgentsConfig.ABSOLUTE_MIN_PIN_LENGTH long
					int length = supplied.length();
					if (pinParts.contains(part))
					{
						if (length < AgentsConfig.ABSOLUTE_MIN_PIN_LENGTH)
							return false;
					}

					if (vals != null)
						vals.put(part, supplied);
					continue;
				}
				else if (!part.equalsIgnoreCase(parts[index])) 
					return false;
			}

			if (vals != null)
				values.putAll(vals);

			return true;
		}

	}

}
