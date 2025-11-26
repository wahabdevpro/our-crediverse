/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package c4uburntester;

import java.util.Hashtable;

/**
 *
 * @author justinguedes
 */
public class Stats
{
	private static Hashtable<Integer, TestResult> results = new Hashtable<Integer, TestResult>();

	//-------------------------------------------------------

	public static synchronized void success(int tps)
	{
		TestResult element = getElement(tps);
		if (element != null)
			element.success();
		else
		{
			element = new TestResult();
			element.success();
			results.put(tps, element);
		}
	}

	public static long successful(int tps)
	{
		TestResult element = getElement(tps);
		if (element != null)
			return element.getSuccesses();

		return 0;
	}

	//-------------------------------------------------------

	public static synchronized void failed(int tps)
	{
		TestResult element = getElement(tps);
		if (element != null)
			element.failure();
		else
		{
			element = new TestResult();
			element.failure();
			results.put(tps, element);
		}
	}

	public static long failures(int tps)
	{
		TestResult element = getElement(tps);
		if (element != null)
			return element.getFailures();

		return 0;
	}

	//-------------------------------------------------------
	public static synchronized void nullReturnCode(int tps)
	{
		TestResult element = getElement(tps);
		if (element != null)
			element.nullReturnCode();
		else
		{
			element = new TestResult();
			element.nullReturnCode();
			results.put(tps, element);
		}
	}

	public static long getNullReturnCodes(int tps)
	{
		TestResult element = getElement(tps);
		if (element != null)
			return element.getNullReturnCodes();

		return 0;
	}

	//-------------------------------------------------------

	public static synchronized void nullResponse(int tps)
	{
		TestResult element = getElement(tps);
		if (element != null)
			element.nullResponse();
		else
		{
			element = new TestResult();
			element.nullResponse();
			results.put(tps, element);
		}
	}

	public static long getNullResponses(int tps)
	{
		TestResult element = getElement(tps);
		if (element != null)
			return element.getNullResponses();

		return 0;
	}


	//-------------------------------------------------------

	public static synchronized void insufficientBalance(int tps)
	{
		TestResult element = getElement(tps);
		if (element != null)
			element.insufficientBalance();
		else
		{
			element = new TestResult();
			element.insufficientBalance();
			results.put(tps, element);
		}
	}

	public static long getInsufficientBalance(int tps)
	{
		TestResult element = getElement(tps);
		if (element != null)
			return element.getInsufficientBalance();

		return 0;
	}

	//-------------------------------------------------------

	public static synchronized void incomplete(int tps)
	{
		TestResult element = getElement(tps);
		if (element != null)
			element.incomplete();
		else
		{
			element = new TestResult();
			element.incomplete();
			results.put(tps, element);
		}
	}

	public static long getIncomplete(int tps)
	{
		TestResult element = getElement(tps);
		if (element != null)
			return element.getIncomplete();

		return 0;
	}

	//-------------------------------------------------------

	public static synchronized void quotaReached(int tps)
	{
		TestResult element = getElement(tps);
		if (element != null)
			element.quotaReached();
		else
		{
			element = new TestResult();
			element.quotaReached();
			results.put(tps, element);
		}
	}

	public static long getQuotaReached(int tps)
	{
		TestResult element = getElement(tps);
		if (element != null)
			return element.getQuotaReached();

		return 0;
	}

	//-------------------------------------------------------

	public static synchronized void alreadyConsumer(int tps)
	{
		TestResult element = getElement(tps);
		if (element != null)
			element.alreadyConsumer();
		else
		{
			element = new TestResult();
			element.alreadyConsumer();
			results.put(tps, element);
		}
	}

	public static long getAlreadyConsumer(int tps)
	{
		TestResult element = getElement(tps);
		if (element != null)
			return element.getAlreadyConsumer();

		return 0;
	}

	//-------------------------------------------------------

	public static synchronized void alreadySubscribed(int tps)
	{
		TestResult element = getElement(tps);
		if (element != null)
			element.alreadySubscribed();
		else
		{
			element = new TestResult();
			element.technicalProblem();
			results.put(tps, element);
		}
	}

	public static long getAlreadySubscribed(int tps)
	{
		TestResult element = getElement(tps);
		if (element != null)
			return element.getAlreadySubscribed();

		return 0;
	}

	//-------------------------------------------------------

	public static synchronized void notSubscribed(int tps)
	{
		TestResult element = getElement(tps);
		if (element != null)
			element.notSubscribed();
		else
		{
			element = new TestResult();
			element.notSubscribed();
			results.put(tps, element);
		}
	}

	public static long getNotSubscribed(int tps)
	{
		TestResult element = getElement(tps);
		if (element != null)
			return element.getNotSubscribed();

		return 0;
	}

	//-------------------------------------------------------

	public static synchronized void authorizationFailure(int tps)
	{
		TestResult element = getElement(tps);
		if (element != null)
			element.authorizationFailure();
		else
		{
			element = new TestResult();
			element.authorizationFailure();
			results.put(tps, element);
		}
	}

