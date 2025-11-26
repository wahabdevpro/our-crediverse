package cs.security.permissions;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target({ TYPE, METHOD })
public @interface RequiredPermissions
{
	enum MatchType {All, ANY}

	RequiredPermission[] permissions();

	MatchType match() default MatchType.All;
}
