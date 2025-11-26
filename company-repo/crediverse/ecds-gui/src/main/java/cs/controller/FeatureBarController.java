package cs.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import featureBar.FeatureBarClient;

@RestController
@RequestMapping("api/featurebar")
public class FeatureBarController 
{
	private static Logger logger = LoggerFactory.getLogger(FeatureBarController.class);

	private static final String featureNameSuffix = "crediverse.";


	String clientKeyFilename = System.getProperty("user.dir")+"/tls/client.key.pem";
	String clientCertificateFilename = System.getProperty("user.dir")+"/tls/client.crt";
	String certificateAuthorityCertificateFilename = System.getProperty("user.dir")+"/tls/concurrent_ca.crt";

	FeatureBarClient featureBar = 
		new FeatureBarClient(
				"https://featurebar:2379",
				clientKeyFilename, 
				clientCertificateFilename, 
				certificateAuthorityCertificateFilename );

	@RequestMapping(method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
	public String list() throws Exception
	{
		logger.info("Checking features that can be marked unavailable. ");
				return "[\"sellerTradeBonusFeature\"]";
	}

	@RequestMapping(value="{featureName}", method = RequestMethod.GET)
	public boolean featureState(@PathVariable("featureName") String featureName)
	{
		featureName = FeatureBarController.featureNameSuffix + featureName;

 		return featureBar.isFeatureAvailable(featureName);
	}
}

