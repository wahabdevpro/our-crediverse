/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package c4uburntester;

import hxc.services.airsim.AirSimService;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;

/**
 *
 * @author justinguedes
 */
public class C4uBurnTester
{

	private final static int MAX_THREADS = 10;
	private static final Date timeStarted = new Date();
	private static Date timeEnded = new Date();
	private static Burner burners[];

	/**
	 * @param args
	 *            the command line arguments
	 * @throws java.io.IOException
	 */
	public static void main(String[] args) throws IOException
	{

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				timeEnded = new Date();

				AirSimService service = new AirSimService();
				service.getAirSimPort().stop();

				for (Burner burner : burners)
				{
					if (burner != null)
					{
						// burner.stop();
					}
				}

				try
				{
					try (BufferedWriter writer = new BufferedWriter(new FileWriter(String.format("burntester-statistics-%s", new Date().toString()))))
					{
						writer.write(String.format("Time started: %s\nTime ended: %s\n", timeStarted.toString(), timeEnded.toString()));
						long successful = Stats.successful();
						long failures = Stats.failures();
						long total = successful + failures;
						writer.write(String.format("Total: %s\nTotal Successful: %s\nTotal Failures: %s\n", total, successful, failures));
						writer.write(String.format("Success Rate: %s\nFailure Rate: %s", (float) ((float) successful / (float) total) * 100, (float) ((float) failures / (float) total) * 100));
					}
				}
				catch (IOException ex)
				{
					System.out.println(ex.getMessage());
				}

			}
		}));

		AirSimService service = new AirSimService();
		service.getAirSimPort().start();

		int CUSTOM_THREADS = 0;
		if (args != null && args.length > 0)
			try
			{
				CUSTOM_THREADS = Integer.parseInt(args[0]);
			}
			catch (NumberFormatException exc)
			{
			}

		burners = new Burner[((CUSTOM_THREADS > 0) ? CUSTOM_THREADS : MAX_THREADS)];
		final String url = (args != null && args.length > 1) ? args[1] : null;
		// if (url != null) {
		// System.out.print("Username: ");
		//
		// BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		// final String username = reader.readLine();
		// System.out.print("Password: ");
		// final char[] password = System.console().readPassword();
		// Authenticator.setDefault(new Authenticator() {
		//
		// @Override
		// protected PasswordAuthentication getPasswordAuthentication() {
		// return new PasswordAuthentication(username, password);
		// }
		//
		// });
		// CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
		// }

		for (int i = 0; i < ((CUSTOM_THREADS > 0) ? CUSTOM_THREADS : MAX_THREADS); i++)
		{
			final int j = i;
			new Thread()
			{

				@Override
				public void run()
				{
					if (url != null)
					{
						try
						{
							burners[j] = new Burner(url);
							burners[j].start();
						}
						catch (MalformedURLException ex)
						{
							System.exit(1);
						}
					}
					else
					{
						burners[j] = new Burner();
						burners[j].start();
					}
				}

			}.start();
		}

		while (true)
		{
			try
			{
				Thread.sleep(10000);
			}
			catch (Exception exc)
			{

			}
		}

	}

}
