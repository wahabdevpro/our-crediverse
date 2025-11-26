package com.concurrent.creditsharing;

import hxc.servicebus.RequestModes;

import java.text.NumberFormat;

import com.concurrent.hxc.IYesNo;
import com.concurrent.hxc.SoapTask;
import com.concurrent.hxc.StickyDialog;
import com.concurrent.soap.Number;
import com.concurrent.soap.RemoveMemberRequest;
import com.concurrent.soap.RemoveMemberResponse;
import com.concurrent.soap.UnsubscribeRequest;
import com.concurrent.soap.UnsubscribeResponse;
import com.concurrent.util.C4SoapClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class IsBeneficiaryActivity extends Activity implements IYesNo
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////
	private SharingState state = null;
	
	private static final String RATE_UNSUBSCRIBE = "rateUnsubscribe";
	private static final String PERFORM_UNSUBSCRIBE = "performUnsubscribe";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Creation
	//
	// /////////////////////////////
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_is_beneficiary);
		setTitle(R.string.title_activity_is_beneficiary);
		
		// Enable Back on the Action Bar
		getActionBar().setDisplayHomeAsUpEnabled(true);

		state = (SharingState) getIntent().getSerializableExtra(SharingState.SHARE_STATE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.is_beneficiary, menu);
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
	// On View Sponsor
	//
	// /////////////////////////////
	public void onViewSponsor(View view)
	{
		String text = CreditSharingActivity.getContactDisplayNameByNumber(this, state.getOwner());
		if (text.equals(state.getOwner()))
			text = String.format("Your Credit Sharing Sponsor is %s", text);
		else
			text = String.format("Your Credit Sharing Sponsor is %s (%s)", text, state.getOwner());

		StickyDialog dlg = new StickyDialog("Sponsor", text);
		dlg.showOk(this, "onViewSponsor", null);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// On View Balances
	//
	// /////////////////////////////
	public void onViewBalances(View view)
	{
		Intent intent = new Intent(this, BeneficiaryBalancesActivity.class);
		intent.putExtra(SharingState.SHARE_STATE, state);
		startActivityForResult(intent, 0);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// On Opt Out
	//
	// /////////////////////////////
	public void onOptOut(View view)
	{
		rateUnsubscribe();
	}

	private void rateUnsubscribe()
	{
		SoapTask<Void, Void> task = new SoapTask<Void, Void>(this, state.getServiceID())
		{
			@Override
			protected void whenCancelled()
			{
				// Do Nothing
			}

			@Override
			protected Void performWork(C4SoapClient client, Void... params) throws Exception
			{
				RemoveMemberRequest request = new RemoveMemberRequest();
				request.setServiceID(state.getServiceID());
				request.setVariantID(state.getVariantID());
				request.setSubscriberNumber(new Number(state.getOwner()));
				request.setMemberNumber(new Number(state.getMsisdn()));
				request.setMode(RequestModes.testOnly);

				RemoveMemberResponse response = client.call(request);
				super.returnCode = response.getReturnCode();
				if (response.wasSuccess())
					state.setCharge(response.getChargeLevied());

				return null;
			}

			@Override
			protected void whenCompleted(boolean wasSuccess, Void result)
			{
				if (!wasSuccess)
				{
					StickyDialog dialog = new StickyDialog(state.getServiceName(), resultText);
					dialog.showOk(IsBeneficiaryActivity.this, "ServerError", null);
					return;
				}

				String msg = String.format("Do you want to opt-out from the %s service ?", state.getServiceName());
				StickyDialog dlg = new StickyDialog(state.getServiceName(), msg);
				dlg.showYesNo(IsBeneficiaryActivity.this, RATE_UNSUBSCRIBE, null);
			}

		};
		task.execute();

	}

	private void performUnsubscribe()
	{
		SoapTask<Void, Void> task = new SoapTask<Void, Void>(this, state.getServiceID())
		{
			@Override
			protected void whenCancelled()
			{
				// Do Nothing
			}

			@Override
			protected Void performWork(C4SoapClient client, Void... params) throws Exception
			{
				RemoveMemberRequest request = new RemoveMemberRequest();
				request.setServiceID(state.getServiceID());
				request.setVariantID(state.getVariantID());
				request.setSubscriberNumber(new Number(state.getOwner()));
				request.setMemberNumber(new Number(state.getMsisdn()));

				RemoveMemberResponse response = client.call(request);
				super.returnCode = response.getReturnCode();
				if (response.wasSuccess())
					state.setCharge(response.getChargeLevied());

				return null;
			}

			@Override
			protected void whenCompleted(boolean wasSuccess, Void result)
			{
				if (!wasSuccess)
				{
					StickyDialog dialog = new StickyDialog(state.getServiceName(), resultText);
					dialog.showOk(IsBeneficiaryActivity.this, "ServerError", null);
					return;
				}

				String msg = String.format("You have opted-out from the %s service", state.getServiceName());

				StickyDialog dlg = new StickyDialog(state.getServiceName(), msg);
				dlg.showOk(IsBeneficiaryActivity.this,  PERFORM_UNSUBSCRIBE, null);
			}

		};
		task.execute();

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// On Back Pressed
	//
	// /////////////////////////////

	@Override
	public void onBackPressed()
	{
		state.setMustExit(true);
		Intent intent = new Intent();
		intent.putExtra(SharingState.SHARE_STATE, state);
		setResult(Activity.RESULT_OK, intent);
		finish();

		super.onBackPressed();
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IYesNo
	//
	// /////////////////////////////

	@Override
	public void onPositive(String tag, Object asyncState)
	{
		if (tag.equals(RATE_UNSUBSCRIBE))
			performUnsubscribe();
	}

	@Override
	public void onNegative(String tag, Object asyncState)
	{
	}

	@Override
	public void onAny(String tag, Object asyncState)
	{
		if (tag.equals(PERFORM_UNSUBSCRIBE))
		{
			state.setMustExit(true);
			Intent intent = new Intent();
			intent.putExtra(SharingState.SHARE_STATE, state);
			setResult(Activity.RESULT_OK, intent);
			finish();
		}
		
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// On Tell Me More
	//
	// /////////////////////////////
	public void onTellMeMore(View view)
	{
		TellMeMoreActivity.start(this,  R.string.title_tell_me_more_3, R.string.content_tell_me_more_3);
	}

	


}
