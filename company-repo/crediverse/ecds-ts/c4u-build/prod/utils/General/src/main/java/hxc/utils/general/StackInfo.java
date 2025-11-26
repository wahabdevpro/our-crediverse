// vim: set ts=4 sw=4 filetype=java noexpandtab:
package hxc.utils.general;

import java.util.Formatter;

public class StackInfo
{
    public static String methodName()
    {
        return methodName( 1 );// test2
    }

    public static String methodName( final int depth )
    {
        final StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        
        return stackTraceElements[ 2 + depth ].getMethodName();
    }
    
    public static int lineNumber()
    {
        return lineNumber( 1 );
    }

    public static int lineNumber( final int depth )
    {
        final StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        return stackTraceElements[ 2 + depth ].getLineNumber();
    }
    
    public static String fileName()
    {
        return fileName( 1 );
    }

    public static String fileName( final int depth )
    {
        final StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        return stackTraceElements[ 2 + depth ].getFileName();
    }

    public static String className()
    {
        return className( 1 );
    }

    public static String className( final int depth )
    {
        final StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        return stackTraceElements[ 2 + depth ].getClassName();
    }

	public static String summary()
	{
		return summary( null, 1 );
	}

	public static String summary( final int depth )
	{
		return summary( null, depth );
	}

	public static String summary( Object object )
	{
		return summary( object, 1 );
	}

	public static String summary( Object object, final int depth )
	{
		final StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		StackTraceElement ste = stackTraceElements[ 2 + depth ];
		return String.format( "%s:%d:%s:%s%s",
			ste.getFileName(),
			ste.getLineNumber(),
			ste.getClassName(),
			ste.getMethodName(),
			( object != null ? String.format( ":%010d",  object.hashCode() ) : "" ) );
	}

	public static String summaryAll()
	{
		return summaryAll( null, 1 );
	}

	public static String summaryAll( final int depth )
	{
		return summaryAll( null, depth );
	}

	public static String summaryAll( Object object )
	{
		return summaryAll( object, 1 );
	}

	public static String summaryAll( Object object, final int depth )
	{
		Formatter out = new Formatter();
		final StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		int first = 2 + depth;
		int last = stackTraceElements.length - 1;
		for ( int i = first; i <= last; ++i )
		{
			StackTraceElement ste = stackTraceElements[ i ];
			out.format( "%s:%d:%s:%s%s%s",
				ste.getFileName(),
				ste.getLineNumber(),
				ste.getClassName(),
				ste.getMethodName(),
				( object != null ? String.format( ":%010d",  object.hashCode() ) : "" ),
				( i == last ? "" : "\n"  ) );
		}
		return out.toString();
	}
}
