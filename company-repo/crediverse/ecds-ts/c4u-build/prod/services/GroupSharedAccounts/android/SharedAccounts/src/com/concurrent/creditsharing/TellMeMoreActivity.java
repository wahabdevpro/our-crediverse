package com.concurrent.creditsharing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class TellMeMoreActivity extends Activity
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////

	public static final String TITLE = "title";
	public static final String CONTENT = "content";
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Creation
	//
	// /////////////////////////////
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tell_me_more);
		
		// Enable Back on the Action Bar
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		Intent intent = getIntent();
		int titleId = intent.getIntExtra(TITLE, R.string.title_tell_me_more_1);
		int contentId = intent.getIntExtra(CONTENT, R.string.content_tell_me_more_1);

		setTitle(titleId);
		TextView contentTextView = (TextView)findViewById(R.id.contentTextView);
		contentTextView.setText(contentId);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tell_me_more, menu);
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
	// Start
	//
	// /////////////////////////////
	public static void start(Context context, int title, int content)
	{
		Intent intent = new Intent(context, TellMeMoreActivity.class);
		intent.putExtra(TellMeMoreActivity.TITLE, title);
		intent.putExtra(TellMeMoreActivity.CONTENT, content);
		context.startActivity(intent);		
	}
}
