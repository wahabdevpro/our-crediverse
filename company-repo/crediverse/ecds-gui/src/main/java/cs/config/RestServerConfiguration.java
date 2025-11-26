package cs.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Configuration
@ConfigurationProperties(prefix = "cs.rest.server")
@PropertySource("classpath:application.properties")
public class RestServerConfiguration
{
	private String protocol;

	private String restServer;

	private String port;

	private String host;

	private String authurl;

	private String permurl;

	// These fields need to be named as per `application.properties` file
	// And are injected as such (Automagically)
	private String analyticsUrl;
	private String tierUrl;
	private String transferRuleUrl;
	private String accountUrl;
	private String agentUrl;
	private String tdrUrl;
	private String reportsUrl;
	private String auditLogUrl;
	private String replenishurl;
	private String transferurl;
	private String serviceClassesUrl;
	private String transactionsConfigUrl;
	private String groupurl;
	private String batchUrl;
	private String batchuploadurl;
	private String batchupconfigurl;
	private String batchstatusurl;
	private String msisdnRecycleUploadUrl;
	private String MsisdnRecycleStatusUrl;
	private String MsisdnRecycleResultsUrl;
	private String MsisdnRecycleSubmitUrl;
	private String adjustmenturl;
	private String reversalurl;
	private String reversalWithoutCoAuthUrl;
	private String partialreversalurl;
	private String partialReversalWithoutCoAuthUrl;
	private String adjudicateurl;
	private String sessionurl;
	private String roleurl;
	private String departmenturl;
	private String webUserurl;
	private String agentUserUrl;
	private String webUiUrl;
	private String workflowurl;
	private String transactionsUrl;
	private String cellUrl;
	private String cellGroupUrl;
	private String areaUrl;
	private String batchhistoryurl;
	private String promotionsUrl;
	private String bundlesUrl;
	private String bundlesInfoUrl;
	private String clientstateurl;
	private String ussdmenuurl;
	private String mobileNumbersTransformationUrl;

	private boolean autologin = true;

	private int timeout = 1500000;

	private String keystorepath;
	private String truststorepath;
	private String keystorepassword;
	private String keypassword;
	private String truststorepassword;

	private String logodir;

	private int keepalive;

	private String notificationLanguages;		// Supported notification languages

	private int maxConnPerRoute;
	private int maxConnTotal;

	public String getRestServer()
	{
		//logger.info("Got URL || "+ussdmenuurl+"||");
		StringBuilder url = new StringBuilder(protocol);
		url.append("://").append(host).append(":").append(port);
		restServer = url.toString();
		return restServer;
	}
}
