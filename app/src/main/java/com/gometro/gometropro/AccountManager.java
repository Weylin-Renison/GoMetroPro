package com.gometro.gometropro;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class AccountManager
{
	
	final Context context;
	private SharedPreferences prefsManager	= null;
	
	final String URL_BASE = "http://54.68.55.70:9000/";//"http://192.168.10.101:9000/";	//| http://54.68.55.70:9000/
	final String API_UPLOAD = "uploadRouteTesting";
	final String API_REGISTER = "registerPhone";
	final String API_GET_AVAILABLE_PROJECTS = "getAvailableProjects";
	final String API_UPLOAD_PHOTO = "uploadPhoto";
	final String API_UPLOAD_STOP_PHOTO = "uploadStopPhoto";

	public AccountManager(Context context)
	{
		this.context = context;
	}
	
	public void setApiUrls()
	{
		prefsManager = PreferenceManager.getDefaultSharedPreferences(context);
		
		prefsManager.edit()
				.putString("URL_BASE", URL_BASE)
				.putString("API_UPLOAD", API_UPLOAD)
				.putString("API_GET_AVAILABLE_PROJECTS", API_GET_AVAILABLE_PROJECTS)
				.putString("API_UPLOAD_PHOTO", API_UPLOAD_PHOTO)
				.putString("API_UPLOAD_STOP_PHOTO", API_UPLOAD_STOP_PHOTO).commit();
	}
	
	public void signUpUser()
	{
		//Sign up user
	}
	
	public void loginUser()
	{
		//Login user
	}
	
	public void registerPhone()
	{	
		prefsManager = PreferenceManager.getDefaultSharedPreferences(context);;
		
		if(!prefsManager.getBoolean("registered", false))
		{
			//Get imei and register phone with server			
			TelephonyManager telMang = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
			String imei = telMang.getDeviceId();
			
			//Store info to shared prefs
			prefsManager.edit().putString("imei", imei).commit();
			
			//Register phone with server	
			RequestParams params = new RequestParams();
			params.put("imei", imei);	
			
			AsyncHttpClient client = new AsyncHttpClient();
			client.setTimeout(240 * 1000);
			client.setUserAgent("android");
			client.post(URL_BASE + API_REGISTER, params,		//192.168.0.29 home
					new AsyncHttpResponseHandler()
					{					
						@Override
						public void onSuccess(String response)
						{
							prefsManager.edit().putBoolean("registered", true);
						}
	
						public void onFailure(Throwable error, String content)
						{
							prefsManager.edit().putBoolean("registered", false);
							//Toast.makeText(context, "Unable to register device, please check internet connection.", Toast.LENGTH_LONG).show();
						}
					});		
		}
	}
	
	public boolean isPhoneRegistered()
	{		
		prefsManager = PreferenceManager.getDefaultSharedPreferences(context);
		return prefsManager.getBoolean("registered", false);
	}
}
