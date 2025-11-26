package com.concurrent.credittransfer;

import hxc.servicebus.RequestModes;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.concurrent.hxc.IYesNo;
import com.concurrent.hxc.Program;
import com.concurrent.hxc.SoapTask;
import com.concurrent.hxc.StickyDialog;
import com.concurrent.soap.AddCreditTransferRequest;
import com.concurrent.soap.AddCreditTransferResponse;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class AddRecipientActivity extends Activity implements IYesNo
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private TransferState state;

	private EditText numberEditText;
	private Spinner typeSpinner;
	private EditText amountEditText;
	private TextView unitsTextView1;
	private EditText maximumEditText;
	private TextView unitsTextView2;
	private Spinner periodSpinner;
	private EditText pinEditText;

	private CreditTransfer selectedTransfer;
	private BigDecimal selectedAmount;
	private String selectedNumber;
	private BigDecimal selectedMaximum;
	private String selectedPIN;

	private static final int REQUEST_CODE_GET_NUMBER = 23;

	private static final String INVALID_PARAMETER = "INVALID PARAMETER";
	private static final String RATE_ADDITION = "RATE ADDITION";
	private static final String PERFORM_ADDITION = "PERFORM ADDITION";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Creation
	//
	// /////////////////////////////////
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_recipient_act);

		// Enable Back on the Action Bar
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// Get State
		Intent intent = this.getIntent();
		state = TransferState.get(intent);

		// Set Title
		setTitle(state.getServiceName());

		// Get Views
		numberEditText = (EditText) findViewById(R.id.numberEditText);
		typeSpinner = (Spinner) findViewById(R.id.typeSpinner);
		amountEditText = (EditText) findViewById(R.id.amountEditText);
		unitsTextView1 = (TextView) findViewById(R.id.unitsTextView1);
		maximumEditText = (EditText) findViewById(R.id.maximumEditText);
		unitsTextView2 = (TextView) findViewById(R.id.unitsTextView2);
		periodSpinner = (Spinner) findViewById(R.id.periodSpinner);
		pinEditText = (EditText) findViewById(R.id.pinEditText);

		// Pre-Populate Number
		String number = state.getSelectedNumber();
		if (number != null && number.length() > 0)
		{
			numberEditText.setText(number);
			numberEditText.setEnabled(false);
			ImageButton button = (ImageButton) findViewById(R.id.lookupImageButton);
			button.setVisibility(ImageButton.GONE);
			String name = state.getSelectedName();
			if (name!=null && name.length() > 0)
				setTitle(name);
		}

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

		// Populate the type spinner
		{
			List<CreditTransfer> automaticTransfers = new ArrayList<CreditTransfer>();
			for (CreditTransfer transfer : state.getTransfers())
			{
				if (transfer.getTransferType() == CreditTransferType.Periodic
						|| transfer.getTransferType() == CreditTransferType.UponDepletion)
					automaticTransfers.add(transfer);
			}
			ArrayAdapter<CreditTransfer> adapter = new ArrayAdapter<CreditTransfer>(this,
					android.R.layout.simple_spinner_item, automaticTransfers);

			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			typeSpinner.setAdapter(adapter);
		}

		// Populate the periods spinner
		{
			String[] periods = new String[] { "per Month" };
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, periods);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			periodSpinner.setAdapter(adapter);
		}
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
		getMenuInflater().inflate(R.menu.add_recipient, menu);
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
		unitsTextView1.setText(item.getUnits());
		unitsTextView2.setText(item.getUnits());
		pinEditText.setVisibility(item.getRequiresPIN() ? EditText.VISIBLE : EditText.GONE);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// On Lookup Number
	//
	// /////////////////////////////////
	public void onLookupNumber(View view)
	{
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
		startActivityForResult(intent, REQUEST_CODE_GET_NUMBER);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Activity Result
	//
	// /////////////////////////////
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
	// On Add Recipient
	//
	// /////////////////////////////////
	public void onAddRecipientButton(View view)
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

		// Read Maximum
		try
		{
			String text = amountEditText.getText().toString();
			if (text == null || text.length() == 0)
				selectedMaximum = null;
			else
				selectedMaximum = new BigDecimal(text);
		}
		catch (NumberFormatException ex)
		{
			maximumEditText.requestFocus();
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

		rateAddition();
	}

	// Rate the Addition
	private void rateAddition()
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
				AddCreditTransferRequest request = new AddCreditTransferRequest();
				request.setServiceID(state.getServiceID());
				// TODO request.setVariantID(state.getSubscribedVariantID());
				request.setSubscriberNumber(new Number(state.getMsisdn()));
				request.setMemberNumber(new Number(selectedNumber));
				request.setTransferMode(selectedTransfer.getTransferModeID());
				request.setAmount(selectedAmount.longValue());
				// TODO request.setNextTransferDate();
				if (selectedMaximum != null)
					request.setTransferLimit(selectedMaximum.longValue());
				// TODO request.setTransferThreshold();
				request.setPin(selectedPIN);
				request.setMode(RequestModes.testOnly);

				AddCreditTransferResponse response = client.call(request);
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
					dialog.showOk(AddRecipientActivity.this, "ServerError", null);
					return;
				}

				String amount = Program.formatMoney(state.getCharge());
				String format = getString(R.string.format_adding);
				String msg = String.format(format, selectedAmount.longValue(), selectedTransfer.getUnits(),
						selectedNumber);

				StickyDialog dlg = new StickyDialog(state.getServiceName(), msg);
				dlg.showYesNo(AddRecipientActivity.this, RATE_ADDITION, null);
			}

		};
		task.execute();
	}

	// Perform the Addition
	private void performAddition()
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
				AddCreditTransferRequest request = new AddCreditTransferRequest();
				request.setServiceID(state.getServiceID());
				// TODO request.setVariantID(state.getSubscribedVariantID());
				request.setSubscriberNumber(new Number(state.getMsisdn()));
				request.setMemberNumber(new Number(selectedNumber));
				request.setTransferMode(selectedTransfer.getTransferModeID());
				request.setAmount(selectedAmount.longValue());
				// TODO request.setNextTransferDate();
				if (selectedMaximum != null)
					request.setTransferLimit(selectedMaximum.longValue());
				// TODO request.setTransferThreshold();
				request.setPin(selectedPIN);

				AddCreditTransferResponse response = client.call(request);
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
					dialog.showOk(AddRecipientActivity.this, "ServerError", null);
					return;
				}

				String amount = Program.formatMoney(state.getCharge());
				String format = getString(R.string.format_added);
				String msg = String.format(format, selectedAmount.longValue(), selectedTransfer.getUnits(),
						selectedNumber);

				StickyDialog dlg = new StickyDialog(state.getServiceName(), msg);
				dlg.showYes(AddRecipientActivity.this, PERFORM_ADDITION, null);
			}

		};
		task.execute();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IYesNo
	//
	// /////////////////////////////////

	@Override
	public void onPositive(String tag, Object asyncState)
	{
		if (RATE_ADDITION.equalsIgnoreCase(tag))
			performAddition();
		else if (PERFORM_ADDITION.equalsIgnoreCase(tag))
			finish();

	}

	@Override
	public void onNegative(String tag, Object asyncState)
	{

	}

	@Override
	public void onAny(String tag, Object asyncState)
	{
		if (INVALID_PARAMETER.equals(tag))
		{
		}

	}

}
