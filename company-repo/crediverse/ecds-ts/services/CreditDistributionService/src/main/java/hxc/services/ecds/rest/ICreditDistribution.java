package hxc.services.ecds.rest;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import hxc.connectors.IInteraction;
import hxc.connectors.air.IAirConnector;
import hxc.connectors.bundles.IBundleProvider;
import hxc.connectors.hlr.IHlrInformation;
import hxc.connectors.kerberos.IAuthenticator;
import hxc.connectors.smtp.ISmtpConnector;
import hxc.connectors.snmp.ISnmpConnector;
import hxc.ecds.protocol.rest.RegisterTransactionNotificationRequest;
import hxc.ecds.protocol.rest.WebUser;
import hxc.servicebus.IServiceBus;
import hxc.services.IService;
import hxc.services.ecds.CompanyInfo;
import hxc.services.ecds.ICallbackItem;
import hxc.services.ecds.Session;
import hxc.services.ecds.model.IAgentUser;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.QueryToken;
import hxc.services.ecds.util.RuleCheckException;

public interface ICreditDistribution
{
	//public EntityManagerFactory getEntityManagerFactory();

	//public EntityManagerFactory getApEntityManagerFactory();

	public abstract EntityManagerEx getEntityManager();

	public abstract EntityManagerEx getApEntityManager();

	public abstract hxc.services.ecds.Sessions getSessions();

	public abstract IService getService();

	public abstract IServiceBus getServiceBus();

	public abstract ISnmpConnector getSnmpConnector();

	public abstract CompanyInfo findCompanyInfoByID(int companyID);

	public abstract CompanyInfo findCompanyInfoByID(EntityManager em, int companyID);
	
	public abstract Session getSession(String sessionID) throws RuleCheckException;

	public abstract NumberFormat getCurrencyFormat(Locale locale);

	public abstract void sendSMS(String source, String msisdn, String languageCode3, String text);
	
	public abstract void sendSMS(String msisdn, String languageCode3, String text);

	public abstract ISmtpConnector getSmtpConnector();

	public abstract void writeTDR(Transaction transaction) throws IOException;

	public abstract IAirConnector getAirConnector();

	public abstract int getMoneyScale();

	public abstract void defineChannelFilter(IChannelTarget target, int companyID, hxc.ecds.protocol.rest.config.Phrase command, hxc.ecds.protocol.rest.config.Phrase[] fields, int tag);

	public abstract IAuthenticator getAuthenticator();

	public abstract IHlrInformation getHlrInformation(String msisdn, boolean needLocation, boolean needMnp, boolean needImsi);
	
	public abstract String getImei(String msisdn);

	public abstract IAuthenticator.Result tryAuthenticate(WebUser user, String password);

	public abstract IAuthenticator.Result tryAuthenticate(IAgentUser abstractAgentUser, String password);

	public abstract int tryChangePassword(String user, String oldPassword, String newPassword);

	public abstract boolean processUssd(IInteraction interaction);

	public abstract void throttleTps();

	public abstract String toMSISDN(String number);
	
	public abstract IBundleProvider getBundleProvider();

	public abstract boolean isMasterServer();

	public abstract void assignTsNumber(boolean assign);

	public abstract String getInvalidUssdCommandNotification();

	public QueryToken getQueryToken();
	
	public abstract boolean isAgentTaggedForCallback(int agentID);
	
	public HashSet<ICallbackItem> getCallbackItems(int agentID);

	public abstract void setAgentTaggedForCallback(RegisterTransactionNotificationRequest request);

	public abstract void deregisterSessionFromCallback(String sessionID, int agentID);
	
	public abstract void pushTransactionNotification(String sessionID, int agentID, String baseUri, String tokenUriPath, String callbackUriPath, List<? extends hxc.ecds.protocol.rest.Transaction> transactions, boolean isAAgent) throws Exception;
	
	public Object getCallbackItemsLock();

	public EntityManagerFactory createOltpEntityManagerFactory(String persistenceUnit, String datasourceName) throws Exception;

	public EntityManagerFactory createOlapEntityManagerFactory(String persistenceUnit, String datasourceName) throws Exception;
	
}
