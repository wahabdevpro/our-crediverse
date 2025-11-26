package hxc.connectors.file.process;

import java.io.File;
import java.io.IOException;

import hxc.connectors.file.IFileProcessorHandler;
import hxc.utils.parsers.csv.CSVParser;

public class CSVProcessor extends FileProcessor
{

	private CSVParser parser;

	// Initialises the CSV Processor and creates the parser
	protected CSVProcessor()
	{
		super();

		parser = new CSVParser(handler);
	}

	// Processes the file
	@Override
	public synchronized void process(File file, String dto, long position) throws IOException
	{
		try
		{
			// Initialises the buffer with data from the file
			configure(file, 0);

			// Load the actual buffer
			buffer.load();

			// Create a byte array
			byte data[] = new byte[buffer.capacity()];

			// Read the data from the buffer
			buffer.get(data);

			// Initialise the CSV Parser if it is uninitialiser
			if (parser == null)
			{
				parser = new CSVParser(handler);
			}

			// Parse the CSV
			parser.parseAll(convertToDTO(dto), file.getName(), data, position);
		}
		finally
		{
			// Clear and close all connections
			buffer.clear();
			fileChannel.close();
			fileStream.close();

			buffer = null;
			fileChannel = null;
			fileStream = null;
		}

		// Complete the file
		handler.completedDTOs(file);

	}

	@Override
	public void setHandler(IFileProcessorHandler handler)
	{
		super.setHandler(handler);

		parser = new CSVParser(handler);
	}
}
