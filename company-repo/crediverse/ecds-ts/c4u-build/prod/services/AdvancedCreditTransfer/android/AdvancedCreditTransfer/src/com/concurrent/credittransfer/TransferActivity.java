package com.concurrent.credittransfer;

import hxc.servicebus.RequestModes;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import com.concurrent.credittransfer.R;
import com.concurrent.hxc.IYesNo;
import com.concurrent.hxc.Program;
import com.concurrent.hxc.SoapTask;
import com.concurrent.hxc.StickyDialog;
import com.concurrent.soap.CreditTransfer;
import com.concurrent.soap.CreditTransferType;
import com.concurrent.soap.Number;
import com.concurrent.soap.TransferRequest;
import com.concurrent.soap.TransferResponse;
import com.concurrent.util.C4SoapClient;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class TransferActivity extends Activity implements IYesNo
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private TransferState state;

	private Spinner typeSpinner;
	private EditText amountEditText;
	private TextView unitsTextView;
	private EditText numberEditText;
	private EditText pinEditText;

	private CreditTransfer selectedTransfer;
	private BigDecimal selectedAmount;
	private String selectedNumber;
	private String selectedPIN;

	private static final int REQUEST_CODE_GET_NUMBER = 19;

	private static final String INVALID_PARAMETER = "INVALID PARAMETER";
	private static final String RATE_TRANSFER = "RATE TRANSFER";
	private static final String PERFORM_TRANSFER = "PERFORM TRANSFER";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Creation
	//
	// /////////////////////////////////

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_transfer_act);

		// Enable Back on the Action Bar
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// Get State
		Intent intent = this.getIntent();
		state = TransferState.get(intent);

		// Set Title
		setTitle(state.getServiceName());

		// Get Views
		typeSpinner = (Spinner) findViewById(R.id.typeSpinner);
		amountEditText = (EditText) findViewById(R.id.amountEditText);
		unitsTextView = (TextView) findViewById(R.id.unitsTextView);
		numberEditText = (EditText) findViewById(R.id.numberEditText);
		pinEditText = (EditText) findViewById(R.id.pinEditText);

		// Set Type Spinner Listener
		typeSpinner.setOnItemSelectedListener(new OnItemSelectedListener()
		{

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				updateViews((CreditTransfer) typeSpinner.getAdapter().getItem(position));
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{
			}

		});

		// Populate the spinner
		List<CreditTransfer> onceOffTransfers = new ArrayList<CreditTransfer>();
		for (CreditTransfer transfer : state.getTransfers())
		{
			if (transfer.getTransferType() == CreditTransferType.OnceOff)
				onceOffTransfers.add(transfer);
		}
		ArrayAdapter<CreditTransfer> adapter = new ArrayAdapter<CreditTransfer>(this,
				android.R.layout.simple_spinner_item, onceOffTransfers)
		{

		};
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		typeSpinner.setAdapter(adapter);

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
		getMenuInflater().inflate(R.menu.transfer, menu);
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
	// View Update
	//
	// /////////////////////////////////

	protected void updateViews(CreditTransfer item)
	{
		selectedTransfer = item;
		unitsTextView.setText(item.getUnits());
		pinEditText.setVisibility(item.getRequiresPIN() ? EditText.VISIBLE : EditText.GONE);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Contact Lookup
	//
	// /////////////////////////////////
	public void onLookupContact(View view)
	{

		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
		startActivityForResult(intent, REQUEST_CODE_GET_NUMBER);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch (requestCode)
		{
			case REQUEST_CODE_GET_NUMBER:
				if (resultCode == Activity.RESULT_OK)
				{

					if (data != null)
					{
						Uri uri = data.getData();

						if (uri != null)
						{
							Cursor c = null;
							try
							{
								c = getContentResolver().query(
										uri,
										new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER,
												ContactsContract.CommonDataKinds.Phone.TYPE }, null, null, null);

								if (c != null && c.moveToFirst())
								{
									String number = c.getString(0);
									numberEditText.setText(number);
								}
							}
							finally
							{
								if (c != null)
								{
									c.close();
								}
							}
						}
					}

				}
				break;

			default:
				finish();
				break;
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Transfer
	//
	// /////////////////////////////
	public void onTransfer(View view)
	{
		// Read Amount
		try
		{
			selectedAmount = new BigDecimal(amountEditText.getText().toString());
		}
		catch (NumberFormatException ex)
		{
			amountEditText.requestFocus();
			StickyDialog dialog = new StickyDialog(state.getServiceName(), getString(R.string.error_amount));
			dialog.showOk(this, INVALID_PARAMETER, null);
			return;
		}

		// Read Number
		selectedNumber = numberEditText.getText().toString();
		if (selectedNumber == null || selectedNumber.length() < 3)
		{
			numberEditText.requestFocus();
			StickyDialog dialog = new StickyDialog(state.getServiceName(), getString(R.string.error_number));
			dialog.showOk(this, INVALID_PARAMETER, null);
			return;
		}

		// Read PIN
		selectedPIN = null;
		if (selectedTransfer.getRequiresPIN())
		{
			selectedPIN = pinEditText.getText().toString();
			if (selectedPIN == null || selectedPIN.length() == 0)
			{
				pinEditText.requestFocus();
				StickyDialog dialog = new StickyDialog(state.getServiceName(), getString(R.string.error_pin));
				dialog.showOk(this, INVALID_PARAMETER, null);
				return;
			}
		}

		rateTransfer();
	}

	// Rate the Transfer
	private void rateTransfer()
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
				TransferRequest request = new TransferRequest();
				request.setServiceID(state.getServiceID());
				request.setVariantID(state.getSubscribedVariantID());
				request.setSubscriberNumber(new Number(state.getMsisdn()));
				request.setRecipientNumber(new Number(selectedNumber));
				request.setTransferModeID(selectedTransfer.getTransferModeID());
				request.setAmount(selectedAmount.longValue());
				request.setPin(selectedPIN);
				request.setVariantID(selectedTransfer.getTransferModeID()); // TODO!
				request.setMode(RequestModes.testOnly);

				TransferResponse response = client.call(request);
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
					dialog.showOk(TransferActivity.this, "ServerError", null);
					return;
				}

				String amount = Program.formatMoney(state.getCharge());
				String format = getString(R.string.format_transferring);
				String msg = String.format(format, selectedAmount.longValue(), selectedTransfer.getUnits(),
						selectedNumber);

				StickyDialog dlg = new StickyDialog(state.getServiceName(), msg);
				dlg.showYesNo(TransferActivity.this, RATE_TRANSFER, null);
			}

		};
		task.execute();
	}

	// Perform the Transfer
	private void performTransfer()
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
				TransferRequest request = new TransferRequest();
				request.setServiceID(state.getServiceID());
				request.setVariantID(state.getSubscribedVariantID());
				request.setSubscriberNumber(new Number(state.getMsisdn()));
				request.setRecipientNumber(new Number(selectedNumber));
				request.setTransferModeID(selectedTransfer.getTransferModeID());
				request.setAmount(selectedAmount.longValue());
				request.setPin(selectedPIN);
				request.setVariantID(selectedTransfer.getTransferModeID()); // TODO!

				TransferResponse response = client.call(request);
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
					dialog.showOk(TransferActivity.this, "ServerError", null);
					return;
				}

				String amount = Program.formatMoney(state.getCharge());
				String format = getString(R.string.format_transferred);
				String msg = String.format(format, selectedAmount.longValue(), selectedTransfer.getUnits(),
						selectedNumber);

				StickyDialog dlg = new StickyDialog(state.getServiceName(), msg);
				dlg.showYes(TransferActivity.this, PERFORM_TRANSFER, null);
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
		if (RATE_TRANSFER.equalsIgnoreCase(tag))
			performTransfer();
		else if (PERFORM_TRANSFER.equalsIgnoreCase(tag))
			finish();
	}

	@Override
	public void onNegative(String tag, Object asyncState)
	{
	}

	@Override
	public void onAny(String tag, Object asyncState)
	{
		if (tag.equals(INVALID_PARAMETER))
		{
			// Too bad
		}

	}

}
