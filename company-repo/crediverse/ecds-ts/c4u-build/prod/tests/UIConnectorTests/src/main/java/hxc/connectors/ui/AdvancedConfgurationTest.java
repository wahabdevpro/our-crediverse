package hxc.connectors.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import hxc.configuration.IConfiguration;
import hxc.configuration.ValidationException;
import hxc.servicebus.IPlugin;
import hxc.servicebus.IServiceBus;
import hxc.services.notification.INotifications;
import hxc.utils.configuration.ConfigurationBase;
import hxc.utils.instrumentation.IMetric;

public class AdvancedConfgurationTest
{

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
	}

	@Test
	public void testUpdate2()
	{
		MainContainer mc = new MainContainer();
		ConfigType1 config = (ConfigType1) mc.getConfiguration();
		Iterator<IConfiguration> iter = config.getConfigurations().iterator();
		int i = 0;
		while (iter.hasNext())
		{
			iter.next();
			ConfigType2 ct2 = new ConfigType2();
			if (i == 0)
			{
				ct2.setSomeIntConfigType2(12);
				ct2.setSomeDoubleConfig(3.1415D);
				ct2.setSomeStringConfigType2("Hello World");
			}
			i++;
		}

		i = 0;
		Iterator<IConfiguration> iter2 = config.getConfigurations().iterator();
		while (iter2.hasNext())
		{

			IConfiguration configInner = iter2.next();
			if (i == 0)
			{
				assert (((ConfigType2) configInner).getSomeIntConfigType2() == 12);
				assert (((ConfigType2) configInner).getSomeDoubleConfig() == 3.1415D);
				assert (((ConfigType2) configInner).getSomeStringConfigType2().equals("Hello World"));
			}
			else
			{
				assert (((ConfigType2) configInner).getSomeIntConfigType2() != 12);
				assert (((ConfigType2) configInner).getSomeDoubleConfig() != 3.1415D);
				assert (((ConfigType2) configInner).getSomeStringConfigType2().equals("Hello World"));
			}
			i++;

		}
	}

	@Test
	public void testCloningAndUpdate()
	{
		ConfigType1 from = new ConfigType1(13, "dd", 25.958D);
		ConfigType1 to = new ConfigType1();

		doClone(from, to);

		assert (to.getIntType() == from.getIntType());
		assert (to.getStringType().equals(from.getStringType()));
		assert (to.getDoubleType() == from.getDoubleType());
	}

	private void doClone(ConfigType1 from, ConfigType1 to)
	{
		UiUtils ut = new UiUtils();
		ut.deepCopy(from, to);
	}

	private class MainContainer implements IPlugin
	{

		ConfigType1 config = new ConfigType1();

		@Override
		public void initialise(IServiceBus esb)
		{
		}

		@Override
		public boolean start(String[] args)
		{
			return false;
		}

		@Override
		public void stop()
		{
		}

		@Override
		public IConfiguration getConfiguration()
		{
			return config;
		}

		@Override
		public void setConfiguration(IConfiguration config) throws ValidationException
		{
			this.config = (ConfigType1) config;
		}

		@Override
		public boolean canAssume(String serverRole)
		{
			return false;
		}

		@Override
		public boolean isFit()
		{
			return false;
		}

		@Override
		public IMetric[] getMetrics()
		{
			return null;
		}

	}

	class ConfigType1 extends ConfigurationBase
	{
		private int intType;
		private String stringType;
		private double doubleType;

		private ConfigType2[] innerConfig = new ConfigType2[2];

		public ConfigType1(int i, String s, double d)
		{
			this.intType = i;
			this.stringType = s;
			this.doubleType = d;
		}

		public ConfigType1()
		{
			innerConfig[0] = new ConfigType2();
			innerConfig[1] = new ConfigType2();
		}

		public int getIntType()
		{
			return intType;
		}

		public void setIntType(int intType)
		{
			this.intType = intType;
		}

		public String getStringType()
		{
			return stringType;
		}

		public void setStringType(String stringType)
		{
			this.stringType = stringType;
		}

		public double getDoubleType()
		{
			return doubleType;
		}

		public void setDoubleType(double doubleType)
		{
			this.doubleType = doubleType;
		}

		@Override
		public String getPath(String languageCode)
		{
			return null;
		}

		@Override
		public INotifications getNotifications()
		{
			return null;
		}

		@Override
		public long getSerialVersionUID()
		{
			return 12345258L;
		}

		@Override
		public String getName(String languageCode)
		{
			return "ConfigType1";
		}

		@Override
		public void validate() throws ValidationException
		{
		}

		@Override
		public Collection<IConfiguration> getConfigurations()
		{
			List<IConfiguration> result = new ArrayList<IConfiguration>();
			result.add(innerConfig[0]);
			result.add(innerConfig[1]);
			return result;
		}

	}

	class ConfigType2 extends ConfigurationBase
	{
		private int someIntConfigType2;
		private String someStringConfigType2;
		private double someDoubleConfig;

		public int getSomeIntConfigType2()
		{
			return someIntConfigType2;
		}

		public void setSomeIntConfigType2(int someIntConfigType2)
		{
			this.someIntConfigType2 = someIntConfigType2;
		}

		public String getSomeStringConfigType2()
		{
			return someStringConfigType2;
		}

		public void setSomeStringConfigType2(String someStringConfigType2)
		{
			this.someStringConfigType2 = someStringConfigType2;
		}

		public double getSomeDoubleConfig()
		{
			return someDoubleConfig;
		}

		public void setSomeDoubleConfig(double someDoubleConfig)
		{
			this.someDoubleConfig = someDoubleConfig;
		}

		@Override
		public String getPath(String languageCode)
		{
			return "";
		}

		@Override
		public INotifications getNotifications()
		{
			return null;
		}

		@Override
		public long getSerialVersionUID()
		{
			return -7606082906429808141L;
		}

		@Override
		public String getName(String languageCode)
		{
			return "ConfigType2";
		}

		@Override
		public void validate() throws ValidationException
		{
		}

	}

}
