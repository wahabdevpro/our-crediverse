package com.concurrent.hxc;

import hxc.servicebus.ReturnCodes;

import java.io.IOException;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.ksoap2.HeaderProperty;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import com.concurrent.creditsharing.CreditSharingActivity;
import com.concurrent.soap.GetServicesRequest;
import com.concurrent.soap.GetServicesResponse;
import com.concurrent.soap.PingRequest;
import com.concurrent.soap.PingResponse;
import com.concurrent.util.C4SoapClient;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.concurrent.soap.Number;

public class MainActivity extends Activity
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String language = Program.getLanguage();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Creation and Options
	//
	// /////////////////////////////////

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// Change Language
		Configuration configuration = new Configuration();
		configuration.locale = new Locale(Program.getLanguage());
		getBaseContext().getResources().updateConfiguration(configuration,
				getBaseContext().getResources().getDisplayMetrics());

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		TextView versionTextView = (TextView)findViewById(R.id.versionTextView);
		try
		{
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);		
			versionTextView.setText(String.format("Version %s", pInfo.versionName));
		}
		catch (NameNotFoundException e)
		{
		}

		if (!Program.isLoggedIn())
			login();

	}

	private void login()
	{
		if (Debug.isDebuggerConnected())
		{
			Program.setLoggedIn(true);
			return;
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = getLayoutInflater();
		View view = inflater.inflate(R.layout.dialog_login, null);
		final EditText idEditText = (EditText) view.findViewById(R.id.idEditText);
		idEditText.setText(Program.getMSISDN());
		final EditText passwordEditText = (EditText) view.findViewById(R.id.passwordEditText);
		passwordEditText.setHint("****");
		builder.setView(view).setTitle("Login").setPositiveButton("OK", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int id)
			{
				String idText = idEditText.getText().toString();
				String passwordText = passwordEditText.getText().toString();
				if (passwordText.length() == 4 && idText.endsWith(passwordText))
				{
					Program.setMSISDN(idText);
					Program.setLoggedIn(true);
				}
				else
				{
					login();
				}
			}
		}).setOnCancelListener(new OnCancelListener()
		{
			@Override
			public void onCancel(DialogInterface dialog)
			{
				android.os.Process.killProcess(android.os.Process.myPid());
			}
		}).create().show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings)
		{
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Lifecycle
	//
	// /////////////////////////////////
	@Override
	protected void onResume()
	{
		super.onResume();

		String currentLanguage = Program.getLanguage();
		if (!currentLanguage.equals(language))
		{
			language = currentLanguage;
			recreate();
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// On My Services
	//
	// /////////////////////////////////
	public void onMyServices(View view)
	{
		displayServices(true);
	}

	public void onAllServices(View view)
	{
		displayServices(false);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////

	private void displayServices(final boolean mineOnly)
	{
		SoapTask<Void, GetServicesResponse> task = new SoapTask<Void, GetServicesResponse>(this, null)
		{
			@Override
			protected void whenCancelled()
			{
				// Do Nothing
			}

			@Override
			protected GetServicesResponse performWork(C4SoapClient client, Void... params) throws Exception
			{
				GetServicesRequest req = new GetServicesRequest();
				req.setActiveOnly(mineOnly);
				req.setSubscriberNumber(new Number(Program.getMSISDN()));
				GetServicesResponse response = client.call(req);
				super.returnCode = response.getReturnCode();
				return response;
			}

			@Override
			protected void whenCompleted(boolean wasSuccess, GetServicesResponse result)
			{
				if (!wasSuccess)
				{
					AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
					alert.setTitle("Error").setMessage("Data Connection Failed").setCancelable(false)
							.setPositiveButton("OK", new OnClickListener()
							{
								@Override
								public void onClick(DialogInterface dialog, int which)
								{
								}
							}).create().show();
					return;
				}

				Intent intent = new Intent(MainActivity.this, ServicesActivity.class);
				intent.putExtra(GetServicesResponse.class.getSimpleName(), result);
				startActivity(intent);

			}

		};
		task.execute();

	}

	@Override
	public void onBackPressed()
	{
		Program.setLoggedIn(false);
		super.onBackPressed();
	}

}
