package com.concurrent.hxc;

import hxc.servicebus.ReturnCodes;

import com.concurrent.soap.GetReturnCodeTextRequest;
import com.concurrent.soap.GetReturnCodeTextResponse;
import com.concurrent.soap.ResponseHeader;
import com.concurrent.util.C4SoapClient;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;

public abstract class SoapTask<P, R> extends AsyncTask<P, Void, R>
{
	private ProgressDialog progress = null;
	private Context context;
	private String serviceID;
	protected ReturnCodes returnCode = ReturnCodes.success;
	protected String resultText = null;

	public SoapTask(Context context, String serviceID)
	{
		this.context = context;
		this.serviceID = serviceID;
	}

	@Override
	protected void onPreExecute()
	{
		progress = ProgressDialog.show(context, null, "Loading...");

		progress.setCancelable(true);
		progress.setOnCancelListener(new OnCancelListener()
		{
			@Override
			public void onCancel(DialogInterface dialog)
			{
				cancel(true);
				whenCancelled();
			}
		});
	}

	protected abstract void whenCancelled();

	@Override
	protected final R doInBackground(P... params)
	{
		C4SoapClient soap = new C4SoapClient(Program.getUsername(), Program.getPassword());

		try
		{
			R result = performWork(soap, params);
			if (ResponseHeader.wasSuccess(returnCode))
				return result;
		}
		catch (Exception e)
		{
			returnCode = ReturnCodes.technicalProblem;
		}

		if (resultText != null)
			return null;
		resultText = "Technical Problem";

		try
		{
			GetReturnCodeTextRequest req = new GetReturnCodeTextRequest();
			req.setServiceID(serviceID);
			req.setReturnCode(returnCode);
			GetReturnCodeTextResponse response = soap.call(req);
			if (response.wasSuccess())
				resultText = response.getReturnCodeText();

		}
		catch (Exception e)
		{
		}

		return null;
	}

	protected abstract R performWork(C4SoapClient client, P... params) throws Exception;

	@Override
	protected final void onPostExecute(R result)
	{
		super.onPostExecute(result);
		progress.dismiss();
		whenCompleted(ResponseHeader.wasSuccess(returnCode), result);
	}

	protected abstract void whenCompleted(boolean wasSuccess, R result);

}
