package com.gometro.gometropro;

import android.util.Log;

/*
 * @author fabien
 */
public class GPSLocConverter
{
    private static final String LOG_TAG = "GPSLocConv";

    private static StringBuilder sb = new StringBuilder(20);

    /**
     * returns ref for latitude which is S or N.
     * @param latitude
     * @return S or N
     */
    public static String latitudeRef(double latitude) {
        return latitude<0.0d?"S":"N";
    }

    /**
     * returns ref for latitude which is S or N.
     * @param longitude
     * @return S or N
     */
    public static String longitudeRef(double longitude) {
        return longitude<0.0d?"W":"E";
    }

    /**
     * convert latitude into DMS (degree minute second) format. For instance<br/>
     * -79.948862 becomes<br/>
     *  79/1,56/1,55903/1000<br/>
     * It works for latitude and longitude<br/>
     * @param cord could be longitude.
     * @return
     */
    synchronized public static final String convert(double cord) {

        Log.d(LOG_TAG, "STARTING Cord = " + cord);
        cord = Math.abs(cord);
        Log.d(LOG_TAG, "Conv Math.abs = " + cord);
        int degree = (int) cord;
        Log.d(LOG_TAG, "Conv cast to int = " + cord);
        cord *= 60;
        Log.d(LOG_TAG, "Conv Math. *=60" + cord);
        cord -= (degree * 60.0d);
        Log.d(LOG_TAG, "Conv Math -= (degree * 60.0d) " + cord);

        int minute = (int) cord;
        Log.d(LOG_TAG, "Conv min cast to int = " + cord);
        cord *= 60;
        Log.d(LOG_TAG, "Conv Math. *=60" + cord);
        cord -= (minute * 60.0d);
        Log.d(LOG_TAG, "Conv Math -= (degree * 60.0d) " + cord);
        int second = (int) (cord*1000.0d);
        Log.d(LOG_TAG, "Conv Math seconds cast (cord*1000) " + cord);

        sb.setLength(0);
        sb.append(degree);
        sb.append("/1,");
        sb.append(minute);
        sb.append("/1,");
        sb.append(second);
        sb.append("/1000,");
        return sb.toString();
    }
}