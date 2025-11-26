package hxc.services.caisim.utils;

public class QuotedTokenizer
{
	private static String otherThanQuote = " [^\"] ";
	private static String quotedString = String.format(" \" %s* \" ", otherThanQuote);
	
	public static String[] tokenize(String input, char delimiter)
	{
        String regex = String.format("(?x) "+ // enable comments, ignore white spaces
                "%c                        "+ // match the delimiter
                "(?=                       "+ // start positive look ahead
                "  (                       "+ //   start group 1
                "    %s*                   "+ //     match 'otherThanQuote' zero or more times
                "    %s                    "+ //     match 'quotedString'
                "  )*                      "+ //   end group 1 and repeat it zero or more times
                "  %s*                     "+ //   match 'otherThanQuote'
                "  $                       "+ // match the end of the string
                ")                         ", // stop positive look ahead
                delimiter, otherThanQuote, quotedString, otherThanQuote);
		
		return input.split(regex, -1);
	}
}