package com.concurrent.credittransfer;

import hxc.servicebus.RequestModes;

import java.text.NumberFormat;
import java.util.Locale;

import com.concurrent.hxc.IYesNo;
import com.concurrent.hxc.Program;
import com.concurrent.hxc.SoapTask;
import com.concurrent.hxc.StickyDialog;
import com.concurrent.credittransfer.R;
import com.concurrent.credittransfer.R.id;
import com.concurrent.credittransfer.R.layout;
import com.concurrent.credittransfer.R.menu;
import com.concurrent.soap.Number;
import com.concurrent.soap.SubscribeRequest;
import com.concurrent.soap.SubscribeResponse;
import com.concurrent.soap.VasServiceInfo;
import com.concurrent.util.C4SoapClient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;

public class SubscribeActivity extends Activity implements IYesNo
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private TransferState state;

	private static final String CONFIRM_SUBSCRIPTION = "CONFIRM SUBSCRIPTION";
	private static final String RATE_SUBSCRIPTION_FAILED = "RATE SUBSCRIPTION FAILED";
	private static final String PERFORM_SUBSCRIPTION_FAILED = "PERFORM SUBSCRIPTION FAILED";
	private static final String CONFIRM_SUBSCRIBED = "CONFIRM SUBSCRIBED";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Creation
	//
	// /////////////////////////////////
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_subscribe_act);

		// Enable Back on the Action Bar
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// Get State
		Intent intent = this.getIntent();
		state = TransferState.get(intent);

		// Set Title
		setTitle(state.getServiceName());

		// Get Linear Layout
		ViewGroup content = (ViewGroup) this.findViewById(android.R.id.content);
		LinearLayout linearLayout = (LinearLayout) content.getChildAt(0);

		// Add Buttons
		for (VasServiceInfo info : state.getServiceInfo())
		{
			Button button = new Button(this);
			button.setText(info.getVariantName());
			button.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			final VasServiceInfo variant = info;
			button.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					SubscribeActivity.this.rateSubscription(variant);
				}
			});

			linearLayout.addView(button);
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Options
	//
	// /////////////////////////////////
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.subscribe, menu);
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
		return super.onOptionsItemSelected(item);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Subscribe
	//
	// /////////////////////////////////

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
		String format = getString(R.string.format_subscribing);
		String msg = String.format(format, variant.getVariantName(), amount);

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

				state.setSubscribedVariantID(variant.getVariantID());
				state.setSubscribedVariantName(variant.getVariantName());
				confirmSubscribed(variant);
			}

		};

		task.execute();

	}

	// Confirm Subscription
	private void confirmSubscribed(final VasServiceInfo variant)
	{
		String amount = Program.formatMoney(state.getCharge());
		String format = getString(R.string.format_subscribed);
		String msg = String.format(format, variant.getVariantName(), amount);

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
		if (tag.equals(RATE_SUBSCRIPTION_FAILED))
		{
			// Too bad
		}
		else if (tag.equals(PERFORM_SUBSCRIPTION_FAILED))
			finish();
		else if (tag.equals(CONFIRM_SUBSCRIBED))
		{
			Intent intent = new Intent();
			state.put(intent);
			setResult(Activity.RESULT_OK, intent);
			finish();
		}
	}

}
