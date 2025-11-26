package cs;

import org.apache.http.util.VersionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import cs.utility.Common;
import cs.utility.BuildInfo;
import org.springframework.scheduling.annotation.EnableScheduling;

/*
 * Modified code to extend SpringBootServletInitializer which is required to deploy as a war file in the servlet
 */
@SpringBootApplication(exclude = {SessionAutoConfiguration.class})
@EnableScheduling
public class Application extends SpringBootServletInitializer
{
	private static final Logger logger = LoggerFactory.getLogger(Application.class);

	private static SpringApplicationBuilder configureApp(SpringApplicationBuilder application)
	{
		Common.configure();

		application.sources(Application.class).profiles(Common.getProfiles());	
		return application;
	}

	/*
	 * Required for configuration in the servlet container
	 */
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application)
	{
		return configureApp(application);
	}

	/**
	 * For when you are deploying as a jar
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		logger.info("ecds-api Service GithubTag:" + BuildInfo.GITHUB_TAG + " DockerTag:" + BuildInfo.DOCKER_TAG + " BranchName:" + BuildInfo.BRANCH_NAME + " BuildNumber:" + BuildInfo.BUILD_NUMBER + " BuildDateTime:" + BuildInfo.BUILD_DATE_TIME + " CommitRef:" + BuildInfo.BUILD_COMMIT_REF +" starting ...");
		SpringApplicationBuilder builder = configureApp(new SpringApplicationBuilder());
		builder.run(args);
	}

}
