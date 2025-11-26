package hxc.utils.general;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

//import lombok.*;
import org.slf4j.Logger;

public interface ScheduledRunnable
{
	public boolean start();
	public boolean stop();
	public void restart();

	public static interface Forwarding extends ScheduledRunnable
	{
		public ScheduledRunnable delegateScheduledRunnable();

		@Override
		default public boolean start() { return this.delegateScheduledRunnable().start(); }

		@Override
		default public boolean stop() { return this.delegateScheduledRunnable().stop(); }

		@Override
		default public void restart() { this.delegateScheduledRunnable().restart(); }
	}

	public static abstract class Default implements ScheduledRunnable, AutoCloseable
	{
		protected ScheduledFuture<?> future = null;
		protected final Object futureMonitor = new Object();

		protected final Logger logger;
		protected final ScheduledExecutorService scheduledExecutorService;

		public abstract void startLogic();
		public abstract void stopLogic();
		public abstract ScheduledFuture<?> schedule(ScheduledExecutorService scheduledExecutorService);

		public Default(Logger logger, ScheduledExecutorService scheduledExecutorService)
		{
			this.logger = logger;
			this.scheduledExecutorService = scheduledExecutorService;
		}

		@Override
		public boolean start()
		{
			logger.info("start: ...");
			synchronized (this.futureMonitor)
			{
				if (this.future == null)
				{
					logger.info("start: starting future={}", this.future);
					this.startLogic();
					this.future = this.schedule(this.scheduledExecutorService);
					this.futureMonitor.notifyAll();
					return true;
				}
				else
				{
					logger.info("start: not starting future={}", this.future);
					return false;
				}
			}
		}

		@Override
		public boolean stop()
		{
			logger.info("stop: ...");
			synchronized (this.futureMonitor)
			{
				if (this.future != null)
				{
					logger.info("start: stoppping future={}", this.future);
					this.future.cancel(true);
					this.future = null;
					this.futureMonitor.notifyAll();
					this.stopLogic();
					return true;
				}
				else
				{
					logger.info("start: not stopping future={}", this.future);
					return false;
				}
			}
		}

		@Override
		public void restart()
		{
			logger.info("restart: ...");
			synchronized (this.futureMonitor)
			{
				if (this.future != null)
				{
					logger.info("start: stoppping future={}", this.future);
					this.future.cancel(true);
					this.future = null;
					this.stopLogic();
				}
				if (this.future == null)
				{
					logger.info("start: starting future={}", this.future);
					this.startLogic();
					this.future = this.schedule(this.scheduledExecutorService);
					this.futureMonitor.notifyAll();
				}
			}
		}

		@Override
		public void close()
		{
			this.stop();
		}
	}

	public static interface RunCountedRunnable extends RunCounted.Runnable, ScheduledRunnable
	{
	}
}
