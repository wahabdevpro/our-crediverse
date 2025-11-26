package hxc.connectors.archiving.archiver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Archiver implements IArchiver
{

	@Override
	public void archive(File[] files, String absoluteFilename) throws Exception
	{
		try (
			FileOutputStream fileOutputStream = new FileOutputStream( absoluteFilename );
			ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
		)
		{
			zipOutputStream.setLevel(9);
			for (File file : files)
			{
				try(
					FileInputStream fileInputStream = new FileInputStream(file);
				)
				{
					zipOutputStream.putNextEntry(new ZipEntry(file.getName()));
					byte buffer[] = new byte[64*1024*1024]; // do it in 64MB chunks ...
					int bytesRead = 0;
					while ( ( bytesRead = fileInputStream.read(buffer) ) > 1 )
					{
						zipOutputStream.write(buffer, 0, bytesRead);
					}
				}
				finally
				{
					zipOutputStream.closeEntry();
				}
			}
		}
	}

}
