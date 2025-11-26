package hxc.connectors.zte;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

import hxc.connectors.IConnection;
import hxc.connectors.zte.ZTEConnector.ZTEConnectionConfig;
import hxc.services.logging.ILogger;
import hxc.services.logging.LoggingLevels;
import hxc.services.notification.Phrase;
import hxc.utils.calendar.DateTime;

import hxc.utils.protocol.zte.basewebservice.TQryProdStateReq;
import hxc.utils.protocol.zte.basewebservice.TQryProdStateRes;
import hxc.utils.protocol.zte.basewebservice.TQueryUserProfileReq;
import hxc.utils.protocol.zte.basewebservice.TQueryUserProfileRsp;
import hxc.utils.protocol.zte.basewebservice.TResponseBO;
import hxc.utils.protocol.zte.basewebservice.TServiceDto;
import hxc.utils.protocol.zte.basewebservice.TServiceDto1;
import hxc.utils.protocol.zte.basewebservice.TServiceDto2;
import hxc.utils.protocol.zte.basewebservice.TSetServiceReq;
//import hxc.utils.protocol.zte.basewebservice.ObjectFactory;
import hxc.utils.protocol.zte.econetwebservice.ObjectFactory;
import hxc.utils.protocol.zte.econetwebservice.TArrayOfBalDtoQryAcctBal;
import hxc.utils.protocol.zte.econetwebservice.TAuthHeader;
import hxc.utils.protocol.zte.econetwebservice.TBalDtoForQryAcctBal;
import hxc.utils.protocol.zte.econetwebservice.TDeductFeeRequest;
import hxc.utils.protocol.zte.econetwebservice.TDeductFeeResponse;
import hxc.utils.protocol.zte.econetwebservice.TQueryAcctBalBO;
import hxc.utils.protocol.zte.econetwebservice.TQueryAcctBalBOResponse;
import hxc.utils.protocol.zte.subsinformation.TAddFellowISDNBO;
import hxc.utils.protocol.zte.subsinformation.TDelFellowISDNBO;
import hxc.utils.protocol.zte.subsinformation.TFellowISDNLogDto;
import hxc.utils.protocol.zte.subsinformation.TModFellowISDNBO;
import hxc.utils.protocol.zte.subsinformation.TQueryFellowISDNLogBO;
import hxc.utils.protocol.zte.subsinformation.TQueryFellowISDNLogBOResponse;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Endpoint;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

