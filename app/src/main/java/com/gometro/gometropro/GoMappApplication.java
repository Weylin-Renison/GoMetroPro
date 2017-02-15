package com.gometro.gometropro;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.widget.Toast;

//import org.acra.annotation.*;

/**
 * Created by IC on 6/5/2015.
 */

//@ReportsCrashes(formUri = "http://54.68.55.70:9000/uploadCrashReport")

public class GoMappApplication extends Application
{
//    @Override
//    protected void attachBaseContext(Context context) {
//        super.attachBaseContext(context);
//        MultiDex.install(this);
//    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        //Init acra
       // ACRA.init(this);
        /*Log.d("Time Test", "System.currentTimeMillis = " + System.currentTimeMillis());
        Log.d("Time Test", "System.nanoTime = " + System.nanoTime());
        Log.d("Time Test", "SystemClock.elapsedRealtime = " + SystemClock.elapsedRealtime());
        Log.d("Time Test", "System.elapsedRealtimeNanos = " + SystemClock.elapsedRealtimeNanos());*/

    }
}
