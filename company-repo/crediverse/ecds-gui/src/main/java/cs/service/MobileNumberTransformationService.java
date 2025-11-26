package cs.service;

import cs.config.RestServerConfiguration;
import cs.template.CsRestTemplate;
import hxc.ecds.protocol.rest.MobileNumberFormatConfig;
import hxc.ecds.protocol.rest.MobileNumberFormatMapping;
import hxc.ecds.protocol.rest.MobileNumberTransformationProgress;
import hxc.ecds.protocol.rest.TransactionServerResponse;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class MobileNumberTransformationService {
	@Autowired
	private CsRestTemplate restTemplate;

	@Autowired
	private RestServerConfiguration restServerConfig;

	private String mobileNumbersTransformationUrl;
	private boolean configured = false;

	@PostConstruct
	public void configure() {
		if (!configured) {
			this.mobileNumbersTransformationUrl = restServerConfig.getRestServer() + restServerConfig.getMobileNumbersTransformationUrl();
			configured = true;
		}
	}

	private String getMobileNumbersTransformationRESTUrl(String path) {
		return String.format("%s/%s", mobileNumbersTransformationUrl, path);
	}

	public MobileNumberFormatMapping getMapping() throws Exception {
		return restTemplate.execute(getMobileNumbersTransformationRESTUrl("mapping"), HttpMethod.GET, MobileNumberFormatMapping.class);
	}

	public MobileNumberFormatMapping updateMapping(MobileNumberFormatMapping mapping) throws Exception {
		return restTemplate.postForObject(getMobileNumbersTransformationRESTUrl("mapping"), mapping, MobileNumberFormatMapping.class);
	}

	public MobileNumberFormatConfig getConfig() throws Exception {
		return restTemplate.execute(getMobileNumbersTransformationRESTUrl("config"), HttpMethod.GET, MobileNumberFormatConfig.class);
	}

	public MobileNumberFormatConfig updateConfig(MobileNumberFormatConfig config) throws Exception {
		return restTemplate.postForObject(getMobileNumbersTransformationRESTUrl("config"), config, MobileNumberFormatConfig.class);
	}

	public TransactionServerResponse start() throws Exception {
		return restTemplate.execute(getMobileNumbersTransformationRESTUrl("start"), HttpMethod.POST, TransactionServerResponse.class);
	}

	public TransactionServerResponse stop() throws Exception {
		return restTemplate.execute(getMobileNumbersTransformationRESTUrl("stop"), HttpMethod.POST, TransactionServerResponse.class);
	}

	public MobileNumberTransformationProgress getProgress() throws Exception {
		return restTemplate.execute(getMobileNumbersTransformationRESTUrl("progress"), HttpMethod.GET, MobileNumberTransformationProgress.class);
	}

	public TransactionServerResponse enableDualPhase(boolean force) throws Exception {
		URIBuilder builder = new URIBuilder(getMobileNumbersTransformationRESTUrl("dual_phase/enable"));
		builder.addParameter("force", String.valueOf(force));
		return restTemplate.execute(builder.build(), HttpMethod.POST, TransactionServerResponse.class);
	}

	public TransactionServerResponse disableDualPhase(boolean force) throws Exception {
		URIBuilder builder = new URIBuilder(getMobileNumbersTransformationRESTUrl("dual_phase/disable"));
		builder.addParameter("force", String.valueOf(force));
		return restTemplate.execute(builder.build(), HttpMethod.POST, TransactionServerResponse.class);
	}
}
