package hxc.jenkins.plugin.c4ubuildplugin;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.export.Exported;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.ParameterValue;

public class C4UParameterValue extends ParameterValue
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	private static final long serialVersionUID = -5912687177735906669L;

	@Exported(visibility = 3)
	private String[] plugins;
	@Exported(visibility = 3)
	private String buildPreffix;
	@Exported(visibility = 3)
	private boolean presetName;
	@Exported(visibility = 3)
	private boolean unitTests;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////

	@DataBoundConstructor
	public C4UParameterValue(String name, String[] plugins)
	{
		super(name);

		this.plugins = plugins;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Accessor Methods
	//
	// /////////////////////////////////

	@DataBoundSetter
	public void setPlugins(String plugins[])
	{
		this.plugins = plugins;
	}

	public String[] getPlugins()
	{
		return plugins;
	}

	@DataBoundSetter
	public void setBuildPreffix(String buildPreffix)
	{
		this.buildPreffix = buildPreffix;
	}

	public String getBuildPreffix()
	{
		return buildPreffix;
	}

	@DataBoundSetter
	public void setPresetName(boolean presetName)
	{
		this.presetName = presetName;
	}

	public boolean getPresetName()
	{
		return presetName;
	}

	@DataBoundSetter
	public void setUnitTests(boolean unitTests)
	{
		this.unitTests = unitTests;
	}

	public boolean getUnitTests()
	{
		return unitTests;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	// /////////////////////////////////

	// Builds the environment variables for the shell
	@Override
	public void buildEnvVars(AbstractBuild<?, ?> build, EnvVars env)
	{
		// Ensure there is actual plugins set
		if (plugins == null || plugins.length <= 0)
			return;

		// Create a comma separated list of plugins
		StringBuilder builder = new StringBuilder();
		builder.append(plugins[0]);
		for (int i = 1; i < plugins.length; i++)
		{
			builder.append(",");
			builder.append(plugins[i]);
		}

		// Add it to the environment variable
		env.put("C4U_PLUGINS", builder.toString());

		// Add the build preffix if there is one
		if (buildPreffix != null && buildPreffix.length() > 0)
			env.put("C4U_BUILD_PREFFIX", buildPreffix);

		// Set the presets flag
		if (presetName)
			env.put("C4U_PRESET", "true");

		// Set the unit tests flag
		if (unitTests)
			env.put("C4U_UNIT_TESTS", "true");
	}

	@Override
	public String toString()
	{
		return "C4UParameter: " + getName();
	}

}
