package hxc.connectors.archiving.archiver;

import java.io.File;

public interface IArchiver
{
	public abstract void archive(File files[], String absoluteFilename) throws Exception;
}
