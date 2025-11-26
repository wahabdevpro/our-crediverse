package hxc.utils.watcher;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileWatcher
{
	final static Logger logger = LoggerFactory.getLogger(FileWatcher.class);

	private WatchService watcher;
	private Thread thread;

	private IFileWatcherProcessHandler handler;
	private Pattern filter;
	private boolean includeModified = true;

	public FileWatcher()
	{
		try
		{
			watcher = FileSystems.getDefault().newWatchService();
		}
		catch (IOException e)
		{
			logger.error("Filewatcher Error", e);
		}
	}

	public WatchKey registerDirectory(String directory)
	{
		Path dir = Paths.get(directory);

		try
		{
			logger.trace("FileWatcher monitoring directory [{}]", directory);
			return dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
		}
		catch (IOException e)
		{
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public void applyFilter(String filter)
	{
		logger.trace("FileWatcher monitoring file filter [{}]", filter);
		this.filter = Pattern.compile(filter);
	}

	public void setIncludeModifed(boolean includeModified)
	{
		this.includeModified = includeModified;
	}

	@SuppressWarnings("unchecked")
	public void processEvents(IFileWatcherProcessHandler handle)
	{
		this.handler = handle;
		thread = null;
		thread = new Thread()
		{
			@Override
			public void run()
			{
				while (!Thread.currentThread().isInterrupted())
				{
					WatchKey key;
					try
					{
						key = watcher.take();
					}
					catch (Exception e)
					{
						return;
					}

					for (WatchEvent<?> event : key.pollEvents())
					{
						WatchEvent.Kind<?> kind = event.kind();

						if (kind == StandardWatchEventKinds.OVERFLOW)
							continue;

						WatchEvent<Path> ev = (WatchEvent<Path>) event;
						Path dir = (Path) key.watchable();
						Path file = dir.resolve(ev.context());

						if (!Thread.currentThread().isInterrupted() && (filter == null || filter.matcher(file.toString()).matches()))
						{
							logger.trace("Processing file [{}]", file);
							
							if (kind == StandardWatchEventKinds.ENTRY_CREATE)
							{
								if (handler != null)
									handler.processNewFile(file.toFile());
							}
							else if (kind == StandardWatchEventKinds.ENTRY_DELETE)
							{
								if (handler != null)
									handler.processDeletedFile(file.toFile());
							}
							else if (kind == StandardWatchEventKinds.ENTRY_MODIFY && includeModified)
							{
								if (handler != null)
								{
									handler.processDeletedFile(file.toFile());
									handler.processNewFile(file.toFile());
								}
							}
						}

					}

					if (!key.reset())
						break;
				}
			}
		};
		thread.start();
	}

	public void destroy()
	{
		try
		{
			watcher.close();
		}
		catch (IOException e)
		{
		}

		watcher = null;
		thread.interrupt();
		handler = null;
		thread = null;
	}

}
