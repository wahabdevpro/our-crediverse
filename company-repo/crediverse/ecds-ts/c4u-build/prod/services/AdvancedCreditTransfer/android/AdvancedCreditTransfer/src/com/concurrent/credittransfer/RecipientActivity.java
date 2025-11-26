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
import com.concurrent.soap.CreditTransfer;
import com.concurrent.soap.GetCreditTransfersRequest;
import com.concurrent.soap.GetCreditTransfersResponse;
import com.concurrent.soap.Number;
import com.concurrent.soap.RemoveCreditTransfersRequest;
import com.concurrent.soap.RemoveCreditTransfersResponse;
import com.concurrent.util.C4SoapClient;
import com.concurrent.util.SwipeDismissListViewTouchListener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

public class RecipientActivity extends Activity implements IYesNo
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private TransferState state;
	private ListView listView;

	private CreditTransfer[] selectedTransfers;

	private static final String TEST_REMOVE_TRANSFER = "testRemoveTransfer";
	private static final String REFRESH_TRANSFERS_LIST_FAILED = "refreshTransfersListFailed";
	private static final String TEST_REMOVE_TRANSFER_FAILED = "testRemoveTransferFailed";
	private static final String REMOVE_TRANSFER = "removeTransfer";
	private static final String REMOVE_TRANSFER_FAILED = "removeTransferFailed";
	
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
		setContentView(R.layout.activity_recipient_act);

		// Enable Back on the Action Bar
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// Get State
		Intent intent = this.getIntent();
		state = TransferState.get(intent);

		// Set Title
		setTitle(state.getSelectedName());

		listView = (ListView) findViewById(R.id.transfersListView);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int position, long id)
			{
//				CreditTransfer entry = (CreditTransfer) parent.getItemAtPosition(position);
//				Intent intent = new Intent(RecipientActivity.this, AdjustTransferActivity.class);
//				state.put(intent);
//				startActivityForResult(intent, RESULT_REQUEST_CODE);
				
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
							CreditTransfer entry = (CreditTransfer) listView.getAdapter().getItem(
									reverseSortedPositions[0]);
							testRemoveTransfer(entry);
						}
					}
				});
		listView.setOnTouchListener(touchListener);

		refreshTransfersList();

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Menu
	//
	// /////////////////////////////////
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.recipient, menu);
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
	// On Add Transfer
	//
	// /////////////////////////////
	public void onAddTransfer(View view)
	{
		Intent intent = new Intent(this, AddRecipientActivity.class);
		state.put(intent);
		startActivityForResult(intent, 0);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// List View
	//
	// /////////////////////////////
	private void refreshTransfersList()
	{
		SoapTask<Void, Void> getMembersTask = new SoapTask<Void, Void>(this, state.getServiceID())
		{
			@Override
			protected void whenCancelled()
			{
				RecipientActivity.this.finish();
			}

			@Override
			protected Void performWork(C4SoapClient client, Void... params) throws Exception
			{
				GetCreditTransfersRequest request = new GetCreditTransfersRequest();
				request.setServiceID(state.getServiceID());
				request.setVariantID(state.getSubscribedVariantID());
				request.setSubscriberNumber(new Number(state.getMsisdn()));
				request.setMemberNumber(new Number(state.getSelectedNumber()));
				request.setActiveOnly(true);

				GetCreditTransfersResponse response = client.call(request);
				returnCode = response.getReturnCode();
				if (response.wasSuccess())
				{
					state.setCharge(response.getChargeLevied());
					selectedTransfers = response.getTransfers();

				}
				return null;
			}

			@Override
			protected void whenCompleted(boolean wasSuccess, Void result)
			{
				if (!wasSuccess)
				{
					StickyDialog dialog = new StickyDialog(state.getServiceName(), resultText);
					dialog.showOk(RecipientActivity.this, REFRESH_TRANSFERS_LIST_FAILED, null);
					return;
				}

				refreshListView();

			}

		};
		getMembersTask.execute();
	}

	private void refreshListView()
	{
		List<CreditTransfer> listItems = new ArrayList<CreditTransfer>();

		if (selectedTransfers != null)
		{
			for (CreditTransfer transfer : selectedTransfers)
			{
				listItems.add(transfer);
			}
		}

		final StableArrayAdapter adapter = new StableArrayAdapter(this, android.R.layout.simple_list_item_1, listItems);
		listView.setAdapter(adapter);

	}

	private class StableArrayAdapter extends ArrayAdapter<CreditTransfer>
	{
		private final Context context;
		private final List<CreditTransfer> values;

		public StableArrayAdapter(Context context, int textViewResourceId, List<CreditTransfer> values)
		{
			super(context, textViewResourceId, values);

			this.context = context;
			this.values = values;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			CreditTransfer entry = values.get(position);

			// Create Layout
			View rowView = convertView;
			if (rowView == null)
			{
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				ViewHolder viewHolder = new ViewHolder();
				{
					rowView = inflater.inflate(R.layout.layout_transfer, parent, false);
					viewHolder.nameTextView = (TextView) rowView.findViewById(R.id.nameTextView);
					viewHolder.quantitiesTextView = (TextView) rowView.findViewById(R.id.quantitiesTextView);
				}

				rowView.setTag(viewHolder);
			}

			ViewHolder holder = (ViewHolder) rowView.getTag();

			holder.nameTextView.setText(entry.getName());
			holder.quantitiesTextView
					.setText(String.format("Auto Transfer %d %s.", entry.getAmount(), entry.getUnits()));

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
		private TextView nameTextView;
		private TextView quantitiesTextView;
	};

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Remove Transfer
	//
	// /////////////////////////////
	private void testRemoveTransfer(final CreditTransfer transfer)
	{
		SoapTask<Void, Void> task = new SoapTask<Void, Void>(this, state.getServiceID())
		{

			@Override
			protected void whenCancelled()
			{
				refreshTransfersList();
			}

			@Override
			protected Void performWork(C4SoapClient client, Void... params) throws Exception
			{
				RemoveCreditTransfersRequest request = new RemoveCreditTransfersRequest();
				request.setServiceID(state.getServiceID());
				request.setVariantID(state.getSubscribedVariantID());
				request.setSubscriberNumber(new Number(state.getMsisdn()));
				request.setMemberNumber(new Number(state.getSelectedNumber()));
				request.setTransferMode(transfer.getTransferModeID());
				request.setMode(RequestModes.testOnly);

				RemoveCreditTransfersResponse response = client.call(request);
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
					dialog.showOk(RecipientActivity.this, TEST_REMOVE_TRANSFER_FAILED, null);
					return;
				}

				String amount = Program.formatMoney(state.getCharge());
				String msg = String.format("Confirm you want to remove the Transfer at a cost of %s ?", amount);

				StickyDialog dlg = new StickyDialog(state.getServiceName(), msg);
				dlg.showOkCancel(RecipientActivity.this, TEST_REMOVE_TRANSFER, transfer);

			}

		};
		task.execute();

	}

	private void removeTransfer(final CreditTransfer transfer)
	{
		SoapTask<Void, Void> removeTransferTask = new SoapTask<Void, Void>(this, state.getServiceID())
		{

			@Override
			protected void whenCancelled()
			{
				refreshTransfersList();
			}

			@Override
			protected Void performWork(C4SoapClient client, Void... params) throws Exception
			{
				RemoveCreditTransfersRequest request = new RemoveCreditTransfersRequest();
				request.setServiceID(state.getServiceID());
				request.setVariantID(state.getSubscribedVariantID());
				request.setSubscriberNumber(new Number(state.getMsisdn()));
				request.setMemberNumber(new Number(state.getSelectedNumber()));
				request.setTransferMode(transfer.getTransferModeID());

				RemoveCreditTransfersResponse response = client.call(request);
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
					dialog.showOk(RecipientActivity.this, REMOVE_TRANSFER_FAILED, null);
					return;
				}

				long money = state.getCharge();
				String amount = Program.formatMoney(money);
				String msg = String.format("You have removed the Transfer at a  cost of %s", amount);

				StickyDialog dialog = new StickyDialog(state.getServiceName(), msg);
				dialog.showOk(RecipientActivity.this, REMOVE_TRANSFER, null);

			}

		};
		removeTransferTask.execute();

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

		refreshTransfersList();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IYesNo
	//
	// /////////////////////////////

	@Override
	public void onPositive(String tag, Object asyncState)
	{
		if (tag.equals(TEST_REMOVE_TRANSFER))
			removeTransfer((CreditTransfer) asyncState);
	}

	@Override
	public void onNegative(String tag, Object asyncState)
	{
		if (tag.equals(TEST_REMOVE_TRANSFER))
			refreshTransfersList();
	}

	@Override
	public void onAny(String tag, Object asyncState)
	{
		if (tag.equals(REFRESH_TRANSFERS_LIST_FAILED))
			finish();
		else if (tag.equals(TEST_REMOVE_TRANSFER_FAILED))
			refreshTransfersList();
		else if (tag.equals(REMOVE_TRANSFER_FAILED))
			refreshTransfersList();
		else if (tag.equals(REMOVE_TRANSFER))
			refreshTransfersList();
	}

}
