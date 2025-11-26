/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package c4uburntester;

import java.net.MalformedURLException;
import java.util.Date;
import java.util.Properties;

import com.concurrent.hxc.RequestHeader;
import c4uburntester.testcases.TestCases;

public class Burner
{
	//////////////////////////////////////////////////////////////////
	// fields
	////////////////
	
	private final Service service;
	private final TestCases testCases;

	private boolean run = true;
	private boolean stopped = false;
	private static int numberOfBurners = 0;
	private int minTPS = 1;
	private int maxTPS = 1;
	private int requestsPerTPS = 10;
	private int tpsStep = 1;
	private int testID = 0;
	private String url = null;


	//////////////////////////////////////////////////////////////////
	// methods
	////////////////
	
//	public Burner(int minTPS, int maxTPS, int tpsStep, int testsPerTPS)
//	{
//		this.minTPS = minTPS;
//		this.maxTPS = maxTPS;
//		this.requestsPerTPS = testsPerTPS;
//		service = new Service();
//		testCases = new TestCases();
//		this.tpsStep = tpsStep;
//		numberOfBurners++;
//	}

//	public Burner(int minTPS, int maxTPS, int tpsStep, int testsPerTPS, String url) throws MalformedURLException
//	{
//		this.minTPS = minTPS;
//		this.maxTPS = maxTPS;
//		this.requestsPerTPS = testsPerTPS;
//		service = new Service(url + ":14100/HxC?wsdl");
//		testCases = new TestCases(url + ":10012/Air?wsdl");
//		this.tpsStep = tpsStep;
//		numberOfBurners++;
//	}

	public Burner(Properties props) throws MalformedURLException
	{
		this.minTPS = Integer.parseInt(props.getProperty("MIN_TPS").trim());
		this.maxTPS = Integer.parseInt(props.getProperty("MAX_TPS").trim());
		this.requestsPerTPS = Integer.parseInt(props.getProperty("REPEATS_PER_TPS").trim());
		this.tpsStep = Integer.parseInt(props.getProperty("TPS_STEP").trim());
		this.testID = Integer.parseInt(props.getProperty("TESTCASE_ID").trim());
		this.service = new Service(props);
		this.testCases = new TestCases(props);
		numberOfBurners++;
	}

	//Randomly execute TestCases contained in TestSuite
	public void loadTest()
	{
		System.out.println( String.format("Entering [%s]", Thread.currentThread().getStackTrace()[1].getMethodName()) );
		int delay = 1000;
		
		RequestHeader[] testCase; 
		for (int tps=minTPS; tps<=maxTPS; tps += tpsStep)
		{
			delay = Math.round(1000/tps);
			System.out.println(String.format("TPS[%d], Delay[%d]", tps, delay));

			for (int repeats=0; repeats<requestsPerTPS; repeats++)
			{
//				try
//				{
					testCase = testCases.getTestCase(testID); 
					service.call(testCase, tps);
					//Thread.sleep(delay);
//				}
//				catch (InterruptedException ex)
//				{
//					System.out.println("INTERRUPTED_EXCEPTION: " + ex.getMessage());
//				}
//				catch (Exception e)
//				{
//					//e.printStackTrace();
//					System.out.println("GENERAL_EXCEPTION: " + e.getMessage());
//				}
			}//for()
		}//for()

		run = false;
		stopped = true;
		System.out.println( String.format("Exiting [%s]", Thread.currentThread().getStackTrace()[1].getMethodName()) );
	}//start

//	public void stop()
//	{
//		run = false;
//		while (!stopped)
//		{
//
//		}
//	}
}
