package hxc.userinterfaces.gui.jetty;

import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class GUILogger
{
	private static FileHandler fileTxt;
	private static SimpleFormatter formatterTxt;

	public static void setupLogger(Level logLevel) throws Exception
	{
		// Get the global logger to configure it
		Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
		logger.setLevel(Level.INFO);
		fileTxt = new FileHandler("Logging.txt");

		// create txt Formatter
		formatterTxt = new SimpleFormatter();
		fileTxt.setFormatter(formatterTxt);
		logger.addHandler(fileTxt);

		// create HTML Formatter
	}
}