@SuppressWarnings("rawtypes")
public class ZTEConnection implements IConnection, IZTEConnection, Comparable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private ILogger logger;
	private ZTEConnectionConfig zteConfig;
	private SOAPConnection soapConnection = null;
	private SOAPConnectionFactory soapConnectionFactory = null;
	private MessageFactory messageFactory;
	private URL fullURL = null;
	private int pendingRequests = 0;
	private int consecutiveErrors = 0;
	private Date useAfter = new Date();
	private Date lastUsed = new Date();
	private HashMap<String, AtomicLong> zteCallsCounter;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public boolean isServicable(DateTime now)
	{
		String uri = zteConfig.getStartingURL();
		return uri != null && uri.length() > 0 && now.after(useAfter);
	}

	public HashMap<String, AtomicLong> getZTECallsCounter()
	{
		return zteCallsCounter;
	}

	@Override
	public String getAddress()
	{
		String url = zteConfig.getStartingURL();
		if (url == null)
			return "";
		try
		{
			URI uri = new URI(url);
			return uri.getHost();
		}
		catch (URISyntaxException e)
		{
			return "";
		}
	}

	@Override
	public Integer getPort()
	{
		String url = zteConfig.getStartingURL();
		if (url == null)
			return null;
		try
		{
			URI uri = new URI(url);
			return Integer.valueOf(uri.getPort());
		}
		catch (URISyntaxException e)
		{
			return null;
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public ZTEConnection(ILogger logger, ZTEConnectionConfig zteConfig)
	{
		try
		{
			this.soapConnectionFactory = SOAPConnectionFactory.newInstance();
			this.soapConnection = soapConnectionFactory.createConnection();
			this.messageFactory = MessageFactory.newInstance();

			this.logger = logger;
			this.zteConfig = zteConfig;
			this.zteCallsCounter = new HashMap<String, AtomicLong>();
		}
		catch (Exception e)
		{

		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IConnection Implementation
	//
	// /////////////////////////////////

	@Override
	public void close() throws IOException
	{

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// UCIP Methods
	//
	// /////////////////////////////////

	/**
	 * getAccountDetails
	 * 
	 * The GetAccountDetails message is used to obtain account information in order to validate and tailor the user communication. Information on subscriber and account level is returned in the
	 * message. Information is only returned in case it has previously been set on the account. Example, serviceFeeExpiryDate is only returned if the account has been activated (and thus has been
	 * assigned an end date for service fee). Note: If pre-activation is wanted then messageCapabilityFlag.accountActivationFlag should be included set to 1. Note: If the locationNumber is not found,
	 * the Visitor Location Register (VLR) is returned.
	 * 
	 * @param GetAccountDetailsRequest
	 *            request
	 * @return GetAccountDetailsResponse response
	 * @throws ZTEException
	 *             if ZTE returns any one of: 0, 1, 2, 100, 102, 197, 260, 999
	 */
	@Override
	public TQueryUserProfileRsp getAccountDetails(TQueryUserProfileReq request) throws ZTEException
	{
		TQueryUserProfileRsp result = null;

		try
		{
			SOAPMessage soapMessage = messageFactory.createMessage();
			SOAPBody soapBody = soapMessage.getSOAPBody();
			soapBody.addNamespaceDeclaration("xsd", "http://com.ztesoft.zsmart/xsd");

			SOAPElement soapBodyElem = soapBody.addChildElement("QueryUserProfileReq", "xsd");
			SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("MSISDN");
			String strMSISDN = request.getMSISDN();
			if (strMSISDN.length() < 5)
				return null;
			else
				soapBodyElem1.addTextNode(request.getMSISDN());

			SOAPBody retElement = call("QueryUserProfileReq", soapBodyElem, "Webservice.WebserviceHttpSoap11Endpoint/");

			org.w3c.dom.Node childNode = retElement.getFirstChild();
			DOMSource source = new DOMSource(childNode);

			String localname = source.getNode().getLocalName();
			if (localname.compareTo("QueryUserProfileRsp") == 0)// QueryUserProfile4BaseBOResponse
			{
				result = JAXB.unmarshal(source, TQueryUserProfileRsp.class); // unmarshal for TQueryAcctBalBOResponse
				//TODO date elements must be extracted manually
				//TArrayOfBalDtoQryAcctBal balDtoListUnmarshalled = null; // variables used for different levels of JAXB.unmarshal
				//TBalDtoForQryAcctBal balDtoUnmarshalled = null;
			}

		}
		catch (SOAPException e)
		{
			throw new ZTEException(e.getMessage(), e.getCause());
		}
		catch (Throwable ex)
		{
			throw ex;
		}

		return result;
	}

	/**
	 * getBalanceAndDate
	 * 
	 * The message GetBalanceAndDate is used to perform a balance enquiry on the account associated with a specific subscriber identity. Also lifecycle dates are presented. Information is given on
	 * both main and dedicated accounts. Note: If pre-activation is wanted then messageCapabilityFlag.accountActivati onFlag should be included set to 1. For a product private (instantiated) DA, the
	 * GetBalanceAndDate request should be used to only get their instance ID (productID). To get the capabilities which the DA share with the Offer, use the GetOffers request.
	 * 
	 * @param GetBalanceAndDateRequest
	 *            request
	 * @return GetBalanceAndDateResponse response
	 * @throws ZTEException
	 *             if ZTE returns any one of: 0, 100, 102, 123, 124, 126, 137, 139, 197, 260, 999
	 */
	@Override
	public TQueryAcctBalBOResponse getBalanceAndDate(TQueryAcctBalBO request) throws ZTEException
	{
		TQueryAcctBalBOResponse result = null;

		try
		{
			SOAPMessage soapMessage = messageFactory.createMessage();
			SOAPBody soapBody = soapMessage.getSOAPBody();
			soapBody.addNamespaceDeclaration("xsd", "http://com.ztesoft.zsmart/xsd");

			SOAPElement soapBodyElem = soapBody.addChildElement("QueryAcctBalBO", "xsd");
			SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("MSISDN");
			String strMSISDN = request.getMSISDN();
			if (strMSISDN.length() < 5)
				return null;
			else
				soapBodyElem1.addTextNode(request.getMSISDN());

			SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("AccountCode");
			String strAccountCode = request.getAccountCode();
			if (strAccountCode != null && strAccountCode.length() > 0)
				soapBodyElem2.addTextNode(request.getAccountCode());
			else
				soapBodyElem2.addTextNode("");

			SOAPBody retElement = call("QueryAcctBalBO", soapBodyElem, null);
			org.w3c.dom.Node childNode = retElement.getFirstChild();
			DOMSource source = new DOMSource(childNode);

			String localname = source.getNode().getLocalName();
			if (localname.compareTo("QueryAcctBalBOResponse") == 0)
			{
				TArrayOfBalDtoQryAcctBal balDtoListUnmarshalled = null; // variables used for different levels of JAXB.unmarshal
				TBalDtoForQryAcctBal balDtoUnmarshalled = null;

				result = JAXB.unmarshal(source, TQueryAcctBalBOResponse.class); // unmarshal for TQueryAcctBalBOResponse

				org.w3c.dom.Node balDtoList = childNode.getFirstChild();
				DOMSource balDtoListSource = new DOMSource(balDtoList);
				balDtoListUnmarshalled = JAXB.unmarshal(balDtoListSource, TArrayOfBalDtoQryAcctBal.class); // unmarshal for TArrayOfBalDtoQryAcctBal

				org.w3c.dom.Node balDto = balDtoList.getFirstChild();
				DOMSource balDtoSource = new DOMSource(balDto);
				balDtoUnmarshalled = JAXB.unmarshal(balDtoSource, TBalDtoForQryAcctBal.class); // unmarshal for TBalDtoForQryAcctBal

				balDtoListUnmarshalled.setBalDto(balDtoUnmarshalled);
				result.getBalDtoList().set(0, balDtoListUnmarshalled); // first iteration index already created by first step, thus no .add()

				if (balDtoSource.getNode().getFirstChild().getNextSibling().getNextSibling().getNextSibling().getNextSibling().getNextSibling().getNextSibling().hasChildNodes())
				{
					String expDate = balDtoSource.getNode().getFirstChild().getNextSibling().getNextSibling().getNextSibling().getNextSibling().getNextSibling().getNextSibling().getFirstChild()
							.getNodeValue();

					DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
					Date parsedExpDate = df.parse(expDate);

					GregorianCalendar cal = new GregorianCalendar();
					cal.setTime(parsedExpDate);
					XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
					result.getBalDtoList().get(0).getBalDto().setExpDate(calendar);
				}

				int index = 1;
				while (index != 0) // loop to recover all DA's associated with the MSISDN in question
				{
					if (balDto.getNextSibling() == null)
					{
						index = 0;
						break;
					}
					balDtoListUnmarshalled = JAXB.unmarshal(balDtoListSource, TArrayOfBalDtoQryAcctBal.class);// required for every iteration (Don't know why yet)
					balDto = balDto.getNextSibling();
					balDtoSource = new DOMSource(balDto);
					balDtoUnmarshalled = JAXB.unmarshal(balDtoSource, TBalDtoForQryAcctBal.class);

					balDtoListUnmarshalled.setBalDto(balDtoUnmarshalled);
					result.getBalDtoList().add(balDtoListUnmarshalled);
					result.getBalDtoList().set(index, balDtoListUnmarshalled);

					if (balDtoSource.getNode().getFirstChild().getNextSibling().getNextSibling().getNextSibling().getNextSibling().getNextSibling().getNextSibling().hasChildNodes())
					{
						String expDate = balDtoSource.getNode().getFirstChild().getNextSibling().getNextSibling().getNextSibling().getNextSibling().getNextSibling().getNextSibling().getFirstChild()
								.getNodeValue();

						DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
						Date parsedExpDate = df.parse(expDate);

						GregorianCalendar cal = new GregorianCalendar();
						cal.setTime(parsedExpDate);
						;
						XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
						result.getBalDtoList().get(index).getBalDto().setExpDate(calendar);
					}

					index++;
				}
				result.getBalDtoList();
			}

		}
		catch (SOAPException e)
		{
			throw new ZTEException(e.getMessage(), e.getCause());
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}
		catch (DatatypeConfigurationException e)
		{
			e.printStackTrace();
		}
		catch (Throwable ex)
		{
			throw ex;
		}

		return result;
	}

	/**
	 * The message GetOffers will return a list of offers currently assigned to an account. The detail level of the returned list can be specified in the request using various flags. To get
	 * subDedicatedAccounts, both requestSubDedicatedAccountDetailsFlag and requestDedicatedAccountDetailsFlag must be set to "1". For product private (instantiated) DA:s, the GetOffers request should
	 * be used to get the capabilities which the DA share with the Offer. Such data are start and expiry date, dateTime, state, offer type, PAM service and offerProviderID.
	 * 
	 * @param GetOffersRequest
	 *            request
	 * @return GetOffersResponse response
	 * @throws ZTEException
	 *             if ZTE returns any one of: 0, 100, 102, 165, 214, 247, 260
	 */
	@Override
	public TQryProdStateRes getOffers(TQryProdStateReq request) throws ZTEException
	{
		TQryProdStateRes result = null;

		try
		{
			SOAPMessage soapMessage = messageFactory.createMessage();
			SOAPBody soapBody = soapMessage.getSOAPBody();
			soapBody.addNamespaceDeclaration("xsd", "http://com.ztesoft.zsmart/xsd");

			SOAPElement soapBodyElem = soapBody.addChildElement("QryProdStateReq", "xsd");
			SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("MSISDN");
			String strMSISDN = request.getMSISDN();
			if (strMSISDN.length() < 5)
				return null;
			else
				soapBodyElem1.addTextNode(request.getMSISDN());

			SOAPElement soapBodyElem1b = soapBodyElem.addChildElement("UserPwd");
			String strUserPwd = request.getUserPwd();
			if (strUserPwd.length() > 2)
				soapBodyElem1b.addTextNode(strUserPwd);
			else
				soapBodyElem1b.addTextNode("");

			if (request.getServiceDtoList() != null)
			{
				SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("ServiceDtoList");
				SOAPElement soapBodyElem3 = soapBodyElem2.addChildElement("ServiceDto");
				SOAPElement soapBodyElem4 = soapBodyElem3.addChildElement("ServiceCode");
				String strServiceCode = request.getServiceDtoList().getServiceCode();
				if (strServiceCode != null && strServiceCode.length() > 0)
					soapBodyElem4.addTextNode(strServiceCode);
				else
					soapBodyElem4.addTextNode("");
			}
			SOAPBody retElement = call("QryProdStateReq", soapBodyElem, "Webservice.WebserviceHttpSoap11Endpoint/");
			org.w3c.dom.Node childNode = retElement.getFirstChild();
			DOMSource source = new DOMSource(childNode);

			String localname = source.getNode().getLocalName();
			if (localname.compareTo("QryProdStateRes") == 0)
			{
				TQryProdStateRes serviceDtoListUnmarshalled = null; // variables used for different levels of JAXB.unmarshal
				TServiceDto2 serviceDtoUnmarshalled = null;

				result = JAXB.unmarshal(source, TQryProdStateRes.class); // unmarshal for TQryProdStateRes

				org.w3c.dom.Node serviceDtoList = childNode.getFirstChild().getNextSibling();
				DOMSource serviceDtoListSource = new DOMSource(serviceDtoList);
				serviceDtoListUnmarshalled = JAXB.unmarshal(serviceDtoListSource, TQryProdStateRes.class); // unmarshal for TQryProdStateRes

				org.w3c.dom.Node serviceDto = serviceDtoList.getFirstChild();
				DOMSource serviceDtoSource = new DOMSource(serviceDto);
				serviceDtoUnmarshalled = JAXB.unmarshal(serviceDtoSource, TServiceDto2.class); // unmarshal for TServiceDto2

				result.setServiceDtoList(serviceDtoUnmarshalled); // first iteration index already created by first step, thus no .add()

				int index = 1;
				while (index != 0) // loop to recover all Offers associated with the MSISDN in question
				{
					if (serviceDto.getNextSibling() == null)
					{
						index = 0;
						break;
					}
					serviceDtoListUnmarshalled = JAXB.unmarshal(serviceDtoListSource, TQryProdStateRes.class);// required for every iteration (Don't know why yet)
					serviceDto = serviceDto.getNextSibling();
					serviceDtoSource = new DOMSource(serviceDto);
					serviceDtoUnmarshalled = JAXB.unmarshal(serviceDtoSource, TServiceDto2.class);

					serviceDtoListUnmarshalled.setServiceDtoList(serviceDtoUnmarshalled);
					result.setServiceDtoList(serviceDtoUnmarshalled);

					index++;
				}
			}

		}
		catch (SOAPException e)
		{
			throw new ZTEException(e.getMessage(), e.getCause());
		}
		catch (Throwable ex)
		{
			throw ex;
		}

		return result;
	}

	/**
	 * updateBalanceAndDate
	 * 
	 * The message UpdateBalanceAndDate is used by external system to adjust balances, start dates and expiry dates on the main account and the dedicated accounts. On the main account it is possible
	 * to adjust the balance and expiry dates both negative and positive (relative) direction and it is also possible to adjust the expiry dates with absolute dates. The dedicated accounts balances,
	 * start dates and expiry dates could be adjusted in negative and positive direction or with absolute values. Note: It is not possible to do both a relative and an absolute balance or date set for
	 * the same data type (example: it is possible to either set an absolute OR a relative adjustment to the service fee expiry date). It is also possible to set the Service removal and Credit
	 * clearance periods on account. Note: If pre-activation is wanted then messageCapabilityFlag.accountActivati onFlag should be included set to 1.
	 * 
	 * @param UpdateBalanceAndDateRequest
	 *            request
	 * @return UpdateBalanceAndDateResponse response
	 * @throws ZTEException
	 *             if ZTE returns any one of: 0, 100, 102, 104, 105, 106, 121, 122, 123, 124, 126, 136, 139, 153, 163, 164, 167, 204, 212, 226, 227, 230, 247, 249, 257, 260, 999
	 */
	@Override
	public TDeductFeeResponse updateBalanceAndDate(TDeductFeeRequest request) throws ZTEException
	{
		TDeductFeeResponse result = null;

		try
		{
			SOAPMessage soapMessage = messageFactory.createMessage();
			SOAPBody soapBody = soapMessage.getSOAPBody();
			soapBody.addNamespaceDeclaration("xsd", "http://com.ztesoft.zsmart/xsd");

			SOAPElement soapBodyElem = soapBody.addChildElement("DeductFeeRequest", "xsd");

			SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("SN");
			request.setSN("CMBK10001");
			soapBodyElem1.addTextNode(request.getSN());

			SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("TransactionDesc");
			request.setTransactionDesc("CMBK");
			soapBodyElem2.addTextNode(request.getTransactionDesc());

			SOAPElement soapBodyElem3 = soapBodyElem.addChildElement("PartyCode");
			request.setPartyCode("USSD GW");
			soapBodyElem3.addTextNode(request.getPartyCode());

			SOAPElement soapBodyElem4 = soapBodyElem.addChildElement("MSISDN");
			String strMSISDN = request.getMSISDN();
			if (strMSISDN.length() < 5)
				return null;
			else
				soapBodyElem4.addTextNode(request.getMSISDN());

			SOAPElement soapBodyElem5 = soapBodyElem.addChildElement("AccountCode");
			String strAccountCode = request.getAccountCode();
			if (strAccountCode != null && strAccountCode.length() > 0)
				soapBodyElem5.addTextNode(request.getAccountCode());
			else
				soapBodyElem5.addTextNode("");

			SOAPElement soapBodyElem6 = soapBodyElem.addChildElement("AcctResCode");
			request.setAcctResCode("");
			soapBodyElem6.addTextNode(request.getAcctResCode());

			SOAPElement soapBodyElem7 = soapBodyElem.addChildElement("Charge");
			soapBodyElem7.addTextNode(request.getCharge());

			SOAPElement soapBodyElem8 = soapBodyElem.addChildElement("ServiceID");
			String strServiceID = request.getServiceID();
			if (strServiceID != null && strServiceID.length() > 0)
				soapBodyElem8.addTextNode(request.getServiceID());
			else
				soapBodyElem8.addTextNode("");

			SOAPElement soapBodyElem9 = soapBodyElem.addChildElement("SourceMSISDN");
			String strSourceMSISDN = request.getSourceMSISDN();
			if (strSourceMSISDN != null && strSourceMSISDN.length() > 0)
				soapBodyElem9.addTextNode(request.getSourceMSISDN());
			else
				soapBodyElem9.addTextNode("");

			SOAPElement soapBodyElem10 = soapBodyElem.addChildElement("Date");
			Date now = new Date();
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(now);
			try
			{
				XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
				soapBodyElem10.addTextNode(calendar.toString());

			}
			catch (DatatypeConfigurationException e)
			{
				e.printStackTrace();
			}

			SOAPBody retElement = call("DeductFeeRequest", soapBodyElem, null);

			org.w3c.dom.Node childNode = retElement.getFirstChild();
			DOMSource source = new DOMSource(childNode);
			source.getNode();

			String localname = source.getNode().getLocalName();
			if (localname.compareTo("DeductFeeResponse") == 0)
			{
				result = JAXB.unmarshal(source, TDeductFeeResponse.class);
			}

		}
		catch (SOAPException e)
		{
			throw new ZTEException(e.getMessage(), e.getCause());
		}
		catch (Throwable ex)
		{
			throw ex;
		}

		return result;
	}

	/**
	 * The GetFaFList message is used to fetch the list of Family and Friends numbers with attached FaF indicators.
	 * 
	 * @param GetFaFListRequest
	 *            request
	 * @return GetFaFListResponse response
	 * @throws ZTEException
	 *             if ZTE returns any one of: 0, 100, 102, 126, 260, 999
	 */
	@Override
	public TQueryFellowISDNLogBOResponse getFaFList(TQueryFellowISDNLogBO request) throws ZTEException
	{
		TQueryFellowISDNLogBOResponse result = null;

		try
		{
			SOAPMessage soapMessage = messageFactory.createMessage();
			SOAPBody soapBody = soapMessage.getSOAPBody();
			soapBody.addNamespaceDeclaration("xsd", "http://com.ztesoft.zsmart/xsd");

			SOAPElement soapBodyElem = soapBody.addChildElement("QueryFellowISDNReq", "xsd");
			SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("MSISDN");
			String strMSISDN = request.getMSISDN();
			if (strMSISDN.length() < 5)
				return null;
			else
				soapBodyElem1.addTextNode(request.getMSISDN());

			SOAPBody retElement = call("QueryFellowISDNReq", soapBodyElem, "Webservice.WebserviceHttpSoap11Endpoint/");
			org.w3c.dom.Node childNode = retElement.getFirstChild();
			DOMSource source = new DOMSource(childNode);

			String localname = source.getNode().getLocalName();
			if (localname.compareTo("QueryFellowISDNRes") == 0)
			{
				TQueryFellowISDNLogBOResponse fellowDtoListUnmarshalled = null; // variables used for different levels of JAXB.unmarshal
				TFellowISDNLogDto fellowDtoUnmarshalled = null;

				result = JAXB.unmarshal(source, TQueryFellowISDNLogBOResponse.class); // unmarshal for TFellowISDNLogDto

				if (!childNode.hasChildNodes())
				{
					return result;
				}
				org.w3c.dom.Node fellowDtoList = childNode.getFirstChild();
				DOMSource fellowDtoListSource = new DOMSource(fellowDtoList);
				fellowDtoListUnmarshalled = JAXB.unmarshal(fellowDtoListSource, TQueryFellowISDNLogBOResponse.class); // unmarshal for TQueryFellowISDNLogBOResponse

				org.w3c.dom.Node fellowDto = childNode.getFirstChild();
				DOMSource fellowDtoSource = new DOMSource(fellowDto);
				fellowDtoUnmarshalled = JAXB.unmarshal(fellowDtoSource, TFellowISDNLogDto.class); // unmarshal for TFellowISDNLogDto

				fellowDtoListUnmarshalled.setFellowISDNLogDtoList(fellowDtoUnmarshalled);

				if (fellowDtoSource.getNode().getFirstChild().getNextSibling().getNextSibling().getNextSibling().getNextSibling().hasChildNodes())
				{
					String expDate = fellowDtoSource.getNode().getFirstChild().getNextSibling().getNextSibling().getNextSibling().getNextSibling().getFirstChild().getNodeValue();

					DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
					Date parsedExpDate = df.parse(expDate.substring(0, 10));

					GregorianCalendar cal = new GregorianCalendar();
					cal.setTime(parsedExpDate);
					XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
					fellowDtoUnmarshalled.setUpdateDate(calendar);
				}
				result.setFellowISDNLogDtoList(fellowDtoUnmarshalled); // first iteration index .add()

				int index = 1;
				while (index != 0) // loop to recover all Offers associated with the MSISDN in question
				{
					if (fellowDto.getNextSibling() == null)
					{
						index = 0;
						break;
					}
					fellowDtoListUnmarshalled = JAXB.unmarshal(fellowDtoListSource, TQueryFellowISDNLogBOResponse.class);// required for every iteration (Don't know why yet)
					fellowDto = fellowDto.getNextSibling();
					fellowDtoSource = new DOMSource(fellowDto);
					fellowDtoUnmarshalled = JAXB.unmarshal(fellowDtoSource, TFellowISDNLogDto.class);

					fellowDtoListUnmarshalled.setFellowISDNLogDtoList(fellowDtoUnmarshalled);

					if (fellowDtoSource.getNode().getFirstChild().getNextSibling().getNextSibling().getNextSibling().getNextSibling().hasChildNodes())
					{
						String expDate = fellowDtoSource.getNode().getFirstChild().getNextSibling().getNextSibling().getNextSibling().getNextSibling().getFirstChild().getNodeValue();

						DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
						Date parsedExpDate = df.parse(expDate.substring(0, 10));

						GregorianCalendar cal = new GregorianCalendar();
						cal.setTime(parsedExpDate);
						XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
						fellowDtoUnmarshalled.setUpdateDate(calendar);
					}
					result.setFellowISDNLogDtoList(fellowDtoUnmarshalled);

					index++;
				}
			}
		}

		catch (SOAPException e)
		{
			throw new ZTEException(e.getMessage(), e.getCause());
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}
		catch (DatatypeConfigurationException e)
		{
			e.printStackTrace();
		}
		catch (Throwable ex)
		{
			throw ex;
		}

		return result;
	}

	/**
	 * updateFaFList
	 * 
	 * The message UpdateFaFList is used to update the Family and Friends list for either the account or subscriber. Note: Charged FaF number change is not supported on account level. It is only
	 * supported on subscription level. The field fafIndicator in fafInformation is mandatory for non-charging operations, and it is optional for charged operations.
	 * 
	 * @param UpdateFaFListRequest
	 *            request
	 * @return UpdateFaFListResponse response
	 * @throws ZTEException
	 *             if ZTE returns any one of: 0, 100, 102, 104, 123, 124, 126, 127, 129, 130, 134, 135, 159, 205, 206, 260, 999
	 */
	@Override
	public TResponseBO updateFaFList(TModFellowISDNBO request) throws ZTEException
	{
		TResponseBO result = null;

		try
		{
			SOAPMessage soapMessage = messageFactory.createMessage();
			SOAPBody soapBody = soapMessage.getSOAPBody();
			soapBody.addNamespaceDeclaration("xsd", "http://com.ztesoft.zsmart/xsd");

			SOAPElement soapBodyElem = soapBody.addChildElement("ModFellowISDNBO", "xsd");
			SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("MSISDN");
			String strMSISDN = request.getMSISDN();
			if (strMSISDN.length() < 5)
				return null;
			else
				soapBodyElem1.addTextNode(request.getMSISDN());
			if (request.getFellowISDN() != null)
			{
				SOAPElement soapBodyElem2a = soapBodyElem.addChildElement("FellowISDN");
				String fellowISDN = request.getFellowISDN();
				if (fellowISDN != null && fellowISDN.length() > 0)
					soapBodyElem2a.addTextNode(fellowISDN);
				else
					soapBodyElem2a.addTextNode("");
			}

			if (request.getNewFellowISDN() != null)
			{
				SOAPElement soapBodyElem2b = soapBodyElem.addChildElement("NewFellowISDN");
				String newFellowISDN = request.getNewFellowISDN();
				if (newFellowISDN != null && newFellowISDN.length() > 0)
					soapBodyElem2b.addTextNode(newFellowISDN);
				else
					soapBodyElem2b.addTextNode("");
			}

			if (request.getNewFellowISDN() != null)
			{
				SOAPElement soapBodyElem2b = soapBodyElem.addChildElement("FellowType");
				String fellowType = request.getFellowType();
				if (fellowType != null && fellowType.length() > 0)
					soapBodyElem2b.addTextNode(fellowType);
				else
					soapBodyElem2b.addTextNode("");
			}

			SOAPBody retElement = call("ModFellowISDNBO", soapBodyElem, "Webservice.WebserviceHttpSoap11Endpoint/");
			org.w3c.dom.Node childNode = retElement.getFirstChild();
			DOMSource source = new DOMSource(childNode);
			String localname = source.getNode().getLocalName();

			if (localname.compareTo("RespBO") == 0)
			{
				result = new TResponseBO();
				String responseCode = childNode.getFirstChild().getFirstChild().getNodeValue();
				String responseDesc = childNode.getFirstChild().getNextSibling().getFirstChild().getNodeValue();

				result.setResponseCode(responseCode);
				result.setResponseDesc(responseDesc);
			}
			if (retElement != null && result == null)// There was a response
			{
				result = new TResponseBO();
			}
		}
		catch (SOAPException e)
		{
			throw new ZTEException(e.getMessage(), e.getCause());
		}
		catch (Throwable ex)
		{
			throw ex;
		}

		return result;
	}

	@Override
	public TResponseBO deleteFaFList(TDelFellowISDNBO request) throws ZTEException
	{
		TResponseBO result = null;

		try
		{
			SOAPMessage soapMessage = messageFactory.createMessage();
			SOAPBody soapBody = soapMessage.getSOAPBody();
			soapBody.addNamespaceDeclaration("xsd", "http://com.ztesoft.zsmart/xsd");

			SOAPElement soapBodyElem = soapBody.addChildElement("DelFellowISDNBO", "xsd");
			SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("MSISDN");
			String strMSISDN = request.getMSISDN();
			if (strMSISDN.length() < 5)
				return null;
			else
				soapBodyElem1.addTextNode(request.getMSISDN());
			if (request.getFellowISDN() != null)
			{
				SOAPElement soapBodyElem2a = soapBodyElem.addChildElement("FellowISDN");
				String fellowISDN = request.getFellowISDN();
				if (fellowISDN != null && fellowISDN.length() > 0)
					soapBodyElem2a.addTextNode(fellowISDN);
				else
					soapBodyElem2a.addTextNode("");
			}

			if (request.getFellowType() != null)
			{
				SOAPElement soapBodyElem2b = soapBodyElem.addChildElement("FellowType");
				String fellowType = request.getFellowType();
				if (fellowType != null && fellowType.length() > 0)
					soapBodyElem2b.addTextNode(fellowType);
				else
					soapBodyElem2b.addTextNode("");
			}

			SOAPBody retElement = call("DelFellowISDNBO", soapBodyElem, "Webservice.WebserviceHttpSoap11Endpoint/");
			org.w3c.dom.Node childNode = retElement.getFirstChild();
			DOMSource source = new DOMSource(childNode);
			String localname = source.getNode().getLocalName();

			if (localname.compareTo("RespBO") == 0)
			{
				result = new TResponseBO();
				String responseCode = childNode.getFirstChild().getFirstChild().getNodeValue();
				String responseDesc = childNode.getFirstChild().getNextSibling().getFirstChild().getNodeValue();

				result.setResponseCode(responseCode);
				result.setResponseDesc(responseDesc);
			}
			if (retElement != null && result == null)// There was a response
			{
				result = new TResponseBO();
			}
		}
		catch (SOAPException e)
		{
			throw new ZTEException(e.getMessage(), e.getCause());
		}
		catch (Throwable ex)
		{
			throw ex;
		}

		return result;
	}

	@Override
	public TResponseBO AddFaFList(TAddFellowISDNBO request) throws ZTEException
	{
		TResponseBO result = null;

		try
		{
			SOAPMessage soapMessage = messageFactory.createMessage();
			SOAPBody soapBody = soapMessage.getSOAPBody();
			soapBody.addNamespaceDeclaration("xsd", "http://com.ztesoft.zsmart/xsd");

			SOAPElement soapBodyElem = soapBody.addChildElement("AddFellowISDNBO", "xsd");
			SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("MSISDN");
			String strMSISDN = request.getMSISDN();
			if (strMSISDN.length() < 5)
				return null;
			else
				soapBodyElem1.addTextNode(request.getMSISDN());
			if (request.getFellowISDN() != null)
			{
				SOAPElement soapBodyElem2a = soapBodyElem.addChildElement("FellowISDN");
				String fellowISDN = request.getFellowISDN();
				if (fellowISDN != null && fellowISDN.length() > 0)
					soapBodyElem2a.addTextNode(fellowISDN);
				else
					soapBodyElem2a.addTextNode("");
			}

			if (request.getFellowType() != null)
			{
				SOAPElement soapBodyElem2b = soapBodyElem.addChildElement("FellowType");
				String fellowType = request.getFellowType();
				if (fellowType != null && fellowType.length() > 0)
					soapBodyElem2b.addTextNode(fellowType);
				else
					soapBodyElem2b.addTextNode("");
			}
			if (request.getEffType() != 0)
			{
				SOAPElement soapBodyElem2c = soapBodyElem.addChildElement("EffType");
				String effType = String.valueOf(request.getEffType());
				if (effType != null && effType.length() > 0)
					soapBodyElem2c.addTextNode(effType);
				else
					soapBodyElem2c.addTextNode("");
			}

			SOAPBody retElement = call("AddFellowISDNBO", soapBodyElem, "Webservice.WebserviceHttpSoap11Endpoint/");
			org.w3c.dom.Node childNode = retElement.getFirstChild();
			DOMSource source = new DOMSource(childNode);
			String localname = source.getNode().getLocalName();

			if (localname.compareTo("RespBO") == 0)
			{
				result = new TResponseBO();
				String responseCode = childNode.getFirstChild().getFirstChild().getNodeValue();
				String responseDesc = childNode.getFirstChild().getNextSibling().getFirstChild().getNodeValue();

				result.setResponseCode(responseCode);
				result.setResponseDesc(responseDesc);
			}
			if (retElement != null && result == null)// There was a response
			{
				result = new TResponseBO();
			}
		}
		catch (SOAPException e)
		{
			throw new ZTEException(e.getMessage(), e.getCause());
		}
		catch (Throwable ex)
		{
			throw ex;
		}

		return result;
	}

	/**
	 * updateOffer
	 * 
	 * The UpdateOffer message will assign a new offer or update an existing offer to an account. If the UpdateOffer request is sent and the offerID is not found for the account, then the update
	 * request is considered to be an assignment request. If the offer is configured to allow multiple products a new product for the specified offer will be assigned. The following principles apply
	 * when assigning a new offer: * It is not allowed to have a start date (and time) beyond the expiry date (and time). * It is not allowed to have an expiry date (and time) set to an earlier date
	 * (and time) than the current date (and time). * If no absolute or relative start date (and time) is provided, then no date (and time) will be assigned as offer start date (and time). * If no
	 * expiry date (or expiry date and time) is provided, then an infinite expiry date is used which means that the offer never expires. The following principles apply when updating an offer: * An
	 * offer (except type Timer) will be active if the start date has been reached and the expiry date is still in the future. * An offer of type Timer will only become active through triggering by a
	 * traffic event. A Timer offer is always installed in a disabled state. * An offer will expire if the expiry date (or expiry date and time) is before the current date (and time). * It is not
	 * allowed to modify the start date (or start date and time) of an active or enabled (in the case of type Timer) offer. * It is not allowed to modify the start date and time of an offer of type
	 * Timer if the start date and time has already passed. * It is not allowed to modify the expiry date (or expiry date and time) to an earlier date (or date and time) than the current date (or date
	 * and time). * It is not allowed to modify the expiry date (or expiry date and time) of an expired offer * It is not allowed to modify the start date (or start date and time) beyond the expiry
	 * date (or expiry date and time). When doing an update, if a date (or date and time) is given in relative days (or days and time expressed in seconds), then the new date (or date and time) will
	 * be the current defined date (or date and time) plus the relative days (or days and time expressed in seconds). This applies to both start date (or date and time) and expiry date (or date and
	 * time). The parameter offerProviderID states the needed provider ID when creating a provider account offer. The parameter offerProviderID states the new provider ID when updating a provider
	 * account offer. Note: OfferType it is mandatory for Timer Offer
	 * 
	 * @param UpdateOfferRequest
	 *            request
	 * @return UpdateOfferResponse response
	 * @throws ZTEException
	 *             if ZTE returns any one of: 0, 100, 102, 104, 136, 165, 214, 215, 223, 224, 225, 226, 227, 230, 237, 238, 247, 248, 256, 257, 258, 259, 260
	 */
	@Override
	public TResponseBO updateOffer(TSetServiceReq request) throws ZTEException
	{
		TResponseBO result = null;

		try
		{
			SOAPMessage soapMessage = messageFactory.createMessage();
			SOAPBody soapBody = soapMessage.getSOAPBody();
			soapBody.addNamespaceDeclaration("xsd", "http://com.ztesoft.zsmart/xsd");

			SOAPElement soapBodyElem = soapBody.addChildElement("SetServiceReq", "xsd");
			SOAPElement soapBodyElem1a = soapBodyElem.addChildElement("MSISDN");
			String strMSISDN = request.getMSISDN();
			if (strMSISDN.length() < 5)
				return null;
			else
				soapBodyElem1a.addTextNode(request.getMSISDN());

			if (request.getServiceDtoList() != null)
			{
				SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("ServiceDtoList");
				SOAPElement soapBodyElem3 = soapBodyElem2.addChildElement("ServiceDto");
				SOAPElement soapBodyElem4 = soapBodyElem3.addChildElement("ServiceCode");
				String strServiceCode = request.getServiceDtoList().getServiceCode();
				if (strServiceCode != null && strServiceCode.length() > 0)
					soapBodyElem4.addTextNode(strServiceCode);
				else
					soapBodyElem4.addTextNode("");
				SOAPElement soapBodyElem4b = soapBodyElem3.addChildElement("Action");
				String strAction = String.valueOf(request.getServiceDtoList().getAction());
				if (strAction != null && strAction.length() > 0)
					soapBodyElem4b.addTextNode(strAction);
				else
					soapBodyElem4b.addTextNode("");
			}
			SOAPBody retElement = call("SetServiceReq", soapBodyElem, "Webservice.WebserviceHttpSoap11Endpoint/");
			org.w3c.dom.Node childNode = retElement.getFirstChild();
			DOMSource source = new DOMSource(childNode);

			String localname = source.getNode().getLocalName();
			if (localname.compareTo("SetServiceResp") == 0)
			{
				result = new TResponseBO();
				String responseCode = childNode.getFirstChild().getFirstChild().getNodeValue();
				String responseDesc = childNode.getFirstChild().getNextSibling().getFirstChild().getNodeValue();

				result.setResponseCode(responseCode);
				result.setResponseDesc(responseDesc);
			}
			if (retElement != null && result == null)// There was a response
			{
				result = new TResponseBO();
			}
		}
		catch (SOAPException e)
		{
			throw new ZTEException(e.getMessage(), e.getCause());
		}
		catch (Throwable ex)
		{
			throw ex;
		}

		return result;
	}

	/**
	 * updateServiceClass
	 * 
	 * This message UpdateServiceClass is used to update the service class (SC) for the subscriber. It is also possible to set a temporary SC with an expiry date. When temporary Service Class date is
	 * expired the Account will fallback to the original Service Class defined for the account.
	 * @param fullURLstr 
	 * 
	 * @param UpdateServiceClassRequest
	 *            request
	 * @return UpdateServiceClassResponse response
	 * @throws ZTEException
	 *             if ZTE returns any one of: 0, 100, 102, 104, 117, 123, 124, 126, 127, 134, 135, 140, 154, 155, 257, 260, 999
	 */
	// @Override
	// public UpdateServiceClassResponse updateServiceClass(UpdateServiceClassRequest request) throws ZTEException
	// {
	// try (SOAPConnection connection = getConnection())
	// {
	// UpdateServiceClassResponse response = call("updateServiceClass", connection, request, UpdateServiceClassResponse.class);
	// succeeded(response.member.responseCode, "updateServiceClass", request, response);
	// return response;
	// }
	// catch (Exception e)
	// {
	// failed(e);
	// logger.log(this, e);
	// throw new ZTEException(e.getMessage(), e);
	// }
	// }

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////

	private SOAPConnection getConnection() throws IOException
	{
		if (this.fullURL == null)
		{
			this.fullURL = new URL(zteConfig.getStartingURL() + zteConfig.getEndPoint());
		}

		SOAPConnection result = null;
		try
		{
			if (soapConnectionFactory != null)
				result = soapConnectionFactory.createConnection();
		}
		catch (Exception e)
		{
			return null;
		}

		pendingRequests++;
		lastUsed = new Date();

		return result;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Comparable to
	//
	// /////////////////////////////////

	@Override
	public int compareTo(Object o)
	{
		ZTEConnection that = (ZTEConnection) o;

		if (pendingRequests > that.pendingRequests)
			return -1;
		else if (pendingRequests < that.pendingRequests)
			return +1;
		else if (lastUsed.after(that.lastUsed))
			return -1;
		else if (lastUsed.before(that.lastUsed))
			return +1;
		else
			return 0;

	}

	public SOAPBody call(String methodName, SOAPElement request, String endPointstr) throws ZTEException
	{
		long started = System.currentTimeMillis();
		try
		{
			try
			{

				if (zteCallsCounter.get(methodName) == null)
					zteCallsCounter.put(methodName, new AtomicLong());

				zteCallsCounter.get(methodName).incrementAndGet();
				SOAPMessage soapMessage = messageFactory.createMessage();
				SOAPPart soapPart = soapMessage.getSOAPPart();

				// String serverURI = zteConfig.getUri();
				String identifier = "xsd";

				// SOAP Envelope
				SOAPEnvelope envelope = soapPart.getEnvelope();
				envelope.addNamespaceDeclaration(identifier, "http://com.ztesoft.zsmart/xsd");

				// SOAP Header
				SOAPHeader soapHeader = envelope.getHeader();
				soapHeader.addNamespaceDeclaration("xsd", "http://com.ztesoft.zsmart/xsd");
				SOAPElement soapHeaderElem = soapHeader.addChildElement("AuthHeader", "xsd");
				SOAPElement soapHeaderElem1 = soapHeaderElem.addChildElement("Username");
				soapHeaderElem1.addTextNode(zteConfig.getUserName());
				SOAPElement soapHeaderElem2 = soapHeaderElem.addChildElement("Password");
				soapHeaderElem2.addTextNode(zteConfig.getPassword());

				// SOAP Body
				SOAPBody soapBody = envelope.getBody();// requestBody
				soapBody.addChildElement(request);


				String fullURLstr;
				if (endPointstr == null)
				{
					fullURLstr = zteConfig.getStartingURL() + zteConfig.getEndPoint();
				}
				else
				{
					fullURLstr = zteConfig.getStartingURL() + endPointstr;
				}

				SOAPConnection connection = getConnection();
				if (logger.getLevel() == LoggingLevels.TRACE)
				{
					ByteArrayOutputStream outReq = new ByteArrayOutputStream();
					soapMessage.writeTo(outReq);
					logger.trace(this, "%s ZTE request : %s", methodName, outReq.toString());
				}

				SOAPMessage response = connection.call(soapMessage, fullURLstr);
				if (response == null)
				{
					ZTEException e = new ZTEException(ZTEException.TIMEOUT);
					throw e;
				}

				if (logger.getLevel() == LoggingLevels.TRACE)
				{				
					ByteArrayOutputStream outRes = new ByteArrayOutputStream();
					response.writeTo(outRes);
					logger.trace(this, "%s ZTE response : %s", methodName, outRes.toString());
				}
				SOAPPart respPart = response.getSOAPPart();
				SOAPEnvelope respEnvelope = respPart.getEnvelope();
				SOAPBody respBody = respEnvelope.getBody();// requestBody

				// Let's 1st check for <faultcode> and <faultstring>
				org.w3c.dom.Node childNode = respBody.getFirstChild();
				String localname = childNode.getLocalName();
				if (localname.compareTo("Fault") == 0)
				{
					String faultCode = childNode.getFirstChild().getFirstChild().getNodeValue();
					String faultString = childNode.getFirstChild().getNextSibling().getFirstChild().getNodeValue();
					logger.info(this, "%s ZTE response Error faultCode(%s) faultString(%s)", methodName, faultCode, faultString);

					ZTEException e = null;
					if (faultString.indexOf("operation unauthorized") != -1)
						e = new ZTEException(ZTEException.AUTHORIZATION_FAILURE);
					if (faultString.indexOf("exceeds the maximum count") != -1)
						e = new ZTEException(ZTEException.FORBIDDEN);
					if (faultString.indexOf("subscriber does not exist") != -1)
						e = new ZTEException(ZTEException.SUBSCRIBER_FAILURE);
					if (faultString.indexOf("number is incorrect") != -1)
						e = new ZTEException(ZTEException.SUBSCRIBER_FAILURE);
					if (faultString.indexOf("has been terminated") != -1)
						e = new ZTEException(ZTEException.SUBSCRIBER_FAILURE);

					if (e == null)
						e = new ZTEException(ZTEException.INTERNAL_SERVER_ERROR);

					throw e;
				}

				return respBody;
			}
			catch (SOAPException e)
			{
				ZTEException ex = new ZTEException(ZTEException.INTERNAL_SERVER_ERROR);
				throw ex;
			}
			catch (IOException e)
			{
				ZTEException ex = new ZTEException(ZTEException.TIMEOUT);
				throw ex;
			}
		}
		finally
		{
			pendingRequests--;
			logger.debug(this, "ZTE call [%s] Completed in [%d]ms pendingRequests[%d]", methodName, System.currentTimeMillis() - started, pendingRequests);
		}
	}
}