	public static long getAuthorizationFailure(int tps)
	{
		TestResult element = getElement(tps);
		if (element != null)
			return element.getAuthorizationFailure();

		return 0;
	}

	//-------------------------------------------------------

	public static synchronized void technicalProblem(int tps)
	{
		TestResult element = getElement(tps);
		if (element != null)
			element.technicalProblem();
		else
		{
			element = new TestResult();
			element.technicalProblem();
			results.put(tps, element);
		}
	}

	public static long getTechnicalProblem(int tps)
	{
		TestResult element = getElement(tps);
		if (element != null)
			return element.getTechnicalProblem();

		return 0;
	}

	//-------------------------------------------------------

	public static synchronized void suspended(int tps)
	{
		TestResult element = getElement(tps);
		if (element != null)
			element.suspended();
		else
		{
			element = new TestResult();
			element.suspended();
			results.put(tps, element);
		}
	}

	public static long getSuspended(int tps)
	{
		TestResult element = getElement(tps);
		if (element != null)
			return element.getSuspended();

		return 0;
	}

	//-------------------------------------------------------

	public static synchronized void serviceBusy(int tps)
	{
		TestResult element = getElement(tps);
		if (element != null)
			element.serviceBusy();
		else
		{
			element = new TestResult();
			element.serviceBusy();
			results.put(tps, element);
		}
	}

	public static long getServiceBusy(int tps)
	{
		TestResult element = getElement(tps);
		if (element != null)
			return element.getServiceBusy();

		return 0;
	}

	//-------------------------------------------------------

	public static synchronized void notEligible(int tps)
	{
		TestResult element = getElement(tps);
		if (element != null)
			element.notEligible();
		else
		{
			element = new TestResult();
			element.notEligible();
			results.put(tps, element);
		}
	}

	public static long getNotEligible(int tps)
	{
		TestResult element = getElement(tps);
		if (element != null)
			return element.getNotEligible();

		return 0;
	}

	//-------------------------------------------------------

	public static synchronized void invalidQuota(int tps)
	{
		TestResult element = getElement(tps);
		if (element != null)
			element.invalidQuota();
		else
		{
			element = new TestResult();
			element.invalidQuota();
			results.put(tps, element);
		}
	}

	public static long getInvalidQuota(int tps)
	{
		TestResult element = getElement(tps);
		if (element != null)
			return element.getInvalidQuota();

		return 0;
	}

	//-------------------------------------------------------

	public static synchronized void invalidVariant(int tps)
	{
		TestResult element = getElement(tps);
		if (element != null)
			element.invalidVariant();
		else
		{
			element = new TestResult();
			element.invalidVariant();
			results.put(tps, element);
		}
	}

	public static long getInvalidVariant(int tps)
	{
		TestResult element = getElement(tps);
		if (element != null)
			return element.getInvalidVariant();

		return 0;
	}

	//-------------------------------------------------------

	public static synchronized void alreadyHasQuota(int tps)
	{
		TestResult element = getElement(tps);
		if (element != null)
			element.alreadyHasQuota();
		else
		{
			element = new TestResult();
			element.alreadyHasQuota();
			results.put(tps, element);
		}
	}

	public static long getAlreadyHasQuota(int tps)
	{
		TestResult element = getElement(tps);
		if (element != null)
			return element.getAlreadyHasQuota();

		return 0;
	}

	//-------------------------------------------------------

	public static synchronized void timedOut(int tps)
	{
		TestResult element = getElement(tps);
		if (element != null)
			element.timedOut();
		else
		{
			element = new TestResult();
			element.timedOut();
			results.put(tps, element);
		}
	}

	public static long getTimedOut(int tps)
	{
		TestResult element = getElement(tps);
		if (element != null)
			return element.getTimedOut();

		return 0;
	}

	//-------------------------------------------------------

	public static synchronized void inactiveAParty(int tps)
	{
		TestResult element = getElement(tps);
		if (element != null)
			element.inactiveAParty();
		else
		{
			element = new TestResult();
			element.inactiveAParty();
			results.put(tps, element);
		}
	}

	public static long getInactiveAParty(int tps)
	{
		TestResult element = getElement(tps);
		if (element != null)
			return element.getInactiveAParty();

		return 0;
	}

	//-------------------------------------------------------

	public static synchronized void inactiveBParty(int tps)
	{
		TestResult element = getElement(tps);
		if (element != null)
			element.inactiveBParty();
		else
		{
			element = new TestResult();
			element.inactiveBParty();
			results.put(tps, element);
		}
	}

	public static long getInactiveBParty(int tps)
	{
		TestResult element = getElement(tps);
		if (element != null)
			return element.getInactiveBParty();

		return 0;
	}

	//==========================================================//
	
	private static TestResult getElement(int tps)
	{
		return (TestResult)results.get(tps);
	}
}
