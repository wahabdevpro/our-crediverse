package hxc.connectors.air;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Air
{
	public boolean Mandatory() default false;

	public String Range() default "";

	public String Length() default "";

	public String PC() default "";

	public String CAP() default "";

	public String Format() default "";
}
