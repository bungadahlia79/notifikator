package com.geekz.forwarder;

import android.app.ListActivity;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppSelectionActivity extends ListActivity
{
	private static final String TAG = "AppSelectionActivity";
	private List<AppInfo> apps;
	private Set<String> selectedApps;
	private SharedPreferences prefs;
	
	static class AppInfo
	{
		String packageName;
		String appName;
		ApplicationInfo info;
		
		AppInfo(String packageName, String appName, ApplicationInfo info)
		{
			this.packageName = packageName;
			this.appName = appName;
			this.info = info;
		}
	}
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_selection);
		
		try {
			prefs = PreferenceManager.getDefaultSharedPreferences(this);
			Set<String> savedApps = prefs.getStringSet(getString(R.string.key_selected_apps), null);
			selectedApps = savedApps != null ? new HashSet<String>(savedApps) : new HashSet<String>();
			
			loadApps();
			
			AppAdapter adapter = new AppAdapter();
			setListAdapter(adapter);
		} catch (Exception e) {
			// Handle any exceptions gracefully
			e.printStackTrace();
			selectedApps = new HashSet<String>();
			apps = new ArrayList<AppInfo>();
			AppAdapter adapter = new AppAdapter();
			setListAdapter(adapter);
		}
	}
	
	protected void onPause()
	{
		super.onPause();
		// Save selected apps
		try {
			SharedPreferences.Editor editor = prefs.edit();
			editor.putStringSet(getString(R.string.key_selected_apps), selectedApps);
			editor.apply();
		} catch (Exception e) {
			// Handle save errors gracefully
			e.printStackTrace();
		}
	}
	
	private void loadApps()
	{
		PackageManager pm = getPackageManager();
		List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
		
		Log.d(TAG, "Total packages found: " + packages.size());
		
		apps = new ArrayList<AppInfo>();
		int systemAppsFiltered = 0;
		int appsIncluded = 0;
		
		for (ApplicationInfo info : packages)
		{
			// More inclusive filtering logic:
			// Include apps that are either:
			// 1. User-installed apps (not system apps), OR
			// 2. System apps that have been updated by the user (FLAG_UPDATED_SYSTEM_APP)
			// This ensures we catch apps like Dana, Gopay, Instagram that might be pre-installed
			boolean isUserApp = (info.flags & ApplicationInfo.FLAG_SYSTEM) == 0;
			boolean isUpdatedSystemApp = (info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;
			
			if (isUserApp || isUpdatedSystemApp)
			{
				String appName = pm.getApplicationLabel(info).toString();
				apps.add(new AppInfo(info.packageName, appName, info));
				appsIncluded++;
			}
			else
			{
				systemAppsFiltered++;
			}
		}
		
		Log.d(TAG, "Apps included: " + appsIncluded + ", System apps filtered: " + systemAppsFiltered);
		
		// Sort by app name
		Collections.sort(apps, new Comparator<AppInfo>()
		{
			public int compare(AppInfo a, AppInfo b)
			{
				return a.appName.compareToIgnoreCase(b.appName);
			}
		});
	}
	
	private class AppAdapter extends ArrayAdapter<AppInfo>
	{
		AppAdapter()
		{
			super(AppSelectionActivity.this, R.layout.app_selection_item, apps);
		}
		
		public View getView(final int position, View convertView, ViewGroup parent)
		{
			View view = convertView;
			if (view == null)
			{
				view = getLayoutInflater().inflate(R.layout.app_selection_item, parent, false);
			}
			
			final AppInfo app = apps.get(position);
			
			TextView textView = (TextView) view.findViewById(android.R.id.text1);
			textView.setText(app.appName);
			
			CheckBox checkBox = (CheckBox) view.findViewById(android.R.id.checkbox);
			checkBox.setChecked(selectedApps.contains(app.packageName));
			
			view.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v)
				{
					CheckBox cb = (CheckBox) v.findViewById(android.R.id.checkbox);
					boolean newState = !cb.isChecked();
					cb.setChecked(newState);
					
					if (newState)
						selectedApps.add(app.packageName);
					else
						selectedApps.remove(app.packageName);
					
					notifyDataSetChanged();
				}
			});
			
			return view;
		}
	}
}
