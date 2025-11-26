package hxc.utils.pcc.factory;

import java.io.File;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import hxc.utils.pcc.Config;

public class PccFactory
{

	public class PccException extends Exception
	{

		private static final long serialVersionUID = -2790405344948645306L;

		public PccException(String message, Object... args)
		{
			super(String.format(message, args));
		}

	}

	interface IPccConfigurationMonitor
	{
		public void newConfig(Config config);
	}

	private final String schemaLocation = "hxc/utils/pcc/schema/pcc-config.xsd";

	public Config createConfig(File configFile) throws PccException
	{

		// Create Schema Sources
		Source pccSchema = new StreamSource(Thread.currentThread().getContextClassLoader().getResourceAsStream(schemaLocation));
		Source libbaseTypes = new StreamSource(Thread.currentThread().getContextClassLoader().getResourceAsStream("hxc/utils/pcc/schema/cs-cxx-libbase-types.xsd"));
		Source xcipTypes = new StreamSource(Thread.currentThread().getContextClassLoader().getResourceAsStream("hxc/utils/pcc/schema/xcip-types.xsd"));

		// Create Schema Factory
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

		// Ensure the order the sources are added are correct
		Schema schema;
		try
		{
			schema = factory.newSchema(new Source[] { libbaseTypes, xcipTypes, pccSchema });
		}
		catch (SAXException e)
		{
			throw new PccException("Unable to create schema from factory: %s", e.toString());
		}

		// Create the JAXB Context for the unmarshaller
		JAXBContext jaxbContext;
		try
		{
			jaxbContext = JAXBContext.newInstance(Config.class);
		}
		catch (JAXBException e)
		{
			throw new PccException("Unable to create a new instance of JAXBContext: %s", e.toString());
		}

		// Create the unmarshaller
		Unmarshaller unmarshaller;
		try
		{
			unmarshaller = jaxbContext.createUnmarshaller();
		}
		catch (JAXBException e)
		{
			throw new PccException("Unable to create unmarshaller: %s", e.toString());
		}

		// Set the schema
		unmarshaller.setSchema(schema);

		// Unmarshal the config file and create the Config object
		JAXBElement<Config> root;
		try
		{
			root = unmarshaller.unmarshal(new StreamSource(configFile), Config.class);
		}
		catch (JAXBException e)
		{
			throw new PccException("Unable to unmarshal the configuration file: %s", e.toString());
		}

		return root.getValue();

	}

	public void monitorPccConfiguration(File configFile, IPccConfigurationMonitor monitor)
	{

	}

}
