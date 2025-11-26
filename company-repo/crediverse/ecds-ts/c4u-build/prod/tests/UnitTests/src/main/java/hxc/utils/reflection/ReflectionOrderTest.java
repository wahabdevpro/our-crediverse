package hxc.utils.reflection;

import java.util.Collection;

import org.junit.Test;

import hxc.configuration.IConfiguration;
import hxc.connectors.ctrl.CtrlConnector;
import hxc.connectors.database.mysql.MySqlConnector;
import hxc.servicebus.IPlugin;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.ServiceBus;
import hxc.services.logging.LoggerService;
import hxc.testsuite.RunAllTestsBase;

public class ReflectionOrderTest extends RunAllTestsBase
{

	@Test
	public void test()
	{
		// General setup
		IServiceBus esb = ServiceBus.getInstance();
		esb.stop();
		esb.registerService(new LoggerService());
		MySqlConnector.overrideDb(getDatabaseConfigurationMap());
		esb.registerConnector(new MySqlConnector());
		esb.registerConnector(new CtrlConnector());
		esb.start(null);

		IConfiguration config = ((IPlugin) esb).getConfiguration();
		Collection<PropertyInfo> properties = ReflectionHelper.getClassInfo(config.getClass()).getProperties().values();

		for (PropertyInfo property : properties)
		{
			System.out.printf("p:%s%n", property.getName());
		}

		Collection<FieldInfo> fields = ReflectionHelper.getClassInfo(config.getClass()).getFields().values();

		for (FieldInfo field : fields)
		{
			System.out.printf("f:%s%n", field.getName());
		}

	}

}
