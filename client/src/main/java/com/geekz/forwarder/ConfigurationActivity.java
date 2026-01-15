package com.geekz.forwarder;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.res.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.preference.*;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class ConfigurationActivity extends PreferenceActivity
{
	private static final int PERMISSION_REQUEST_POST_NOTIFICATIONS = 1;

	@SuppressWarnings("deprecation")
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		try {
			PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
			addPreferencesFromResource(R.xml.preferences);
			
			// Request POST_NOTIFICATIONS permission for Android 13+ (API 33+)
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
				if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
					ActivityCompat.requestPermissions(this,
						new String[]{Manifest.permission.POST_NOTIFICATIONS},
						PERMISSION_REQUEST_POST_NOTIFICATIONS);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, "Error loading preferences", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == PERMISSION_REQUEST_POST_NOTIFICATIONS) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, "Notification permission denied. App may not work properly.", Toast.LENGTH_LONG).show();
			}
		}
	}

	@SuppressWarnings("deprecation")
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference)
	{
		try {
			Resources res = getResources();
			if (res.getString(R.string.key_send).equals(preference.getKey()))
			{
				sendTestNotification();
				return false;
			}
			else if (res.getString(R.string.key_selected_apps).equals(preference.getKey()))
			{
				Intent intent = new Intent(this, AppSelectionActivity.class);
				startActivity(intent);
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, "Error handling preference click", Toast.LENGTH_SHORT).show();
		}

		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	private void sendTestNotification()
	{
		try {
			NotificationManager mgr = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
			Resources res = getResources();
			
			// Create notification channel for Android O and above
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				NotificationChannel channel = new NotificationChannel(
					"test_channel",
					"Test Notifications",
					NotificationManager.IMPORTANCE_DEFAULT
				);
				mgr.createNotificationChannel(channel);
			}
			
			Notification.Builder nb;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				nb = new Notification.Builder(this, "test_channel");
			} else {
				nb = new Notification.Builder(this);
			}

			nb.setContentTitle(res.getString(R.string.notification_title));
			nb.setContentText(res.getString(R.string.notification_text));
			nb.setSmallIcon(R.drawable.mask);

			BitmapDrawable largeIconDrawable = (BitmapDrawable) res.getDrawable(R.drawable.icon);
			Bitmap largeIconBitmap = largeIconDrawable.getBitmap();
			nb.setLargeIcon(largeIconBitmap);

			mgr.notify(0, nb.build());
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, "Error sending test notification", Toast.LENGTH_SHORT).show();
		}
	}
}