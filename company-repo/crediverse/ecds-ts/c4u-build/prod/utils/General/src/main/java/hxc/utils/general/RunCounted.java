// vim: set ts=4 sw=4 filetype=java noexpandtab:
package hxc.utils.general;

//import lombok.*;
import org.slf4j.Logger;

public interface RunCounted {

	public long getRunStartCounter();
	public long getRunCompleteCounter();

	public void resetRunCounters();
	public long resetRunStartCounter();
	public long resetRunCompleteCounter();

	public boolean waitForRunStartCount(int count, Long timeout) throws Exception;
	public boolean waitForRunCompleteCount(int count, Long timeout) throws Exception;

	public interface Forwarding extends RunCounted {
		public RunCounted delegateRunCounted();

		@Override
		default public long getRunStartCounter() { return this.delegateRunCounted().getRunStartCounter(); }
		@Override
		default public long getRunCompleteCounter() { return this.delegateRunCounted().getRunCompleteCounter(); }

		@Override
		default public void resetRunCounters() { this.delegateRunCounted().resetRunCounters(); }
		@Override
		default public long resetRunStartCounter() { return this.delegateRunCounted().resetRunStartCounter(); }
		@Override
		default public long resetRunCompleteCounter() { return this.delegateRunCounted().resetRunCompleteCounter(); }

		@Override
		default public boolean waitForRunStartCount(int count, Long timeout) throws Exception { return this.delegateRunCounted().waitForRunStartCount(count, timeout); }
		@Override
		default public boolean waitForRunCompleteCount(int count, Long timeout) throws Exception { return this.delegateRunCounted().waitForRunCompleteCount(count, timeout); }
	}

	public class Default implements RunCounted {
		protected final Logger logger;

		protected long runStartCounter = 0;
		protected long runCompleteCounter = 0;

		protected final Object runCounterMonitor = new Object();

		public Default(Logger logger) {
			this.logger = logger;
		}

		@Override
		public long getRunStartCounter() {
			return this.runStartCounter;
		}

		@Override
		public long getRunCompleteCounter() {
			return this.runCompleteCounter;
		}

		@Override
		public void resetRunCounters()
		{
			synchronized (this.runCounterMonitor)
			{
				this.runStartCounter = 0;
				this.runCompleteCounter = 0;
				this.runCounterMonitor.notify();
			}
		}

		@Override
		public long resetRunStartCounter()
		{
			synchronized (this.runCounterMonitor)
			{
				long result = this.runStartCounter;
				this.runStartCounter = 0;
				this.runCounterMonitor.notify();
				return result;
			}
		}

		@Override
		public long resetRunCompleteCounter()
		{
			synchronized (this.runCounterMonitor)
			{
				long result = this.runCompleteCounter;
				this.runCompleteCounter = 0;
				this.runCounterMonitor.notify();
				return result;
			}
		}

		@Override
		public boolean waitForRunStartCount(int count, Long timeout) throws Exception
		{
			Long deadline = null;
			if (timeout != null)
			{
				if (timeout < 0)
					throw new IllegalArgumentException("waitForRunStartCount: timeout may not be negative");
				long start = System.nanoTime() / 1000 / 1000;
				deadline = start + timeout;
			}
			synchronized (this.runCounterMonitor)
			{
				while (this.runStartCounter < count)
				{
					if (deadline != null)
					{
						long now = (System.nanoTime() / 1000 / 1000);
						if (deadline <= now)
							break;
						long useTimeout = deadline - now;
						logger.trace("waitForRunStartCount: waiting up to {} milliseconds for runCounterMonitor ...", useTimeout);
						this.runCounterMonitor.wait(useTimeout);
					}
					else
					{
						this.runCounterMonitor.wait();
					}
				}
				logger.trace("waitForRunStartCount: returning !( {} < {} )", this.runStartCounter, count);
				return !(this.runStartCounter < count);
			}
		}

		@Override
		public boolean waitForRunCompleteCount(int count, Long timeout) throws Exception
		{
			Long deadline = null;
			if (timeout != null)
			{
				if (timeout < 0)
					throw new IllegalArgumentException("waitForRunCompleteCount: timeout may not be negative");
				long start = System.nanoTime() / 1000 / 1000;
				deadline = start + timeout;
			}
			synchronized (this.runCounterMonitor)
			{
				while (this.runCompleteCounter < count)
				{
					if (deadline != null)
					{
						long now = (System.nanoTime() / 1000 / 1000);
						if (deadline <= now)
							break;
						long useTimeout = deadline - now;
						logger.trace("waitForRunCompleteCount: waiting up to {} milliseconds for runCounterMonitor ...", useTimeout);
						this.runCounterMonitor.wait(useTimeout);
					}
					else
					{
						this.runCounterMonitor.wait();
					}
				}
				logger.trace("waitForRunCompleteCount: returning !( {} < {} )", this.runCompleteCounter, count);
				return !(this.runCompleteCounter < count);
			}
		}
	}

	public interface Helper {
		public void countStart();
		public void countComplete();
		public void runWithCounters(java.lang.Runnable runnable);

		public interface Forwarding extends Helper, RunCounted.Forwarding {
			public RunCounted.Helper delegateRunCountHelper();

			@Override
			default public void countStart() { this.delegateRunCountHelper().countStart(); }
			@Override
			default public void countComplete() { this.delegateRunCountHelper().countComplete(); }
			@Override
			default public void runWithCounters(java.lang.Runnable runnable) { this.delegateRunCountHelper().runWithCounters(runnable); }
		}
	
		public class Default extends RunCounted.Default implements RunCounted.Helper {

			public Default(Logger logger) {
				super(logger);
			}

			@Override
			public void countStart()
			{
				synchronized (this.runCounterMonitor)
				{
					logger.trace("countStart: intial runStartCounter={}", this.runStartCounter);
					this.runStartCounter++;
					this.runCounterMonitor.notify();
				}
			}

			@Override
			public void countComplete()
			{
				synchronized (this.runCounterMonitor)
				{
					logger.trace("countComplete: intial runCompleteCounter={}", this.runCompleteCounter);
					this.runCompleteCounter++;
					this.runCounterMonitor.notify();
				}
			}

			@Override
			public void runWithCounters(java.lang.Runnable runnable)
			{
				countStart();
				try
				{
					runnable.run();
				}
				finally
				{
					countComplete();
				}
			}
		}
	}

	public interface Runnable extends RunCounted, java.lang.Runnable {

		public interface Forwarding extends RunCounted.Runnable, RunCounted.Forwarding, ForwardingRunnable {
			public RunCounted.Runnable delegateRunCountedRunnable();

			@Override
			default public java.lang.Runnable delegateRunnable() { return this.delegateRunCountedRunnable(); }

			@Override
			default public RunCounted delegateRunCounted() { return this.delegateRunCountedRunnable(); }
		}

		public class Default extends RunCounted.Helper.Default implements RunCounted.Runnable {
			public final java.lang.Runnable delegateRunnable;
			public Default(Logger logger, java.lang.Runnable runnable) {
				super(logger);
				this.delegateRunnable = runnable;
			}

			public java.lang.Runnable delegateRunnable()
			{
				return this.delegateRunnable;
			}

			@Override
			public void run()
			{
				this.runWithCounters(this.delegateRunnable());
			}
		}
	}
}
