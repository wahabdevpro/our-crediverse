package com.concurrent.creditsharing;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.concurrent.hxc.IYesNo;
import com.concurrent.hxc.SoapTask;
import com.concurrent.hxc.StickyDialog;
import com.concurrent.soap.GetBalancesRequest;
import com.concurrent.soap.GetBalancesResponse;
import com.concurrent.soap.Number;
import com.concurrent.soap.ServiceBalance;
import com.concurrent.util.C4SoapClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class BeneficiaryBalancesActivity extends Activity implements IYesNo
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
		setContentView(R.layout.activity_beneficiary_balances);
		setTitle(R.string.title_activity_beneficiary_balances);
		
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
		// getMenuInflater().inflate(R.menu.beneficiary_balances, menu);
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
		final List<ServiceBalance> entries = new ArrayList<ServiceBalance>();
		SoapTask<Void, Void> getBalancesTask = new SoapTask<Void, Void>(this, state.getServiceID())
		{
			@Override
			protected void whenCancelled()
			{
				BeneficiaryBalancesActivity.this.finish();
			}

			@Override
			protected Void performWork(C4SoapClient client, Void... params) throws Exception
			{
				GetBalancesRequest request = new GetBalancesRequest();
				request.setServiceID(state.getServiceID());
				request.setVariantID(state.getVariantID());
				request.setSubscriberNumber(new Number(state.getMsisdn()));
				request.setRequestSMS(false);

				GetBalancesResponse response = client.call(request);
				this.returnCode = response.getReturnCode();

				if (response.wasSuccess())
				{
					for (ServiceBalance balance : response.getBalances())
					{
						entries.add(balance);
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
					dialog.showOk(BeneficiaryBalancesActivity.this, REFRESH_BALANCES_FAILED, null);
					return;
				}

				final StableArrayAdapter adapter = new StableArrayAdapter(BeneficiaryBalancesActivity.this,
						android.R.layout.simple_list_item_1, entries);
				listView.setAdapter(adapter);
			}

		};
		getBalancesTask.execute();

	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// List View
	//
	// /////////////////////////////
	private class StableArrayAdapter extends ArrayAdapter<ServiceBalance>
	{
		private final Context context;
		private final List<ServiceBalance> values;
		private final SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy HH:mm");

		public StableArrayAdapter(Context context, int textViewResourceId, List<ServiceBalance> values)
		{
			super(context, textViewResourceId, values);

			this.context = context;
			this.values = values;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			ServiceBalance entry = values.get(position);
			boolean isHeader = getItemViewType(position) == 0;

			// Create Layout
			View rowView = convertView;
			if (rowView == null)
			{
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				ViewHolder viewHolder = new ViewHolder();

				{
					rowView = inflater.inflate(R.layout.layout_balance, parent, false);
					viewHolder.serviceTextView = (TextView) rowView.findViewById(R.id.serviceTextView);
					viewHolder.quantityTextView = (TextView) rowView.findViewById(R.id.quantityTextView);
					viewHolder.expiresTextView = (TextView) rowView.findViewById(R.id.expiresTextView);
				}

				rowView.setTag(viewHolder);
			}

			ViewHolder holder = (ViewHolder) rowView.getTag();

			{
				holder.serviceTextView.setText(entry.getName());
				holder.quantityTextView.setText(String.format("%d %s", entry.getValue(), entry.getUnit()));
				holder.expiresTextView.setText(String.format("Expires: %s", sdf.format(entry.getExpiryDate())));
			}

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

	private class ViewHolder
	{
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
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// On Tell Me More
	//
	// /////////////////////////////
	public void onTellMeMore(View view)
	{
		TellMeMoreActivity.start(this,  R.string.title_tell_me_more_4, R.string.content_tell_me_more_4);
	}


	
}
