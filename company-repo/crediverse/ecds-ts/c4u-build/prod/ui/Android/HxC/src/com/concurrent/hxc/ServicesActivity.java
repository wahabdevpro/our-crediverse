package com.concurrent.hxc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.concurrent.creditsharing.CreditSharingActivity;
import com.concurrent.soap.GetServicesResponse;
import com.concurrent.soap.SubscriptionState;
import com.concurrent.soap.VasServiceInfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ServicesActivity extends Activity
{
	private StableArrayAdapter adapter;
	private ListView listview;
	
	private static final int REQUEST_CODE = 17;

	protected Map<String, Class<?>> serviceMap = new HashMap<String, Class<?>>();

	public ServicesActivity()
	{
		super();
		serviceMap.put("langch", com.concurrent.languagechange.LanguageChangeActivity.class);
		serviceMap.put("gsa", com.concurrent.creditsharing.CreditSharingActivity.class);
		serviceMap.put("act", com.concurrent.credittransfer.CreditTransferActivity.class);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_services);

		GetServicesResponse response = (GetServicesResponse) getIntent()
				.getSerializableExtra(GetServicesResponse.class.getSimpleName());

		List<VasServiceInfo> list;
		{
			Map<String, VasServiceInfo> map = new HashMap<String, VasServiceInfo>();
			list = new ArrayList<VasServiceInfo>();

			for (VasServiceInfo service : response.getServiceInfo())
			{
				VasServiceInfo entry = map.get(service.getServiceID());
				if (entry == null || service.getState().equals(SubscriptionState.active))
				{
					entry = service;
					map.put(entry.getServiceID(), entry);
					list.add(entry);
				}

				if (service.getState() == SubscriptionState.active)
					entry.setState(SubscriptionState.active);
			}

			Collections.sort(list, new Comparator<VasServiceInfo>()
			{
				@Override
				public int compare(VasServiceInfo lhs, VasServiceInfo rhs)
				{
					if (lhs.getState() == SubscriptionState.active
							&& rhs.getState() != SubscriptionState.active)
						return -1;
					else if (lhs.getState() != SubscriptionState.active
							&& rhs.getState() == SubscriptionState.active)
						return +1;

					return lhs.getServiceName().compareTo(rhs.getServiceName());
				}
			});

		}

		listview = (ListView) findViewById(R.id.servicesListView);
		adapter = new StableArrayAdapter(this,
				android.R.layout.simple_list_item_1, list);
		listview.setAdapter(adapter);

		listview.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int position, long id)
			{
				VasServiceInfo entry = (VasServiceInfo) parent
						.getItemAtPosition(position);
				Class<?> klass = serviceMap.get(entry.getServiceID()
						.toLowerCase());
				if (klass != null)
				{
					Intent intent = new Intent(ServicesActivity.this, klass);
					intent.putExtra("VASINFO", entry); //TODO Magic
					startActivityForResult(intent, REQUEST_CODE);
					return;
				}

				new AlertDialog.Builder(ServicesActivity.this)
						.setTitle(entry.getServiceName())
						.setMessage(
								"This Service can unfortunately not be managed on the Phone Application. Please call Customer Care on 123 for assistance.")
						.setPositiveButton("OK", new OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
					
							}
						})
						.setIcon(android.R.drawable.ic_dialog_alert).show();
			}

		});

		listview.setStackFromBottom(false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.services, menu);
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

	private class StableArrayAdapter extends ArrayAdapter<VasServiceInfo>
	{
		private final Context context;
		private final List<VasServiceInfo> values;

		public StableArrayAdapter(Context context, int textViewResourceId,
				List<VasServiceInfo> values)
		{
			super(context, textViewResourceId, values);

			this.context = context;
			this.values = values;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			VasServiceInfo entry = values.get(position);

			// Create Layout
			View rowView = convertView;
			if (rowView == null)
			{
				LayoutInflater inflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				ViewHolder viewHolder = new ViewHolder();
				if (entry.getState() == SubscriptionState.active)
				{
					rowView = inflater.inflate(R.layout.layout_services,
							parent, false);
					viewHolder.icon = (ImageView) rowView
							.findViewById(R.id.iconImageView);
					viewHolder.name = (TextView) rowView
							.findViewById(R.id.serviceTextView);
					viewHolder.variant = (TextView) rowView
							.findViewById(R.id.variantTextView);
				}
				else
				{
					rowView = inflater.inflate(R.layout.layout_services,
							parent, false);
					viewHolder.icon = (ImageView) rowView
							.findViewById(R.id.iconImageView);
					viewHolder.name = (TextView) rowView
							.findViewById(R.id.serviceTextView);
					viewHolder.variant = (TextView) rowView
							.findViewById(R.id.variantTextView);
				}
				rowView.setTag(viewHolder);
			}

			ViewHolder holder = (ViewHolder) rowView.getTag();

			switch (entry.getState())
			{
				case active:
				{
					holder.name.setText(entry.getServiceName());
					holder.variant.setText(entry.getVariantName());
					holder.icon.setImageResource(R.drawable.subscribed);
				}
					break;

				default:
				{
					holder.name.setText(entry.getServiceName());
					holder.variant.setText("");
					holder.icon.setImageResource(R.drawable.manage);
				}
					break;

			}
			return rowView;

		}

		@Override
		public int getViewTypeCount()
		{
			return 2;
		}

		@Override
		public int getItemViewType(int position)
		{
			return values.get(0).getState() == SubscriptionState.active ? 0 : 1;
		}

		@Override
		public boolean isEnabled(int position)
		{
			return true;
		}

	}

	class ViewHolder
	{
		public ImageView icon;
		public TextView name;
		public TextView variant;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == REQUEST_CODE)
			finish();
	};

}
