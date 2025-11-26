package com.concurrent.languagechange;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import hxc.servicebus.ReturnCodes;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.concurrent.hxc.Program;
import com.concurrent.soap.GetReturnCodeTextRequest;
import com.concurrent.soap.GetReturnCodeTextResponse;
import com.concurrent.soap.GetServicesRequest;
import com.concurrent.soap.GetServicesResponse;
import com.concurrent.soap.MigrateRequest;
import com.concurrent.soap.MigrateResponse;
import com.concurrent.soap.Number;
import com.concurrent.soap.SubscriptionState;
import com.concurrent.soap.VasServiceInfo;
import com.concurrent.util.C4SoapClient;

public class LanguageChangeActivity extends Activity
{
	private Spinner spinner;
	private String serviceID;
	private String variantID;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_language_change);
				
		// Enable Back on the Action Bar
		getActionBar().setDisplayHomeAsUpEnabled(true); 
		
		Intent intent = getIntent();
		VasServiceInfo serviceInfo = (VasServiceInfo) intent.getSerializableExtra("VASINFO"); 
		serviceID = serviceInfo.getServiceID();
		variantID = serviceInfo.getVariantID();

		spinner = (Spinner) findViewById(R.id.spinner1);

		setTitle(serviceInfo.getServiceName());

		getVariantsTask.execute();

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.beneficiary_balances, menu);
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
			return true;
		}
		else if (id == android.R.id.home)
		{
		   onBackPressed();
		   return true;
		}
		return super.onOptionsItemSelected(item);
	}

	protected AsyncTask<Boolean, Void, GetServicesResponse> getVariantsTask = new AsyncTask<Boolean, Void, GetServicesResponse>()
	{
		ProgressDialog progress = null;

		@Override
		protected void onPreExecute()
		{
			progress = ProgressDialog //
					.show(LanguageChangeActivity.this, "Loading",
							"Loading Languages...");

			progress.setCancelable(true);
			progress.setOnCancelListener(new OnCancelListener()
			{

				@Override
				public void onCancel(DialogInterface dialog)
				{
					cancel(true);
				}
			});
		}

		@Override
		protected GetServicesResponse doInBackground(Boolean... params)
		{
			C4SoapClient soap = new C4SoapClient(Program.getUsername(),
					Program.getPassword());
			GetServicesRequest req = new GetServicesRequest();
			req.setActiveOnly(false);
			req.setSubscriberNumber(new Number(Program.getMSISDN()));
			GetServicesResponse response;

			try
			{
				response = soap.call(req);
			}
			catch (Exception e)
			{
				return null;
			}

			return response;
		}

		@Override
		protected void onPostExecute(GetServicesResponse result)
		{
			progress.dismiss();

			if (result == null || result.getReturnCode() != ReturnCodes.success)
			{
				Toast.makeText(getApplicationContext(),
						"Data Connection Failed", Toast.LENGTH_LONG).show();
				finish();
			}
			else
			{
				List<Variant> variants = new ArrayList<Variant>();
				int index = 1;
				for (VasServiceInfo variant : result.getServiceInfo())
				{
					if (variant.getServiceID().equals(serviceID))
					{
						Variant var = new Variant();
						var.id = variant.getVariantID();
						var.name = variant.getVariantName();
						var.active = variant.getState() == SubscriptionState.active;
						var.index = index++;
						variants.add(var);
					}
				}

				Collections.sort(variants, new Comparator<Variant>()
				{
					@Override
					public int compare(Variant lhs, Variant rhs)
					{
						if (lhs.active && !rhs.active)
							return -1;
						else if (!lhs.active && rhs.active)
							return +1;

						if (lhs.index < rhs.index)
							return -1;
						else if (lhs.index > rhs.index)
							return +1;

						return 0;
					}
				});

				 ArrayAdapter<Variant> dataAdapter = new
				 ArrayAdapter<Variant>(
				 LanguageChangeActivity.this,
				 android.R.layout.simple_spinner_item, variants);
				 dataAdapter
				 .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				 spinner.setAdapter(dataAdapter);

				spinner.setOnItemSelectedListener(new OnItemSelectedListener()
				{
					

					@Override
					public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
					{
						Variant variant = (Variant) spinner.getSelectedItem();
						if (!variant.id.equals(variantID))
							new MigrateTask().execute(variant);
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent)
					{
						Toast.makeText(getApplicationContext(),
								"Nothing Selected", Toast.LENGTH_LONG).show();

					}
				});

			}
		}

	};


	private class MigrateTask extends
			AsyncTask<Variant, Void, GetReturnCodeTextResponse>
	{
		ProgressDialog progress = null;

		@Override
		protected void onPreExecute()
		{
			progress = ProgressDialog //
					.show(LanguageChangeActivity.this, "Setting",
							"Setting Language...");

			progress.setCancelable(true);
			progress.setOnCancelListener(new OnCancelListener()
			{

				@Override
				public void onCancel(DialogInterface dialog)
				{
					cancel(true);
				}
			});
		}

		@Override
		protected GetReturnCodeTextResponse doInBackground(Variant... params)
		{
			Variant variant = params[0];
			String newVariantID = variant.id;
			C4SoapClient soap = new C4SoapClient(Program.getUsername(),
					Program.getPassword());
			MigrateRequest req = new MigrateRequest();
			req.setServiceID(serviceID);
			req.setVariantID(variantID);
			req.setNewServiceID(serviceID);
			req.setNewVariantID(newVariantID);
			req.setSubscriberNumber(new Number(Program.getMSISDN()));
			MigrateResponse response;

			try
			{
				response = soap.call(req);

				if (response.getReturnCode() == ReturnCodes.success)
				{
					// Save the new Language ID/Code
					variantID = newVariantID;
					Program.setLanguageID(variant.index);
					Program.setLanguageCode(variant.id);

					// Change Language
					Configuration configuration = new Configuration();
					configuration.locale = new Locale(Program.getLanguage());
					getBaseContext().getResources()
							.updateConfiguration(
									configuration,
									getBaseContext().getResources()
											.getDisplayMetrics());
					
					System.out.print(response.getSessionId());

				}

							
				GetReturnCodeTextRequest req2 = new GetReturnCodeTextRequest();
				req2.setServiceID(serviceID);
				req2.setReturnCode(response.getReturnCode());
				GetReturnCodeTextResponse resp2 = soap.call(req2);

				return resp2;

			}
			catch (Exception e)
			{
				return null;
			}

		}

		@Override
		protected void onPostExecute(GetReturnCodeTextResponse result)
		{
			progress.dismiss();

			if (result == null)
			{
				Toast.makeText(getApplicationContext(),
						"Data Connection Failed", Toast.LENGTH_LONG).show();
				finish();
			}
			else
			{
				Toast.makeText(getApplicationContext(),
						result.getReturnCodeText(), Toast.LENGTH_LONG).show();
				finish();
			}
		}

	};

	private class Variant
	{
		String id;
		String name;
		boolean active;
		int index;

		@Override
		public String toString()
		{
			return name;
		}
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// On Back Pressed
	//
	// /////////////////////////////

	@Override
	public void onBackPressed()
	{
		finish();

		super.onBackPressed();
	}

}
