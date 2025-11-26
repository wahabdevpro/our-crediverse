package com.concurrent.hxc;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class SettingsActivity extends Activity
{
	private EditText hostEditText;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
	

		Program program = Program.getInstance();
		hostEditText = (EditText) findViewById(R.id.hostEditText);
		hostEditText.setText(program.getHostAddress()); 
	}

	public void OnOkButton(View view)
	{
		Program program = Program.getInstance();
		program.setHostAddress(hostEditText.getText().toString());
		finish();
	}

	public void OnCancelButton(View view)
	{
		finish();
	}

}
