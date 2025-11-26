package hxc.services.logging;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;

import org.junit.Test;

public class LoggerSpeedTest
{

	private FileWriter fileWriter;

	private RandomAccessFile stream;
	private MappedByteBuffer mappedWriter;

	@Test
	public void testFileWritingSpeed()
	{
		long fileWriterTime = 0L;
		long mappedWriterTime = 0L;

		File file = new File("test");

		try
		{
			fileWriter = new FileWriter(file);
		}
		catch (IOException e)
		{
			fail("Could not open test file");
		}

		fileWriterTime = System.currentTimeMillis();
		for (int i = 0; i < 1000; i++)
		{
			try
			{
				fileWriter.append("testing with some random text\n");
				fileWriter.flush();
			}
			catch (IOException e)
			{
				fail("Failed to write to file");
			}
		}
		fileWriterTime = System.currentTimeMillis() - fileWriterTime;

		try
		{
			fileWriter.close();
		}
		catch (IOException e)
		{
			fail("Failed to close file");
		}

		file.delete();

		try
		{
			stream = new RandomAccessFile(file, "rw");
			mappedWriter = stream.getChannel().map(MapMode.READ_WRITE, 0, (long) 1e5);
		}
		catch (FileNotFoundException e)
		{
			fail("Could not open file");
		}
		catch (IOException e)
		{
			fail("Could not map file");
		}

		mappedWriterTime = System.currentTimeMillis();
		for (int i = 0; i < 1000; i++)
		{
			mappedWriter.put("testing with some random text\n".getBytes());
			mappedWriter.force();
		}
		mappedWriterTime = System.currentTimeMillis() - mappedWriterTime;

		try
		{
			stream.close();
		}
		catch (IOException e)
		{
			fail("Failed to close file");
		}

		file.delete();

		System.out.println("MappedWriter: " + mappedWriterTime + " ms");
		System.out.println("FileWriter: " + fileWriterTime + " ms");
		assertTrue(mappedWriterTime > fileWriterTime);
	}

}
