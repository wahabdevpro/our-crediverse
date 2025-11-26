package cs.c4u.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;

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

    public static boolean createFolderIfNotExist(String path)
    {
        File dir = new File(path);
        if (!dir.exists())
            return dir.mkdir();
        else
            return true;
    }
    
    public static void writeFile(File targetFile, String content) throws IOException
    {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        Files.write(Paths.get(targetFile.toURI()), bytes, StandardOpenOption.CREATE);
    }
    
    public static void writeImage(File tagetFile, String uml) throws IOException
    {
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(tagetFile));)
        {
            SourceStringReader reader = new SourceStringReader(uml);
            String desc = reader.generateImage(os, new FileFormatOption(FileFormat.PNG));
        }
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