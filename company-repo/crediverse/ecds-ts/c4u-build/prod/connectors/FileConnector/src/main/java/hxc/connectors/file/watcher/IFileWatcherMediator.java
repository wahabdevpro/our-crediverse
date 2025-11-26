package hxc.connectors.file.watcher;

import java.io.File;

public interface IFileWatcherMediator
{
	public abstract void distribute(File file, String copyCommand);

	public abstract long lastRecord(File file);

	public abstract boolean isCompleted(File file);
}
