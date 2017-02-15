package com.gometro.gometropro;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wprenison on 13/04/16.
 */
public class FragDiagUploadPhoto extends AppCompatDialogFragment
{
    MainActivity activity;
    DialogFragment thisDiagFrag = this;
    Spinner spnrProjectCode;
    ProgressBar progbProjectCode;
    EditText etxtProjectCodeError;
    EditText etxtPhotoDescription;
    ImageView imgvPhotoPreview;
    Button btnAddPhoto;
    Button btnUpload;

    ListAdapterProjectCode lstAdapterProjCode;
    private File photoFile;

    private static final int REQ_CODE_CAPTURE_PHOTO = 1001;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View constructedView = inflater.inflate(R.layout.custom_upload_photo_dialog, container, false);

        //Get handle on views
        spnrProjectCode = (Spinner) constructedView.findViewById(R.id.spnrProjectCode);
        progbProjectCode = (ProgressBar) constructedView.findViewById(R.id.progbProjectCode);
        etxtProjectCodeError = (EditText) constructedView.findViewById(R.id.txtvProjectCodeError);
        etxtPhotoDescription = (EditText) constructedView.findViewById(R.id.etxtUploadPhotoDescriptionDialogValue);
        imgvPhotoPreview = (ImageView) constructedView.findViewById(R.id.imgvUploadPhotoDialogImagePreview);
        btnAddPhoto = (Button) constructedView.findViewById(R.id.btnStopNameDialogAddPhoto);
        btnUpload = (Button) constructedView.findViewById(R.id.btnUploadPhoto);

        return constructedView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        activity = (MainActivity) getActivity();
        init();
    }

    private void init()
    {
//        DisplayMetrics metrics = getResources().getDisplayMetrics();
//        int width = metrics.widthPixels;
//        int height = metrics.heightPixels;
//
//        getDialog().getWindow().setLayout((6 * width)/7, (height)/3);

        getDialog().setTitle("Upload Photo");
        getAvailableProjects();

        //set on click
        btnAddPhoto.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //Photo intent thing
                CamHelper.intentCamCapturePhoto(thisDiagFrag, REQ_CODE_CAPTURE_PHOTO, Environment.getExternalStorageDirectory() + "/GoMetroPro/Photos/", "Photo.jpg");
            }
        });

        imgvPhotoPreview.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                CamHelper.intentCamCapturePhoto(thisDiagFrag, REQ_CODE_CAPTURE_PHOTO, Environment.getExternalStorageDirectory() + "/GoMetroPro/Photos/", "Photo.jpg");
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //Upload pic to server api
                if(validate())
                {
                    uploadPhoto();
                }
            }
        });
    }

    //Check server for available project codes
    private void getAvailableProjects()
    {
        final List<String> lstProjects = new ArrayList<>();
        lstProjects.add("Project Code");
		/*lstProjects.add("ZI0MW2");
		lstProjects.add("ZI0MW2");
		lstProjects.add("CM76AL");
		lstProjects.add("QHJ5U1");*/

        //Get url
        SharedPreferences prefMang = PreferenceManager.getDefaultSharedPreferences(activity);
        String URL_BASE = prefMang.getString("URL_BASE", null);
        String API_GET_AVAILABLE_PROJECTS = prefMang.getString("API_GET_AVAILABLE_PROJECTS", null);

        //Make Get request
        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(240 * 1000);
        client.setUserAgent("android");
        client.get(URL_BASE + API_GET_AVAILABLE_PROJECTS, new AsyncHttpResponseHandler()
        {
            @Override
            public void onSuccess(int j, String s)
            {
                try
                {
                    Log.d("FragDiagProjectCode", "success project code check: " + s);
                    JSONArray jsonAvailableProjects = new JSONArray(s);

                    if (jsonAvailableProjects.length() > 0)
                    {

                        for (int i = 0; i < jsonAvailableProjects.length(); i++)
                            lstProjects.add(jsonAvailableProjects.getString(i));

                        //Set spinner data and adapter then make it visible
                        lstAdapterProjCode = new ListAdapterProjectCode(activity, R.layout.custom_spinner_item_centered, lstProjects);
                        spnrProjectCode.setAdapter(lstAdapterProjCode);
                        progbProjectCode.setVisibility(View.GONE);
                        spnrProjectCode.setVisibility(View.VISIBLE);

                    }
                } catch (JSONException je)
                {
                    progbProjectCode.setVisibility(View.GONE);
                    je.printStackTrace();
                }
            }

            @Override
            public void onFailure(Throwable throwable, String s)
            {
                Log.d("NewRouteActivity", "failed project code check: " + s);
                progbProjectCode.setVisibility(View.GONE);
                super.onFailure(throwable, s);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        //Capture Camera
        if(requestCode == REQ_CODE_CAPTURE_PHOTO)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                //Set image preview
                photoFile = new File(Environment.getExternalStorageDirectory() + "/GoMetroPro/Photos/photo.jpg");
                imgvPhotoPreview.setImageBitmap(null);
                imgvPhotoPreview.setImageURI(Uri.fromFile(photoFile));
                imgvPhotoPreview.setVisibility(View.VISIBLE);
                btnAddPhoto.setVisibility(View.GONE);

                CamHelper.geoTagWithNetworkLocation(activity, photoFile);
            }

        }
    }

    private boolean validate()
    {

        boolean valid = true;

        int projCodeSelectedIndex = spnrProjectCode.getSelectedItemPosition();

        if(projCodeSelectedIndex == 0)
        {
            valid = false;
            etxtProjectCodeError.setError(getString(R.string.error_msg_select_an_item));
            etxtProjectCodeError.setVisibility(View.VISIBLE);
        }
        else
            etxtProjectCodeError.setVisibility(View.GONE);

        String description = "";
        description = etxtPhotoDescription.getText().toString();

        if(description.isEmpty() || description == "")
        {
            valid = false;
            etxtPhotoDescription.setError("Required");
        }

        if(imgvPhotoPreview.getVisibility() != View.VISIBLE)
        {
            valid = false;
            btnAddPhoto.setError("Required to add a photo");
        }

        return valid;
    }

    private void uploadPhoto()
    {
        //Get url
        SharedPreferences prefMang = PreferenceManager.getDefaultSharedPreferences(activity);
        String URL_BASE = prefMang.getString("URL_BASE", null);
        String API_UPLOAD_PHOTO = prefMang.getString("API_UPLOAD_PHOTO", null);

        //Make Get request
        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(240 * 1000);
        client.setUserAgent("android");

        //Prep perams
        String projectCode = spnrProjectCode.getSelectedItem().toString();
        String description = etxtPhotoDescription.getText().toString();

        RequestParams params = new RequestParams();
        params.put("projectCode", projectCode);
        params.put("description", description);

        try
        {
            params.put("photo", photoFile);
        } catch(FileNotFoundException e)
        {
            e.printStackTrace();
        }

        client.post(URL_BASE + API_UPLOAD_PHOTO, params ,new AsyncHttpResponseHandler()
        {
            @Override
            public void onSuccess(int j, String s)
            {
                Toast.makeText(activity, "Upload Successful", Toast.LENGTH_LONG).show();
                thisDiagFrag.dismiss();
            }

            @Override
            public void onFailure(Throwable throwable, String s)
            {
                Toast.makeText(activity, "Upload failed", Toast.LENGTH_LONG).show();
                thisDiagFrag.dismiss();
            }
        });
    }
}
