package com.concurrent.creditsharing;

import hxc.servicebus.RequestModes;

import java.text.NumberFormat;

import com.concurrent.hxc.IYesNo;
import com.concurrent.hxc.Program;
import com.concurrent.hxc.SoapTask;
import com.concurrent.hxc.StickyDialog;
import com.concurrent.soap.AddMemberRequest;
import com.concurrent.soap.AddMemberResponse;
import com.concurrent.soap.Number;
import com.concurrent.util.C4SoapClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class AddBeneficiaryActivity extends Activity implements IYesNo
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////
	private SharingState state = null;
	private EditText numberEditText;

	public static final int REQUEST_CODE_GET_NUMBER = 7;

	public static final String CONFIRM_ADDED = "confirmAdded";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Creation
	//
	// /////////////////////////////

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_beneficiary);
		setTitle(R.string.title_activity_add_beneficiary);
		
		// Enable Back on the Action Bar
		getActionBar().setDisplayHomeAsUpEnabled(true);

		state = (SharingState) getIntent().getSerializableExtra(SharingState.SHARE_STATE);

		numberEditText = (EditText) findViewById(R.id.numberEditText);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.add_beneficiary, menu);
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
	// Search
	//
	// /////////////////////////////
	public void onSearch(View view)
	{
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
		startActivityForResult(intent, REQUEST_CODE_GET_NUMBER);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Activity Result
	//
	// /////////////////////////////
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch (requestCode)
		{
			case REQUEST_CODE_GET_NUMBER:
				if (resultCode == Activity.RESULT_OK)
				{

					if (data != null)
					{
						Uri uri = data.getData();

						if (uri != null)
						{
							Cursor c = null;
							try
							{
								c = getContentResolver().query(
										uri,
										new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER,
												ContactsContract.CommonDataKinds.Phone.TYPE }, null, null, null);

								if (c != null && c.moveToFirst())
								{
									String number = c.getString(0);
									numberEditText.setText(number);
								}
							}
							finally
							{
								if (c != null)
								{
									c.close();
								}
							}
						}
					}

				}
				break;

			default:
				finish();
				break;
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Continue
	//
	// /////////////////////////////
	public void onContinue(View view)
	{
		// Get the Number
		String number = numberEditText.getText().toString();
		if (number == null || number.length() == 0)
		{
			StickyDialog dlg = new StickyDialog(state.getServiceName(), "Invalid Number");
			dlg.showOk(this, "InvalidNumber", null);
			return;

		}
		final String memberNumber = number.startsWith("+") ? number.substring(1) : number;

		SoapTask<Void, Void> addMemberTask = new SoapTask<Void, Void>(this, state.getServiceID())
		{
			@Override
			protected void whenCancelled()
			{
				AddBeneficiaryActivity.this.finish();
			}

			@Override
			protected Void performWork(C4SoapClient client, Void... params) throws Exception
			{
				AddMemberRequest request1 = new AddMemberRequest();
				request1.setServiceID(state.getServiceID());
				request1.setVariantID(state.getVariantID());
				request1.setSubscriberNumber(new Number(state.getMsisdn()));
				request1.setMemberNumber(new Number(memberNumber));
				request1.setMode(RequestModes.testOnly);

				AddMemberResponse response1 = client.call(request1);
				returnCode = response1.getReturnCode();
				if (response1.wasSuccess())
				{
					state.setCharge(response1.getChargeLevied());
				}
				return null;
			}

			@Override
			protected void whenCompleted(boolean wasSuccess, Void result)
			{
				if (!wasSuccess)
				{
					StickyDialog dialog = new StickyDialog(state.getServiceName(), resultText);
					dialog.showOk(AddBeneficiaryActivity.this, "ServerError", null);
					return;
				}

				Intent intent = new Intent(AddBeneficiaryActivity.this, AddBenefitActivity.class);
				intent.putExtra(SharingState.SHARE_STATE, state);
				String name = CreditSharingActivity.getContactDisplayNameByNumber(AddBeneficiaryActivity.this,
						memberNumber);
				intent.putExtra("NAME", name);
				intent.putExtra("NUMBER", memberNumber);
				intent.putExtra("NEWGUY", true);
				startActivityForResult(intent, 0);

			}

		};

		addMemberTask.execute();

	}

	// Confirm Subscription
	private void confirmAdded(String memberMsisdn)
	{
		String amount = Program.formatMoney(state.getCharge());
		String msg = String.format("You have added %s at a cost of %s", memberMsisdn, amount);

		StickyDialog dlg = new StickyDialog(state.getServiceName(), msg);
		dlg.showOk(this, CONFIRM_ADDED, null);

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
		if (tag.equals(CONFIRM_ADDED))
		{
			Intent intent = new Intent();
			intent.putExtra(SharingState.SHARE_STATE, state);
			setResult(Activity.RESULT_OK, intent);
			finish();
		}

	}

}
