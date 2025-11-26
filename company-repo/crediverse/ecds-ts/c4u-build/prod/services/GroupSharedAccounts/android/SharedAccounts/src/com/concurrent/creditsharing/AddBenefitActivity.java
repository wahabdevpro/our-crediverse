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
import com.concurrent.soap.ContactInfo;
import com.concurrent.soap.GetQuotasRequest;
import com.concurrent.soap.GetQuotasResponse;
import com.concurrent.soap.Number;
import com.concurrent.soap.ServiceQuota;
import com.concurrent.soap.UpdateContactInfoRequest;
import com.concurrent.soap.UpdateContactInfoResponse;
import com.concurrent.util.C4SoapClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class AddBenefitActivity extends Activity implements IYesNo
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////
	private SharingState state = null;
	private String name;
	private String memberNumber;
	private boolean isNewBeneficiary = false;
	private List<ServiceQuota> serviceQuotas;

	private Spinner serviceSpinner;
	private Spinner destinationSpinner;
	private Spinner daySpinner;
	private Spinner timeSpinner;
	private TextView unitsTextView;
	private EditText amountEditText;
	
	
	private static final String ON_ADD_BENEFIT = "onAddBenefit";
	private static final String UPDATE_QUOTAS_FAILED = "updateQuotasFailed";
	private static final String NUMBER_FORMAT_EXCEPTION = "NumberFormatException";
	private static final String ADD_QUOTA = "addQuota";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Creation
	//
	// /////////////////////////////
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_benefit);
		setTitle(R.string.title_activity_add_benefit);

		unitsTextView = (TextView) findViewById(R.id.unitTextView);
		amountEditText = (EditText) findViewById(R.id.amountEditText);
		
		// Enable Back on the Action Bar
		getActionBar().setDisplayHomeAsUpEnabled(true);

		Intent intent = getIntent();
		state = (SharingState) intent.getSerializableExtra(SharingState.SHARE_STATE);
		name = intent.getStringExtra("NAME");
		memberNumber = intent.getStringExtra("NUMBER");
		isNewBeneficiary = intent.getBooleanExtra("NEWGUY", false);
		String title = String.format("Add benefit for %s", name);
		setTitle(title);

		serviceSpinner = (Spinner) findViewById(R.id.serviceSpinner);
		serviceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				onServiceChanged();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{
			}
		});

		destinationSpinner = (Spinner) findViewById(R.id.destinationSpinner);
		destinationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				onDestinationChanged();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{
			}
		});

		daySpinner = (Spinner) findViewById(R.id.daySpinner);
		daySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				onDayChanged();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{
			}
		});

		timeSpinner = (Spinner) findViewById(R.id.timeSpinner);
		timeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				onTimeChanged();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{
			}
		});

		// Update Quotas
		updateQuotas();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.add_benefit, menu);
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
	// Update Quotas
	//
	// /////////////////////////////

	private void updateQuotas()
	{
		SoapTask<Void, Void> getQuotasTask = new SoapTask<Void, Void>(this, state.getServiceID())
		{

			@Override
			protected void whenCancelled()
			{
				AddBenefitActivity.this.finish();
			}

			@Override
			protected Void performWork(C4SoapClient client, Void... params) throws Exception
			{
				GetQuotasRequest request = new GetQuotasRequest();
				request.setServiceID(state.getServiceID());
				request.setVariantID(state.getVariantID());
				request.setSubscriberNumber(new Number(state.getMsisdn()));
				request.setMemberNumber(new Number(memberNumber));
				request.setActiveOnly(false);

				GetQuotasResponse response = client.call(request);
				this.returnCode = response.getReturnCode();

				if (response.wasSuccess())
				{
					serviceQuotas = new ArrayList<ServiceQuota>();
					for (ServiceQuota serviceQuota : response.getServiceQuotas())
					{
						if (serviceQuota.getQuantity() == 0)
							serviceQuotas.add(serviceQuota);
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
					dialog.showOk(AddBenefitActivity.this, UPDATE_QUOTAS_FAILED, null);
					return;
				}

				// Service Adapter
				ArrayAdapter<String> serviceAdapter = new ArrayAdapter<String>(AddBenefitActivity.this,
						android.R.layout.simple_spinner_dropdown_item);
				List<String> serviceItems = new ArrayList<String>();
				for (ServiceQuota service : serviceQuotas)
				{
					if (!serviceItems.contains(service.getService()))
						serviceItems.add(service.getService());
				}
				serviceAdapter.addAll(serviceItems);
				serviceSpinner.setAdapter(serviceAdapter);

			}

		};
		getQuotasTask.execute();

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Service Changed
	//
	// /////////////////////////////
	private void onServiceChanged()
	{
		String serviceName = (String) serviceSpinner.getSelectedItem();
		ArrayAdapter<String> destinationAdapter = new ArrayAdapter<String>(AddBenefitActivity.this,
				android.R.layout.simple_spinner_dropdown_item);
		List<String> destinationItems = new ArrayList<String>();
		for (ServiceQuota service : serviceQuotas)
		{
			if (service.getService().equals(serviceName) && !destinationItems.contains(service.getDestination()))
				destinationItems.add(service.getDestination());
		}
		destinationAdapter.addAll(destinationItems);
		destinationSpinner.setAdapter(destinationAdapter);

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Destination Changed
	//
	// /////////////////////////////
	private void onDestinationChanged()
	{
		String serviceName = (String) serviceSpinner.getSelectedItem();
		String destinationName = (String) destinationSpinner.getSelectedItem();
		ArrayAdapter<String> dayAdapter = new ArrayAdapter<String>(AddBenefitActivity.this,
				android.R.layout.simple_spinner_dropdown_item);
		List<String> dayItems = new ArrayList<String>();
		for (ServiceQuota service : serviceQuotas)
		{
			if (service.getService().equals(serviceName) && service.getDestination().equals(destinationName)
					&& !dayItems.contains(service.getDaysOfWeek()))
				dayItems.add(service.getDaysOfWeek());
		}
		dayAdapter.addAll(dayItems);
		daySpinner.setAdapter(dayAdapter);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Day Changed
	//
	// /////////////////////////////
	private void onDayChanged()
	{
		String serviceName = (String) serviceSpinner.getSelectedItem();
		String destinationName = (String) destinationSpinner.getSelectedItem();
		String dayName = (String) daySpinner.getSelectedItem();
		ArrayAdapter<String> timeAdapter = new ArrayAdapter<String>(AddBenefitActivity.this,
				android.R.layout.simple_spinner_dropdown_item);
		List<String> timeItems = new ArrayList<String>();
		for (ServiceQuota service : serviceQuotas)
		{
			if (service.getService().equals(serviceName) && service.getDestination().equals(destinationName)
					&& service.getDaysOfWeek().equals(dayName) && !timeItems.contains(service.getTimeOfDay()))
				timeItems.add(service.getTimeOfDay());
		}
		timeAdapter.addAll(timeItems);
		timeSpinner.setAdapter(timeAdapter);
		// onTimeChanged();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Time Change
	//
	// /////////////////////////////
	private void onTimeChanged()
	{
		unitsTextView.setText("");
		String serviceName = (String) serviceSpinner.getSelectedItem();
		String destinationName = (String) destinationSpinner.getSelectedItem();
		String dayName = (String) daySpinner.getSelectedItem();
		String timeName = (String) timeSpinner.getSelectedItem();
		for (ServiceQuota service : serviceQuotas)
		{
			if (service.getService().equals(serviceName) && service.getDestination().equals(destinationName)//
					&& service.getDaysOfWeek().equals(dayName) && service.getTimeOfDay().equals(timeName))
			{
				unitsTextView.setText(service.getUnits());
				break;
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// On Add Benefit
	//
	// /////////////////////////////
	public void onAddBenefit(View view)
	{
		long amount = 0L;

		try
		{
			amount = Long.parseLong(amountEditText.getText().toString());
		}
		catch (NumberFormatException e)
		{
			StickyDialog dlg = new StickyDialog(state.getServiceName(), "Invalid Amount");
			dlg.showOk(this, NUMBER_FORMAT_EXCEPTION, null);
			return;
		}

		final String serviceName = (String) serviceSpinner.getSelectedItem();
		final String destinationName = (String) destinationSpinner.getSelectedItem();
		final String dayName = (String) daySpinner.getSelectedItem();
		final String timeName = (String) timeSpinner.getSelectedItem();
		String units = null;
		String quotaID = null;
		String name = null;
		for (ServiceQuota service : serviceQuotas)
		{
			if (service.getService().equals(serviceName) && service.getDestination().equals(destinationName)//
					&& service.getDaysOfWeek().equals(dayName) && service.getTimeOfDay().equals(timeName))
			{
				units = service.getUnits();
				quotaID = service.getQuotaID();
				name = service.getName();
				break;
			}
		}

		final ServiceQuota quota = new ServiceQuota();
		quota.setQuotaID(quotaID);
		quota.setName(name);
		quota.setService(serviceName);
		quota.setDestination(destinationName);
		quota.setTimeOfDay(timeName);
		quota.setDaysOfWeek(dayName);
		quota.setQuantity(amount);
		quota.setUnits(units);

		SoapTask<Void, Void> addQuotaTask = new SoapTask<Void, Void>(this, state.getServiceID())
		{
			@Override
			protected void whenCancelled()
			{

			}

			@Override
			protected Void performWork(C4SoapClient client, Void... params) throws Exception
			{
		
				
				AddQuotaRequest request = new AddQuotaRequest();
				request.setServiceID(state.getServiceID());
				request.setVariantID(state.getVariantID());
				request.setSubscriberNumber(new Number(state.getMsisdn()));
				request.setMemberNumber(new Number(memberNumber));
				request.setQuota(quota);
				request.setMode(RequestModes.testOnly);

				AddQuotaResponse response = client.call(request);
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
					dialog.showOk(AddBenefitActivity.this, "ServerError", null);
					return;
				}

				String amount = Program.formatMoney(state.getCharge());
				String msg = String.format("Confirm you want to give %s %d %s %s %s, %s, %s at a cost of %s ?",
						AddBenefitActivity.this.name, quota.getQuantity(), quota.getUnits(), quota.getService(),
						quota.getDestination(), quota.getDaysOfWeek(), quota.getTimeOfDay(), amount);

				StickyDialog dlg = new StickyDialog(state.getServiceName(), msg);
				dlg.showYesNo(AddBenefitActivity.this, ON_ADD_BENEFIT, quota);

			}

		};
		addQuotaTask.execute();

	}

	private void addQuota(final ServiceQuota quota)
	{
		SoapTask<Void, Void> addQuotaTask = new SoapTask<Void, Void>(this, state.getServiceID())
		{
			@Override
			protected void whenCancelled()
			{

			}

			@Override
			protected Void performWork(C4SoapClient client, Void... params) throws Exception
			{
				AddQuotaRequest request = new AddQuotaRequest();
				request.setServiceID(state.getServiceID());
				request.setVariantID(state.getVariantID());
				request.setSubscriberNumber(new Number(state.getMsisdn()));
				request.setMemberNumber(new Number(memberNumber));
				request.setQuota(quota);

				AddQuotaResponse response = client.call(request);
				this.returnCode = response.getReturnCode();

				if (response.wasSuccess())
				{
					state.setCharge(response.getChargeLevied());
										
					if (isNewBeneficiary && !AddBenefitActivity.this.name.equals(memberNumber))
					{
						ContactInfo contactInfo = new ContactInfo();
						contactInfo.setName(AddBenefitActivity.this.name);
						UpdateContactInfoRequest request2 = new UpdateContactInfoRequest();
						request2.setServiceID(state.getServiceID());
						request2.setSubscriberNumber(new Number(memberNumber));
						request2.setContactInfo(contactInfo);
						UpdateContactInfoResponse response2 = client.call(request2);
						// TODO
						System.out.print(response2.getReturnCode());
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
					dialog.showOk(AddBenefitActivity.this, "ServerError", null);
					return;
				}

				String amount = Program.formatMoney(state.getCharge());
				String msg = String.format("You have given %s %d %s %s %s, %s, %s at a cost of %s",
						AddBenefitActivity.this.name, quota.getQuantity(), quota.getUnits(), quota.getService(),
						quota.getDestination(), quota.getDaysOfWeek(), quota.getTimeOfDay(), amount);

				StickyDialog dlg = new StickyDialog(state.getServiceName(), msg);
				dlg.showOk(AddBenefitActivity.this, ADD_QUOTA, null);
			}

		};
		addQuotaTask.execute();

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IYesNo
	//
	// /////////////////////////////

	@Override
	public void onPositive(String tag, Object asyncState)
	{
		if (tag.equals(ON_ADD_BENEFIT))
			addQuota((ServiceQuota)asyncState);

	}

	@Override
	public void onNegative(String tag, Object asyncState)
	{
	}

	@Override
	public void onAny(String tag, Object asyncState)
	{
		if (tag.equals(UPDATE_QUOTAS_FAILED))
			finish();
		else if (tag.equals(NUMBER_FORMAT_EXCEPTION))
			amountEditText.requestFocus();
		else if (tag.equals(ADD_QUOTA))
			finish();
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// On Tell Me More
	//
	// /////////////////////////////
	public void onTellMeMore(View view)
	{
		TellMeMoreActivity.start(this,  R.string.title_tell_me_more_6, R.string.content_tell_me_more_6);
	}

}
