package net.kzxiv.notify.client;

import android.app.ListActivity;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
		
		apps = new ArrayList<AppInfo>();
		for (ApplicationInfo info : packages)
		{
			// Skip system apps that don't have launcher icons
			if ((info.flags & ApplicationInfo.FLAG_SYSTEM) != 0)
			{
				if (pm.getLaunchIntentForPackage(info.packageName) == null)
					continue;
			}
			
			String appName = pm.getApplicationLabel(info).toString();
			apps.add(new AppInfo(info.packageName, appName, info));
		}
		
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
