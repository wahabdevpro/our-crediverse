package cs.dto;

import java.util.Map;
import java.util.TreeMap;

import hxc.ecds.protocol.rest.config.Phrase;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ConfigurationVariables
{
	public ConfigurationVariables()
	{
		variables = new TreeMap<>();
	}

	private Map<String, Phrase[]> variables;
}
