package hxc.connectors.file.process;

import hxc.connectors.file.FileProcessorType;

public class FileProcessorFactory
{

	// Builds the file processor
	public static FileProcessor buildFileProcessor(FileProcessorType type)
	{
		FileProcessor fileProcessor = null;

		switch (type)
		{
			case CSV:
				fileProcessor = new CSVProcessor();
				break;
			case ASN1:
				break;
		}

		return fileProcessor;
	}

}
