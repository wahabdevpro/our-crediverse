package com.concurrent.creditsharing;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.concurrent.creditsharing.BeneficiariesActivity.ViewHolder;
import com.concurrent.hxc.IYesNo;
import com.concurrent.hxc.SoapTask;
import com.concurrent.hxc.StickyDialog;
import com.concurrent.soap.GetBalancesRequest;
import com.concurrent.soap.GetBalancesResponse;
import com.concurrent.soap.GetMembersRequest;
import com.concurrent.soap.GetMembersResponse;
import com.concurrent.soap.Number;
import com.concurrent.soap.ServiceBalance;
import com.concurrent.util.C4SoapClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class BalancesActivity extends Activity implements IYesNo
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////
	private SharingState state = null;
	private ListView listView;

	private static final String REFRESH_BALANCES_FAILED = "refreshBalancesFailed";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Creation
	//
	// /////////////////////////////
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_balances);
		setTitle(R.string.title_activity_balances);
		
		// Enable Back on the Action Bar
		getActionBar().setDisplayHomeAsUpEnabled(true);

		state = (SharingState) getIntent().getSerializableExtra(SharingState.SHARE_STATE);

		listView = (ListView) findViewById(R.id.listView1);

		refreshBalances();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.balances, menu);
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
	// Refresh Balances
	//
	// /////////////////////////////
	private void refreshBalances()
	{
		final List<ListEntry> entries = new ArrayList<ListEntry>();

		SoapTask<Void, Void> balancesTask = new SoapTask<Void, Void>(this, state.getServiceID())
		{
			@Override
			protected void whenCancelled()
			{
				BalancesActivity.this.finish();
			}

			@Override
			protected Void performWork(C4SoapClient client, Void... params) throws Exception
			{
				ListEntry entry = new ListEntry();
				entry.person = "Yourself";
				entries.add(entry);

				GetBalancesRequest request = new GetBalancesRequest();
				request.setServiceID(state.getServiceID());
				request.setVariantID(state.getVariantID());
				request.setSubscriberNumber(new Number(state.getMsisdn()));
				request.setRequestSMS(false);

				GetBalancesResponse response = client.call(request);
				this.returnCode = response.getReturnCode();

				if (response.wasSuccess())
				{
					ServiceBalance[] balances = response.getBalances();
					if (balances != null)
					{
						for (ServiceBalance balance : balances)
						{
							entry = new ListEntry();
							entry.name = balance.getName();
							entry.value = balance.getValue();
							entry.unit = balance.getUnit();
							entry.expiryDate = balance.getExpiryDate();
							entries.add(entry);
						}
					}

					GetMembersRequest request2 = new GetMembersRequest();
					request2.setServiceID(state.getServiceID());
					request2.setVariantID(state.getVariantID());
					request2.setSubscriberNumber(new Number(state.getMsisdn()));

					GetMembersResponse response2 = client.call(request2);
					this.returnCode = response.getReturnCode();

					if (response2.wasSuccess())
					{
						Number[] members = response2.getMembers();
						if (members != null)
						{
							for (Number member : members)
							{
								entry = new ListEntry();
								entry.person = CreditSharingActivity.getContactDisplayNameByNumber(
										BalancesActivity.this, member.getAddressDigits());
								entries.add(entry);

								request.setSubscriberNumber(member);
								response = client.call(request);
								this.returnCode = response.getReturnCode();

								if (!response.wasSuccess())
									break;
								else
								{
									balances = response.getBalances();
									if (balances != null)
									{
										for (ServiceBalance balance : balances)
										{
											entry = new ListEntry();
											entry.name = balance.getName();
											entry.value = balance.getValue();
											entry.unit = balance.getUnit();
											entry.expiryDate = balance.getExpiryDate();
											entries.add(entry);
										}
									}
								}

							}
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
					dialog.showOk(BalancesActivity.this, REFRESH_BALANCES_FAILED, null);
					return;
				}

				final StableArrayAdapter adapter = new StableArrayAdapter(BalancesActivity.this,
						android.R.layout.simple_list_item_1, entries);
				listView.setAdapter(adapter);

			}

		};
		balancesTask.execute();
	}

	private class StableArrayAdapter extends ArrayAdapter<ListEntry>
	{
		private final Context context;
		private final List<ListEntry> values;
		private final SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy HH:mm");

		public StableArrayAdapter(Context context, int textViewResourceId, List<ListEntry> values)
		{
			super(context, textViewResourceId, values);

			this.context = context;
			this.values = values;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			ListEntry entry = values.get(position);
			boolean isHeader = getItemViewType(position) == 0;

			// Create Layout
			View rowView = convertView;
			if (rowView == null)
			{
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				ViewHolder viewHolder = new ViewHolder();

				if (isHeader)
				{
					rowView = inflater.inflate(R.layout.layout_person, parent, false);
					viewHolder.benefitTextView = (TextView) rowView.findViewById(R.id.benefitTextView);
				}
				else
				{
					rowView = inflater.inflate(R.layout.layout_balance, parent, false);
					viewHolder.serviceTextView = (TextView) rowView.findViewById(R.id.serviceTextView);
					viewHolder.quantityTextView = (TextView) rowView.findViewById(R.id.quantityTextView);
					viewHolder.expiresTextView = (TextView) rowView.findViewById(R.id.expiresTextView);
				}

				rowView.setTag(viewHolder);
			}

			ViewHolder holder = (ViewHolder) rowView.getTag();

			if (isHeader)
			{
				holder.benefitTextView.setText(entry.person);
			}
			else
			{
				holder.serviceTextView.setText(entry.name);
				holder.quantityTextView.setText(String.format("%d %s", entry.value, entry.unit));
				holder.expiresTextView.setText(String.format("Expires: %s", sdf.format(entry.expiryDate)));
			}

			return rowView;

		}

		@Override
		public int getViewTypeCount()
		{
			return 2;
		}

		@Override
		public int getItemViewType(int position)
		{
			ListEntry entry = values.get(position);
			return entry.person != null ? 0 : 1;
		}

	}

	private class ListEntry
	{
		private String person = null;
		private String name;
		private long value;
		private String unit;
		private Date expiryDate;
	}

	private class ViewHolder
	{
		private TextView benefitTextView;
		private TextView serviceTextView;
		private TextView quantityTextView;
		private TextView expiresTextView;
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
		if (tag.equals(REFRESH_BALANCES_FAILED))
			finish();
	}

}
