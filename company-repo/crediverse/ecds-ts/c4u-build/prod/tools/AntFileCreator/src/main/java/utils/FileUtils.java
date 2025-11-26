package utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Properties;

public class FileUtils
{
	public static String findRoot() throws IOException
	{
		File pwd = new File(".");
		boolean foundRoot = false;

		while (!foundRoot)
		{
			File root = new File(pwd.getCanonicalPath() + File.separator + "prod");
			foundRoot = root.exists();
			if (!foundRoot)
			{
				pwd = pwd.toPath().resolve("../").toFile();
			}
		}
		return pwd.getCanonicalPath();
	}

	public static boolean writeFile(File file, String content)
	{
		boolean success = false;
		Writer writer = null;
		try
		{
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"));
			writer.write(content);
			success = true;
		}
		catch (IOException ex)
		{
		}
		finally
		{
			try
			{
				if (writer != null)
				{
					writer.close();
				}
			}
			catch (Exception e)
			{
			}
		}
		return success;
	}
	
	public static Properties readPropertiesFile(String propertyPath)
	{
		File propFile = new File(propertyPath);
		
		if (propFile.exists())
		{
			
			try
			{
				Properties jarProps = new Properties();
				jarProps.load(new FileInputStream(propFile));
				return jarProps;
			}
			catch (IOException e)
			{
				try
				{
					System.err.printf("Problem Reading in %s : %s", propFile.getCanonicalPath(), e.getMessage());
				}
				catch (IOException e1)
				{
				}
			}
		}
		return null;
	}
}
