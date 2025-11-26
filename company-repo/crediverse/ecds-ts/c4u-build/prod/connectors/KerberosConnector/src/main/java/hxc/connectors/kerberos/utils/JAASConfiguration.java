package hxc.connectors.kerberos.utils;

import java.util.Collections;
import java.util.Map;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

public class JAASConfiguration extends Configuration
{
    private final AppConfigurationEntry[] defaultConfiguration;
    private final Map<String, AppConfigurationEntry[]> mappedConfigurations;

    // //////////////////////////////////////////////////////////////////////////////////////
    //
    // Constructors
    //
    // /////////////////////////////////
    
    /**
     * Creates a new instance with only a defaultConfiguration. Any
     * configuration name will result in defaultConfiguration being returned.
     *
     * @param defaultConfiguration
     *            The result for any calls to
     *            {@link #getAppConfigurationEntry(String)}. Can be
     *            <code>null</code>.
     */
    public JAASConfiguration(AppConfigurationEntry[] defaultConfiguration)
    {
        this(Collections.<String, AppConfigurationEntry[]> emptyMap(),
                defaultConfiguration);
    }

    /**
     * Creates a new instance with a mapping of login context name to an array
     * of {@link javax.security.auth.login.AppConfigurationEntry}s.
     *
     * @param mappedConfigurations
     *            each key represents a login context name and each value is an
     *            Array of
     *            {@link javax.security.auth.login.AppConfigurationEntry}s that
     *            should be used.
     */
    public JAASConfiguration(
            Map<String, AppConfigurationEntry[]> mappedConfigurations)
    {
        this(mappedConfigurations, null);
    }

    /**
     * Creates a new instance with a mapping of login context name to an array
     * of {@link javax.security.auth.login.AppConfigurationEntry}s along with a
     * default configuration that will be used if no mapping is found for the
     * given login context name.
     *
     * @param mappedConfigurations
     *            each key represents a login context name and each value is an
     *            Array of
     *            {@link javax.security.auth.login.AppConfigurationEntry}s that
     *            should be used.
     * @param defaultConfiguration
     *            The result for any calls to
     *            {@link #getAppConfigurationEntry(String)}. Can be
     *            <code>null</code>.
     */
    public JAASConfiguration(
            Map<String, AppConfigurationEntry[]> mappedConfigurations,
            AppConfigurationEntry[] defaultConfiguration)
    {
        // Assert.notNull(mappedConfigurations, "mappedConfigurations cannot be
        // null.");
        this.mappedConfigurations = mappedConfigurations;
        this.defaultConfiguration = defaultConfiguration;
    }

    // //////////////////////////////////////////////////////////////////////////////////////
    //
    // Methods
    //
    // /////////////////////////////////
    
    @Override
    public AppConfigurationEntry[] getAppConfigurationEntry(String name)
    {
        AppConfigurationEntry[] mappedResult = mappedConfigurations.get(name);
        return mappedResult == null ? defaultConfiguration : mappedResult;
    }

    public void refresh()
    {
    }
}
