package hxc.connectors.database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column
{
	String name() default "";

	boolean primaryKey() default false;

	boolean nullable() default false;

	boolean readonly() default false;

	int maxLength() default -1;

	boolean persistent() default true;

	String defaultValue() default "\0";

	// private object pseudoNull = null;
	// private boolean persistent = true;
	// private boolean identity = false;

}
