package hxc.connectors.file;

public interface IFileConnector
{
	public abstract boolean hasIncumbent(String inputDirectory, FileType fileType);

	public abstract void setIncumbent(String inputDirectory, FileType fileType, String serverRole);
}
