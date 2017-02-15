package com.gometro.gometropro;


import java.util.ArrayList;
import java.util.List;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;

public class GpsCaptureService extends Service
{
	private LocationManager LocMang;							//Location Manger used to for all location based Req
	private GpsStatus.Listener statusListener;					//Used to listen to gps statuses to det when signal is lost
	private LocationListener locListner;						//Used to listen for location updates
	private final IBinder GpsBinder = new GpsLocalBinder();		//Binder to make this service's methods available to binding activity
	public CaptureActivity captureActivity;						//Used to access methods trough interface ICaptureActivity, set by binding activity
	public boolean isRecordingLocation = false;					//Det if location updates should be recorded at present time
	public List<RoutePoint> routePoints;						//every location for the route captured
	public List<RouteStop> routeStops;							//every location marked as a stop
	private boolean gpsHasSignal;								//Used to det if gps fix has been obtained or lost
	private RoutePoint newestLocation;

	private NotificationCompat.Builder notifBuilder;
	private NotificationManagerCompat notifMang;
	
	public static int GPS_UPDATE_INTERVAL	= 3000;				// milliseconds
	public static int GPS_MIN_ACCURACY = 50;					//in meters
	public static int GPS_MIN_DIST = 10;
	private static int GPS_NOTIF_ID = 1337;

	@Override
	public IBinder onBind(Intent bindingIntent)
	{
		return GpsBinder;
	}
	
	@Override
	public boolean onUnbind(Intent intent)
	{		
		Log.i("GPS onUnbind", "Attempting to unbind");
		
		//Create stop end point	
//		if(routePoints.size() != 0)
//		{
//			RouteStop endStop = new RouteStop();
//			endStop.location = routePoints.get(routePoints.size() - 1).getLocation();
//			endStop.arrivalTime = routePoints.get(routePoints.size() - 1).getLocation().getTime();
//			endStop.stopName = "";
//
//			routeStops.add(endStop);
//
//			captureActivity.buildTagStopNameDialog(false);
//		}

		//Unregister location listener
		LocMang.removeGpsStatusListener(statusListener);
		LocMang.removeUpdates(locListner);

		this.stopForeground(true);
		this.stopSelf();
		return super.onUnbind(intent);		
	}

    @Override
    public void onLowMemory()
    {
        //Wite current route data to protobuf file that is to be updated as we run out of memory,
        // then clear objects to retrieve more memory to continue recording route
        captureActivity.saveRecordedRoute(routePoints, routeStops, false);

        routePoints.clear();
        routeStops.clear();

        super.onLowMemory();
    }

    //Creates binder for activity to access service methods
	public class GpsLocalBinder extends Binder
	{
		GpsCaptureService getService()
		{
			return GpsCaptureService.this;
		}
	}

	public void runAsForegroundService()
	{
		Intent notificationIntent = new Intent(this, CaptureActivity.class);
		notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP);

		PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
				notificationIntent,  PendingIntent.FLAG_CANCEL_CURRENT);

		notifBuilder = new NotificationCompat.Builder(getApplicationContext());
		notifBuilder.setSmallIcon(R.drawable.ic_launcher_pro)
				.setContentTitle("GM Pro")
				.setContentText("Acquiring GPS Fix...")
				.setContentIntent(pendingIntent);

		Notification notification = notifBuilder.build();

		//Get notif manager for updates later
		notifMang = NotificationManagerCompat.from(getApplicationContext());

		startForeground(GPS_NOTIF_ID, notification);
	}
	
	@Override
	public void onCreate()
	{
		runAsForegroundService();

		//Init route points list
		routePoints = new ArrayList<RoutePoint>();
		routeStops = new ArrayList<RouteStop>();
		
		//Create Location Listener
		LocMang = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		locListner = new LocationListener()
		{					
			@Override
			public void onProviderEnabled(String provider)
			{
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProviderDisabled(String provider)
			{
				if(captureActivity != null)
					captureActivity.onGpsProviderDisabled();
			}
			
			@Override
			public void onLocationChanged(Location location)
			{
				if(location != null)
				{
					//New location received
					if(captureActivity != null)
					{
						//Report back  om gps fix
						captureActivity.updateGpsStatus(location.getAccuracy());

						long currentTime = captureActivity.getCurrentTime();
						newestLocation = new RoutePoint(location, currentTime);

						//Saves into location Point list location if recording is set to true & accuracy is higher than minimum
						if(isRecordingLocation && (location.getAccuracy() < GPS_MIN_ACCURACY))
						{
							//Update notif status
							notifBuilder.setContentText("Recording Trip| GPS Accuracy " + location.getAccuracy() + "m");
							notifMang.notify(GPS_NOTIF_ID, notifBuilder.build());

							//tag starting point as a stop
							if(routePoints.size() == 0)
							{
								RouteStop startStop = new RouteStop();
								startStop.location = location;
								startStop.arrivalTime = location.getTime();
								startStop.stopName = "Unknown Start Stop";

								routeStops.add(startStop);

								captureActivity.buildTagStopNameDialog();
							}


							if(routePoints.size() > 1)
							{
								long distanceTravled = distanceFromLocation(routePoints.get(routePoints.size() - 1).location, location);

								if(distanceTravled > GPS_MIN_DIST)
								{
									if(captureActivity != null)
										captureActivity.updateDistance(distanceTravled);

									routePoints.add(newestLocation);
								}
							}
							else
								routePoints.add(newestLocation);
						}
						else
						{
							//Update gps status notf
							notifBuilder.setContentText("GPS Accuracy: " + location.getAccuracy() + "m");
							notifMang.notify(GPS_NOTIF_ID, notifBuilder.build());
						}
					}
					else
					{
						//Report back on gps
						notifBuilder.setContentText("GPS Accuracy: " + location.getAccuracy() + "m");
						notifMang.notify(GPS_NOTIF_ID, notifBuilder.build());
					}
				}
			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras)
			{
				// TODO Auto-generated method stub
				
			}
		};
		
		statusListener = new GpsStatus.Listener()
		{
			
			@Override
			public void onGpsStatusChanged(int event)
			{
				//Check status and updates capture activity if fix was found ect					
				switch (event)
				{
					case GpsStatus.GPS_EVENT_FIRST_FIX:
						gpsHasSignal = true;
						if(captureActivity != null)
							captureActivity.logFromGpsService("GPS fix found");
						break;
					
					case GpsStatus.GPS_EVENT_SATELLITE_STATUS:						
						if(newestLocation != null)
						{
							if((SystemClock.elapsedRealtime() - newestLocation.getTime()) < (GPS_UPDATE_INTERVAL * 2))
							{
								if(!gpsHasSignal)
								{
									gpsHasSignal = true;
									if(captureActivity != null)
										captureActivity.logFromGpsService("GPS fix found");
								}
							}
							else
							{
								if(gpsHasSignal)
								{
									gpsHasSignal = false;
									if(captureActivity != null)
									{
										captureActivity.updateGpsStatus("Searching...");
										captureActivity.logFromGpsService("GPS Lost signal, Searching...");
									}
								}
							}
						}
						break;

					default:
						break;
				}
				
			}
		};
		
		//Req location updates
		LocMang.addGpsStatusListener(statusListener);
		LocMang.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_UPDATE_INTERVAL, 0, locListner);
	}
	
	//Starts capturing locations for route, called by capturing activity
	public void onCaptureClicked()
	{
		isRecordingLocation = true;
	}
	
	
	//Stops capturing locations for route, called by capturing activity
	public void onStopCaptureClicked()
	{
		isRecordingLocation = false;
	}

	public void finishTripRecording()
	{
		if(captureActivity != null)
			captureActivity.saveRecordedRoute(routePoints, routeStops, true);
	}
	
	//marks and saves last routePoint as a stop called by capturing activity
	public boolean tagStop(long stopTime)
	{
		boolean stopRecorded = false;
		
		if(!routePoints.isEmpty())
		{

			RoutePoint stopLocation = routePoints.get(routePoints.size()-1);

            RouteStop stop = new RouteStop();
            stop.location = stopLocation.getLocation();
            stop.arrivalTime = stopTime;

            routeStops.add(stop);

			stopRecorded = true;

			//Check if finish button can become visible
			if(routeStops.size() == 2)
				captureActivity.txtvDone.setVisibility(View.VISIBLE);
		}
		
		return stopRecorded;
	}

	//Removes last tagged stop
	public boolean untagStop()
	{
		boolean stopDeleted = false;

		if(!routeStops.isEmpty())
		{
			routeStops.remove(routeStops.size() - 1);
			stopDeleted = true;
		}

		return stopDeleted;
	}

	//Moves last tagged stop to new location
	public boolean moveStop(long stopTime, String newName)
	{
		boolean stopMoved = false;

		if(!routePoints.isEmpty())
		{

			RoutePoint stopLocation = routePoints.get(routePoints.size()-1);

			RouteStop stop = routeStops.get(routeStops.size() -1);
			stop.location = stopLocation.getLocation();
			stop.arrivalTime = stopTime;

			if(!newName.equals("") || !newName.isEmpty())
				stop.stopName = newName;

			stopMoved = true;
		}

		return stopMoved;
	}

	public long distanceFromLocation(Location l1, Location l2)
	{

		LatLng ll1 = new LatLng(l1.getLatitude(), l1.getLongitude());
		LatLng ll2 = new LatLng(l2.getLatitude(), l2.getLongitude());

		return Math.round(LatLngTool.distance(ll1, ll2, LengthUnit.METER));
	}
	
}
