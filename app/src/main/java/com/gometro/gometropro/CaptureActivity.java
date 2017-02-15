package com.gometro.gometropro;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import com.gometro.gometropro.GoMappProtos.Upload;
import com.gometro.gometropro.GpsCaptureService.GpsLocalBinder;

import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.Vibrator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class CaptureActivity extends AppCompatActivity implements ICaptureActivity{


    View.OnTouchListener gestureListener;
    private static FragMainCapture fragMain;
    private FragmentManager fragManager;
    private FrameLayout fLayFragContainer;
    public RelativeLayout relLayMainCapture;
    private FragmentTransaction fragTrans;
    private FloatingActionButton ibtnStartStop;
	public TextView txtvDone;
    private TextView txtvStartStopDesc;
    private TextView txtvGpsAccuracy;
    private Spinner spnrReasonForTrip;
    private Chronometer chronElapsedTime;
    
    private boolean hasExtras = false;
    private String intentFrom; 
    private Bundle bundleRouteData;

    private Vibrator vibratorService;
    
    //Service vars
//    private static Intent			serviceIntent;		//Used to start gps service
//	public GpsCaptureService		GpsService;			//Used to access Gps service methods
	private CaptureActivity thisActivity = this;		//Used to set capturing activity in gps service
	private LocationManager locMang;					//Used to update gps accuracy field and more, recieves calls from gps service when gps status is changed	
//	private boolean isServiceConnected = false;
	
	private Double distanceTraveledRunningTotal = 0.00;
	private long startTime;
	private long stopTime;
	private boolean cancelDuringRecording = false;
	
	private int planIndex = -1;							//Used to delete plan on completion of capture
	public SavedRoutesActivity savedRoutesActivity;

	//Tag stop vars
	public File stopPhotoFile;
//	private ImageView imgvPreview;
//	private FloatingActionButton btnAddPhoto;
//	private FloatingActionButton btnBoardPass;
//	private FloatingActionButton btnAlightPass;
//	private TextView txtvTotalPassValue;
//	private Button btnClearBoard;
//	private Button btnClearAlight;
//	private ImageView imgvBadgeBgMinusPass;
//	private TextView txtvPassBoardValue;
//	private TextView txtvPassAlightValue;
//	private EditText etxtFarePrice;
//	private TextView txtvCurrency;
//	private Chronometer chronWaitingTime;
	public int lastPassRunningTotal = 0;
	public int passRuningTotal = 0;
//	private int passBoardValue;
//	private int passAlightValue;
//	private static final int REQ_CODE_CAPTURE_PHOTO = 1001;
	
//	//Required to bind to service, allows access to service methodsd
//	private ServiceConnection myServiceConnection = new ServiceConnection()
//	{
//
//		@Override
//		public void onServiceDisconnected(ComponentName name)
//		{
//			// TODO Auto-generated method stub
//			isServiceConnected = false;
//		}
//
//		@Override
//		public void onServiceConnected(ComponentName name, IBinder service)
//		{
//			// TODO Auto-generated method stub
//			GpsLocalBinder binder = (GpsLocalBinder) service;
//			GpsService = binder.getService();
//			GpsService.captureActivity = thisActivity;
//			isServiceConnected = true;
//		}
//	};

	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_capture);

		//Connect with gps service
		GpsGlobalVars.getInstance().GpsService.captureActivity = this;

		//Check if location services are enabled, if not prompt user
		locMang = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if(!locMang.isProviderEnabled(LocationManager.GPS_PROVIDER))
		{
			buildAlertMessageNoGps();
		}
		else
		{
			//init frags
	        fLayFragContainer = (FrameLayout) findViewById(R.id.fLayFragContainer);
	        relLayMainCapture = (RelativeLayout) findViewById(R.id.relLayMainCapture);
	        relLayMainCapture.setOnTouchListener(gestureListener);
	        
	        fragMain = new FragMainCapture();
//	        fragSecondary = new FragSecondaryCapture();
	        
	        fragManager = getFragmentManager();
	        fragTrans = fragManager.beginTransaction();        
	        
	      //Check for intent data
	        Intent callingIntent = getIntent();
	        
	        ibtnStartStop = (FloatingActionButton) findViewById(R.id.ibtnStartStop);
	        ibtnStartStop.setTag(R.drawable.ic_tag);	//tag set and used for swapping images of button
	        
	        txtvStartStopDesc = (TextView) findViewById(R.id.txtvStartStopDesc);

			txtvDone = (TextView) findViewById(R.id.txtvDone);
	        
	        if(callingIntent.getExtras() != null)
	        {
	        	hasExtras = true;
	        	intentFrom = callingIntent.getStringExtra("intentFrom");
	        	
	        	bundleRouteData = callingIntent.getBundleExtra("bundleRouteData");
	        	fragMain.setArguments(bundleRouteData); 
	        	
	        	
	        }
	        
	        fragTrans.add(R.id.fLayFragContainer, fragMain, "fragMain");
	        fragTrans.commit();
				        
	        //GpsService Setup
//			Intent gpsServiceIntent = new Intent(this, GpsCaptureService.class);
//			gpsServiceIntent.putExtras(callingIntent);
//
//			bindService(gpsServiceIntent, myServiceConnection, Context.BIND_AUTO_CREATE);
			
			//Hide status notification bar
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}                
	}
	
	@Override
	public void logFromGpsService(String msg)
	{
		Snackbar.make(relLayMainCapture, msg, Snackbar.LENGTH_LONG).show();
//		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}
	
	public void updateGpsStatus(float accuracy)
	{
		TextView txtvGpsAccuracy = (TextView) findViewById(R.id.txtvGpsAccuracy);
		if(accuracy > GpsCaptureService.GPS_MIN_ACCURACY)
		{			
			txtvGpsAccuracy.setText("Searching, accuracy to low...");
		}
		else
			txtvGpsAccuracy.setText("+/- " + accuracy + "m");
	}
	
	@Override
	public void updateGpsStatus(String statusMsg)
	{
		TextView txtvGpsAccuracy = (TextView) findViewById(R.id.txtvGpsAccuracy);
		txtvGpsAccuracy.setText(statusMsg);
	}
	
	@Override
	public void updateDistance(long distTravled)
	{
		double roundingTemp = distanceTraveledRunningTotal + (((double)distTravled)/1000);
		roundingTemp = roundingTemp * 1000;
		roundingTemp = Math.round(roundingTemp);
		roundingTemp = roundingTemp / 1000;
		distanceTraveledRunningTotal = roundingTemp;
						
		TextView txtvDistance = (TextView) findViewById(R.id.txtvDistance);	
		
		DecimalFormat form2dec = new DecimalFormat("0.000");
		txtvDistance.setText(form2dec.format(distanceTraveledRunningTotal).toString());
	}
	
	@Override
	public void saveRecordedRoute(List<RoutePoint> routePoints, List<RouteStop> routeStops, boolean serviceUnbinding)
	{	
		
		//Check data was recorded
		if(!routePoints.isEmpty() && !routeStops.isEmpty() && !cancelDuringRecording)
		{
			//Collect & prepare all data required to create protobuf file
			//Toast.makeText(this, "Attempting to write protobuf!", Toast.LENGTH_LONG).show();

			//Compulsory fields
			String routeName = ((TextView) findViewById(R.id.txtvRouteNameCaptureHeading)).getText().toString();
			String transportCompany = null;
			String transportMode = null;
			String startPoint = "";
			String endPoint = "";
			String mapperName = "";
			String routeComments = ((EditText) findViewById(R.id.etxtMappingNotes)).getText().toString(); //Mapping notes
			long totalDistance = distanceTraveledRunningTotal.longValue();
			boolean oneWay = false;


			//Get time values
			startTime = routePoints.get(0).getLocation().getTime();
			stopTime = routePoints.get(routePoints.size()-1).getLocation().getTime();

			//get bundle data
			if(hasExtras)
			{
				bundleRouteData = getIntent().getBundleExtra("bundleRouteData");
				planIndex = bundleRouteData.getInt("planIndex", -1);
				transportCompany = bundleRouteData.getString("transportCompany");
				transportMode = bundleRouteData.getString("transportMode");
				startPoint = bundleRouteData.getString("startPoint");
				endPoint = bundleRouteData.getString("endPoint");
				mapperName = bundleRouteData.getString("mapperName");
			}


			//Create protobuf file and save it
			RouteCapture capturedRoute = new RouteCapture();
			capturedRoute.commonReasonForTrip = "";
			capturedRoute.distance = totalDistance;
			capturedRoute.startPoint = startPoint;
			capturedRoute.endPoint = endPoint;
			capturedRoute.oneWay = oneWay;
			capturedRoute.ratingComfort = -1;
			capturedRoute.ratingDrivingStyle = -1;
			capturedRoute.ratingSaftey = -1;
			capturedRoute.routeComments = routeComments;
			capturedRoute.routeName = routeName;
			capturedRoute.mapperName = mapperName;
			capturedRoute.startTime = startTime;

			long durTime = SystemClock.elapsedRealtime() - chronElapsedTime.getBase();
			TimeZone tz = TimeZone.getDefault();
			int timeOffset = tz.getOffset(durTime);

			capturedRoute.duration = durTime - timeOffset;
			capturedRoute.transportCompany = transportCompany;
			capturedRoute.transportMode = transportMode;

			//Toast.makeText(this, "Location data: No of routePoinrs: " + routePoints.size() + " No of stops: " + routeStops.size(), Toast.LENGTH_LONG).show();
			capturedRoute.points = routePoints;
			capturedRoute.stops = routeStops;

			Upload.Route routePb = capturedRoute.seralize();

			if(!routePb.isInitialized())
				Snackbar.make(relLayMainCapture, "Protobuf was not initialized correctly", Snackbar.LENGTH_LONG).show();
//				Toast.makeText(this, "Protobuf was not initialized correctly", Toast.LENGTH_LONG).show();

			File file = new File(getFilesDir() , "route_" + routeName + "_" + startTime + ".pb");
			FileOutputStream os;

			try
			{

				os = new FileOutputStream(file);
				routePb.writeDelimitedTo(os);
				os.flush();
				os.close();

			} catch (FileNotFoundException e)
			{
				e.printStackTrace();
			} catch (IOException e)
			{
				e.printStackTrace();
			}

			if(planIndex != -1)
			{
				Log.i("PlanIndex", "Plan index is: " + planIndex);
				//Delete plan
				FileHelper fileHlp = new FileHelper(this);
				fileHlp.deletePlan(planIndex);
			}

			Snackbar.make(relLayMainCapture, "Saved!", Snackbar.LENGTH_LONG).show();
//			Toast.makeText(this, "Saved!", Toast.LENGTH_LONG).show();

			Intent uploadIntent = new Intent(CaptureActivity.this, UploadActivity.class);
			startActivity(uploadIntent);
			thisActivity.setResult(RESULT_OK);

		}
		else
		{
			//get bundle data
			if(hasExtras)
			{
				bundleRouteData = getIntent().getBundleExtra("bundleRouteData");
				planIndex = bundleRouteData.getInt("planIndex", -1);
			}
			
			//Save plan if user came straight from new route activity
			if(planIndex != -1)
			{
				Snackbar.make(relLayMainCapture, "No data was recorded", Snackbar.LENGTH_LONG).show();
//				Toast.makeText(this, "No data was recorded", Toast.LENGTH_LONG).show();
			}
			else
			{
				//Get data to save plan
				String mapperName = null;
				String routeName = ((TextView) findViewById(R.id.txtvRouteNameCaptureHeading)).getText().toString();
				String transportCompany = null;
				String transportMode = null;
				String startPoint = null;
				String endPoint = null;
				
				//get bundle data
				if(hasExtras)
				{
					bundleRouteData = getIntent().getBundleExtra("bundleRouteData");
					mapperName = bundleRouteData.getString("mapperName");
					transportCompany = bundleRouteData.getString("transportCompany");
					transportMode = bundleRouteData.getString("transportMode");
					startPoint = bundleRouteData.getString("startPoint");
					endPoint = bundleRouteData.getString("endPoint");
				}
				
				FileHelper fileHlp = new FileHelper(this);
				fileHlp.writePlan(mapperName, routeName, transportMode, transportCompany, startPoint, endPoint, true);
			}
		}

		resetGpsService();
		this.finish();
	}

	private void resetGpsService()
	{
		GpsCaptureService gpsService = GpsGlobalVars.getInstance().GpsService;
		gpsService.captureActivity = null;
		gpsService.isRecordingLocation = false;
		gpsService.routePoints = new ArrayList<>();
		gpsService.routeStops = new ArrayList<>();
	}

	@Override
	public void onGpsProviderDisabled()
	{
		buildAlertMessageNoGps();
	}

	public long getCurrentTime(){return System.currentTimeMillis();}
	
	//onClick for start stop button
	public void onClickStartStop(View view)
	{
		//Checks with service if currently recording
		txtvGpsAccuracy = (TextView) findViewById(R.id.txtvGpsAccuracy);
		if(txtvGpsAccuracy.getText().toString().contains("Searching") || txtvGpsAccuracy.getText().toString().contains("searching"))
		{
			Snackbar.make(relLayMainCapture, "GPS is searching, please wait for an accurate fix", Snackbar.LENGTH_LONG).show();
//			Toast.makeText(this, "GPS is searching, please wait for an accurate fix", Toast.LENGTH_LONG).show();
		}
		else if(GpsGlobalVars.getInstance().GpsService.isRecordingLocation)
		{
			//Tag a stop after long press only
			Snackbar.make(relLayMainCapture, "Tagging stop", Snackbar.LENGTH_LONG).show();
//			Toast.makeText(this, "Tagging stop", Toast.LENGTH_LONG).show();
			
			txtvGpsAccuracy = (TextView) findViewById(R.id.txtvGpsAccuracy);
			if(txtvGpsAccuracy.getText().toString().contains("Searching") || txtvGpsAccuracy.getText().toString().contains("searching"))
			{
				Snackbar.make(relLayMainCapture, "GPS accuracy to low", Snackbar.LENGTH_LONG).show();
//				Toast.makeText(this, "GPS accuracy to low", Toast.LENGTH_LONG).show();
			}
			else
			{

				if(GpsGlobalVars.getInstance().GpsService.tagStop(getCurrentTime()))
				{
                    buildTagStopNameDialog();
				}
				else
					Snackbar.make(relLayMainCapture, "No locations have been captured yet", Snackbar.LENGTH_LONG);
//					Toast.makeText(this, "No locations have been captured yet!", Toast.LENGTH_LONG).show();
			}
		}
		else if(!GpsGlobalVars.getInstance().GpsService.isRecordingLocation)
		{
			GpsGlobalVars.getInstance().GpsService.onCaptureClicked();

			Snackbar.make(relLayMainCapture, "Recording Activated", Snackbar.LENGTH_LONG).show();
//			Toast.makeText(this, "Recording Activated", Toast.LENGTH_LONG).show();
			chronElapsedTime = (Chronometer) findViewById(R.id.chronDuration);
			chronElapsedTime.setBase(SystemClock.elapsedRealtime());
			chronElapsedTime.start();
			
			TextView txtvStartStopDesc = (TextView) findViewById(R.id.txtvStartStopDesc);
			txtvStartStopDesc.setText("Tag Stop");
			
			ibtnStartStop.setImageResource(R.drawable.ic_tag);
		}
		else
		{
			Log.e("CaptureActivity", "Recording status was never set in GpsCaptureService");
		}
	}

	public void updateStopCountStatus()
	{
		TextView txtvStopCounter = (TextView) findViewById(R.id.txtvStops);
		int noOfStops = GpsGlobalVars.getInstance().GpsService.routeStops.size();
		txtvStopCounter.setText(noOfStops + "");
	}

	public void buildTagStopNameDialog()
	{
		//cache running total
		lastPassRunningTotal = passRuningTotal;

		FragDiagTagStop diagTagStop = new FragDiagTagStop();
		diagTagStop.show(getSupportFragmentManager(), "fragDiagTagStop");
	}

//	public void buildTagStopNameDialog(final boolean lastStop)
//	{
//		LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		View stopNameDialogView = inflater.inflate(R.layout.custom_tag_stop_name_dialog, null);
//		final EditText etxtStopName = (EditText) stopNameDialogView.findViewById(R.id.etxtStopNameDialogValue);
//
//		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
//		dialogBuilder.setView(stopNameDialogView);
//		dialogBuilder.setCancelable(false);
//
//		//Get handle on views
//		etxtFarePrice = (EditText) stopNameDialogView.findViewById(R.id.etxtFarePrice);
//		txtvCurrency = (TextView) stopNameDialogView.findViewById(R.id.txtvFarePriceCurrency);
//		chronWaitingTime = (Chronometer) stopNameDialogView.findViewById(R.id.chronStopDuration);
//		imgvPreview =  (ImageView) stopNameDialogView.findViewById(R.id.imgvStopNameDialogImagePreview);
//		btnAddPhoto = (FloatingActionButton) stopNameDialogView.findViewById(R.id.ibtnStopNameDialogAddPhoto);
//		btnBoardPass = (FloatingActionButton) stopNameDialogView.findViewById(R.id.ibtnAddPass);
//		btnAlightPass = (FloatingActionButton) stopNameDialogView.findViewById(R.id.ibtnMinusPass);
//		imgvBadgeBgMinusPass = (ImageView) stopNameDialogView.findViewById(R.id.ivBadgeBgMinusPass);
//		btnClearBoard = (Button) stopNameDialogView.findViewById(R.id.btnClearBoard);
//		btnClearAlight = (Button) stopNameDialogView.findViewById(R.id.btnClearAlight);
//		txtvTotalPassValue = (TextView) stopNameDialogView.findViewById(R.id.txtvTotalPassValue);
//		txtvPassBoardValue = (TextView) stopNameDialogView.findViewById(R.id.txtvBoardPassValue);
//		txtvPassAlightValue = (TextView) stopNameDialogView.findViewById(R.id.txtvAlightPassValue);
//		passBoardValue = 0;
//		passAlightValue = 0;
//
//		//disable alight button if total pass in vehicle is 0
//		if(passRuningTotal == 0)
//			btnAlightPass.setEnabled(false);
//
//		//Set currency
//		Locale defaultLocale = Locale.getDefault();
//		Currency defaultCurrency = Currency.getInstance(defaultLocale);
//
//		String currencySymbol = defaultCurrency.getSymbol();
//
//		if(currencySymbol != null || !currencySymbol.isEmpty())
//			txtvCurrency.setText(defaultCurrency.getSymbol());
//
//		Log.d("currency", txtvCurrency.getText().toString());
//
//		btnAddPhoto.setOnClickListener(new View.OnClickListener()
//		{
//			@Override
//			public void onClick(View view)
//			{
//				stopPhotoFile = CamHelper.intentCamCapturePhoto(thisActivity, REQ_CODE_CAPTURE_PHOTO, Environment.getExternalStorageDirectory() + "/GoMetroPro/Photos/", null);
//			}
//		});
//
//		imgvPreview.setOnClickListener(new View.OnClickListener()
//		{
//			@Override
//			public void onClick(View view)
//			{
//				stopPhotoFile = CamHelper.intentCamCapturePhoto(thisActivity, REQ_CODE_CAPTURE_PHOTO, Environment.getExternalStorageDirectory() + "/GoMetroPro/Photos/", null);
//			}
//		});
//
//		txtvTotalPassValue.addTextChangedListener(new TextWatcher()
//		{
//			@Override
//			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
//			{
//
//			}
//
//			@Override
//			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
//			{
//				if(passRuningTotal == 0)
//				{
//					btnAlightPass.setEnabled(false);
//					btnAlightPass.setImageDrawable(getDrawable(R.drawable.ic_minus_inactive));
//					imgvBadgeBgMinusPass.setImageDrawable(getDrawable(R.drawable.bg_badge_inactive));
//				}
//				else
//				{
//					btnAlightPass.setEnabled(true);
//					btnAlightPass.setImageDrawable(getDrawable(R.drawable.ic_minus));
//					imgvBadgeBgMinusPass.setImageDrawable(getDrawable(R.drawable.bg_badge));
//				}
//			}
//
//			@Override
//			public void afterTextChanged(Editable editable)
//			{
//
//			}
//		});
//
//		btnBoardPass.setOnClickListener(new View.OnClickListener()
//		{
//			@Override
//			public void onClick(View view)
//			{
//				passBoardValue++;
//				txtvPassBoardValue.setText(passBoardValue + "");
//			}
//		});
//
//		//Disable button if total pass in vehicle is 0
//		if(passRuningTotal == 0)
//		{
//			btnAlightPass.setEnabled(false);
//			btnAlightPass.setImageDrawable(getDrawable(R.drawable.ic_minus_inactive));
//			imgvBadgeBgMinusPass.setImageDrawable(getDrawable(R.drawable.bg_badge_inactive));
//		}
//
//		btnAlightPass.setOnClickListener(new View.OnClickListener()
//		{
//			@Override
//			public void onClick(View view)
//			{
//				passAlightValue++;
//				txtvPassAlightValue.setText(passAlightValue + "");
//
//				passRuningTotal--;
//				txtvTotalPassValue.setText(passRuningTotal + "");
//			}
//		});
//
//		btnClearBoard.setOnClickListener(new View.OnClickListener()
//		{
//			@Override
//			public void onClick(View view)
//			{
//
//				passBoardValue = 0;
//				txtvPassBoardValue.setText(passBoardValue + "");
//			}
//		});
//
//		btnClearAlight.setOnClickListener(new View.OnClickListener()
//		{
//			@Override
//			public void onClick(View view)
//			{
//
//				passRuningTotal += passAlightValue;
//				txtvTotalPassValue.setText(passRuningTotal + "");
//
//				passAlightValue = 0;
//				txtvPassAlightValue.setText(passAlightValue + "");
//			}
//		});
//
//		dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener()
//		{
//			@Override
//			public void onClick(DialogInterface dialog, int which)
//			{
//
//				chronWaitingTime.stop();
//				RouteStop stop = GpsService.routeStops.get(GpsService.routeStops.size() - 1);
//
//				if (etxtStopName.getText().toString().isEmpty())
//				{
//					stop.stopName = "Unknown";
//				} else
//				{
//					Log.d("StopName", "Setting stop name to: " + etxtStopName.getText().toString());
//					stop.stopName = etxtStopName.getText().toString().trim();
//				}
//
//				//set stop alight and boarding figures
//				stop.alight = passRuningTotal;
//				stop.board = passBoardValue;
//				passRuningTotal += passBoardValue;
//
//				if(!etxtFarePrice.getText().toString().isEmpty())
//					stop.stopFarePrice = Float.parseFloat(etxtFarePrice.getText().toString());
//				else
//					stop.stopFarePrice = 0f;
//
//
//				stop.departureTime = getCurrentTime();
//				stop.currency = txtvCurrency.getText().toString().trim() + "";
//
//				passAlightValue = 0;
//				passBoardValue = 0;
//
//				if(lastStop)
//					unbindService(myServiceConnection);
//				else	//Might as well avoid the extra work if it's the last stop
//					updateStopCountStatus();
//
//				hideKeyboardImplicit();
//				Log.d("StopName", "Stop name was set to: " + GpsService.routeStops.get(GpsService.routeStops.size() - 1).stopName);
//			}
//		});
//
//		//Only display this if at least one stop has been tagged, keep in mind stop is added as soon
//		// as the button is pressed so stop count is always inflated by 1
//		if(GpsService.routeStops.size() > 1)
//		{
//			dialogBuilder.setNegativeButton("Move Prev Stop", new DialogInterface.OnClickListener()
//			{
//				@Override
//				public void onClick(final DialogInterface dialogInterface, int i)
//				{
//					AlertDialog.Builder confirmDiagBuilder = new AlertDialog.Builder(thisActivity);
//					confirmDiagBuilder.setMessage("Are you sure you want to move the previous stop location?");
//
//					confirmDiagBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
//					{
//						@Override
//						public void onClick(DialogInterface dialogconfirmInterface, int i)
//						{
//							dialogconfirmInterface.dismiss();
//							dialogInterface.dismiss();
//
//							//untag premature stop
//							if (GpsService.untagStop())
//							{
//								if (GpsService.moveStop(getCurrentTime(), etxtStopName.getText().toString().trim()))
//								{
//									Snackbar.make(relLayMainCapture, "Stop Moved", Snackbar.LENGTH_LONG).show();
////									Toast.makeText(thisActivity, "Stop Moved", Toast.LENGTH_LONG).show();
//								}
//							} else
//								Snackbar.make(relLayMainCapture, "Failed to move stop", Snackbar.LENGTH_LONG).show();
////								Toast.makeText(thisActivity, "Failed to move stop", Toast.LENGTH_LONG).show();
//
//							if(lastStop)
//								unbindService(myServiceConnection);
//							else	//Might as well avoid the extra work if it's the last stop
//								updateStopCountStatus();
//
//							hideKeyboardImplicit();
//						}
//					});
//
//					confirmDiagBuilder.setNegativeButton("No", new DialogInterface.OnClickListener()
//					{
//						@Override
//						public void onClick(DialogInterface dialogconfirmInterface, int i)
//						{
//							dialogconfirmInterface.dismiss();
//							GpsService.untagStop();
//							updateStopCountStatus();
//
//							hideKeyboardImplicit();
//						}
//					});
//
//					final AlertDialog confirmStopMove = confirmDiagBuilder.create();
//					confirmDiagBuilder.show();
//
//				}
//			});
//		}
//
//		chronWaitingTime.setBase(SystemClock.elapsedRealtime());
//		chronWaitingTime.start();
//
//        final AlertDialog stopNameDialog = dialogBuilder.create();
//        stopNameDialog.show();
////	}
//
//	private void hideKeyboardImplicit()
//	{
//		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
//	}

	
	//onClick for done button
	public void onClickDone(View view)
	{
		buildDoneConfirmation();
	}
	
	//onClick for cancel button
	public void onClickCancel(View view)
	{
		buildCancelConfirmation();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		//unBinds service correctly if back button was pressed
		if ((keyCode == KeyEvent.KEYCODE_BACK)) 
		{
			buildCancelConfirmation();
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	private void buildAlertMessageNoGps() 
	{
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS is disabled, to capture a route you need to enabled it.").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() 
        {
        	public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
            startActivityForResult((new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)), 0);
            
        }
        }).setNegativeButton("No", new DialogInterface.OnClickListener()
        {
            public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) 
            {
            	finish();
            }
            
        });
        
        final AlertDialog alert = builder.create();
        alert.show();
    }

	private void buildCancelConfirmation()
	{
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to cancel capturing?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener()
		{
			public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id)
			{
				if(GpsGlobalVars.getInstance().isServiceConnected)
				{
					cancelDuringRecording = true;
					//unbindService(GpsGlobalVars.getInstance().myServiceConnection);
					GpsGlobalVars.getInstance().GpsService.captureActivity = null;
					finish();
				}
				else
					Snackbar.make(relLayMainCapture, "Capturing canceled", Snackbar.LENGTH_LONG).show();
//					Toast.makeText(getApplicationContext(), "Capturing canceled", Toast.LENGTH_LONG).show();
			}
		}).setNegativeButton("No", new DialogInterface.OnClickListener()
		{
			public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id)
			{
				dialog.dismiss();
			}

		});

		final AlertDialog alert = builder.create();
		alert.show();
	}

	private void buildDoneConfirmation()
	{
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you are done?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener()
		{
			public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id)
			{
				GpsGlobalVars.getInstance().GpsService.onStopCaptureClicked();

				chronElapsedTime = (Chronometer) findViewById(R.id.chronDuration);
				chronElapsedTime.stop();

				//balance last stop pass board and alight
				RouteStop rsStop =  GpsGlobalVars.getInstance().GpsService.routeStops.get(GpsGlobalVars.getInstance().GpsService.routeStops.size()-1);
				rsStop.alight = lastPassRunningTotal;
				rsStop.board = 0;

				GpsGlobalVars.getInstance().GpsService.finishTripRecording();//// unbindService(GpsGlobalVars.getInstance().myServiceConnection);
				//buildTagStopNameDialog(true); //unbinds in last stop name diag
			}
		}).setNegativeButton("No", new DialogInterface.OnClickListener()
		{
			public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id)
			{
				dialog.dismiss();
			}

		});

		final AlertDialog alert = builder.create();
		alert.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(requestCode == 0)
		{
			if(resultCode == RESULT_CANCELED)
			{
				finish();
			}
		}
	}
}
