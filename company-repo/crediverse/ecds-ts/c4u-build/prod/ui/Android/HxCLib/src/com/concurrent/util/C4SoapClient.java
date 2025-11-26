package com.concurrent.util;

import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.ksoap2.HeaderProperty;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import com.concurrent.hxc.Program;

public class C4SoapClient
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String basic;
	private static final String NAMESPACE = "http://hxc.concurrent.com/";

	// private static final String MAIN_REQUEST_URL =
	// "http://172.17.8.10:14100/HxC";

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

	public C4SoapClient(String username, String password)
	{
		basic = String.format("%s:%s", username, password);
		basic = "Basic " + org.kobjects.base64.Base64.encode(basic.getBytes());
	}

	public <Tresp extends IDeserialisable> Tresp call(ICallable<Tresp> req) throws Exception
	{
		SoapObject object = new SoapObject("", "request");
		ISerialiser serialiser = new SoapSerialiser(object);
		req.serialise(serialiser);

		SoapObject top = new SoapObject(NAMESPACE, req.getMethodID());

		top.addSoapObject(object);

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.dotNet = false;
		envelope.implicitTypes = true;
		envelope.setAddAdornments(false);
		envelope.setOutputSoapObject(top);

	// 	HttpTransportSE ht = new HttpTransportSE(Proxy.NO_PROXY, Program.getHostAddress(), 60000);
		HttpTransportSE ht = new HttpTransportSE(Program.getHostAddress(), 600000);

		List<HeaderProperty> headerList = new ArrayList<HeaderProperty>();
		headerList.add(new HeaderProperty("Authorization", basic));

		ht.call("\"" + NAMESPACE + req.getMethodID() + "\"", envelope, headerList);
		try
		{
			object = (SoapObject) envelope.getResponse();
		}
		catch (SoapFault e)
		{
			try
			{
				object = (SoapObject) envelope.getResponse();
			}
			catch (SoapFault ex)
			{
				throw ex;
			}
		}

		ht.reset();
		serialiser = new SoapSerialiser(object);
		Tresp response = req.deserialiseResponse(serialiser);

		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
}
