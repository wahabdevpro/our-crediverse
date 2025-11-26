package cs.service.batch;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import cs.config.RestServerConfiguration;
import cs.template.CsRestTemplate;
import hxc.ecds.protocol.rest.config.BatchConfig;
import hxc.ecds.protocol.rest.config.Phrase;

@Service
public class BatchConfigService
{
	@Autowired //ask @Configuration-marked class for this
	private RestServerConfiguration restServerConfig;

	@Autowired //ask @Configuration-marked class for this
	private CsRestTemplate restTemplate;

	private boolean configured = false;
	private String restServerUrl;

	@PostConstruct
	public void configure()
	{
		if (!configured)
		{
			this.restServerUrl = restServerConfig.getRestServer() + restServerConfig.getBatchupconfigurl();
			configured = true;
		}
	}

	public BatchConfig getConfiguration() throws Exception
	{
		return restTemplate.execute(restServerUrl, HttpMethod.GET, BatchConfig.class);
	}

	public void updateConfiguration(BatchConfig updatedBatchConfig) throws Exception
	{
		try {
			restTemplate.execute(restServerUrl, HttpMethod.PUT, updatedBatchConfig, Void.class);
		} catch(Exception e) {
			throw e;
		}

	}

	public Map<String, Phrase[]> getConfigurationVariables()
	{
		Map<String, Phrase[]> result = new HashMap<>();

		return result;
	}
}
