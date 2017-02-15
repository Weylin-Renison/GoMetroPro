package com.gometro.gometropro;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * Created by wprenison on 2016/09/02.
 */
public class CamHelper
{

    private static final String LOG_TAG = "CamHelper";

    public static final int REQ_PERM_CREATE_PHOTO_FILE = 9991;

    public static final String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    //A helper class to handle ops with the camera intent and additional features
    public static File intentCamCapturePhoto(android.support.v4.app.DialogFragment frag, int RequestCode, @Nullable String photoDir, @Nullable String photoFileName)
    {
        Intent intentCam = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File imageUriFile = null;

        if(photoDir != null)
        {
            if(!photoDir.isEmpty())
            {
                if(ContextCompat.checkSelfPermission(frag.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                {
                    //Perm denied show explanation?
                    if(ActivityCompat.shouldShowRequestPermissionRationale(frag.getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE))
                    {
                        //Show explanation
                    }
                    else
                    {
                        //Req perm
                        ActivityCompat.requestPermissions(frag.getActivity(), PERMISSIONS_STORAGE, REQ_PERM_CREATE_PHOTO_FILE);
                    }
                }
                else
                {//We have perm
                    File photoFile = new File(photoDir);
                    if(!photoFile.exists())
                    {
                        photoFile.mkdirs();
                    }

                    if(photoFileName != null)
                    {
                        imageUriFile = new File(photoFile, photoFileName);
                    } else
                    {
                        try
                        {
                            imageUriFile = File.createTempFile("cam", ".jpg", photoFile);
                        } catch(IOException e)
                        {
                            e.printStackTrace();
                        }
                    }

                    if(imageUriFile.exists())
                        imageUriFile.delete();

                    intentCam.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageUriFile));
                }
            }
            else
            {
                Log.e(LOG_TAG, "Method: intentCamCapturePhoto photoDir was empty");
            }
        }

        frag.startActivityForResult(intentCam, RequestCode);

        return imageUriFile;
    }

    public static File intentCamCapturePhoto(Activity activity, int RequestCode, @Nullable String photoDir, @Nullable String photoFileName)
    {
        Intent intentCam = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File imageUriFile = null;

        if(photoDir != null)
        {
            if(!photoDir.isEmpty())
            {
                File photoFile = new File(photoDir);
                if(!photoFile.exists())
                {
                    photoFile.mkdirs();
                }

                if(photoFileName != null)
                {
                    imageUriFile = new File(photoFile, photoFileName);
                }
                else
                {
                    try
                    {
                        imageUriFile = File.createTempFile("cam", ".jpg", photoFile);
                    } catch(IOException e)
                    {
                        e.printStackTrace();
                    }
                }

                if(imageUriFile.exists())
                    imageUriFile.delete();

                intentCam.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageUriFile));
            }
            else
            {
                Log.e(LOG_TAG, "Method: intentCamCapturePhoto photoDir was empty");
            }
        }

        activity.startActivityForResult(intentCam, RequestCode);

        return imageUriFile;
    }

    public static File intentCamCapturePhoto(Fragment frag, int RequestCode, @Nullable String photoDir, @Nullable String photoFileName)
    {
        Intent intentCam = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File imageUriFile = null;

        if(photoDir != null)
        {
            if(!photoDir.isEmpty())
            {
                if(ContextCompat.checkSelfPermission(frag.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                {
                    //Perm denied show explanation?
                    if(ActivityCompat.shouldShowRequestPermissionRationale(frag.getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE))
                    {
                        //Show explanation
                    }
                    else
                    {
                        //Req perm
                        ActivityCompat.requestPermissions(frag.getActivity(), PERMISSIONS_STORAGE, REQ_PERM_CREATE_PHOTO_FILE);
                    }
                }
                else
                {//We have perm
                    File photoFile = new File(photoDir);
                    if(!photoFile.exists())
                    {
                        photoFile.mkdirs();
                    }

                    if(photoFileName != null)
                    {
                        imageUriFile = new File(photoFile, photoFileName);
                    } else
                    {
                        try
                        {
                            imageUriFile = File.createTempFile("cam", ".jpg", photoFile);
                        } catch(IOException e)
                        {
                            e.printStackTrace();
                        }
                    }

                    if(imageUriFile.exists())
                        imageUriFile.delete();

                    intentCam.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageUriFile));
                }
            }
            else
            {
                Log.e(LOG_TAG, "Method: intentCamCapturePhoto photoDir was empty");
            }
        }

        frag.startActivityForResult(intentCam, RequestCode);

        return imageUriFile;
    }

    public static void geoTagWithNetworkLocation(Activity activity, File photoFile)
    {
        LocationManager locMang = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        Location loc = locMang.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        double locLat = loc.getLatitude();
        double locLon = loc.getLongitude();

        //Edit file's Exif header with location data tag
        ExifInterface exif;

        try
        {
            exif = new ExifInterface(photoFile.getAbsolutePath());

            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, GPSLocConverter.convert(locLat));
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, GPSLocConverter.convert(locLon));
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, GPSLocConverter.latitudeRef(locLat));
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, GPSLocConverter.longitudeRef(locLon));
            exif.saveAttributes();

        }
        catch(IOException ioe)
        {
            Log.e(LOG_TAG, "geoTagWithNetworkLocation: An io exception has occurred: " + ioe.getLocalizedMessage());
            ioe.printStackTrace();
        }
    }

    public static void geoTagWithGpsLocation(Activity activity, File photoFile)
    {
        LocationManager locMang = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        Location loc = locMang.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        double locLat = loc.getLatitude();
        double locLon = loc.getLongitude();

        //Edit file's Exif header with location data tag
        ExifInterface exif;

        try
        {
            exif = new ExifInterface(photoFile.getAbsolutePath());

            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, GPSLocConverter.convert(locLat));
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, GPSLocConverter.convert(locLon));
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, GPSLocConverter.latitudeRef(locLat));
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, GPSLocConverter.longitudeRef(locLon));
            exif.saveAttributes();

        }
        catch(IOException ioe)
        {
            Log.e(LOG_TAG, "geoTagWithGpsLocation: An io exception has occurred: " + ioe.getLocalizedMessage());
            ioe.printStackTrace();
        }
    }

    public static void geoTagWithGpsLocation(Fragment frag, File photoFile)
    {
        LocationManager locMang = (LocationManager) frag.getContext().getSystemService(Context.LOCATION_SERVICE);
        Location loc = locMang.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        double locLat = loc.getLatitude();
        double locLon = loc.getLongitude();

        //Edit file's Exif header with location data tag
        ExifInterface exif;

        try
        {
            exif = new ExifInterface(photoFile.getAbsolutePath());

            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, GPSLocConverter.convert(locLat));
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, GPSLocConverter.convert(locLon));
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, GPSLocConverter.latitudeRef(locLat));
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, GPSLocConverter.longitudeRef(locLon));
            exif.saveAttributes();

        }
        catch(IOException ioe)
        {
            Log.e(LOG_TAG, "geoTagWithGpsLocation: An io exception has occurred: " + ioe.getLocalizedMessage());
            ioe.printStackTrace();
        }
    }
}
