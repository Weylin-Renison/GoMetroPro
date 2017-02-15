package com.gometro.gometropro;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class GoogleMapActivity extends Activity implements OnMapReadyCallback
{
    private MapFragment fragMap;
    private GoMappProtos.Upload.Route routeData;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_google_map);

        //Hide status notification bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        fragMap = (MapFragment) getFragmentManager().findFragmentById(R.id.fragMap);
        fragMap.getMapAsync(this);

        //Read file data
        File routeFile = findFile(getIntent().getStringExtra("routeName"));

        routeData = null;

        if(routeFile != null)
            routeData = readFile(routeFile);
        else
            Toast.makeText(this, "File was not found", Toast.LENGTH_LONG).show();

    }

    public GoMappProtos.Upload.Route readFile(File routeFile)
    {
        GoMappProtos.Upload.Route routeData = null;

        try
        {
            FileInputStream is = new FileInputStream(routeFile);
            routeData = GoMappProtos.Upload.Route.parseDelimitedFrom(is);
            is.close();
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }

        return routeData;
    }

    public File findFile(String filename)
    {
        File routeFile = null;
        File dirHistory = new File(getFilesDir(), "/History/");

        File[] allFiles;

        if(getIntent().getBooleanExtra("fromHistory", false))
            allFiles = dirHistory.listFiles();
        else
            allFiles = getFilesDir().listFiles();



        for(int i = 0; i < allFiles.length; i++)
        {
            if(allFiles[i].getName().contains(filename))
                routeFile = allFiles[i];
        }

        return routeFile;
    }

    @Override
    public void onMapReady(final GoogleMap googleMap)
    {
        //Init map
        //Animate camera to route making both start and end points visible at the max amount of zoom
        GoMappProtos.Upload.Route.Point startPt = routeData.getPoint(0);
        GoMappProtos.Upload.Route.Point endPt = routeData.getPoint(routeData.getPointCount()-1);

        LatLng startLoc = new LatLng(startPt.getLat(), startPt.getLon());
        LatLng endLoc = new LatLng(endPt.getLat(), endPt.getLon());

        // Pan to see all markers in view.
        // Cannot zoom to bounds until the map has a size.
        final View mapView = fragMap.getView();
        if (mapView.getViewTreeObserver().isAlive())
        {
            mapView.getViewTreeObserver().addOnGlobalLayoutListener (new ViewTreeObserver.OnGlobalLayoutListener()
                    {
                        @SuppressLint("NewApi") // We check which build version we are using.
                        @Override
                        public void onGlobalLayout() {
                            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

                            for(int i = 0; i < routeData.getStopCount(); i++)
                                boundsBuilder.include(new LatLng(routeData.getStop(i).getLat(), routeData.getStop(i).getLon()));

                            LatLngBounds routeStopBounds = boundsBuilder.build();

                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                                mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                            } else {
                                mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            }
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(routeStopBounds, 0));
                        }
                    });
        }

        //Paint route
        PolylineOptions polyRoute = new PolylineOptions();

        for(int i = 0; i < routeData.getPointCount(); i++)
            polyRoute.add(new LatLng(routeData.getPoint(i).getLat(), routeData.getPoint(i).getLon()));

        polyRoute.width(20f);
        polyRoute.color(getResources().getColor(R.color.lighTurquoise));

        googleMap.addPolyline(polyRoute);


        //Add start, end point and stops
        GoMappProtos.Upload.Route.Stop startStop = routeData.getStop(0);
        GoMappProtos.Upload.Route.Stop lastStop = routeData.getStop(routeData.getStopCount() - 1);
        googleMap.addMarker(new MarkerOptions().position(startLoc).title("Start: " + startStop.getStopName()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
        googleMap.addMarker(new MarkerOptions().position(endLoc).title("End: " + lastStop.getStopName()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));

        //Exclude start and end points, they have diffrent marker icons
        for(int i = 1; i < routeData.getStopCount()-1; i++)
        {
            GoMappProtos.Upload.Route.Stop currentStop = routeData.getStop(i);
            googleMap.addMarker(new MarkerOptions().position(new LatLng(currentStop.getLat(), currentStop.getLon())).title("Stop: " + currentStop.getStopName()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN + 20)));
        }
    }


}
