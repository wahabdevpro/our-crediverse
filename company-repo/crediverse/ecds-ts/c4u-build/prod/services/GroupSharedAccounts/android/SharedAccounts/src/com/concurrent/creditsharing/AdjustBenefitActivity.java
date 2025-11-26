package com.concurrent.creditsharing;

import hxc.servicebus.RequestModes;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import com.concurrent.hxc.IYesNo;
import com.concurrent.hxc.Program;
import com.concurrent.hxc.SoapTask;
import com.concurrent.hxc.StickyDialog;
import com.concurrent.soap.AddQuotaRequest;
import com.concurrent.soap.AddQuotaResponse;
import com.concurrent.soap.ChangeQuotaRequest;
import com.concurrent.soap.ChangeQuotaResponse;
import com.concurrent.soap.Number;
import com.concurrent.soap.ServiceQuota;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class AdjustBenefitActivity extends Activity implements IYesNo
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////
	private SharingState state;
	private String name;
	private String memberNumber;
	private ServiceQuota quota;

	private Spinner serviceSpinner;
	private Spinner destinationSpinner;
	private Spinner daySpinner;
	private Spinner timeSpinner;
	private TextView unitsTextView;
	private EditText amountEditText;

	public static final String ON_ADJUST_BENEFIT = "onAdjustBenefit";
	public static final String ON_ADJUST_BENEFIT_FAILED = "onAdjustBenefitFailed";
	public static final String INVALID_AMOUNT = "InvalidAmount";
	public static final String ADJUST_QUOTA = "AdjustQuota";
	public static final String ADJUST_QUOTA_FAILED = "AdjustQuotaFailed";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Creation
	//
	// /////////////////////////////

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_adjust_benefit);
		setTitle(R.string.title_activity_adjust_benefit);
		
		// Enable Back on the Action Bar
		getActionBar().setDisplayHomeAsUpEnabled(true);

		Intent intent = getIntent();
		state = (SharingState) intent.getSerializableExtra(SharingState.SHARE_STATE);
		name = intent.getStringExtra("NAME");
		memberNumber = intent.getStringExtra("NUMBER");
		quota = (ServiceQuota) intent.getSerializableExtra("QUOTA");
		String title = String.format("%s %s", name, quota.getName());
		setTitle(title);

		unitsTextView = (TextView) findViewById(R.id.unitTextView);
		amountEditText = (EditText) findViewById(R.id.amountEditText);
		serviceSpinner = (Spinner) findViewById(R.id.serviceSpinner);
		ArrayAdapter<String> serviceAdapter = new ArrayAdapter<String>(AdjustBenefitActivity.this,
				android.R.layout.simple_spinner_dropdown_item);
		serviceAdapter.add(quota.getService());
		serviceSpinner.setAdapter(serviceAdapter);

		destinationSpinner = (Spinner) findViewById(R.id.destinationSpinner);
		ArrayAdapter<String> destinationAdapter = new ArrayAdapter<String>(AdjustBenefitActivity.this,
				android.R.layout.simple_spinner_dropdown_item);
		destinationAdapter.add(quota.getDestination());
		destinationSpinner.setAdapter(destinationAdapter);

		daySpinner = (Spinner) findViewById(R.id.daySpinner);
		ArrayAdapter<String> dayAdapter = new ArrayAdapter<String>(AdjustBenefitActivity.this,
				android.R.layout.simple_spinner_dropdown_item);
		dayAdapter.add(quota.getDaysOfWeek());
		daySpinner.setAdapter(dayAdapter);

		timeSpinner = (Spinner) findViewById(R.id.timeSpinner);
		ArrayAdapter<String> timeAdapter = new ArrayAdapter<String>(AdjustBenefitActivity.this,
				android.R.layout.simple_spinner_dropdown_item);
		timeAdapter.add(quota.getTimeOfDay());
		timeSpinner.setAdapter(timeAdapter);

		unitsTextView.setText(quota.getUnits());
		amountEditText.setText(Long.toString(quota.getQuantity()));

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.adjust_benefit, menu);
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
	// On Adjust Benefit
	//
	// /////////////////////////////
	public void onAdjustBenefit(View view)
	{
		long amount = 0L;

		try
		{
			amount = Long.parseLong(amountEditText.getText().toString());
		}
		catch (NumberFormatException e)
		{
			StickyDialog dlg = new StickyDialog(state.getServiceName(), "Invalid Amount");
			dlg.showOk(this, INVALID_AMOUNT, null);
			return;

		}

		final ServiceQuota newQuota = new ServiceQuota();
		newQuota.setQuotaID(quota.getQuotaID());
		newQuota.setName(quota.getName());
		newQuota.setService(quota.getService());
		newQuota.setDestination(quota.getDestination());
		newQuota.setTimeOfDay(quota.getTimeOfDay());
		newQuota.setDaysOfWeek(quota.getDaysOfWeek());
		newQuota.setQuantity(amount);
		newQuota.setUnits(quota.getUnits());

		SoapTask<Void, Void> adjustQuotaTask = new SoapTask<Void, Void>(this, state.getServiceID())
		{
			@Override
			protected void whenCancelled()
			{
				AdjustBenefitActivity.this.finish();
			}

			@Override
			protected Void performWork(C4SoapClient client, Void... params) throws Exception
			{
				ChangeQuotaRequest request = new ChangeQuotaRequest();
				request.setServiceID(state.getServiceID());
				request.setVariantID(state.getVariantID());
				request.setSubscriberNumber(new Number(state.getMsisdn()));
				request.setMemberNumber(new Number(memberNumber));
				request.setOldQuota(quota);
				request.setNewQuota(newQuota);
				request.setMode(RequestModes.testOnly);

				ChangeQuotaResponse response = client.call(request);
				this.returnCode = response.getReturnCode();

				if (response.wasSuccess())
				{
					state.setCharge(response.getChargeLevied());
				}

				return null;
			}

			@Override
			protected void whenCompleted(boolean wasSuccess, Void result)
			{

				if (!wasSuccess)
				{
					StickyDialog dialog = new StickyDialog(state.getServiceName(), resultText);
					dialog.showOk(AdjustBenefitActivity.this, ON_ADJUST_BENEFIT_FAILED, null);
					return;
				}

				String amount = Program.formatMoney(state.getCharge());
				String msg = String.format("Confirm you now want to give %s %d %s %s %s, %s, %s at a cost of %s ?",
						AdjustBenefitActivity.this.name, newQuota.getQuantity(), newQuota.getUnits(),
						newQuota.getService(), newQuota.getDestination(), newQuota.getDaysOfWeek(),
						newQuota.getTimeOfDay(), amount);

				StickyDialog dlg = new StickyDialog(state.getServiceName(), msg);
				dlg.showYesNo(AdjustBenefitActivity.this, ON_ADJUST_BENEFIT, newQuota);

			}

		};
		adjustQuotaTask.execute();
	}

	private void adjustQuota(final ServiceQuota newQuota)
	{
		SoapTask<Void, Void> adjustQuotaTask = new SoapTask<Void, Void>(this, state.getServiceID())
		{
			@Override
			protected void whenCancelled()
			{
				AdjustBenefitActivity.this.finish();
			}

			@Override
			protected Void performWork(C4SoapClient client, Void... params) throws Exception
			{
				ChangeQuotaRequest request = new ChangeQuotaRequest();
				request.setServiceID(state.getServiceID());
				request.setVariantID(state.getVariantID());
				request.setSubscriberNumber(new Number(state.getMsisdn()));
				request.setMemberNumber(new Number(memberNumber));
				request.setOldQuota(quota);
				request.setNewQuota(newQuota);

				ChangeQuotaResponse response = client.call(request);
				this.returnCode = response.getReturnCode();

				if (response.wasSuccess())
				{
					state.setCharge(response.getChargeLevied());
				}

				return null;
			}

			@Override
			protected void whenCompleted(boolean wasSuccess, Void result)
			{

				if (!wasSuccess)
				{
					StickyDialog dialog = new StickyDialog(state.getServiceName(), resultText);
					dialog.showOk(AdjustBenefitActivity.this, ADJUST_QUOTA_FAILED, null);
					return;
				}

				String amount = Program.formatMoney(state.getCharge());
				String msg = String.format("You have now shared with %s %d %s %s %s, %s, %s at a cost of %s",
						AdjustBenefitActivity.this.name, newQuota.getQuantity(), newQuota.getUnits(),
						newQuota.getService(), newQuota.getDestination(), newQuota.getDaysOfWeek(),
						newQuota.getTimeOfDay(), amount);

				StickyDialog dlg = new StickyDialog(state.getServiceName(), msg);
				dlg.showOk(AdjustBenefitActivity.this, ADJUST_QUOTA, null);
			}

		};
		adjustQuotaTask.execute();

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IYesNo
	//
	// /////////////////////////////

	@Override
	public void onPositive(String tag, Object asyncState)
	{
		if (tag.equals(ON_ADJUST_BENEFIT))
			adjustQuota((ServiceQuota) asyncState);
	}

	@Override
	public void onNegative(String tag, Object asyncState)
	{
		if (tag.equals(ON_ADJUST_BENEFIT))
			finish();
	}

	@Override
	public void onAny(String tag, Object asyncState)
	{
		if (tag.equals(INVALID_AMOUNT))
			amountEditText.requestFocus();
		else if (tag.equals(ON_ADJUST_BENEFIT_FAILED))
			finish();
		else if (tag.equals(ADJUST_QUOTA_FAILED))
			finish();
		else if (tag.equals(ADJUST_QUOTA))
			finish();
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// On Tell Me More
	//
	// /////////////////////////////
	public void onTellMeMore(View view)
	{
		TellMeMoreActivity.start(this,  R.string.title_tell_me_more_7, R.string.content_tell_me_more_7);
	}


}
