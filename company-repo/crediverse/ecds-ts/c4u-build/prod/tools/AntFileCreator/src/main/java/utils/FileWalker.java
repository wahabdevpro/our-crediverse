/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

/**
 *
 * @author jceatwell
 */
public abstract class FileWalker
{
	public FileWalker(String path) throws IOException, InterruptedException
	{
		walkFolder(path);
	}

	private final void walkFolder(String path) throws IOException, InterruptedException
	{
		final Path rootDir = Paths.get(path);

		Files.walkFileTree(rootDir, new FileVisitor<Path>()
		{

			@Override
			public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes atts) throws IOException
			{

				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path path, BasicFileAttributes mainAtts) throws IOException
			{
				if (path.endsWith(".classpath"))
				{
					File classpathFile = path.toFile();
					File projectFile = path.resolveSibling(".project").toFile();
					if (classpathFile.exists() && projectFile.exists())
						found(path.getParent(), classpathFile, projectFile);
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path path, IOException exc) throws IOException
			{

				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path path, IOException exc) throws IOException
			{
				return FileVisitResult.CONTINUE;
			}
		});
	}

	public abstract void found(Path path, File classpathFile, File projectFile);
}
