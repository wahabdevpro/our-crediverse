package hxc.jenkins.plugin.c4ubuildplugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.auth.SVNAuthentication;
import org.tmatesoft.svn.core.auth.SVNPasswordAuthentication;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNAuthenticationManager;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNLogClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc2.ISvnObjectReceiver;
import org.tmatesoft.svn.core.wc2.SvnList;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;
import org.tmatesoft.svn.core.wc2.SvnTarget;

import hudson.Extension;
import hudson.model.ParameterValue;
import hudson.model.ParameterDefinition;
import hudson.util.FormValidation;

public class C4UParameterDefinition extends ParameterDefinition
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	private static final long serialVersionUID = -567420670106774435L;

	private String svnUri;
	private String username;
	private String password;

	private String folders;
	private String presetsFile;
	private int maxDepth = 2;

	private StringBuilder status = new StringBuilder();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////

	@DataBoundConstructor
	public C4UParameterDefinition(String name, String description, String svnUri, String username, String password, String folders, int maxDepth, String presetsFile)
	{
		super(name, description);

		this.svnUri = svnUri;
		this.username = username;
		this.password = password;
		this.folders = folders;
		this.maxDepth = maxDepth;
		SimpleSVNDirEntryHandler.maxDepth = maxDepth;
		this.presetsFile = presetsFile;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	// /////////////////////////////////

	// Creates the parameter value for jenkins
	@Override
	public ParameterValue createValue(StaplerRequest req, JSONObject jo)
	{
		// Get the c4u parameter values
		C4UParameterValue value = req.bindJSON(C4UParameterValue.class, jo);

		// Set the plugins array from the json
		value.setPlugins(jo.get("value") instanceof JSONArray ? (String[]) jo.getJSONArray("value").toArray(new String[0]) : new String[] { (String) jo.get("value") });

		// Check if there is a build preffix
		if (jo.getString("build_preffix") != null)
			// Set the build preffix from the c4u gui
			value.setBuildPreffix(jo.getString("build_preffix"));

		// Check if there is a preset name been set
		if (jo.getBoolean("presetName"))
			// Set the preset name value
			value.setPresetName(jo.getBoolean("presetName"));

		// Check if unit tests have been applied
		if (jo.getBoolean("unitTests"))
			// Set the unit tests value
			value.setUnitTests(jo.getBoolean("unitTests"));

		return value;
	}

	// Another way of creating the value that rarely gets called
	@Override
	public ParameterValue createValue(StaplerRequest req)
	{
		// Get the values from the parameters
		String values[] = req.getParameterValues(getName());
		// Ensure the parameters are not null
		if (values == null || values.length < 1)
			return this.getDefaultParameterValue();
		else
			return new C4UParameterValue(getName(), values);
	}

	public List<List<String>> getTags()
	{
		// Create an empty list that will contain the plugins
		List<List<String>> dirs = new ArrayList<List<String>>();

		// Split the comma separater list of folders into an array
		String folder[] = folders.split(",");

		// Iterate through the folders
		for (int i = 0; i < folder.length; i++)
		{

			try
			{
				// Get the plugins
				dirs.add(getPlugins(folder[i]));
			}
			catch (SVNException e)
			{
				
			}
			
			// Check the folder exists and remove the parent string part
			if (dirs.get(i) != null)
			{
				removeParentDir(dirs.get(i));
			}
			else
			{
				ArrayList<String> list = new ArrayList<String>();
				list.add("No Directory Entries Found.");
				dirs.add(list);
			}
		}

		return dirs;
	}
	
	private List<String> getPlugins(String folder) throws SVNException
	{
		return getPlugins(folder, 0);
	}
	
	private List<String> getPlugins(final String folder, final int depth) throws SVNException
	{
		// Get the repository URL
		SVNURL repositoryURL = SVNURL.parseURIEncoded(String.format("%s/%s", svnUri, folder));
		
		// Create the operation factory
		SvnOperationFactory operationFactory = new SvnOperationFactory();
		
		// Set the authentication
		SVNAuthentication authentication = new SVNPasswordAuthentication(getUsername(), getPassword(), false, repositoryURL, false);
        operationFactory.setAuthenticationManager(new BasicAuthenticationManager(new SVNAuthentication[] { authentication }));
        
        // Create a list for the results
        final List<String> results = new ArrayList<String>();
        
        // Create the svn list
        SvnList list = operationFactory.createList();
        list.setDepth(SVNDepth.IMMEDIATES);
        list.setRevision(SVNRevision.HEAD);
        list.addTarget(SvnTarget.fromURL(repositoryURL, SVNRevision.HEAD));
        
        // Set the receiver for the list
        list.setReceiver(new ISvnObjectReceiver<SVNDirEntry>()
		{
			
			public void receive(SvnTarget target, SVNDirEntry dirEntry) throws SVNException
			{
				// Get the kind
				SVNNodeKind kind = dirEntry.getKind();
				
				// Get the name
				String name = dirEntry.getName();
				
				// Check if it is a directory
				if (kind == SVNNodeKind.DIR && depth < 3)
				{
					// Ensure it is not empty, a source directory, a classes directory or a hidden directory
					if (!name.isEmpty() && !name.equalsIgnoreCase("java") && !name.equalsIgnoreCase("classes") && !name.startsWith("."))
					{
						// Get the results from that directory
						List<String> results2 = getPlugins(String.format("%s/%s", folder, dirEntry.getName()), depth + 1);
						
						// Add it to the original results
						results.addAll(results2);
					}
				}
				// Else check if it is a file
				else if (kind == SVNNodeKind.FILE)
				{
					// Check if it is a jar.properties file
					if (name.equals("jar.properties"))
					{
						// Add it to the list
						results.add(folder.substring(folder.lastIndexOf('/') + 1));
					}
				}
			}
			
		});
		
        // Run the SVN list operation
        list.run();
        
        return results;
	}

/*
	// Goes through SVN to get the plugins
	public List<List<String>> getTags()
	{
		// Create a string builder to show progress
		if (status == null)
			status = new StringBuilder();

		// Create an empty list that will contain the plugins
		List<List<String>> dirs = new ArrayList<List<String>>();

		// Split the comma separater list of folders into an array
		String folder[] = folders.split(",");

		// Iterate through the folders
		for (int i = 0; i < folder.length; i++)
		{
			try
			{
				status.append("\nCreating Authentication Manager for Folder: " + folder[i]);
				status.append("\nUsing username: " + getUsername());

				// Create a neat password string to display in the status
				String p = password.charAt(0) + "";
				for (int d = 1; d < password.length() - 1; d++)
				{
					p += "*";
				}
				p += password.charAt(password.length() - 1);

				// Add password string to the status
				status.append("\nWith password: " + p);

				// Create the authentication manager with the username and password
				ISVNAuthenticationManager authManager = new DefaultSVNAuthenticationManager(null, true, getUsername(), getPassword());

				// Create the SVN URL
				@SuppressWarnings("deprecation")
				SVNURL url = SVNURL.parseURIDecoded(svnUri);
				status.append("\nSvn Url: " + url.toString());

				// Get the repository from the URL
				SVNRepository repository = SVNRepositoryFactory.create(url);

				// Set the authentication manager for the repository
				repository.setAuthenticationManager(authManager);

				// Create a client to connect to the repository
				status.append("\nCreating svn client.");
				SVNLogClient client = new SVNLogClient(authManager, null);

				status.append("\nListing directory: " + url.appendPath(folder[i], false));
				// Create a directory handler
				SimpleSVNDirEntryHandler dirEntryHandler = new SimpleSVNDirEntryHandler(authManager, true);

				// Make the client do a svn list
				client.doList(url.appendPath(folder[i], false), SVNRevision.HEAD, SVNRevision.HEAD, false, SVNDepth.IMMEDIATES, SVNDirEntry.DIRENT_TIME, dirEntryHandler);

				// Add the directories from directory handler
				dirs.add(dirEntryHandler.getDirs(isReverseByDate(), isReverseByName()));
				status.append("\nDirs: " + dirs.toString());
			}
			catch (SVNException exc)
			{
				// If any error, make an empty list
				ArrayList<String> list = new ArrayList<String>();
				list.add("Exception: " + exc.getMessage());
				dirs.add(list);
			}

			// Check the folder exists and remove the parent string part
			if (dirs.get(i) != null)
			{
				removeParentDir(dirs.get(i));
			}
			else
			{
				ArrayList<String> list = new ArrayList<String>();
				list.add("No Directory Entries Found.");
				dirs.add(list);
			}
		}

		return dirs;
	}
*/
	
	protected void removeParentDir(List<String> dirs)
	{
		List<String> dirsToRemove = new ArrayList<String>();
		for (String dir : dirs)
		{
			if (getSvnUri().endsWith(dir))
			{
				dirsToRemove.add(dir);
			}
		}
		dirs.removeAll(dirsToRemove);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Accessor Methods
	//
	// /////////////////////////////////

	public String getSvnUri()
	{
		return svnUri;
	}

	public void setSvnUri(String svnUri)
	{
		this.svnUri = svnUri;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getFolders()
	{
		return folders;
	}

	public void setFolders(String folders)
	{
		this.folders = folders;
	}

	public String[] getFolderNames()
	{
		return folders.split(",");
	}

	public void setMaxDepth(int maxDepth)
	{
		this.maxDepth = maxDepth;
		SimpleSVNDirEntryHandler.maxDepth = maxDepth;
	}

	public int getMaxDepth()
	{
		return maxDepth;
	}

	public void setPresetsFile(String presetsFile)
	{
		this.presetsFile = presetsFile;
	}

	public String getPresetsFile()
	{
		return presetsFile;
	}

	// Gets a list of presets
	public List<List<String>> getPresets()
	{
		// Ensure the file exists
		if (presetsFile == null || !new File(presetsFile).exists())
			return null;

		// Read the file and add it to the presets
		List<List<String>> presets = new ArrayList<List<String>>();
		BufferedReader reader = null;
		try
		{
			reader = new BufferedReader(new FileReader(new File(presetsFile)));
			String line;
			while ((line = reader.readLine()) != null)
			{
				// Create an empty list
				List<String> preset = new ArrayList<String>();

				// Add the name of the preset
				preset.add(line.substring(0, line.indexOf('=')));

				// Add the plugins that go with the preset
				line = line.substring(line.indexOf('=') + 1);
				preset.add(line);

				// Add the list to the presets
				presets.add(preset);
			}
		}
		catch (IOException e)
		{

		}
		finally
		{
			try
			{
				reader.close();
			}
			catch (Exception e)
			{

			}
		}
		return presets;
	}

	public String getStatus()
	{
		return status.toString();
	}

	public boolean isReverseByDate()
	{
		return false;
	}

	public boolean isReverseByName()
	{
		return false;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Descriptor Implementation
	//
	// /////////////////////////////////

	// Used for description and validation
	@Extension
	public static class DescriptorImpl extends ParameterDescriptor
	{

		// Get the namd of the plugin that will appear in jenkins
		@Override
		public String getDisplayName()
		{
			return "C4U Parameter";
		}

		// Check the name is valid
		public FormValidation doCheckName(@QueryParameter String value)
		{
			if (value == null || value.length() <= 0)
			{
				return FormValidation.error("Please enter in a name.");
			}

			return FormValidation.ok();
		}

		// Check the SVN URL is valid
		public FormValidation doCheckSvnUri(@QueryParameter String value)
		{
			if (value == null || value.length() <= 0)
			{
				return FormValidation.error("Please enter in a url.");
			}

			return FormValidation.ok();
		}

		// Check the username is valid
		public FormValidation doCheckUsername(@QueryParameter String value)
		{
			if (value == null || value.length() <= 0)
			{
				return FormValidation.error("Please enter in a username.");
			}

			return FormValidation.ok();
		}

		// Check the password is valid
		public FormValidation doCheckPassword(@QueryParameter String value)
		{
			if (value == null || value.length() <= 0)
			{
				return FormValidation.error("Please enter in a password.");
			}

			return FormValidation.ok();
		}

	}

}
