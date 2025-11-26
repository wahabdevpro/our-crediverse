package hxc.jenkins.plugin.c4ubuildplugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.tmatesoft.svn.core.ISVNDirEntryHandler;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.wc.SVNLogClient;
import org.tmatesoft.svn.core.wc.SVNRevision;

public class SimpleSVNDirEntryHandler implements ISVNDirEntryHandler
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public static String identifier = "jar.properties";
	public static int maxDepth = 2;

	private static int currentDepth = 0;

	private ISVNAuthenticationManager manager;
	private SimpleSVNDirEntryHandler parent;
	private boolean root;

	private final List<SVNDirEntry> dirs = new ArrayList<SVNDirEntry>();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	// /////////////////////////////////

	public SimpleSVNDirEntryHandler()
	{
		currentDepth++;
	}

	public SimpleSVNDirEntryHandler(ISVNAuthenticationManager manager, boolean root)
	{
		this();
		this.manager = manager;
		this.root = root;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Accessor Methods
	//
	// /////////////////////////////////

	public void setParent(SimpleSVNDirEntryHandler parent)
	{
		this.parent = parent;
	}

	public List<SVNDirEntry> getSVNDir()
	{
		return dirs;
	}

	public List<String> getDirs()
	{
		return getDirs(false, false);
	}

	public List<String> getDirs(boolean reverseByDate, boolean reverseByName)
	{

		if (reverseByDate)
		{
			Collections.sort(dirs, new Comparator<SVNDirEntry>()
			{
				public int compare(SVNDirEntry dir1, SVNDirEntry dir2)
				{
					return dir2.getDate().compareTo(dir1.getDate());
				}
			});

		}
		else if (reverseByName)
		{
			Collections.sort(dirs, new Comparator<SVNDirEntry>()
			{
				public int compare(SVNDirEntry dir1, SVNDirEntry dir2)
				{
					return dir2.getName().compareTo(dir1.getName());
				}
			});
		}
		else
		{
			Collections.sort(dirs, new Comparator<SVNDirEntry>()
			{
				public int compare(SVNDirEntry dir1, SVNDirEntry dir2)
				{
					return dir1.getName().compareTo(dir2.getName());
				}
			});
		}

		List<String> sortedDirs = new ArrayList<String>();
		for (SVNDirEntry dirEntry : dirs)
		{
			sortedDirs.add(dirEntry.getName());
		}

		return sortedDirs;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	// /////////////////////////////////

	// Helper method to set the current folder depth to 0
	public static void end()
	{
		currentDepth = 0;
	}

	// When we get a directory entry given to us
	public void handleDirEntry(SVNDirEntry dirEntry) throws SVNException
	{
		// Make sure the directory is valid
		if (dirEntry.getName() == null || dirEntry.getName().length() == 0)
			return;

		// Make sure the authentication manager is valid or if the directory has the properties file or this is not root directory 
		if (manager == null || hasProperty(dirEntry) || !root)
		{
			// Add the dir
			dirs.add(dirEntry);
		}
	}

	// Check if the file is a jar.properties file
	public boolean hasProperty(SVNDirEntry dirEntry)
	{
		// Ensure it is not a hidden directory or a java folder
		if (dirEntry.getName().startsWith(".") || dirEntry.getName().startsWith("java"))
			return false;

		// Create a handler if the current depth is less than the max depth
		SimpleSVNDirEntryHandler handler;
		if (currentDepth < maxDepth + 1)
			handler = new SimpleSVNDirEntryHandler(manager, false);
		else
			return false;

		// Set this handler as the parent
		handler.setParent(this);

		// Get the items in this directory
		List<String> items = getDirectoryContent(dirEntry, handler);
		SimpleSVNDirEntryHandler.end();

		// Check if the directory contains the file
		return items != null && items.contains(identifier);
	}

	// Gets the items in the directory
	private List<String> getDirectoryContent(SVNDirEntry dirEntry, SimpleSVNDirEntryHandler handler)
	{
		// Create the client
		SVNLogClient client = new SVNLogClient(manager, null);

		try
		{
			// Do a svn list
			client.doList(dirEntry.getURL(), SVNRevision.HEAD, SVNRevision.HEAD, false, SVNDepth.IMMEDIATES, SVNDirEntry.DIRENT_TIME, handler);
		}
		catch (SVNException e)
		{
			return null;
		}

		// Clean the list of empty items
		List<String> items = cleanList(handler.getDirs());

		// Check if the items contain the file
		if (items != null && items.contains(identifier) && !root)
		{
			// If so add the directory to the parent
			if (!dirEntry.getName().contains(identifier))
				parent.dirs.add(dirEntry);
		}

		return items;
	}

	private List<String> cleanList(List<String> list)
	{
		List<String> newList = list;
		for (int i = 0; i < list.size(); i++)
		{
			String item = list.get(i);
			if (item == null || item.length() == 0)
				newList.remove(i);
		}
		return newList;
	}
}