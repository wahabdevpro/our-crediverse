package hxc.services.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Perm
{
	String name();

	String description();

	String category();

	String implies() default "";

	int helpId() default -1;

	boolean supplier() default false;

}
