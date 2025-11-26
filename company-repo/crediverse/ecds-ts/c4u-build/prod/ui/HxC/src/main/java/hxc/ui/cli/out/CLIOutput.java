package hxc.ui.cli.out;

import java.io.OutputStream;
import java.io.PrintStream;

public class CLIOutput
{

	private static PrintStream out;

	public CLIOutput(OutputStream out)
	{
		CLIOutput.out = new PrintStream(out, true);
	}

	public static void setOutputStream(OutputStream out)
	{
		CLIOutput.out = new PrintStream(out, true);
	}

	// Prints the text to the outputstream with new line
	public static void println(String line)
	{
		out.println(line);
	}

	public static void println(char character)
	{
		out.println(character);
	}

	// Prints a new line to the outputstream
	public static void println()
	{
		out.println();
	}

	// Prints the text to the outputstream
	public static void print(String line)
	{
		out.print(line);
	}

	public static void print(char character)
	{
		out.print(character);
	}
}
