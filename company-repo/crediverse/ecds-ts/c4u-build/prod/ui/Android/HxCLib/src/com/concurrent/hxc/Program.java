package com.concurrent.hxc;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Application;
import android.content.Context;
import android.telephony.TelephonyManager;

public class Program extends Application
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	// Product
//	private static String hostAddress = "http://c4u.concurrent.co.za/mobile";
//	private static String msisdn = "12300000";
//	private static String username = "crm";
//	private static String password = "crm";  

//	private static String hostAddress = "http://172.17.8.11:14100/HxC"; 
	private static String hostAddress = "http://192.168.245.1:14100/HxC"; 
    private static String msisdn = "0824452655";
	private static String username = "c4u"; 
	private static String password = "c4u"; 

	public static final String Tag = "HxC";

	private static String sessionID = new SimpleDateFormat("yyMMddHHmmss").format(new Date());
	private static String imei = "";
	private static int languageID = 1;
	private static String languageCode = "eng";
	private static boolean loggedIn = false;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public static String getHostAddress()
	{
		return hostAddress;
	}

	public static void setHostAddress(String hostAddress)
	{
		Program.hostAddress = hostAddress;
	}

	public static String getMSISDN()
	{
		return msisdn;
	}

	public static void setMSISDN(String msisdn)
	{
		Program.msisdn = msisdn;
	}

	public static String getSessionID()
	{
		return sessionID;
	}

	public static String getUsername()
	{
		return username;
	}

	public static void setUsername(String username)
	{
		Program.username = username;
	}

	public static String getPassword()
	{
		return password;
	}

	public static void setPassword(String password)
	{
		Program.password = password;
	}

	public static String getIMEI()
	{
		if (imei == null || imei.isEmpty())
		{
			TelephonyManager mngr = (TelephonyManager) instance.getSystemService(Context.TELEPHONY_SERVICE);
			imei = mngr.getDeviceId();
		}

		return imei;
	}

	public static int getLanguageID()
	{
		return languageID;
	}

	public static void setLanguageID(int languageID)
	{
		Program.languageID = languageID;
	}

	public static String getLanguageCode()
	{
		return languageCode;
	}

	public static void setLanguageCode(String languageCode)
	{
		Program.languageCode = languageCode;
	}

	public static String getLanguage()
	{
		if ("fre".equalsIgnoreCase(languageCode))
			return "fr";

		return "en";
	}

	public static boolean isLoggedIn()
	{
		return loggedIn;
	}

	public static void setLoggedIn(boolean loggedIn)
	{
		Program.loggedIn = loggedIn;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helpers
	//
	// /////////////////////////////////
	public static String formatMoney(long amount)
	{
		NumberFormat currency = NumberFormat.getCurrencyInstance();
		return currency.format(amount).replace("R", "QR ");
	}
		
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Singleton
	//
	// /////////////////////////////////
	private static Program instance;

	public static Program getInstance()
	{
		return instance;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Application Implementation
	//
	// /////////////////////////////////
	@Override
	public void onCreate()
	{

		super.onCreate();
		instance = this;
	}

}
