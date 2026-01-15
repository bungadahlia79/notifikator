package com.geekz.forwarder;

import android.app.*;
import android.content.*;
import android.util.*;
import java.io.*;
import java.net.*;

public class HttpTransportService extends IntentService
{
	public final static String EXTRA_URL = "com.geekz.forwarder.http.URL";
	public final static String EXTRA_AUTH = "com.geekz.forwarder.http.AUTH";
	public final static String EXTRA_USERNAME = "com.geekz.forwarder.http.USERNAME";
	public final static String EXTRA_PASSWORD = "com.geekz.forwarder.http.PASSWORD";
	public final static String EXTRA_PAYLOAD_TYPE = "com.geekz.forwarder.http.PAYLOAD_TYPE";
	public final static String EXTRA_PAYLOAD = "com.geekz.forwarder.http.PAYLOAD";

	public HttpTransportService()
	{
		super("Geekz Forwarder HTTP Transport Service");
	}

	protected void onHandleIntent(Intent intent)
	{
		final String endpointUrl = intent.getStringExtra(EXTRA_URL);
		final boolean endpointAuth = intent.getBooleanExtra(EXTRA_AUTH, false);
		final String endpointUsername = intent.getStringExtra(EXTRA_USERNAME);
		final String endpointPassword = intent.getStringExtra(EXTRA_PASSWORD);
		final String contentType = intent.getStringExtra(EXTRA_PAYLOAD_TYPE);
		final byte[] postData = intent.getByteArrayExtra(EXTRA_PAYLOAD);

		Log.d("Geekz Forwarder", "HttpTransportService triggered");
		
		// Validate URL
		if (endpointUrl == null || endpointUrl.isEmpty())
		{
			Log.e("Geekz Forwarder", "Cannot send notification: URL is null or empty");
			return;
		}
		
		// Validate payload
		if (postData == null || postData.length == 0)
		{
			Log.e("Geekz Forwarder", "Cannot send notification: payload is null or empty");
			return;
		}
		
		Log.d("Geekz Forwarder", String.format("Sending POST to %s with content-type %s and payload size %d bytes", 
			endpointUrl, contentType, postData.length));

		try
		{
			URL url = new URL(endpointUrl);
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();

			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", contentType);

			if (endpointAuth && endpointUsername != null && endpointPassword != null)
			{
				connection.setRequestProperty("Authorization",
					String.format("Basic %s",
						Base64.encodeToString(
							String.format("%s:%s", endpointUsername, endpointPassword).getBytes(),
							Base64.NO_WRAP)));
				Log.d("Geekz Forwarder", "Using HTTP Basic Authentication");
			}

			connection.setConnectTimeout(2500);
			connection.setReadTimeout(5000);
			connection.setFixedLengthStreamingMode(postData.length);

			connection.setUseCaches(false);
			connection.setDoInput(false);
			connection.setDoOutput(true);

			try (OutputStream ostrm = connection.getOutputStream())
			{
				ostrm.write(postData);
				ostrm.flush();
			}

			int responseCode = connection.getResponseCode();
			Log.i("Geekz Forwarder", String.format("HTTP POST completed with response code: %d", responseCode));
			
			if (responseCode >= 200 && responseCode < 300)
			{
				Log.i("Geekz Forwarder", "Notification sent successfully");
			}
			else
			{
				Log.w("Geekz Forwarder", String.format("HTTP POST returned non-success response: %d %s", 
					responseCode, connection.getResponseMessage()));
			}
			
			connection.disconnect();
		}
		catch (MalformedURLException e)
		{
			Log.e("Geekz Forwarder", String.format("Invalid URL format: %s - %s", endpointUrl, e.toString()));
		}
		catch (java.net.SocketTimeoutException e)
		{
			Log.e("Geekz Forwarder", String.format("Connection timeout to %s: %s", endpointUrl, e.toString()));
		}
		catch (java.net.UnknownHostException e)
		{
			Log.e("Geekz Forwarder", String.format("Unknown host %s: %s", endpointUrl, e.toString()));
		}
		catch (IOException e)
		{
			Log.e("Geekz Forwarder", String.format("IO error during HTTP POST to %s: %s", endpointUrl, e.toString()));
		}
		catch (Exception e)
		{
			Log.e("Geekz Forwarder", String.format("Unexpected error during HTTP POST to %s: %s", endpointUrl, e.toString()));
		}
	}
}
