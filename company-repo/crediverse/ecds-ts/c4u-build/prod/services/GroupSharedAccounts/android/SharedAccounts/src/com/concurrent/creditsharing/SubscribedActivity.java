package com.concurrent.creditsharing;

import java.text.NumberFormat;

import hxc.servicebus.RequestModes;

import com.concurrent.hxc.IYesNo;
import com.concurrent.hxc.Program;
import com.concurrent.hxc.SoapTask;
import com.concurrent.hxc.StickyDialog;
import com.concurrent.soap.Number;
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

public class SubscribedActivity extends Activity implements IYesNo
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////
	private SharingState state;

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
		setContentView(R.layout.activity_subscribed);
		setTitle(R.string.title_activity_subscribed);

		// Enable Back on the Action Bar
		getActionBar().setDisplayHomeAsUpEnabled(true);

		state = (SharingState) getIntent().getSerializableExtra(SharingState.SHARE_STATE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.subscribed, menu);
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
	// Beneficiaries
	//
	// /////////////////////////////
	public void onBeneficiaries(View view)
	{
		Intent intent = new Intent(this, BeneficiariesActivity.class);
		intent.putExtra(SharingState.SHARE_STATE, state);
		startActivityForResult(intent, 0);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Balances
	//
	// /////////////////////////////
	public void onBalances(View view)
	{
		Intent intent = new Intent(this, BalancesActivity.class);
		intent.putExtra(SharingState.SHARE_STATE, state);
		startActivityForResult(intent, 0);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Unsubscribe
	//
	// /////////////////////////////
	public void onUnsubscribe(View view)
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
				UnsubscribeRequest request = new UnsubscribeRequest();
				request.setServiceID(state.getServiceID());
				request.setVariantID(state.getVariantID());
				request.setSubscriberNumber(new Number(state.getMsisdn()));
				request.setMode(RequestModes.testOnly);

				UnsubscribeResponse response = client.call(request);
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
					dialog.showOk(SubscribedActivity.this, "ServerError", null);
					return;
				}

				String amount = Program.formatMoney(state.getCharge());
				String msg = String.format("Do you want to unsubscribe from the %s service at a cost of %s ?",
						state.getVariantName(), amount);

				StickyDialog dlg = new StickyDialog(state.getServiceName(), msg);
				dlg.showYesNo(SubscribedActivity.this, RATE_UNSUBSCRIBE, null);

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
				UnsubscribeRequest request = new UnsubscribeRequest();
				request.setServiceID(state.getServiceID());
				request.setVariantID(state.getVariantID());
				request.setSubscriberNumber(new Number(state.getMsisdn()));

				UnsubscribeResponse response = client.call(request);
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
					dialog.showOk(SubscribedActivity.this, "ServerError", null);
					return;
				}

				String amount = Program.formatMoney(state.getCharge());
				String msg = String.format("You have been unsubscribed from the %s service at a cost of %s",
						state.getVariantName(), amount);

				StickyDialog dlg = new StickyDialog(state.getServiceName(), msg);
				dlg.showOk(SubscribedActivity.this, PERFORM_UNSUBSCRIBE, null);
			}

		};
		task.execute();

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// On Back Button
	//
	// /////////////////////////////
	@Override
	public void onBackPressed()
	{
		Intent intent = new Intent();
		state.setMustExit(true);
		intent.putExtra(SharingState.SHARE_STATE, state);
		setResult(Activity.RESULT_OK, intent);
		super.onBackPressed();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// On Activity Result
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
			this.finish();
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// On Tell Me More
	//
	// /////////////////////////////
	public void onTellMeMore(View view)
	{
		TellMeMoreActivity.start(this, R.string.title_tell_me_more_5, R.string.content_tell_me_more_5);
	}

}
