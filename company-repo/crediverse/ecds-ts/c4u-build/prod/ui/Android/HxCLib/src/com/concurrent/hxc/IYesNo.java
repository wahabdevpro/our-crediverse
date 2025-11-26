package com.concurrent.hxc;

import android.app.FragmentManager;

public interface IYesNo
{
	public abstract void onPositive(String tag, Object asyncState);
	public abstract void onNegative(String tag, Object asyncState);
	public abstract void onAny(String tag, Object asyncState);
	
	public abstract FragmentManager getFragmentManager();
}