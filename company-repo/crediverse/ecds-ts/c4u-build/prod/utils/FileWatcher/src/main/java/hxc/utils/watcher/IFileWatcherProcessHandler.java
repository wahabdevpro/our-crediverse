package hxc.utils.watcher;

import java.io.File;

public interface IFileWatcherProcessHandler
{
	public void processNewFile(File file);

	public void processDeletedFile(File file);
}
