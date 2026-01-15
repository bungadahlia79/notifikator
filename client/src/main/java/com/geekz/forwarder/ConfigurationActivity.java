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
import android.util.Base64;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import org.json.*;
import java.io.*;
import java.nio.charset.*;

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
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			Resources res = getResources();
			
			// Check if endpoint is configured
			final String endpointUrl = prefs.getString(res.getString(R.string.key_endpointurl), null);
			if (endpointUrl == null || "".equals(endpointUrl))
			{
				Toast.makeText(this, "Please configure endpoint URL first", Toast.LENGTH_LONG).show();
				return;
			}
			
			final String protocol = prefs.getString(res.getString(R.string.key_protocol), null);
			if (protocol == null)
			{
				Toast.makeText(this, "Please select a protocol first", Toast.LENGTH_LONG).show();
				return;
			}
			
			// Create test notification with mock data
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
			
			Notification notification = nb.build();
			
			// Generate payload based on protocol
			Object[] payload = null;
			String packageName = getPackageName();
			
			if (res.getString(R.string.protocol_kodi).equals(protocol))
				payload = createPayloadKodi(notification);
			else if (res.getString(R.string.protocol_kodi_addon).equals(protocol))
				payload = createPayloadKodiAddon(notification);
			else if (res.getString(R.string.protocol_adtv).equals(protocol))
				payload = createPayloadAdtv(packageName, notification);
			else if (res.getString(R.string.protocol_json).equals(protocol))
				payload = createPayloadJson(packageName, notification);
			
			if (payload == null)
			{
				Toast.makeText(this, "Failed to create test payload", Toast.LENGTH_LONG).show();
				return;
			}
			
			// Send directly to HttpTransportService
			final boolean endpointAuth = prefs.getBoolean(res.getString(R.string.key_endpointauth), false);
			final String endpointUsername = prefs.getString(res.getString(R.string.key_endpointuser), null);
			final String endpointPassword = prefs.getString(res.getString(R.string.key_endpointpw), null);
			
			Intent i = new Intent(this, HttpTransportService.class);
			i.putExtra(HttpTransportService.EXTRA_URL, endpointUrl);
			i.putExtra(HttpTransportService.EXTRA_AUTH, endpointAuth);
			if (endpointAuth)
			{
				i.putExtra(HttpTransportService.EXTRA_USERNAME, endpointUsername);
				i.putExtra(HttpTransportService.EXTRA_PASSWORD, endpointPassword);
			}
			i.putExtra(HttpTransportService.EXTRA_PAYLOAD_TYPE, (String)payload[0]);
			i.putExtra(HttpTransportService.EXTRA_PAYLOAD, (byte[])payload[1]);
			
			startService(i);
			Toast.makeText(this, "Test notification sent", Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, "Error sending test notification: " + e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
	
	private final static int determineDisplayTime(String title, String text)
	{
		final int rawTime = ((title.length() + text.length()) * 1000) / 5;
		return Math.max(5000, rawTime);
	}

	private final Object[] createPayloadKodi(Notification notification)
	{
		final String title = notification.extras.getString(Notification.EXTRA_TITLE);
		final String text = notification.extras.getString(Notification.EXTRA_TEXT);

		if (title == null || text == null)
			return null;

		JSONObject result = new JSONObject();
		try
		{
			result.put("jsonrpc", "2.0");
			result.put("method", "GUI.ShowNotification");
			result.put("id", 0);

			JSONObject parameters = new JSONObject();
			parameters.put("title", title);
			parameters.put("message", text);
			parameters.put("displaytime", determineDisplayTime(title, text));

			result.put("params", parameters);
		}
		catch (JSONException ex) {}

		return new Object[] { "application/json", result.toString().getBytes() };
	}

	private final Object[] createPayloadKodiAddon(Notification notification)
	{
		final String title = notification.extras.getString(Notification.EXTRA_TITLE);
		final String text = notification.extras.getString(Notification.EXTRA_TEXT);

		if (title == null || text == null)
			return null;

		final Bitmap icon = (Bitmap)notification.extras.get(Notification.EXTRA_LARGE_ICON);

		JSONObject result = new JSONObject();
		try
		{
			result.put("jsonrpc", "2.0");
			result.put("method", "Addons.ExecuteAddon");
			result.put("id", 0);

			JSONObject parameters0 = new JSONObject();
			parameters0.put("addonid", "script.notifikator");

			JSONObject parameters1 = new JSONObject();
			parameters1.put("title", title);
			parameters1.put("message", text);
			parameters1.put("image", icon == null ? "" : BitmapHelper.getBase64(BitmapHelper.ensureSize(icon, 75, 75)));
			parameters1.put("displaytime", Integer.toString(determineDisplayTime(title, text)));

			parameters0.put("params", parameters1);
			result.put("params", parameters0);
		}
		catch (JSONException ex) {}

		return new Object[] { "application/json", result.toString().getBytes() };
	}

	private final Object[] createPayloadAdtv(String packageName, Notification notification)
	{
		final String title = notification.extras.getString(Notification.EXTRA_TITLE);
		final String text = notification.extras.getString(Notification.EXTRA_TEXT);

		if (title == null || text == null)
			return null;

		Bitmap icon = (Bitmap)notification.extras.get(Notification.EXTRA_LARGE_ICON);

		if (icon == null)
			icon = ((BitmapDrawable) getResources().getDrawable(R.drawable.icon)).getBitmap();

		final Integer zero = Integer.valueOf(0);
		final Object[] body = new Object[]
		{
			"type", zero,
			"title", title,
			"msg", text,
			"duration", Integer.valueOf(determineDisplayTime(title, text) / 1000),
			"fontsize", zero,
			"position", zero,
			"width", zero,
			"bkgcolor", "#000000",
			"transparency", zero,
			"offset", zero,
			"offsety", zero,
			"app", packageName,
			"force", Boolean.valueOf(true),
			"filename", BitmapHelper.getBytes(icon)
		};

		final String separator = HttpHelper.generateMultipartSeparator();
		final Charset charset = Charset.forName("UTF-8");

		byte[] result;
		try
		{
			result = HttpHelper.generateMultipartBody(separator, body, charset);
		}
		catch (IOException ex)
		{
			return null;
		}

		return new Object[] { "multipart/form-data; boundary=" + separator, result };
	}

	private final Object[] createPayloadJson(String packageName, Notification notification)
	{
		final String title = notification.extras.getString(Notification.EXTRA_TITLE);
		final String text = notification.extras.getString(Notification.EXTRA_TEXT);
		final Bitmap iconSm = BitmapHelper.getPackageIcon(this, packageName, notification.extras.getInt(Notification.EXTRA_SMALL_ICON));
		final Bitmap iconLg = (Bitmap)notification.extras.get(Notification.EXTRA_LARGE_ICON);

		JSONObject result = new JSONObject();
		try
		{
			if (title != null)
				result.put("title", title);
			if (packageName != null)
				result.put("package", packageName);
			if (text != null || iconSm != null || iconLg != null)
			{
				JSONObject options = new JSONObject();

				if (text != null)
					options.put("body", text);
				if (iconSm != null)
					options.put("badge", BitmapHelper.getDataUri(BitmapHelper.ensureSize(iconSm, 72, 72)));
				if (iconLg != null)
					options.put("icon", BitmapHelper.getDataUri(BitmapHelper.ensureSize(iconLg, 192, 192)));

				result.put("options", options);
			}
		}
		catch (JSONException ex) {}

		return new Object[] { "application/json", result.toString().getBytes() };
	}
}