package com.geekz.forwarder;

import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.util.*;
import java.io.*;

final class BitmapHelper
{
	public final static Bitmap getPackageIcon(Context context, String packageName, int id)
	{
		if (id == 0)
			return null;

		try
		{
			Context remoteContext = context.createPackageContext(packageName, 0);
			Drawable icon = remoteContext.getResources().getDrawable(id);
			if (icon == null)
				return null;

			return ((BitmapDrawable)icon).getBitmap();
		}
		catch (Exception ex)
		{
			return null;
		}
	}

	public final static Bitmap getIconBitmap(Context context, Object iconObject)
	{
		if (iconObject == null)
			return null;

		try
		{
			// Handle Bitmap directly (API < 23)
			if (iconObject instanceof Bitmap)
			{
				return (Bitmap)iconObject;
			}
			
			// Handle Icon object (API 23+)
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && iconObject instanceof android.graphics.drawable.Icon)
			{
				android.graphics.drawable.Icon icon = (android.graphics.drawable.Icon)iconObject;
				Drawable drawable = icon.loadDrawable(context);
				if (drawable instanceof BitmapDrawable)
				{
					return ((BitmapDrawable)drawable).getBitmap();
				}
				else if (drawable != null)
				{
					// Convert drawable to bitmap
					Bitmap bitmap = Bitmap.createBitmap(
						drawable.getIntrinsicWidth() > 0 ? drawable.getIntrinsicWidth() : 1,
						drawable.getIntrinsicHeight() > 0 ? drawable.getIntrinsicHeight() : 1,
						Bitmap.Config.ARGB_8888
					);
					Canvas canvas = new Canvas(bitmap);
					drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
					drawable.draw(canvas);
					return bitmap;
				}
			}
		}
		catch (Exception ex)
		{
			// Log error but don't crash
			android.util.Log.w("BitmapHelper", "Failed to extract icon bitmap", ex);
		}
		
		return null;
	}

	public final static Bitmap ensureSize(Bitmap bitmap, int maxWidth, int maxHeight)
	{
		if (bitmap.getWidth() > maxWidth || bitmap.getHeight() > maxHeight)
			return Bitmap.createScaledBitmap(bitmap, maxWidth, maxHeight, true);
		else
			return bitmap;
	}

	public final static byte[] getBytes(Bitmap bitmap)
	{
		ByteArrayOutputStream strm = null;
		try
		{
			strm = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.PNG, 0, strm);
			return strm.toByteArray();
		}
		finally
		{
			if (strm != null)
			{
				try
				{
					strm.close();
				}
				catch (IOException ex) {}
			}
		}
	}

	public final static String getBase64(Bitmap bitmap)
	{
		return Base64.encodeToString(getBytes(bitmap), Base64.NO_WRAP);
	}

	public final static String getDataUri(Bitmap bitmap)
	{
		return "data:image/png;base64," + getBase64(bitmap);
	}
}
