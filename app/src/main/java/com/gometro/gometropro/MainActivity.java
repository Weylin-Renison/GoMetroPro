package com.gometro.gometropro;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.stream.Stream;

public class MainActivity extends AppCompatActivity
{
	private final String LOG_TAG = "MainActivity";
	private final int PERM_REQ_READ_PHONE_STATE = 1;
	private final int REQ_PERMISSION_FINE_LOC = 100;

	private AccountManager accMang;
	private final MainActivity activity = this;

	//Views
	private RelativeLayout relLayPromptUserId;
	private LinearLayout linLayRootActionBtns;
	private EditText etxtIdNumber;
	private TextView txtBtnStartSession;
	private ProgressBar progbStartSession;

	//Required to bind to service, allows access to service methodsd
	public ServiceConnection myServiceConnection = new ServiceConnection()
	{

		@Override
		public void onServiceDisconnected(ComponentName name)
		{
			GpsGlobalVars.getInstance().isServiceConnected = false;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service)
		{
			GpsCaptureService.GpsLocalBinder binder = (GpsCaptureService.GpsLocalBinder) service;
			GpsGlobalVars.getInstance().GpsService = binder.getService();
			GpsGlobalVars.getInstance().isServiceConnected = true;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		relLayPromptUserId = (RelativeLayout) findViewById(R.id.relLayPromptUserID);
		linLayRootActionBtns = (LinearLayout) findViewById(R.id.linLayRootActionBtns);
		etxtIdNumber = (EditText) findViewById(R.id.etxtIdNumber);
		txtBtnStartSession = (TextView) findViewById(R.id.txtBtnStartSession);
		progbStartSession = (ProgressBar) findViewById(R.id.progbStartSession);

		GpsGlobalVars.getInstance().myServiceConnection = this.myServiceConnection;
		GpsGlobalVars.getInstance().mainActivity = this;

		//Check if phone is registered if not attempt to register
		accMang = new AccountManager(this);
		
		if(!accMang.isPhoneRegistered())
		{
			if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE)
					!= PackageManager.PERMISSION_GRANTED)
			{

				// Should we show an explanation?
				if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_PHONE_STATE))
				{

					// Show an expanation to the user *asynchronously* -- don't block
					// this thread waiting for the user's response! After the user
					// sees the explanation, try again to request the permission.
					//TODO: Provide explanation
					ActivityCompat.requestPermissions(this,	new String[]{Manifest.permission.READ_PHONE_STATE},	PERM_REQ_READ_PHONE_STATE);
				} else
				{

					// No explanation needed, we can request the permission.

					ActivityCompat.requestPermissions(this,	new String[]{Manifest.permission.READ_PHONE_STATE},	PERM_REQ_READ_PHONE_STATE);

					// MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
					// app-defined int constant. The callback method gets the
					// result of the request.
				}
			}
		}

		//Check Perm for GPS
		//Check for run time permissions
		// Here, thisActivity is the current activity
		if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
		{

			// Should we show an explanation?
			if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION))
			{

				// Show an expanation to the user *asynchronously* -- don't block
				// this thread waiting for the user's response! After the user
				// sees the explanation, try again to request the permission.
				final Activity activity = this;
				android.support.v7.app.AlertDialog.Builder diagBuilder = new android.support.v7.app.AlertDialog.Builder(activity);

				diagBuilder.setTitle("Permission");
				diagBuilder.setMessage(R.string.perm_ex_access_fine_location);
				diagBuilder.setPositiveButton("GOT IT", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialogInterface, int i)
					{
						ActivityCompat.requestPermissions(activity,	new String[]{Manifest.permission.ACCESS_FINE_LOCATION},	REQ_PERMISSION_FINE_LOC);
					}
				});

				diagBuilder.show();

			}
			else
			{

				// No explanation needed, we can request the permission.

				ActivityCompat.requestPermissions(this,	new String[]{Manifest.permission.ACCESS_FINE_LOCATION},	REQ_PERMISSION_FINE_LOC);

				// MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
				// app-defined int constant. The callback method gets the
				// result of the request.
			}
		}
		else
		{
			//We have permission continue as planned
			connectGpsService();
		}
		
		//Set api access urls
		accMang.setApiUrls();
		
		//Hide status notification bar
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	private void connectGpsService()
	{
//		GpsService.captureActivity = thisActivity;
		//GpsService Setup
		Intent gpsServiceIntent = new Intent(this, GpsCaptureService.class);

		getApplicationContext().bindService(gpsServiceIntent, GpsGlobalVars.getInstance().myServiceConnection, Context.BIND_AUTO_CREATE);
	}

	public void disconnectGpsService()
	{
		if(GpsGlobalVars.getInstance().isServiceConnected)
			getApplicationContext().unbindService(GpsGlobalVars.getInstance().myServiceConnection);
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		//unbinds service if app is quit through backpress
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			disconnectGpsService();
		}

		return super.onKeyDown(keyCode, event);
	}

	public void onClickStartSession(View view)
	{
		String idNumber = etxtIdNumber.getText().toString().trim();

		if(validteId(idNumber))
		{
			etxtIdNumber.setVisibility(View.INVISIBLE);
			txtBtnStartSession.setVisibility(View.INVISIBLE);

			progbStartSession.setVisibility(View.VISIBLE);

			//Make API Request

			//Success Response
			relLayPromptUserId.setVisibility(View.GONE);

			//animate menu btns into frame
			Animation animSlideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down_bounce);
			linLayRootActionBtns.setVisibility(View.VISIBLE);
			linLayRootActionBtns.startAnimation(animSlideDown);
		}
	}

	private boolean validteId(String idNumberString)
	{
		boolean valid = true;

		//Empty check
		if(idNumberString.isEmpty())
		{
			valid = false;
			etxtIdNumber.setError(getString(R.string.error_msg_cannot_be_empty));
		}
		else if(idNumberString.length() != 13) //Length check
		{
			valid = false;
			etxtIdNumber.setError(getString(R.string.error_msg_id_number_to_short));
		}
		else
		{
			//Prep values for validation
			//Month check
			int mnthValue = Integer.parseInt(idNumberString.substring(2,4));
			Log.d(LOG_TAG, "Month value: " + mnthValue);
			//Day check
			int dayValue = Integer.parseInt(idNumberString.substring(4,6));
			Log.d(LOG_TAG, "Day value: " + dayValue);
			//Check citizenship digit
			int citizenDigit = Integer.parseInt(idNumberString.substring(10,11));
			Log.d(LOG_TAG, "Citizen digit: " + citizenDigit);
			//calculate control digit
			int controlDigit = Integer.parseInt(idNumberString.substring(12));
			Log.d(LOG_TAG, "Control digit: " + controlDigit);

			if(mnthValue < 1 || mnthValue > 12) //month check
			{
				valid = false;
				etxtIdNumber.setError(getString(R.string.error_msg_invalid_id_number));
				Log.e(LOG_TAG, "ID number month lower than 1 or higher than 12");
			}
			else if(dayValue < 1 || dayValue > 31) //day check
			{
				valid = false;
				etxtIdNumber.setError(getString(R.string.error_msg_invalid_id_number));
				Log.e(LOG_TAG, "ID number day value less than 0 or greater than 31");
			}
			else if(citizenDigit != 0 && citizenDigit != 1) //citizen digit check
			{
				valid = false;
				etxtIdNumber.setError(getString(R.string.error_msg_invalid_id_number));
				Log.e(LOG_TAG, "ID number citizen digit not 0 or 1");
			}
			else	//check digit check
			{
				//Split id into int array
				char[] idChars = idNumberString.toCharArray();
				int[] idNumber = new int[idChars.length];

				for(int i = 0; i < idChars.length; i++)
					idNumber[i] = Character.getNumericValue(idChars[i]);

				Log.d(LOG_TAG, "idNumber digit list: " + idNumberString.toLowerCase());

				//calculate comparison check digit

				//Sum all digit in odd position except last digit (check digit)
				int oddSum = idNumber[0] + idNumber[2] + idNumber[4] + idNumber[6] + idNumber[8] + idNumber[10];
				Log.d(LOG_TAG, idNumber[0] + " + " + idNumber[2] + " + " + idNumber[4] + " + " + idNumber[6] + " + " + idNumber[8] + " + " + idNumber[10] + " = " +oddSum);

				//Group digits in even position and multiply by 2
				String field = idNumber[1] + "" + idNumber[3] + "" + idNumber[5] + "" + idNumber[7] + "" + idNumber[9] + "" + idNumber[11];
				long evenFieldDoubled = (Long.parseLong(field) * 2);
				Log.d(LOG_TAG, field +  " x 2 = " + evenFieldDoubled);

				//Add digits of even result
				String log = "";
				char[] evenDoubledChars = (evenFieldDoubled + "").toCharArray();
				int evenFinalSum = 0;
				for(int i = 0; i < evenDoubledChars.length; i++)
				{
					evenFinalSum += Character.getNumericValue(evenDoubledChars[i]);
					log += Character.getNumericValue(evenDoubledChars[i]) +  " + ";
				}

				log += " = " + evenFinalSum;

				//add two sums results
				int finalSumResult = oddSum + evenFinalSum;

				Log.d(LOG_TAG, oddSum + " + " + evenFinalSum + " = " + finalSumResult);

				//If result is 2 digits use the second digit to calc check digit value
				if(finalSumResult > 9)
					finalSumResult = Character.getNumericValue((finalSumResult + "").charAt(1));

				int calculatedCheckDigit = 10 - finalSumResult;

				//If result is 2 digits use the second digit to calc check digit value
				if(calculatedCheckDigit > 9)
					calculatedCheckDigit = Character.getNumericValue((calculatedCheckDigit + "").charAt(1));

				Log.d(LOG_TAG, "Calc Check Digit " + calculatedCheckDigit + " = " + controlDigit + " Control Digit");
				if(calculatedCheckDigit != controlDigit)
				{
					valid = false;
					etxtIdNumber.setError(getString(R.string.error_msg_invalid_id_number));
					Log.e(LOG_TAG, "ID Check digit was invalid");
				}
			}
		}

		return valid;
	}

	public void onClickNewRoute(View view)
	{
		Intent planningIntent = new Intent(MainActivity.this, NewRouteActivity.class);
		startActivity(planningIntent);
	}

	public void onClickSavedRoutes(View view)
	{
		Intent savedRoutes = new Intent(MainActivity.this, SavedRoutesActivity.class);
		startActivity(savedRoutes);
	}
	
	public void onClickUpload(View view)
	{
		Intent uploadIntent = new Intent(MainActivity.this, UploadActivity.class);
		startActivity(uploadIntent);

	}

	public void onClickHistory(View view)
	{
		Intent historyIntent = new Intent(MainActivity.this, HistoryActivity.class);
		startActivity(historyIntent);
	}


	public void onClickUploadPhoto(View view)
	{
		//Display upload photo dialog
		FragDiagUploadPhoto fragDiagUploadPhoto = new FragDiagUploadPhoto();
		fragDiagUploadPhoto.show(getSupportFragmentManager(), "fragDiagUploadPhoto");
	}

	public void onClickStats(View view)
	{
		Snackbar.make(view, "Coming Soon", Snackbar.LENGTH_SHORT).show();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		switch(requestCode)
		{
			case PERM_REQ_READ_PHONE_STATE:
			{
				// If request is cancelled, the result arrays are empty.
				if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
				{

					// permission was granted, yay! Do the
					// contacts-related task you need to do.
					accMang.registerPhone();

				} else
				{

					// permission denied, boo! Disable the
					// functionality that depends on this permission.
					AlertDialog.Builder diagBuilder = new AlertDialog.Builder(activity);
					diagBuilder.setTitle("Exit?");
					diagBuilder.setMessage("This app requires the read phone state to operate");
					diagBuilder.setPositiveButton("Grant Permission", new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialogInterface, int i)
						{
							dialogInterface.dismiss();
							ActivityCompat.requestPermissions(activity,	new String[]{Manifest.permission.READ_PHONE_STATE},	PERM_REQ_READ_PHONE_STATE);
						}
					}).setNegativeButton("Exit", new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialogInterface, int i)
						{
							activity.finish();
						}
					});

					diagBuilder.show();
				}

				return;
			}

			case REQ_PERMISSION_FINE_LOC:
			{
				// If request is cancelled, the result arrays are empty.
				if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
				{

					// permission was granted, yay! Do the
					// contacts-related task you need to do.
					connectGpsService();

				} else
				{

					// permission denied, boo! Disable the
					// functionality that depends on this permission.
					AlertDialog.Builder diagBuilder = new AlertDialog.Builder(activity);
					diagBuilder.setTitle("Exit?");
					diagBuilder.setMessage("This app requires the Fine Location permission to operate");
					diagBuilder.setPositiveButton("Grant Permission", new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialogInterface, int i)
						{
							dialogInterface.dismiss();
							ActivityCompat.requestPermissions(activity,	new String[]{Manifest.permission.READ_PHONE_STATE},	PERM_REQ_READ_PHONE_STATE);
						}
					}).setNegativeButton("Exit", new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialogInterface, int i)
						{
							activity.finish();
						}
					});

					diagBuilder.show();
				}

				return;
			}
		}
	}
}
