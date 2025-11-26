package com.concurrent.credittransfer;

import java.text.NumberFormat;

import hxc.servicebus.RequestModes;

import com.concurrent.hxc.IYesNo;
import com.concurrent.hxc.Program;
import com.concurrent.hxc.SoapTask;
import com.concurrent.hxc.StickyDialog;
import com.concurrent.credittransfer.R;
import com.concurrent.soap.CreditTransfer;
import com.concurrent.soap.CreditTransferType;
import com.concurrent.soap.GetCreditTransfersRequest;
import com.concurrent.soap.GetCreditTransfersResponse;
import com.concurrent.soap.GetServiceRequest;
import com.concurrent.soap.GetServiceResponse;
import com.concurrent.soap.Number;
import com.concurrent.soap.SubscriptionState;
import com.concurrent.soap.UnsubscribeRequest;
import com.concurrent.soap.UnsubscribeResponse;
import com.concurrent.soap.VasServiceInfo;
import com.concurrent.util.C4SoapClient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class CreditTransferActivity extends Activity implements IYesNo
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private TransferState state;
	private Button subscribeButton;
	private Button ussubscribeButton;
	private Button recipientsButton;
	private Button transferNowButton;

	private static final String GET_SERVICE_FAILED = "GetServiceFailed";
	private static final String RATE_UNSUBSCRIBE = "RATE UNSUBSCRIBE";
	private static final String PERFORM_UNSUBSCRIBE = "PERFORM UNSUBSCRIBE";

	private static final int RESULT_REQUEST_CODE = 17;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Creation
	//
	// /////////////////////////////////
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_credit_transfer);
		setTitle(R.string.service_name);

		// Enable Back on the Action Bar
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// Find Buttons
		subscribeButton = (Button) findViewById(R.id.buttonSubscribe);
		recipientsButton = (Button) findViewById(R.id.buttonRecipients);
		transferNowButton = (Button) findViewById(R.id.buttonTransferNow);
		ussubscribeButton = (Button) findViewById(R.id.buttonUnsubscribe);

		// Hide them first
		subscribeButton.setVisibility(View.GONE);
		recipientsButton.setVisibility(View.GONE);
		transferNowButton.setVisibility(View.GONE);
		ussubscribeButton.setVisibility(View.GONE);

		// Get the original Intent
		Intent intent = getIntent();
		VasServiceInfo serviceInfo = (VasServiceInfo) intent.getSerializableExtra("VASINFO");

		// Create a Sharing Context
		if (state == null)
		{
			state = new TransferState();
			state.setMsisdn(Program.getMSISDN());
			state.setServiceID(serviceInfo.getServiceID());
			state.setServiceName(serviceInfo.getServiceName());
			setTitle(state.getServiceName());
		}

		// Get Subscriptions
		getSubscriptions();

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
		getMenuInflater().inflate(R.menu.credit_transfer, menu);
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
	// Initialization
	//
	// /////////////////////////////////
	private void getSubscriptions()
	{
		SoapTask<Void, Void> task = new SoapTask<Void, Void>(this, state.getServiceID())
		{

			@Override
			protected void whenCancelled()
			{
				CreditTransferActivity.this.finish();
			}

			@Override
			protected Void performWork(C4SoapClient client, Void... params) throws Exception
			{
				GetServiceRequest request = new GetServiceRequest();
				request.setServiceID(state.getServiceID());
				request.setVariantID(state.getSubscribedVariantID());
				request.setSubscriberNumber(new Number(state.getMsisdn()));

				GetServiceResponse response = client.call(request);
				returnCode = response.getReturnCode();
				if (response.wasSuccess())
				{
					state.setServiceInfo(response.getServiceInfo());
					state.setSubscribedVariantID(null);
					state.setSubscribedVariantName(null);
					for (VasServiceInfo variant : response.getServiceInfo())
					{
						if (variant.getState() == SubscriptionState.active)
						{
							state.setSubscribedVariantID(variant.getVariantID());
							state.setSubscribedVariantName(variant.getVariantName());
							break;
						}
					}
				}

				return null;
			}

			@Override
			protected void whenCompleted(boolean wasSuccess, Void result)
			{
				if (!wasSuccess)
				{
					StickyDialog dialog = new StickyDialog(state.getServiceName(), resultText);
					dialog.showOk(CreditTransferActivity.this, GET_SERVICE_FAILED, null);
					return;
				}

				getTransferModes();

			}

		};
		task.execute();

	}

	private void getTransferModes()
	{
		SoapTask<Void, Void> task = new SoapTask<Void, Void>(this, state.getServiceID())
		{

			@Override
			protected void whenCancelled()
			{
				CreditTransferActivity.this.finish();
			}

			@Override
			protected Void performWork(C4SoapClient client, Void... params) throws Exception
			{
				GetCreditTransfersRequest request = new GetCreditTransfersRequest();
				request.setServiceID(state.getServiceID());
				request.setSubscriberNumber(new Number(state.getMsisdn()));

				GetCreditTransfersResponse response = client.call(request);
				returnCode = response.getReturnCode();
				if (response.wasSuccess())
				{
					state.setTransfers(response.getTransfers());
				}

				return null;
			}

			@Override
			protected void whenCompleted(boolean wasSuccess, Void result)
			{
				if (!wasSuccess)
				{
					StickyDialog dialog = new StickyDialog(state.getServiceName(), resultText);
					dialog.showOk(CreditTransferActivity.this, GET_SERVICE_FAILED, null);
					return;
				}

				enableButtons();

			}

		};
		task.execute();

	}

	private void enableButtons()
	{
		VasServiceInfo[] serviceInfo = state.getServiceInfo();
		boolean hasServices = serviceInfo != null && serviceInfo.length > 1;
		String subscribedVariantID = state.getSubscribedVariantID();
		boolean subscribed = subscribedVariantID != null && subscribedVariantID.length() > 0;
		boolean canTransferNow = false;
		for (CreditTransfer transfer : state.getTransfers())
		{
			if (transfer.getTransferType() == CreditTransferType.OnceOff)
			{
				canTransferNow = true;
				break;
			}
		}

		boolean canSubscribe = hasServices && !subscribed;
		subscribeButton.setEnabled(canSubscribe);
		subscribeButton.setVisibility(canSubscribe ? View.VISIBLE : View.GONE);
		boolean canHaveRecipients = true;
		recipientsButton.setEnabled(canHaveRecipients);
		recipientsButton.setVisibility(canHaveRecipients ? View.VISIBLE : View.GONE);
		transferNowButton.setEnabled(canTransferNow);
		transferNowButton.setVisibility(canTransferNow ? View.VISIBLE : View.GONE);
		ussubscribeButton.setEnabled(subscribed);
		ussubscribeButton.setVisibility(subscribed ? View.VISIBLE : View.GONE);

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Subscribe
	//
	// /////////////////////////////////
	public void onSubscribe(View view)
	{
		Intent intent = new Intent(this, SubscribeActivity.class);
		state.put(intent);
		startActivityForResult(intent, RESULT_REQUEST_CODE);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Recipients
	//
	// /////////////////////////////////
	public void onRecipients(View view)
	{		
		Intent intent = new Intent(this, RecipientsActivity.class);
		state.put(intent);
		startActivity(intent);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Transfer Now
	//
	// /////////////////////////////////
	public void onTransferNow(View view)
	{
		Intent intent = new Intent(this, TransferActivity.class);
		state.put(intent);
		startActivity(intent);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Unsubscribe
	//
	// /////////////////////////////////
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
				request.setVariantID(state.getSubscribedVariantID());
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
					dialog.showOk(CreditTransferActivity.this, "ServerError", null);
					return;
				}

				String amount = Program.formatMoney(state.getCharge());
				String format = getString(R.string.format_unsubscribing);
				String msg = String.format(format, state.getSubscribedVariantName(), amount);

				StickyDialog dlg = new StickyDialog(state.getServiceName(), msg);
				dlg.showYesNo(CreditTransferActivity.this, RATE_UNSUBSCRIBE, null);

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
				request.setVariantID(state.getSubscribedVariantID());
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
					dialog.showOk(CreditTransferActivity.this, "ServerError", null);
					return;
				}

				String amount = Program.formatMoney(state.getCharge());
				String format = getString(R.string.format_unsubscribed);
				String msg = String.format(format, state.getSubscribedVariantName(), amount);
				StickyDialog dlg = new StickyDialog(state.getServiceName(), msg);
				dlg.showOk(CreditTransferActivity.this, PERFORM_UNSUBSCRIBE, null);

				state.setSubscribedVariantID(null);
				state.setSubscribedVariantName(null);
			}

		};
		task.execute();

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IYesNo
	//
	// /////////////////////////////

	@Override
	public void onPositive(String tag, Object asyncState)
	{
		if (RATE_UNSUBSCRIBE.equalsIgnoreCase(tag))
			performUnsubscribe();
	
	}

	@Override
	public void onNegative(String tag, Object asyncState)
	{

	}

	@Override
	public void onAny(String tag, Object asyncState)
	{
		if (PERFORM_UNSUBSCRIBE.equalsIgnoreCase(tag))
			enableButtons();
		else if (GET_SERVICE_FAILED.equalsIgnoreCase(tag))
			finish();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// On Result
	//
	// /////////////////////////////
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == RESULT_REQUEST_CODE)
		{
			if (data != null)
			{
				state = TransferState.get(data);
				enableButtons();
			}
		}
	}

}
