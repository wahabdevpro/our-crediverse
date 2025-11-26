package hxc.connectors.ui.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import hxc.connectors.file.FileDTO;
import hxc.connectors.file.IFileProcessorHandler;
import hxc.utils.parsers.csv.CSVParser;

public class SeperatedFileProcessor
{
	private File file;
	// private static int SIZE = 8048;
	private IFileProcessorHandler handler;

	public SeperatedFileProcessor(File file)
	{
		this.file = file;
		this.handler = new LogFileProcessHandler();
	}

	public void parseFile() throws FileNotFoundException, IOException
	{
		CSVParser parser = new CSVParser(handler);
		parser.setDelimiter('|');

		try (FileInputStream f = new FileInputStream(file); FileChannel ch = f.getChannel();)
		{
			MappedByteBuffer buffer = ch.map(MapMode.READ_ONLY, 0L, ch.size());
			buffer.load();
			byte[] data = new byte[buffer.capacity()];
			buffer.get(data);

			// parser.parseAll(LogDTO.class, file.getName(), data, 0L);
			handler.completedDTOs(file);
			buffer.clear();
		}
	}

	private class LogFileProcessHandler implements IFileProcessorHandler
	{

		@Override
		public void dispatchDTO(FileDTO dto)
		{
		}

		@Override
		public void dispatchError(Exception exc)
		{

		}

		@Override
		public void malformedRecord(FileDTO dto, Exception exc)
		{
			// System.err.printf("Malformed Error: %s File:%s %n", exc.getMessage(), dto.filename);
		}

		@Override
		public void processingFailed(FileDTO dto)
		{

		}

		@Override
		public void completedDTOs(File file)
		{
		}

	}

}
