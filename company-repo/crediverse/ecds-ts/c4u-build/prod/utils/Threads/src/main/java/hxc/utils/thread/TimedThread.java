package hxc.utils.thread;

public abstract class TimedThread extends Thread
{

	public enum TimedThreadType
	{
		INTERVAL, EXECUTE_ONCE
	}

	private Object waitSignal;
	private long waitTime;
	private TimedThreadType type = TimedThreadType.EXECUTE_ONCE;
	private boolean interrupted = false;

	public TimedThread(String name, long waitTime)
	{
		super(name);
		this.waitSignal = new Object();
		this.waitTime = waitTime;
	}

	public TimedThread(String name, long waitTime, TimedThreadType type)
	{
		this(name, waitTime);

		this.type = type;
	}

	public void setWaitTime(long waitTime)
	{
		setWaitTime(waitTime, false);
	}

	public void setWaitTime(long waitTime, boolean forceRestart)
	{
		this.waitTime = waitTime;

		if (forceRestart)
		{
			kill();
			start();
		}
	}

	public long getWaitTime()
	{
		return waitTime;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// General Methods
	//
	// /////////////////////////////////

	@Override
	public synchronized void start()
	{
		interrupted = false;
		super.start();
	}

	public synchronized void sleep()
	{

		synchronized (waitSignal)
		{
			try
			{
				waitSignal.wait(waitTime);
			}
			catch (InterruptedException e)
			{
			}
		}

	}

	public synchronized void kill()
	{
		interrupted = true;
		this.interrupt();
	}

	public abstract void action();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Type Methods
	//
	// /////////////////////////////////

	@Override
	public void run()
	{
		do
		{
			synchronized (waitSignal)
			{
				try
				{
					waitSignal.wait(waitTime);
				}
				catch (InterruptedException e)
				{
				}
			}

			if (interrupted)
				break;

			action();

		} while (!interrupted && //
				type == TimedThreadType.INTERVAL);
	}

}
