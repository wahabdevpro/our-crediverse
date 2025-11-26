/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package c4uburntester;

import c4uburntester.packages.Packages;
import java.net.MalformedURLException;

/**
 *
 * @author justinguedes
 */
public class Burner
{

	private final Service service;
	private final Packages packages;

	private boolean run = true, stopped = false;

	public Burner()
	{
		service = new Service();
		packages = new Packages();
	}

	public Burner(String url) throws MalformedURLException
	{
		service = new Service(url + ":14100/HxC?wsdl");
		packages = new Packages(url + ":10012/Air?wsdl");
	}

	public void start()
	{
		while (run)
		{
			service.call(packages.getRandomPackage());

			try
			{
				Thread.sleep(100);
			}
			catch (InterruptedException ex)
			{

			}

			if (!run)
				stopped = true;
		}
	}

	public void stop()
	{
		run = false;
		while (!stopped)
		{

		}
	}
}
