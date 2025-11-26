package hxc.connectors.ui.utils;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import hxc.servicebus.IServiceBus;
import hxc.utils.protocol.uiconnector.request.AuthenticateRequest;
import hxc.utils.protocol.uiconnector.request.PublicKeyRequest;
import hxc.utils.protocol.uiconnector.response.AuthenticateResponse;
import hxc.utils.protocol.uiconnector.response.ErrorResponse;
import hxc.utils.protocol.uiconnector.response.PublicKeyResponse;
import hxc.utils.protocol.uiconnector.response.UiBaseResponse;
import hxc.utils.uiconnector.client.UIClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionUtils
{
	final static Logger logger = LoggerFactory.getLogger(ConnectionUtils.class);
	public static UIClient createTestClientConnection(IServiceBus esb, int port) throws Exception
	{
		UIClient uic = new UIClient();

		try
		{
			uic.connect("localhost", port);
		}
		catch (IOException e1)
		{
			throw e1;
		}
		return uic;
	}

	public static String loginAndRetrieveSessionId(IServiceBus esb, int uiConnectorPort, String user, String password) throws Exception
	{
		// And Client (and send something)
		String sessionId = null;
		try (UIClient uic = createTestClientConnection(esb, uiConnectorPort))
		{
			// Step 1: Get Key
			PublicKeyRequest pkr = new PublicKeyRequest("supplier");
			UiBaseResponse br = null;
			PublicKeyResponse pr = null;
			byte[] publicKey = null;
			try
			{
				br = uic.call(pkr, UiBaseResponse.class);

			}
			catch (ClassNotFoundException | IOException e)
			{
				fail("Call failed");
			}
			assertNotNull(br);
			if (br instanceof ErrorResponse)
			{
				throw new Exception(((ErrorResponse) br).getError());
			}

			pr = (PublicKeyResponse) br;
			publicKey = pr.getPublicKey();

			// Step 2: Authenticate
			AuthenticateRequest auth2 = new AuthenticateRequest(user);
			auth2.generateSalted(publicKey, password);
			UiBaseResponse authResp = null;
			try
			{
				authResp = uic.call(auth2, AuthenticateResponse.class);
			}
			catch (ClassNotFoundException | IOException e)
			{
				throw new Exception("Authentication 2 Call failed");
			}
			sessionId = authResp.getSessionId();
		}
		catch (Exception e)
		{
			fail("Exception thrown retrieving login ... " + e.getMessage());
			throw e;
		}
		return sessionId;
	}

	public Map<String, Integer> extractTransactionID(File file)
	{
		System.out.println("Extracting from " + file.getName());
		Map<String, Integer> result = new HashMap<String, Integer>();

		try (BufferedReader br = new BufferedReader(new FileReader(file)))
		{
			String line = null;
			while ((line = br.readLine()) != null)
			{
				String[] elements = line.split("\\|");
				if (elements.length > 3)
				{
					String tid = elements[2].trim();
					if (tid.length() > 0)
					{
						if (!result.containsKey(tid))
						{
							result.put(tid, 0);
						}
						result.put(tid, result.get(tid) + 1);
					}
				}
			}
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
		return result;
	}

	public Map<String, Integer> extractTransactionIDInFolder(final File folder)
	{
		Map<String, Integer> list = new HashMap<String, Integer>();
		for (final File fileEntry : folder.listFiles())
		{
			if (fileEntry.isFile())
			{
				Map<String, Integer> tids = extractTransactionID(fileEntry);
				for (String tid : tids.keySet())
				{
					if (list.containsKey(tid))
					{
						list.put(tid, tids.get(tid) + list.get(tid));
					}
					else
					{
						list.put(tid, tids.get(tid));
					}
				}
			}
		}
		return list;
	}

}
