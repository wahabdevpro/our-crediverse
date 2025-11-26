package com.concurrent.creditsharing;

import com.concurrent.hxc.IYesNo;
import com.concurrent.hxc.Program;
import com.concurrent.hxc.StickyDialog;
import com.concurrent.soap.GetOwnersRequest;
import com.concurrent.soap.GetOwnersResponse;
import com.concurrent.soap.GetServiceRequest;
import com.concurrent.soap.GetServiceResponse;
import com.concurrent.soap.Number;
import com.concurrent.soap.SubscriptionState;
import com.concurrent.soap.VasServiceInfo;
import com.concurrent.util.C4SoapClient;
import com.concurrent.hxc.SoapTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;

public class CreditSharingActivity extends Activity implements IYesNo
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////
	private SharingState state = null;

	private static final String ESTABLISH_SUBSCRIPTION_STATE_FAILED = "establishSubscriptionStateFailed";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Creation and Options
	//
	// /////////////////////////////
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_credit_sharing);
		setTitle(R.string.title_activity_credit_sharing);
		
		// Enable Back on the Action Bar
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// Get the original Intent
		Intent intent = getIntent();
		VasServiceInfo serviceInfo = (VasServiceInfo) intent.getSerializableExtra("VASINFO");

		// Create a Sharing Context
		if (state == null)
		{
			state = new SharingState();
			state.setMsisdn(Program.getMSISDN());
			state.setServiceID(serviceInfo.getServiceID());
			state.setServiceName(serviceInfo.getServiceName());
		}

		// Launch Appropriate Activity
		establishSubscriptionState();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.credit_sharing, menu);
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

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Activity Result
	//
	// /////////////////////////////
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (data != null)
			state = (SharingState) data.getSerializableExtra(SharingState.SHARE_STATE);

		if (state.getMustExit())
		{
			finish();
			return;
		}

		if (state == null)
			establishSubscriptionState();
		else
			launchAppropriateActivity();

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////

	// Launch Appropriate Activity
	private void establishSubscriptionState()
	{
		SoapTask<Void, Void> task = new SoapTask<Void, Void>(this, state.getServiceID())
		{
			@Override
			protected void whenCancelled()
			{
				CreditSharingActivity.this.finish();
			}

			@Override
			protected Void performWork(C4SoapClient client, Void... params) throws Exception
			{
				// Check if Subscribed
				GetServiceRequest request1 = new GetServiceRequest();
				request1.setServiceID(state.getServiceID());
				request1.setActiveOnly(true);
				request1.setSubscriberNumber(new Number(state.getMsisdn()));

				GetServiceResponse response1 = client.call(request1);
				returnCode = response1.getReturnCode();
				if (response1.wasSuccess())
				{
					VasServiceInfo[] variants = response1.getServiceInfo();
					if (variants != null)
					{
						for (VasServiceInfo serviceInfo : variants)
						{
							if (serviceInfo.getServiceID().equals(state.getServiceID())
									&& serviceInfo.getState() == SubscriptionState.active)
							{
								state.setVariantID(serviceInfo.getVariantID());
								state.setVariantName(serviceInfo.getVariantName());
								state.setSubscribed(true);
								return null;
							}
						}
					}

					// Check if Beneficiary
					GetOwnersRequest request2 = new GetOwnersRequest();
					request2.setServiceID(state.getServiceID());
					request2.setMemberNumber(new Number(Program.getMSISDN()));
					GetOwnersResponse response2 = client.call(request2);
					returnCode = response2.getReturnCode();
					if (response2.wasSuccess())
					{
						Number[] owners = response2.getOwners();
						if (owners != null && owners.length > 0)
						{
							state.setOwner(owners[0].getAddressDigits());
							state.setBeneficiary(true);
						}
						return null;
					}

				}

				return null;
			}

			@Override
			protected void whenCompleted(boolean success, Void result)
			{
				if (!success)
				{
					StickyDialog dialog = new StickyDialog(state.getServiceName(), resultText);
					dialog.showOk(CreditSharingActivity.this, ESTABLISH_SUBSCRIPTION_STATE_FAILED, null);
					return;
				}

				// Launch Appropriate Activity
				launchAppropriateActivity();

			}

		};

		task.execute();

	}

	// Launch Appropriate Activity
	private void launchAppropriateActivity()
	{
		Intent intent = null;

		if (state.isSubscribed())
		{
			intent = new Intent(CreditSharingActivity.this, SubscribedActivity.class);
		}

		else if (!state.isBeneficiary() && !state.isSubscribed())
		{
			intent = new Intent(CreditSharingActivity.this, NeitherActivity.class);
		}

		else
		{
			intent = new Intent(CreditSharingActivity.this, IsBeneficiaryActivity.class);
		}

		intent.putExtra(SharingState.SHARE_STATE, state);
		startActivityForResult(intent, 0);

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get Contact Display Name by Number
	//
	// /////////////////////////////
	public static String getContactDisplayNameByNumber(Context context, String number)
	{
		Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
		String name = number;

		ContentResolver contentResolver = context.getContentResolver();
		Cursor contactLookup = contentResolver.query(uri, new String[] { BaseColumns._ID,
				ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);

		try
		{
			if (contactLookup != null && contactLookup.getCount() == 1)
			{
				contactLookup.moveToNext();
				name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
			}
		}
		finally
		{
			if (contactLookup != null)
			{
				contactLookup.close();
			}
		}

		return name;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IYesNo
	//
	// /////////////////////////////

	@Override
	public void onPositive(String tag, Object asyncState)
	{
	}

	@Override
	public void onNegative(String tag, Object asyncState)
	{
	}

	@Override
	public void onAny(String tag, Object asyncState)
	{
		if (tag.equals(ESTABLISH_SUBSCRIPTION_STATE_FAILED))
			finish();
	}

}
