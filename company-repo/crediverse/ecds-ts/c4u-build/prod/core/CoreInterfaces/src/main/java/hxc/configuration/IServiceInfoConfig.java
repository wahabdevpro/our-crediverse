package hxc.configuration;

public interface IServiceInfoConfig
{
	public abstract String getDirectory();

	public abstract String getTimeFormat();

	public abstract String getRotatedFilename();

	public abstract String getInterimFilename();

	public abstract boolean getSkipArchival(); // = false
	public abstract Integer getArchiveAfterDays(); // = null
	public abstract Integer getDeleteAfterDays(); // = null

}
