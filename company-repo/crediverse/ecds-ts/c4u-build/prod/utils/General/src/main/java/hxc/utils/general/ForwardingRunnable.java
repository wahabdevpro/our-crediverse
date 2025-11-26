// vim: set ts=4 sw=4 filetype=java noexpandtab:
package hxc.utils.general;

public interface ForwardingRunnable extends Runnable {
	public Runnable delegateRunnable();

	@Override
	default public void run()
	{
		this.delegateRunnable().run();
	}
}
