package hxc.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.connectors.ctrl.CtrlConnector;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.ServiceBus;
import hxc.services.logging.LoggerService;
import hxc.services.transactions.TransactionService;
import hxc.servicebus.VersionInfo;

public class HostObject implements Runnable
{
	final static Logger logger = LoggerFactory.getLogger(HostObject.class);
	
	private IServiceBus esb;

	private boolean run = true;

	private void init(String[] args)
	{		
		esb = ServiceBus.getInstance();

		// The first 3 dependent services
		LoggerService loggerService = new LoggerService();
		esb.registerService(loggerService);
		esb.registerConnector(new CtrlConnector());
		esb.registerService(new TransactionService());
        
		/*
        if (logger != null)
        {
        	logger.redirectErrorStream();
        	logger.redirectOutputStream();
        }
		*/

		esb.start(args);
		
		(new Thread(this)).start();
	}
	

	public static void main(String[] args)
	{
		logger.info("ecds-ts Service GithubTag:" + VersionInfo.GITHUB_TAG + "  DockerTag:" + VersionInfo.DOCKER_TAG + "  BranchName:" + VersionInfo.BRANCH_NAME + "  BuildNumber:" + VersionInfo.BUILD_NUMBER + "  BuildDateTime:" + VersionInfo.BUILD_DATE_TIME + "  CommitRef:" +  VersionInfo.BUILD_COMMIT_REF +" starting ...");
		HostObject th = new HostObject();
		th.init(args);
	}

	//
	@Override
	public void run()
	{
		while (run)
		{
			try
			{
				synchronized (this)
				{
					Thread.sleep(1000);
				}
			}
			catch (Exception e)
			{
				// TODO: handle exception
			}
		}
	}

}
