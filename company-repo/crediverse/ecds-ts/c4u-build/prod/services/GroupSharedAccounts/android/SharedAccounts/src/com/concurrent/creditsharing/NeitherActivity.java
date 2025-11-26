package com.concurrent.creditsharing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class NeitherActivity extends Activity
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private SharingState state;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Creation and Options
	//
	// /////////////////////////////////

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_neither);
		setTitle(R.string.app_name);

		// Enable Back on the Action Bar
		getActionBar().setDisplayHomeAsUpEnabled(true);

		state = (SharingState) getIntent().getSerializableExtra(SharingState.SHARE_STATE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.neither, menu);
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
	// Event Handlers
	//
	// /////////////////////////////////
	public void onNext(View view)
	{
		Intent intent = new Intent(this, SubscribeActivity.class);
		intent.putExtra(SharingState.SHARE_STATE, state);
		startActivityForResult(intent, 0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		NeitherActivity.this.setResult(resultCode, data);
		finish();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// On Back Pressed
	//
	// /////////////////////////////

	@Override
	public void onBackPressed()
	{
		state.setMustExit(true);
		Intent intent = new Intent();
		intent.putExtra(SharingState.SHARE_STATE, state);
		setResult(Activity.RESULT_OK, intent);
		finish();

		super.onBackPressed();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// On Tell Me More
	//
	// /////////////////////////////
	public void onTellMeMore(View view)
	{
		TellMeMoreActivity.start(this, R.string.title_tell_me_more_1, R.string.content_tell_me_more_1);
	}

}
