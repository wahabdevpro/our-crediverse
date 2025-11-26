package hxc.connectors.file;

import java.io.File;

public interface IFileProcessorHandler
{

	public void dispatchDTO(FileDTO dto);

	public void dispatchError(Exception exc);

	public void malformedRecord(FileDTO dto, Exception exc);

	public void processingFailed(FileDTO dto);

	public void completedDTOs(File file);

}
