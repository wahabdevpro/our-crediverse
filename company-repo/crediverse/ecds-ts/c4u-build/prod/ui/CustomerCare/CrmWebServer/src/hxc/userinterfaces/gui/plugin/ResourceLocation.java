package hxc.userinterfaces.gui.plugin;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ResourceLocation
{
	public String virtual();

	public String actual();
}
