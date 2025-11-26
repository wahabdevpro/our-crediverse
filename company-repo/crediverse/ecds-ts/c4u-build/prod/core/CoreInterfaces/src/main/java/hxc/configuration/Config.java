package hxc.configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Config
{
	String description();

	String defaultValue() default "";

	Rendering renderAs() default Rendering.DEFAULT;

	int maxLength() default -1;

	String minValue() default "";

	String maxValue() default "";

	String possibleValues() default "";

	String link() default "";

	int scaleFactor() default 1; // Amount dividedBy to diplay on UI

	int decimalDigitsToDisplay() default 0; // Digits to show in the GUI (e.g. 1.000 == 3)

	int helpID() default -1;

	boolean hidden() default false;

	String group() default ""; // For grouping elements in the GUI

	boolean unique() default false;

	String referencesKey() default "";
	
	String comment() default "";
}
