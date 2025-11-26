package hxc.utils.processmodel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.bind.DatatypeConverter;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

import hxc.configuration.IConfigurable;
import hxc.connectors.database.IDatabaseConnection;
import hxc.processmodel.IProcess;
import hxc.utils.processmodel.ui.UIProperties;

public abstract class Start extends Action implements IProcess
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private Value<String> serviceID;
	private Value<String> processID;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	@UIProperties(category = "Input")
	public IValueT<String> getServiceID()
	{
		return serviceID;
	}

	@UIProperties(category = "Input")
	public IValueT<String> getProcessID()
	{
		return processID;
	}

	public Start(String serviceID, String processID)
	{
		super(null);
		this.serviceID = new Value<String>(serviceID);
		this.processID = new Value<String>(processID);
	}

	@Override
	public String serialize()
	{
		XStream xstream = new XStream(new StaxDriver());
		return xstream.toXML(this);
	}

	public static IProcess deserialize(String xml)
	{
		XStream xstream = new XStream(new StaxDriver());
		return (IProcess) xstream.fromXML(xml);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IConfigurable Implementation
	//
	// /////////////////////////////////
	@Override
	public void save(IDatabaseConnection database, long serialVersionUID) throws SQLException
	{
		ProcessRecord processRecord = new ProcessRecord();
		processRecord.SerialVersionUID = serialVersionUID;
		processRecord.Name = processID.getValue(null);
		XStream xstream = new XStream(new StaxDriver());
		processRecord.Process = compress(xstream.toXML(this));
		database.upsert(processRecord);
	}

	@Override
	public IConfigurable load(IDatabaseConnection databaseConnection, long serialVersionUID) throws SQLException
	{
		ProcessRecord processRecord = databaseConnection.select(ProcessRecord.class, "where SerialVersionUID = %s and Name = %s", serialVersionUID, processID.getValue(null));
		if (processRecord == null)
			return null;

		try
		{
			XStream xstream = new XStream(new StaxDriver());
			return (IProcess) xstream.fromXML(decompress(processRecord.Process));
		}
		catch (Exception e)
		{
			return null;
		}
	}

	@Override
	public IProcess getStart()
	{
		return this;
	}

	private String compress(String xml) throws SQLException
	{
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		GZIPOutputStream zip;
		try
		{
			zip = new GZIPOutputStream(outStream);
			zip.write(xml.getBytes("UTF8"));
			zip.close();
			outStream.flush();
			return DatatypeConverter.printBase64Binary(outStream.toByteArray());
		}
		catch (IOException e)
		{
			throw new SQLException(e.getMessage());
		}
		finally
		{
			if (outStream != null)
				try
				{
					outStream.close();
				}
				catch (IOException e)
				{
				}
		}
	}

	private static String decompress(String xml)
	{
		try
		{
			try (GZIPInputStream zip = new GZIPInputStream(new ByteArrayInputStream(DatatypeConverter.parseBase64Binary(xml))))
			{
				try (ByteArrayOutputStream output = new ByteArrayOutputStream())
				{
					byte buffer[] = new byte[10000];
					while (zip.available() != 0)
					{
						int count = zip.read(buffer, 0, buffer.length);
						if (count < 0)
							break;
						output.write(buffer, 0, count);
					}
					output.flush();
					byte data[] = output.toByteArray();
					return new String(data, "UTF8");
				}
			}
		}
		catch (IOException e)
		{
		}
		return null;
	}

}
