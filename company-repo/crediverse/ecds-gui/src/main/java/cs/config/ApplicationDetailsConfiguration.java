package cs.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Component
@ConfigurationProperties(prefix = "cs.application")
public class ApplicationDetailsConfiguration
{
	private String name;
	private String version;
	private String ciBuildId;
	private int companyid;
	private boolean jsdebug;
	private int keepalive;
    private boolean logtsrequest;
	private boolean showDisregardBonusOption;
}
