package cs.template;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/*
 * Currently, CsRestTemplate uses the users session data to create requests.
 * However, that data is not available in new threads.  So to use CsRestTemplate
 * in another thread, we need to make the users request attributes available
 * to that thread.
 *
 * Subclassing this thread will do that and perform cleanup of the data when
 * the thread exits.  Note that you use the onRun method instead of run in the
 * subclass.
 */
public abstract class BatchThreadWorker implements Runnable
{
	protected RequestAttributes requestAttributes;
	private Thread thread;



	protected String restServerUrl;

	public BatchThreadWorker()
	{
		this.requestAttributes = RequestContextHolder.getRequestAttributes();
		this.thread = Thread.currentThread();
	}

	@Override
	public void run()
	{
		try
		{
			RequestContextHolder.setRequestAttributes(requestAttributes);
			onRun();
		}
		finally
		{
			if (Thread.currentThread() != thread)
			{
				RequestContextHolder.resetRequestAttributes();
			}
			thread = null;
		}
	}

	protected abstract void onRun();
}
