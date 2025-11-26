package com.concurrent.credittransfer;

import hxc.servicebus.RequestModes;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

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
import com.concurrent.util.C4SoapClient;
import com.concurrent.util.SwipeDismissListViewTouchListener;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
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

public class RecipientsActivity extends Activity implements IYesNo
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private TransferState state;
	private ListView listView;

	private Number[] selectedMembers;
	private ContactInfo[] selectedContactInfo;

	private static final int RESULT_REQUEST_CODE = 17;

	private static final String REFRESH_RECIPIENTS_LIST_FAILED = "REFRESH FAILED";

	private static final String REMOVE_RECIPIENT_FAILED = "REMOVE RECIPIENT FAILED";
	private static final String ON_CONFIRM_REMOVAL = "ON CONFIRM REMOVAL";
	private static final String PERFORM_RENEWAL_FAILED = "PERFORM RENEWAL FAILED";
	private static final String PERFORM_RENEWAL = "PERFORM RENEWAL";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Creation
	//
	// /////////////////////////////////
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recipients_act);

		// Enable Back on the Action Bar
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// Get State
		Intent intent = this.getIntent();
		state = TransferState.get(intent);

		// Set Title
		setTitle(state.getServiceName());

		listView = (ListView) findViewById(R.id.recipientsListView);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int position, long id)
			{
				ListEntry entry = (ListEntry) parent.getItemAtPosition(position);
				Intent intent = new Intent(RecipientsActivity.this, RecipientActivity.class);
				state.setSelectedNumber(entry.number);
				state.setSelectedName(entry.name);
				state.put(intent);
				startActivityForResult(intent, RESULT_REQUEST_CODE);
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
							ListEntry entry = (ListEntry) listView.getAdapter().getItem(reverseSortedPositions[0]);
							removeRecipient(entry.name, entry.number);
						}
					}

				});
		listView.setOnTouchListener(touchListener);

		refreshRecipientsList();

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	// /////////////////////////////////
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.recipients, menu);
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
	// Refresh
	//
	// /////////////////////////////////
	private void refreshRecipientsList()
	{
		SoapTask<Void, Void> getMembersTask = new SoapTask<Void, Void>(this, state.getServiceID())
		{
			@Override
			protected void whenCancelled()
			{
				RecipientsActivity.this.finish();
			}

			@Override
			protected Void performWork(C4SoapClient client, Void... params) throws Exception
			{
				GetMembersRequest request1 = new GetMembersRequest();
				request1.setServiceID(state.getServiceID());
				request1.setVariantID(state.getSubscribedVariantID());
				request1.setSubscriberNumber(new Number(state.getMsisdn()));
				request1.setMode(RequestModes.testOnly);

				GetMembersResponse response1 = client.call(request1);
				returnCode = response1.getReturnCode();
				if (response1.wasSuccess())
				{
					state.setCharge(response1.getChargeLevied());
					selectedMembers = response1.getMembers();
					selectedContactInfo = response1.getContactInfo();
				}
				return null;
			}

			@Override
			protected void whenCompleted(boolean wasSuccess, Void result)
			{
				if (!wasSuccess)
				{
					StickyDialog dialog = new StickyDialog(state.getServiceName(), resultText);
					dialog.showOk(RecipientsActivity.this, REFRESH_RECIPIENTS_LIST_FAILED, null);
					return;
				}

				refreshListView();

			}

		};
		getMembersTask.execute();
	}

	private void refreshListView()
	{
		List<ListEntry> listItems = new ArrayList<ListEntry>();

		if (selectedMembers != null)
		{
			for (int index = 0; index < selectedMembers.length; index++)
			{
				Number member = selectedMembers[index];
				ListEntry item = new ListEntry();
				item.number = member.getAddressDigits();
				if (selectedContactInfo != null && index < selectedContactInfo.length)
					item.name = selectedContactInfo[index].getName();
				if (item.name == null || item.name.length() == 0)
					item.name = RecipientsActivity.getContactDisplayNameByNumber(this, item.number);
				listItems.add(item);
			}
		}

		final StableArrayAdapter adapter = new StableArrayAdapter(this, android.R.layout.simple_list_item_1, listItems);
		listView.setAdapter(adapter);

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// List View
	//
	// /////////////////////////////////
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
					rowView = inflater.inflate(R.layout.layout_recipient, parent, false);
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
	// Remove Recipient
	//
	// /////////////////////////////

	private void removeRecipient(final String name, final String number)
	{
		SoapTask<Void, Void> rateTask = new SoapTask<Void, Void>(this, state.getServiceID())
		{

			@Override
			protected void whenCancelled()
			{
				refreshRecipientsList();
			}

			@Override
			protected Void performWork(C4SoapClient client, Void... params) throws Exception
			{
				RemoveMemberRequest request = new RemoveMemberRequest();
				request.setServiceID(state.getServiceID());
				request.setVariantID(state.getSubscribedVariantID());
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
					dialog.showOk(RecipientsActivity.this, REMOVE_RECIPIENT_FAILED, null);
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
		String msg = String.format("Confirm you want to remove %s as a recipient at a cost of %s ?", name, amount);

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
				refreshRecipientsList();
			}

			@Override
			protected Void performWork(C4SoapClient client, Void... params) throws Exception
			{
				RemoveMemberRequest request = new RemoveMemberRequest();
				request.setServiceID(state.getServiceID());
				request.setVariantID(state.getSubscribedVariantID());
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
					dialog.showOk(RecipientsActivity.this, PERFORM_RENEWAL_FAILED, null);
					return;
				}

				String amount = Program.formatMoney(state.getCharge());
				String msg = String.format("%s has been removed as an recipient at a cost of %s", name, amount);

				StickyDialog dlg = new StickyDialog(state.getServiceName(), msg);
				dlg.showOk(RecipientsActivity.this, PERFORM_RENEWAL, null);

			}

		};
		removeTask.execute();

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// On Add
	//
	// /////////////////////////////////
	public void onAddRecipient(View view)
	{
		Intent intent = new Intent(this, AddRecipientActivity.class);
		state.setSelectedName(null);
		state.setSelectedNumber(null);
		state.put(intent);
		startActivityForResult(intent, RESULT_REQUEST_CODE);
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
			refreshRecipientsList();
	}

	@Override
	public void onAny(String tag, Object asyncState)
	{
		if (tag.equals(REFRESH_RECIPIENTS_LIST_FAILED))
			finish();
		else if (tag.equals(REMOVE_RECIPIENT_FAILED))
			refreshRecipientsList();
		else if (tag.equals(PERFORM_RENEWAL_FAILED))
			refreshRecipientsList();
		else if (tag.equals(PERFORM_RENEWAL))
			refreshRecipientsList();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get Contact Display Name by Number
	//
	// /////////////////////////////
	public static String getContactDisplayNameByNumber(Context context, String number)
	{
		Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
		String name = number;

		ContentResolver contentResolver = context.getContentResolver();
		Cursor contactLookup = contentResolver.query(uri, new String[] { BaseColumns._ID,
				ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);
		try
		{
			if (contactLookup != null)
			{
				int count = contactLookup.getCount();
				if (count > 0 && count <= 2)
				{
					contactLookup.moveToNext();
					name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
				}
			}
		}
		finally
		{
			if (contactLookup != null)
			{
				contactLookup.close();
			}
		}

		return name;
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
			state = TransferState.get(data);
		}

		refreshRecipientsList();
	}

}
