package hxc.utils.thread;

import java.io.File;
import java.security.InvalidParameterException;
import java.util.ArrayList;

public abstract class FileSearchThread extends Thread
{
	private ArrayList<String> skip;

	private String filename;
	private File folder;

	public FileSearchThread(String filename)
	{
		this.filename = filename;
	}

	public FileSearchThread(String filename, String startFolder)
	{
		this(filename);

		folder = new File(startFolder);
		if (folder == null || !folder.isDirectory())
			throw new InvalidParameterException(startFolder + " is not a valid folder.");
	}

	@Override
	public void run()
	{
		foundFile(searchPath(folder == null || !folder.isDirectory() ? File.listRoots() : folder.listFiles(), filename));
	}

	public void next()
	{
		start();
	}

	public boolean isSearching()
	{
		return isAlive();
	}

	public abstract void foundFile(String filename);

	private String searchPath(File files[], String filename)
	{
		if (skip == null)
			skip = new ArrayList<String>();

		if (files == null)
			return null;

		String location = null;

		for (File file : files)
		{
			if (this.isInterrupted())
				return null;

			if (file == null)
				continue;

			if (file.isDirectory() && !skip.contains(file.getAbsolutePath()))
			{
				skip.add(file.getAbsolutePath());

				location = searchPath(file.listFiles(), filename);
				if (location != null)
				{
					break;
				}
				continue;
			}

			if (!file.canExecute())
			{
				continue;
			}

			if (skip.contains(file.getAbsolutePath()))
				continue;

			if (file.getName().equalsIgnoreCase(filename) || (file.getName().indexOf('.') > 0 && file.getName().substring(0, file.getName().indexOf('.')).equalsIgnoreCase(filename)))
			{
				location = file.getAbsolutePath();
				skip.add(location);
				break;
			}
		}

		return location;
	}
}
