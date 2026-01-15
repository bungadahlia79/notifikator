package net.kzxiv.notify.client;

import android.app.*;
import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.preference.*;
import android.widget.Toast;

public class ConfigurationActivity extends PreferenceActivity
{
	@SuppressWarnings("deprecation")
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		try {
			PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
			addPreferencesFromResource(R.xml.preferences);
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, "Error loading preferences", Toast.LENGTH_SHORT).show();
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