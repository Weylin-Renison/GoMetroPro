package com.gometro.gometropro;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

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
public class FragDiagProjectCode extends AppCompatDialogFragment
{
    UploadActivity activity;
    Spinner spnrProjectCode;
    ProgressBar progbProjectCode;
    EditText etxtProjectCodeError;
    Button btnDone;
    FragDiagProjectCode thisFragDiag = this;

    ListAdapterProjectCode lstAdapterProjCode;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View constructedView = inflater.inflate(R.layout.diag_frag_project_code, container, false);

        //Get handle on views
        spnrProjectCode = (Spinner) constructedView.findViewById(R.id.spnrProjectCode);
        progbProjectCode = (ProgressBar) constructedView.findViewById(R.id.progbProjectCode);
        etxtProjectCodeError = (EditText) constructedView.findViewById(R.id.txtvProjectCodeError);
        btnDone = (Button) constructedView.findViewById(R.id.btnProjectCodeDone);

        return constructedView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        activity = (UploadActivity)getActivity();
        init();
    }

    private void init()
    {

//        DisplayMetrics metrics = getResources().getDisplayMetrics();
//        int width = metrics.widthPixels;
//        int height = metrics.heightPixels;
//
//        getDialog().getWindow().setLayout((6 * width)/7, (height)/5);

        getDialog().setTitle("Project Code");
        getAvailableProjects();

        btnDone.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //Validate
                if (validate())
                {
                    //add project code to protobuf route

                    //Get args
                    int routeDataIndex = getArguments().getInt("routeDataIndex");
                    GoMappProtos.Upload.Route routeData = activity.capturedRoutes.get(routeDataIndex);

                    GoMappProtos.Upload.Route.Builder routeBuilder = routeData.toBuilder();
                    routeBuilder.setProjectCode(spnrProjectCode.getSelectedItem().toString());
                    routeData = routeBuilder.build();

                    //Adjust value in upload activity
                    activity.capturedRoutes.set(routeDataIndex, routeData);

                    if(!routeData.isInitialized())
                        Toast.makeText(activity, "Protobuf was not initialized correctly", Toast.LENGTH_LONG).show();

                    File routeFile = new File(activity.getFilesDir() , "route_" + routeData.getRouteName() + "_" + routeData.getStartTime() + ".pb");

                    //Write file & upload
                    FileOutputStream os;

                    try
                    {
                        os = new FileOutputStream(routeFile);
                        routeData.writeDelimitedTo(os);
                        os.flush();
                        os.close();

                        activity.uploadRouteToServer(routeDataIndex, thisFragDiag);

                        getDialog().setCancelable(false);
                        getDialog().setTitle("Uploading...");
                        spnrProjectCode.setVisibility(View.INVISIBLE);
                        progbProjectCode.setVisibility(View.VISIBLE);

                    } catch (FileNotFoundException e)
                    {
                        e.printStackTrace();
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void resolveUpload(boolean success)
    {
        if(!success)
        {
            getDialog().setTitle("Project Code");
            getDialog().setCancelable(true);
            spnrProjectCode.setVisibility(View.VISIBLE);
            progbProjectCode.setVisibility(View.INVISIBLE);
        }
        else
            getDialog().dismiss();
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

    private boolean validate()
    {

        boolean valid = true;

        int projCodeSelectedIndex = 0;

        if(spnrProjectCode.getVisibility() == View.VISIBLE)
            projCodeSelectedIndex = spnrProjectCode.getSelectedItemPosition();

        if(projCodeSelectedIndex == 0)
        {
            valid = false;
            etxtProjectCodeError.setError(getString(R.string.error_msg_select_an_item));
            etxtProjectCodeError.setVisibility(View.VISIBLE);
        }
        else
            etxtProjectCodeError.setVisibility(View.GONE);

        return valid;
    }
}
