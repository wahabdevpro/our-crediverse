package hxc.utils.processmodel.ui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UIProperties
{
	String category() default "General";

	String value() default "";

	boolean editable() default false;
}
