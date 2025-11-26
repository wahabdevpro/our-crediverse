package com.concurrent.creditsharing;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import hxc.servicebus.RequestModes;

import com.concurrent.hxc.IYesNo;
import com.concurrent.hxc.Program;
import com.concurrent.hxc.SoapTask;
import com.concurrent.hxc.StickyDialog;
import com.concurrent.soap.ContactInfo;
import com.concurrent.soap.GetMembersRequest;
import com.concurrent.soap.GetMembersResponse;
import com.concurrent.soap.Number;
import com.concurrent.soap.RemoveMemberRequest;
import com.concurrent.soap.RemoveMemberResponse;
import com.concurrent.soap.SubscribeRequest;
import com.concurrent.soap.SubscribeResponse;
import com.concurrent.soap.VasServiceInfo;
import com.concurrent.util.C4SoapClient;
import com.concurrent.util.SwipeDismissListViewTouchListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class BeneficiariesActivity extends Activity implements IYesNo
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////
	private SharingState state = null;
	private ListView listview;

	public static final String ON_CONFIRM_REMOVAL = "onConfirmRemoval";
	public static final String REFRESH_BENEFICIARIES_LIST_FAILED = "refreshBeneficiariesListFailed";
	public static final String REMOVE_BENEFICIARY_FAILED = "removeBeneficiaryFailed";
	public static final String PERFORM_RENEWAL = "performRemoval";
	public static final String PERFORM_RENEWAL_FAILED = "performRemovalFailed";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Creation
	//
	// /////////////////////////////

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_beneficiaries);
		setTitle(R.string.title_activity_beneficiaries);

		// Enable Back on the Action Bar
		getActionBar().setDisplayHomeAsUpEnabled(true);

		state = (SharingState) getIntent().getSerializableExtra(SharingState.SHARE_STATE);

		listview = (ListView) findViewById(R.id.listView1);
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int position, long id)
			{
				ListEntry entry = (ListEntry) parent.getItemAtPosition(position);
				Intent intent = new Intent(BeneficiariesActivity.this, BeneficiaryActivity.class);
				intent.putExtra(SharingState.SHARE_STATE, state);
				intent.putExtra("NAME", entry.name);
				intent.putExtra("NUMBER", entry.number);
				startActivityForResult(intent, 0);
			}

		});

		// Swipe to Dismiss
		SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(listview,
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
							ListEntry entry = (ListEntry) listView.getAdapter().getItem(reverseSortedPositions[0]);
							removeBeneficiary(entry.name, entry.number);
						}
					}

				});
		listview.setOnTouchListener(touchListener);

		refreshBeneficiariesList();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.beneficiaries, menu);
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
	// Add Beneficiary
	//
	// /////////////////////////////

	public void onAddBeneficiary(View view)
	{
		Intent intent = new Intent(this, AddBeneficiaryActivity.class);
		intent.putExtra(SharingState.SHARE_STATE, state);
		startActivityForResult(intent, 0);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// List View
	//
	// /////////////////////////////
	private void refreshBeneficiariesList()
	{
		SoapTask<Void, Void> getMembersTask = new SoapTask<Void, Void>(this, state.getServiceID())
		{
			@Override
			protected void whenCancelled()
			{
				BeneficiariesActivity.this.finish();
			}

			@Override
			protected Void performWork(C4SoapClient client, Void... params) throws Exception
			{
				GetMembersRequest request1 = new GetMembersRequest();
				request1.setServiceID(state.getServiceID());
				request1.setVariantID(state.getVariantID());
				request1.setSubscriberNumber(new Number(state.getMsisdn()));
				request1.setMode(RequestModes.testOnly);

				GetMembersResponse response1 = client.call(request1);
				returnCode = response1.getReturnCode();
				if (response1.wasSuccess())
				{
					state.setCharge(response1.getChargeLevied());
					state.setMembers(response1.getMembers());
					state.setContactInfo(response1.getContactInfo());
				}
				return null;
			}

			@Override
			protected void whenCompleted(boolean wasSuccess, Void result)
			{
				if (!wasSuccess)
				{
					StickyDialog dialog = new StickyDialog(state.getServiceName(), resultText);
					dialog.showOk(BeneficiariesActivity.this, REFRESH_BENEFICIARIES_LIST_FAILED, null);
					return;
				}

				refreshListView(state.getMembers(), state.getContactInfo());

			}

		};
		getMembersTask.execute();
	}

	private void refreshListView(Number[] members, ContactInfo[] contacts)
	{
		List<ListEntry> listItems = new ArrayList<ListEntry>();

		if (members != null)
		{
			for (int index = 0; index < members.length; index++)
			{
				Number member = members[index];
				ListEntry item = new ListEntry();
				item.number = member.getAddressDigits();
				if (contacts != null && index < contacts.length)
					item.name = contacts[index].getName();
				if (item.name == null || item.name.length() == 0)
					item.name = CreditSharingActivity.getContactDisplayNameByNumber(this, item.number);
				listItems.add(item);
			}
		}

		final StableArrayAdapter adapter = new StableArrayAdapter(this, android.R.layout.simple_list_item_1, listItems);
		listview.setAdapter(adapter);

	}

	private class StableArrayAdapter extends ArrayAdapter<ListEntry>
	{
		private final Context context;
		private final List<ListEntry> values;

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

			// Create Layout
			View rowView = convertView;
			if (rowView == null)
			{
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				ViewHolder viewHolder = new ViewHolder();
				{
					rowView = inflater.inflate(R.layout.layout_beneficiary, parent, false);
					viewHolder.nameTextView = (TextView) rowView.findViewById(R.id.benefitTextView);
					viewHolder.numberTextView = (TextView) rowView.findViewById(R.id.numberTextView);
				}

				rowView.setTag(viewHolder);
			}

			ViewHolder holder = (ViewHolder) rowView.getTag();

			holder.nameTextView.setText(entry.name);
			holder.numberTextView.setText(entry.number);

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
		public TextView nameTextView;
		public TextView numberTextView;
	};

	private class ListEntry
	{
		private String name;
		private String number;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Remove Beneficiary
	//
	// /////////////////////////////

	private void removeBeneficiary(final String name, final String number)
	{
		SoapTask<Void, Void> rateTask = new SoapTask<Void, Void>(this, state.getServiceID())
		{

			@Override
			protected void whenCancelled()
			{
				refreshBeneficiariesList();
			}

			@Override
			protected Void performWork(C4SoapClient client, Void... params) throws Exception
			{
				RemoveMemberRequest request = new RemoveMemberRequest();
				request.setServiceID(state.getServiceID());
				request.setVariantID(state.getVariantID());
				request.setSubscriberNumber(new Number(state.getMsisdn()));
				request.setMemberNumber(new Number(number));
				request.setMode(RequestModes.testOnly);

				RemoveMemberResponse response = client.call(request);
				returnCode = response.getReturnCode();
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
					dialog.showOk(BeneficiariesActivity.this, REMOVE_BENEFICIARY_FAILED, null);
					return;
				}

				confirmRemoval(name, number);
			}

		};
		rateTask.execute();
	}

	// Confirm Removal
	private void confirmRemoval(final String name, final String number)
	{
		String amount = Program.formatMoney(state.getCharge());
		String msg = String.format("Confirm you want to remove %s as a beneficiary at a cost of %s ?", name, amount);

		StickyDialog dlg = new StickyDialog(state.getServiceName(), msg);
		dlg.showYesNo(this, ON_CONFIRM_REMOVAL, name + "," + number);
	}

	private void performRemoval(final String nameNumber)
	{
		String[] parts = nameNumber.split("\\,");
		final String name = parts[0];
		final String number = parts[1];

		SoapTask<Void, Void> removeTask = new SoapTask<Void, Void>(this, state.getServiceID())
		{

			@Override
			protected void whenCancelled()
			{
				refreshBeneficiariesList();
			}

			@Override
			protected Void performWork(C4SoapClient client, Void... params) throws Exception
			{
				RemoveMemberRequest request = new RemoveMemberRequest();
				request.setServiceID(state.getServiceID());
				request.setVariantID(state.getVariantID());
				request.setSubscriberNumber(new Number(state.getMsisdn()));
				request.setMemberNumber(new Number(number));

				RemoveMemberResponse response = client.call(request);
				returnCode = response.getReturnCode();
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
					dialog.showOk(BeneficiariesActivity.this, PERFORM_RENEWAL_FAILED, null);
					return;
				}

				String amount = Program.formatMoney(state.getCharge());
				String msg = String.format("%s has been removed as an beneficiary at a cost of %s", name, amount);

				StickyDialog dlg = new StickyDialog(state.getServiceName(), msg);
				dlg.showOk(BeneficiariesActivity.this, PERFORM_RENEWAL, null);

			}

		};
		removeTask.execute();

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

		refreshBeneficiariesList();

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IYesNo
	//
	// /////////////////////////////

	@Override
	public void onPositive(String tag, Object asyncState)
	{
		if (tag.equals(ON_CONFIRM_REMOVAL))
			performRemoval((String) asyncState);

	}

	@Override
	public void onNegative(String tag, Object asyncState)
	{
		if (tag.equals(ON_CONFIRM_REMOVAL))
			refreshBeneficiariesList();
	}

	@Override
	public void onAny(String tag, Object asyncState)
	{
		if (tag.equals(REFRESH_BENEFICIARIES_LIST_FAILED))
			finish();
		else if (tag.equals(REMOVE_BENEFICIARY_FAILED))
			refreshBeneficiariesList();
		else if (tag.equals(PERFORM_RENEWAL_FAILED))
			refreshBeneficiariesList();
		else if (tag.equals(PERFORM_RENEWAL))
			refreshBeneficiariesList();
	}

}
