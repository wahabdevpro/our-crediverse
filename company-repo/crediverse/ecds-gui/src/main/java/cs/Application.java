package cs;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cs.utility.Common;
import cs.config.BuildInfo;

/*
 * Modified code to extend SpringBootServletInitializer which is required to deploy as a war file in the servlet
 */
@SpringBootApplication
public class Application extends SpringBootServletInitializer
{
	private static final Logger logger = LoggerFactory.getLogger(Application.class);
	private static SpringApplicationBuilder configureApp(SpringApplicationBuilder application)
	{
		Common.configure();

		application.sources(Application.class).profiles(Common.getProfiles());
		// .banner(banner)
		;
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
		// Done this way to allow easy addition of multiple profiles.
		logger.info("ecds-GUI Service GithubTag:" + BuildInfo.GITHUB_TAG + " DockerTag:" + BuildInfo.DOCKER_TAG + " BranchName:" + BuildInfo.BRANCH_NAME + " BuildNumber:" + BuildInfo.BUILD_NUMBER + " BuildDateTime:" + BuildInfo.BUILD_DATE_TIME + " CommitRef:" + BuildInfo.BUILD_COMMIT_REF +" starting ...");
		SpringApplicationBuilder builder = configureApp(new SpringApplicationBuilder());
		builder.run(args);
	}

}
