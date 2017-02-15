package com.gometro.gometropro;

import android.content.ServiceConnection;

/**
 * Created by wprenison on 2017/02/14.
 */

public class GpsGlobalVars
{
    private static GpsGlobalVars mInstance = null;

    //Vars
    public MainActivity mainActivity;
    public GpsCaptureService		GpsService;			//Used to access Gps service methods
    public boolean isServiceConnected = false;

    //Required to bind to service, allows access to service methodsd
    public ServiceConnection myServiceConnection;

    protected GpsGlobalVars(){}

    public static synchronized GpsGlobalVars getInstance()
    {
        if(mInstance == null)
            mInstance = new GpsGlobalVars();

        return mInstance;
    }
}
