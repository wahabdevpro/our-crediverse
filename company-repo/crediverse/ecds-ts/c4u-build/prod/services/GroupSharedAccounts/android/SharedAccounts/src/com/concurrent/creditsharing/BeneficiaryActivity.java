package com.concurrent.creditsharing;

import hxc.servicebus.RequestModes;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import com.concurrent.hxc.IYesNo;
import com.concurrent.hxc.Program;
import com.concurrent.hxc.SoapTask;
import com.concurrent.hxc.StickyDialog;
import com.concurrent.soap.GetQuotasRequest;
import com.concurrent.soap.GetQuotasResponse;
import com.concurrent.soap.Number;
import com.concurrent.soap.RemoveQuotaRequest;
import com.concurrent.soap.RemoveQuotaResponse;
import com.concurrent.soap.ServiceQuota;
import com.concurrent.util.C4SoapClient;
import com.concurrent.util.SwipeDismissListViewTouchListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class BeneficiaryActivity extends Activity implements IYesNo
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////
	private SharingState state = null;
	private String name;
	private String memberNumber;
	private ListView listView;

	private static final String TEST_REMOVE_BENEFIT = "testRemoveBenefit";
	private static final String REFRESH_BENEFITS_LIST_FAILED = "refreshBenefitsListFailed";
	private static final String TEST_REMOVE_BENEFIT_FAILED = "testRemoveBenefitFailed";
	private static final String REMOVE_BENEFIT = "removeBenefit";
	private static final String REMOVE_BENEFIT_FAILED = "removeBenefitFailed";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Creation
	//
	// /////////////////////////////
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_beneficiary);

		// Enable Back on the Action Bar
		getActionBar().setDisplayHomeAsUpEnabled(true);

		Intent intent = getIntent();
		state = (SharingState) intent.getSerializableExtra(SharingState.SHARE_STATE);
		name = intent.getStringExtra("NAME");
		memberNumber = intent.getStringExtra("NUMBER");
		String title = String.format("%s Benefits", name);
		setTitle(title);

		listView = (ListView) findViewById(R.id.listView1);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int position, long id)
			{
				ServiceQuota quota = (ServiceQuota) parent.getItemAtPosition(position);
				Intent intent = new Intent(BeneficiaryActivity.this, AdjustBenefitActivity.class);
				intent.putExtra(SharingState.SHARE_STATE, state);
				intent.putExtra("NAME", name);
				intent.putExtra("NUMBER", memberNumber);
				intent.putExtra("QUOTA", quota);
				startActivityForResult(intent, 0);
			}
		});

		// Swipe to Dismiss
		SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(listView,
				new SwipeDismissListViewTouchListener.DismissCallbacks()
				{
					@Override
					public boolean canDismiss(int position)
					{
						return true;
					}

					@Override
					public void onDismiss(ListView listView, int[] reverseSortedPositions)
					{
						if (reverseSortedPositions != null && reverseSortedPositions.length > 0)
						{
							ServiceQuota entry = (ServiceQuota) listView.getAdapter()
									.getItem(reverseSortedPositions[0]);
							testRemoveBenefit(entry);
						}
					}
				});
		listView.setOnTouchListener(touchListener);

		refreshBenefitsList();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.beneficiary, menu);
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
	// On Add Quota
	//
	// /////////////////////////////
	public void onAddQuota(View view)
	{
		Intent intent = new Intent(this, AddBenefitActivity.class);
		intent.putExtra(SharingState.SHARE_STATE, state);
		intent.putExtra("NAME", name);
		intent.putExtra("NUMBER", memberNumber);
		startActivityForResult(intent, 0);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// List View
	//
	// /////////////////////////////
	private void refreshBenefitsList()
	{
		SoapTask<Void, Void> getMembersTask = new SoapTask<Void, Void>(this, state.getServiceID())
		{
			@Override
			protected void whenCancelled()
			{
				BeneficiaryActivity.this.finish();
			}

			@Override
			protected Void performWork(C4SoapClient client, Void... params) throws Exception
			{
				GetQuotasRequest request = new GetQuotasRequest();
				request.setServiceID(state.getServiceID());
				request.setVariantID(state.getVariantID());
				request.setSubscriberNumber(new Number(state.getMsisdn()));
				request.setMemberNumber(new Number(memberNumber));
				request.setActiveOnly(true);

				GetQuotasResponse response = client.call(request);
				returnCode = response.getReturnCode();
				if (response.wasSuccess())
				{
					state.setCharge(response.getChargeLevied());
					state.setQuotas(response.getServiceQuotas());
				}
				return null;
			}

			@Override
			protected void whenCompleted(boolean wasSuccess, Void result)
			{
				if (!wasSuccess)
				{
					StickyDialog dialog = new StickyDialog(state.getServiceName(), resultText);
					dialog.showOk(BeneficiaryActivity.this, REFRESH_BENEFITS_LIST_FAILED, null);
					return;
				}

				refreshListView(state.getQuotas());

			}

		};
		getMembersTask.execute();
	}

	private void refreshListView(ServiceQuota[] quotas)
	{
		List<ServiceQuota> listItems = new ArrayList<ServiceQuota>();

		if (quotas != null)
		{
			for (ServiceQuota quota : quotas)
			{
				listItems.add(quota);
			}
		}

		final StableArrayAdapter adapter = new StableArrayAdapter(this, android.R.layout.simple_list_item_1, listItems);
		listView.setAdapter(adapter);

	}

	private class StableArrayAdapter extends ArrayAdapter<ServiceQuota>
	{
		private final Context context;
		private final List<ServiceQuota> values;

		public StableArrayAdapter(Context context, int textViewResourceId, List<ServiceQuota> values)
		{
			super(context, textViewResourceId, values);

			this.context = context;
			this.values = values;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			ServiceQuota entry = values.get(position);

			// Create Layout
			View rowView = convertView;
			if (rowView == null)
			{
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				ViewHolder viewHolder = new ViewHolder();
				{
					rowView = inflater.inflate(R.layout.layout_benefit, parent, false);
					viewHolder.serviceTextView = (TextView) rowView.findViewById(R.id.serviceTextView);
					viewHolder.quantityTextView = (TextView) rowView.findViewById(R.id.quantityTextView);
					viewHolder.destinationTextView = (TextView) rowView.findViewById(R.id.destinationTextView);
					viewHolder.temporalTextView = (TextView) rowView.findViewById(R.id.expiresTextView);

				}

				rowView.setTag(viewHolder);
			}

			ViewHolder holder = (ViewHolder) rowView.getTag();

			holder.serviceTextView.setText(entry.getService());
			holder.quantityTextView.setText(String.format("%d %s", entry.getQuantity(), entry.getUnits()));
			holder.destinationTextView.setText(entry.getDestination());
			holder.temporalTextView.setText(String.format("%s / %s", entry.getDaysOfWeek(), entry.getTimeOfDay()));

			return rowView;

		}

		@Override
		public int getViewTypeCount()
		{
			return 1;
		}

		@Override
		public int getItemViewType(int position)
		{
			return 0;
		}

	}

	class ViewHolder
	{
		private TextView serviceTextView;
		private TextView quantityTextView;
		private TextView destinationTextView;
		private TextView temporalTextView;
	};

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Remove Benefit
	//
	// /////////////////////////////
	private void testRemoveBenefit(final ServiceQuota quota)
	{
		SoapTask<Void, Void> removeQuotaTask = new SoapTask<Void, Void>(this, state.getServiceID())
		{

			@Override
			protected void whenCancelled()
			{
				refreshBenefitsList();
			}

			@Override
			protected Void performWork(C4SoapClient client, Void... params) throws Exception
			{
				RemoveQuotaRequest request = new RemoveQuotaRequest();
				request.setServiceID(state.getServiceID());
				request.setVariantID(state.getVariantID());
				request.setSubscriberNumber(new Number(state.getMsisdn()));
				request.setMemberNumber(new Number(memberNumber));
				request.setQuota(quota);
				request.setMode(RequestModes.testOnly);

				RemoveQuotaResponse response = client.call(request);
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
					dialog.showOk(BeneficiaryActivity.this, TEST_REMOVE_BENEFIT_FAILED, null);
					return;
				}

				String amount = Program.formatMoney(state.getCharge());
				String msg = String.format("Confirm you want to remove %s %d %s %s %s, %s, %s at a cost of %s ?",
						BeneficiaryActivity.this.name, quota.getQuantity(), quota.getUnits(), quota.getService(),
						quota.getDestination(), quota.getDaysOfWeek(), quota.getTimeOfDay(), amount);

				StickyDialog dlg = new StickyDialog(state.getServiceName(), msg);
				dlg.showOkCancel(BeneficiaryActivity.this, TEST_REMOVE_BENEFIT, quota);

			}

		};
		removeQuotaTask.execute();

	}

	private void removeBenefit(final ServiceQuota quota)
	{
		SoapTask<Void, Void> removeQuotaTask = new SoapTask<Void, Void>(this, state.getServiceID())
		{

			@Override
			protected void whenCancelled()
			{
				refreshBenefitsList();
			}

			@Override
			protected Void performWork(C4SoapClient client, Void... params) throws Exception
			{
				RemoveQuotaRequest request = new RemoveQuotaRequest();
				request.setServiceID(state.getServiceID());
				request.setVariantID(state.getVariantID());
				request.setSubscriberNumber(new Number(state.getMsisdn()));
				request.setMemberNumber(new Number(memberNumber));
				request.setQuota(quota);

				RemoveQuotaResponse response = client.call(request);
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
					dialog.showOk(BeneficiaryActivity.this, REMOVE_BENEFIT_FAILED, null);
					return;
				}

				String amount = Program.formatMoney(state.getCharge());
				String msg = String.format("You have removed %s %d %s %s %s, %s, %s at a cost of %s",
						BeneficiaryActivity.this.name, quota.getQuantity(), quota.getUnits(), quota.getService(),
						quota.getDestination(), quota.getDaysOfWeek(), quota.getTimeOfDay(), amount);

				StickyDialog dialog = new StickyDialog(state.getServiceName(), msg);
				dialog.showOk(BeneficiaryActivity.this, REMOVE_BENEFIT, null);

			}

		};
		removeQuotaTask.execute();

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// On Activity Result
	//
	// /////////////////////////////

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if (data != null)
		{
			state = (SharingState) data.getSerializableExtra(SharingState.SHARE_STATE);

			if (state.getMustExit())
			{
				finish();
				return;
			}
		}

		refreshBenefitsList();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IYesNo
	//
	// /////////////////////////////

	@Override
	public void onPositive(String tag, Object asyncState)
	{
		if (tag.equals(TEST_REMOVE_BENEFIT))
			removeBenefit((ServiceQuota) asyncState);
	}

	@Override
	public void onNegative(String tag, Object asyncState)
	{
		if (tag.equals(TEST_REMOVE_BENEFIT))
			refreshBenefitsList();
	}

	@Override
	public void onAny(String tag, Object asyncState)
	{
		if (tag.equals(REFRESH_BENEFITS_LIST_FAILED))
			finish();
		else if (tag.equals(TEST_REMOVE_BENEFIT_FAILED))
			refreshBenefitsList();
		else if (tag.equals(REMOVE_BENEFIT_FAILED))
			refreshBenefitsList();
		else if (tag.equals(REMOVE_BENEFIT))
			refreshBenefitsList();
	}

}
