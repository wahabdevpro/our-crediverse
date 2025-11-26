package com.concurrent.creditsharing;

import java.text.NumberFormat;

import hxc.servicebus.RequestModes;

import com.concurrent.hxc.IYesNo;
import com.concurrent.hxc.Program;
import com.concurrent.hxc.SoapTask;
import com.concurrent.hxc.StickyDialog;
import com.concurrent.soap.GetServiceRequest;
import com.concurrent.soap.GetServiceResponse;
import com.concurrent.soap.Number;
import com.concurrent.soap.SubscribeRequest;
import com.concurrent.soap.SubscribeResponse;
import com.concurrent.soap.VasServiceInfo;
import com.concurrent.util.C4SoapClient;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class SubscribeActivity extends Activity implements IYesNo
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////
	private SharingState state;

	private static final String CONFIRM_SUBSCRIPTION = "confirmSubscription";
	private static final String GET_AVAILABLE_VARIANTS_FAILED = "getAvailableVariantsFailed";
	private static final String RATE_SUBSCRIPTION_FAILED = "rateSubscriptionFailed";
	private static final String PERFORM_SUBSCRIPTION_FAILED = "performSubscriptionFailed";
	private static final String CONFIRM_SUBSCRIBED = "confirmSubscribed";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Create
	//
	// /////////////////////////////
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_subscribe);
		state = (SharingState) getIntent().getSerializableExtra(SharingState.SHARE_STATE);
		setTitle(R.string.title_activity_subscribe);

		// Enable Back on the Action Bar
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// Get available Variants to subscribe to
		getAvailableVariants();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.subscribe, menu);
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
	// On Subscribe Button
	//
	// /////////////////////////////
	public void onSubscribe(View view)
	{
		Button button = (Button) view;
		VasServiceInfo variant = (VasServiceInfo) button.getTag();

		// Rate Subscription and Confirm
		rateSubscription(variant);

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////

	// Get available Variants to subscribe to
	private void getAvailableVariants()
	{

		SoapTask<Void, Void> task = new SoapTask<Void, Void>(this, state.getServiceID())
		{

			@Override
			protected void whenCancelled()
			{
				SubscribeActivity.this.finish();
			}

			@Override
			protected Void performWork(C4SoapClient client, Void... params) throws Exception
			{
				// Get Variants
				GetServiceRequest request1 = new GetServiceRequest();
				request1.setServiceID(state.getServiceID());
				request1.setActiveOnly(false);
				request1.setSubscriberNumber(new Number(state.getMsisdn()));

				GetServiceResponse response1 = client.call(request1);
				returnCode = response1.getReturnCode();
				if (response1.wasSuccess())
				{
					state.setAllVariants(response1.getServiceInfo());
				}
				return null;
			}

			@Override
			protected void whenCompleted(boolean wasSuccess, Void result)
			{
				if (!wasSuccess)
				{
					StickyDialog dialog = new StickyDialog(state.getServiceName(), resultText);
					dialog.showOk(SubscribeActivity.this, GET_AVAILABLE_VARIANTS_FAILED, null);
					return;
				}

				enableVariantButtons();

			}

		};

		task.execute();
	}

	// Enable Variant Buttons and set their text
	private void enableVariantButtons()
	{
		VasServiceInfo[] variants = state.getAllVariants();
		if (variants != null)
		{
			int[] buttonIDs = new int[] { R.id.addBenefitButton, R.id.button2, R.id.button3, R.id.button4,
					R.id.button5, R.id.button6 };

			for (int index = 0; index < variants.length && index < buttonIDs.length; index++)
			{
				Button button = (Button) SubscribeActivity.this.findViewById(buttonIDs[index]);
				button.setText(variants[index].getVariantName() + " >");
				button.setTag(variants[index]);
				button.setVisibility(View.VISIBLE);
			}
		}
	}

	// Rate Subscription
	private void rateSubscription(final VasServiceInfo variant)
	{
		SoapTask<Void, Void> task = new SoapTask<Void, Void>(this, state.getServiceID())
		{
			@Override
			protected void whenCancelled()
			{
				SubscribeActivity.this.finish();
			}

			@Override
			protected Void performWork(C4SoapClient client, Void... params) throws Exception
			{
				SubscribeRequest request1 = new SubscribeRequest();
				request1.setServiceID(state.getServiceID());
				request1.setVariantID(variant.getVariantID());
				request1.setSubscriberNumber(new Number(state.getMsisdn()));
				request1.setMode(RequestModes.testOnly);

				SubscribeResponse response1 = client.call(request1);
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
					dialog.showOk(SubscribeActivity.this, RATE_SUBSCRIPTION_FAILED, null);
					return;
				}

				confirmSubscription(variant);

			}

		};

		task.execute();

	}

	// Confirm Subscription
	private void confirmSubscription(final VasServiceInfo variant)
	{
		String amount = Program.formatMoney(state.getCharge());
		String msg = String.format("Confirm you want to subscribe to the %s service at a cost of %s ?",
				variant.getVariantName(), amount);

		StickyDialog dialog = new StickyDialog(state.getServiceName(), msg);
		dialog.showOkCancel(this, CONFIRM_SUBSCRIPTION, variant);
	}

	// Perform Subscription
	private void performSubscription(final VasServiceInfo variant)
	{
		SoapTask<Void, Void> task = new SoapTask<Void, Void>(this, state.getServiceID())
		{

			@Override
			protected void whenCancelled()
			{
				SubscribeActivity.this.finish();
			}

			@Override
			protected Void performWork(C4SoapClient client, Void... params) throws Exception
			{
				SubscribeRequest request1 = new SubscribeRequest();
				request1.setServiceID(state.getServiceID());
				request1.setVariantID(variant.getVariantID());
				request1.setSubscriberNumber(new Number(state.getMsisdn()));

				SubscribeResponse response1 = client.call(request1);
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
					dialog.showOk(SubscribeActivity.this, PERFORM_SUBSCRIPTION_FAILED, null);
					return;
				}

				state.setSubscribed(true);
				state.setVariantID(variant.getVariantID());
				state.setVariantName(variant.getVariantName());
				confirmSubscribed(variant);

			}

		};

		task.execute();

	}

	// Confirm Subscription
	private void confirmSubscribed(final VasServiceInfo variant)
	{
		String amount = Program.formatMoney(state.getCharge());
		String msg = String.format("You have been subscribed to the %s service at a cost of %s",
				variant.getVariantName(), amount);

		StickyDialog dlg = new StickyDialog(state.getServiceName(), msg);
		dlg.showOk(this, CONFIRM_SUBSCRIBED, null);

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IYesNo
	//
	// /////////////////////////////

	@Override
	public void onPositive(String tag, Object asyncState)
	{
		if (tag.equals(CONFIRM_SUBSCRIPTION))
			performSubscription((VasServiceInfo) asyncState);

	}

	@Override
	public void onNegative(String tag, Object asyncState)
	{
	}

	@Override
	public void onAny(String tag, Object asyncState)
	{
		if (tag.equals(GET_AVAILABLE_VARIANTS_FAILED))
			finish();
		else if (tag.equals(RATE_SUBSCRIPTION_FAILED))
			finish();
		else if (tag.equals(PERFORM_SUBSCRIPTION_FAILED))
			finish();
		else if (tag.equals(CONFIRM_SUBSCRIBED))
		{
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
		TellMeMoreActivity.start(this, R.string.title_tell_me_more_2, R.string.content_tell_me_more_2);
	}

}
