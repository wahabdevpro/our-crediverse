package com.concurrent.hxc;

import android.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

// new MyDialogFragment().show(getSupportFragmentManager(), "tag"); // or getFragmentManager() in API 11+

public final class StickyDialog extends DialogFragment
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////
	private String title;
	private String message;
	private int positive;
	private int negative = 0;
	private Object asyncState;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////
	public StickyDialog(String title, String message, Object... params)
	{
		this.title = title;
		if (message != null)
			this.message = String.format(message, params);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Show
	//
	// /////////////////////////////
	public void showOk(IYesNo activity, String tag, Object asyncState)
	{
		positive = R.string.ok;
		this.asyncState = asyncState;
		this.show(activity.getFragmentManager(), tag);
	}

	public void showOkCancel(IYesNo activity, String tag, Object asyncState)
	{
		positive = R.string.ok;
		negative = R.string.cancel;
		this.asyncState = asyncState;
		this.show(activity.getFragmentManager(), tag);
	}
	
	public void showYes(IYesNo activity, String tag, Object asyncState)
	{
		positive = R.string.yes;
		this.asyncState = asyncState;
		this.show(activity.getFragmentManager(), tag);
	}

	public void showYesNo(IYesNo activity, String tag, Object asyncState)
	{
		positive = R.string.yes;
		negative = R.string.no;
		this.asyncState = asyncState;
		this.show(activity.getFragmentManager(), tag);
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Cancellation
	//
	// /////////////////////////////

	@Override
	public void onDestroyView()
	{
		if (getDialog() != null && getRetainInstance())
			getDialog().setDismissMessage(null);
		super.onDestroyView();
	}

	@Override
	public void onCancel(DialogInterface dialog)
	{
		super.onCancel(dialog);
		((IYesNo)getActivity()).onNegative(getTag(), asyncState);
		((IYesNo)getActivity()).onAny(getTag(), asyncState);
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Creation
	//
	// /////////////////////////////

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		setRetainInstance(true);

		Builder dlg = new AlertDialog.Builder(getActivity());

		if (title != null)
			dlg = dlg.setTitle(title);

		if (message != null)
			dlg = dlg.setMessage(message);

		dlg = dlg.setPositiveButton(positive, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				((IYesNo)getActivity()).onPositive(getTag(), asyncState);
				((IYesNo)getActivity()).onAny(getTag(), asyncState);
			}
		});

		if (negative != 0)
		{
			dlg = dlg.setNegativeButton(negative, new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					((IYesNo)getActivity()).onNegative(getTag(), asyncState);
					((IYesNo)getActivity()).onAny(getTag(), asyncState);
				}
			});
		}

		return dlg.create();
	}


}
