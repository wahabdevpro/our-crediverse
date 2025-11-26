// vim: set ts=4 sw=4 filetype=java noexpandtab:
package hxc.utils.general;

import java.util.Formatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StackTraceInfo
{
	private final static Logger logger = LoggerFactory.getLogger(StackTraceInfo.class);

	private final StackTraceElement[] stackTraceElements;
	private final int pointer;

	public StackTraceInfo() {
		this( Thread.currentThread().getStackTrace(), 1 );
	}

	public StackTraceInfo(StackTraceElement[] stackTraceElements) {
		this( Thread.currentThread().getStackTrace(), 0 );
	}

	public StackTraceInfo( StackTraceElement[] stackTraceElements, int pointer ) {
		this.stackTraceElements = stackTraceElements;
		this.pointer = pointer;
	}

	public static StackTraceInfo here() {
		return new StackTraceInfo(Thread.currentThread().getStackTrace(), 2);
	}

	public static StackTraceInfo here( int depth ) {
		return new StackTraceInfo(Thread.currentThread().getStackTrace(), 2 + depth);
	}

    public String methodName()
    {
        return this.methodName( this.pointer );
    }

    public String methodName( final int depth )
    {
        return this.stackTraceElements[ this.pointer + depth ].getMethodName();
    }
    
    public int lineNumber()
    {
        return this.lineNumber( this.pointer );
    }

    public int lineNumber( final int depth )
    {
        return this.stackTraceElements[ this.pointer + depth ].getLineNumber();
    }
    
    public String fileName()
    {
        return this.fileName( this.pointer );
    }

    public String fileName( final int depth )
    {
        return this.stackTraceElements[ this.pointer + depth ].getFileName();
    }

    public String className()
    {
        return this.className( this.pointer );
    }

    public String className( final int depth )
    {
        return this.stackTraceElements[ this.pointer + depth ].getClassName();
    }

	public String summary( Object object, final int depth ) throws Exception
	{
		StackTraceElement ste = stackTraceElements[ this.pointer + depth ];
		return String.format( "%s:%d:%s%s",
			ste.getClassName(),
			ste.getLineNumber(),
			ste.getMethodName(),
			( object != null ? String.format( ":%010d",  object.hashCode() ) : "" ) );
	}

	public String invariantSummary( Object object, final int depth ) throws Exception
	{
		StackTraceElement ste = stackTraceElements[ this.pointer + depth ];
		return String.format( "%s:%s%s",
			ste.getClassName(),
			ste.getMethodName(),
			( object != null ? String.format( ":%010d",  object.hashCode() ) : "" ) );
	}

	public String summaryAll( Object object, final int depth, final Integer frames, boolean oneLine ) throws Exception
	{
		Formatter out = new Formatter();
		int first = this.pointer + depth;
		int last = ( frames != null ? Math.min( first + frames - 1, stackTraceElements.length - 1 ) : stackTraceElements.length - 1 );
		for ( int i = first; i <= last; ++i )
		{
			StackTraceElement ste = stackTraceElements[ i ];
			out.format( "%s:%d:%s%s%s",
				ste.getClassName(),
				ste.getLineNumber(),
				ste.getMethodName(),
				( object != null ? String.format( ":%010d",  object.hashCode() ) : "" ),
				( i == last ? "" : ( oneLine ? " -> " : "\n" ) ) );
		}
		return out.toString();
	}

	public static class Summariser {
		private final StackTraceInfo stackTraceInfo;
		private final Object object;
		private final int depth;
		private final Integer frames;
		private final boolean oneLine;
		public Summariser( StackTraceInfo stackTraceInfo, Object object, final int depth, final Integer frames, boolean oneLine ) {
			this.stackTraceInfo = stackTraceInfo;
			this.object = object;
			this.depth = depth;
			this.frames = frames;
			this.oneLine = oneLine;
		}

		@Override
		public String toString() {
			try {
				return this.stackTraceInfo.summaryAll(object, depth, frames, oneLine);
			} catch( Throwable throwable ) {
				logger.warn("Summariser::toString: caught", throwable);
				return throwable.toString();
			}
		}
	}
}
