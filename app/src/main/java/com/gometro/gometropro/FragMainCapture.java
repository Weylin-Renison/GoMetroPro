package com.gometro.gometropro;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FragMainCapture extends Fragment
{
	private RelativeLayout relLayMainCaptureRoot;
	private ImageView ivTransporMode;
	private TextView txtvTransportCompany;
	private TextView txtvMapperName;
	private TextView txtvRouteName;
	private TextView txtvStartPoint;
	private TextView txtvEndPoint;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_main_capture, container, false);
		
		//Initialize data fields
		ivTransporMode = (ImageView) view.findViewById(R.id.ivTransportModeCapture);
		txtvTransportCompany = (TextView) view.findViewById(R.id.txtvCompanyCapture);
		txtvMapperName = (TextView) view.findViewById(R.id.txtvMapperNameCaptureHeading);
		txtvRouteName = (TextView) view.findViewById(R.id.txtvRouteNameCaptureHeading);
		txtvStartPoint = (TextView) view.findViewById(R.id.txtvFromCapture);
		txtvEndPoint = (TextView) view.findViewById(R.id.txtvToCapture);
		relLayMainCaptureRoot = (RelativeLayout) view.findViewById(R.id.relLayMainCapture);

		//Used to close keayboard after tagging a stop with a name, this receives focus first so that etxt fare does not
		relLayMainCaptureRoot.setOnFocusChangeListener(new View.OnFocusChangeListener()
		{
			@Override
			public void onFocusChange(View v, boolean hasFocus)
			{
				if(hasFocus)
				{
					InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(relLayMainCaptureRoot.getWindowToken(), 0);
				}
			}
		});
		
		//Catch arguments to populate data fields
		if(getArguments() != null)
		{
			Bundle bundleRouteData = getArguments();
			
			populateData(bundleRouteData.getString("mapperName"), bundleRouteData.getString("transportMode"), bundleRouteData.getString("transportCompany"), bundleRouteData.getString("routeName"),
					bundleRouteData.getString("startPoint"), bundleRouteData.getString("endPoint"));
		}
		
		return view;
	}
			
	public void populateData(String mapperName, String transportMode, String transportCompany, String routeName, String startPoint, String endPoint)
	{	
		Log.w("FragMainCapture",mapperName + ", " + transportMode + ", " + transportCompany + ", " + routeName + ", " + startPoint + ", " + endPoint);

		txtvMapperName.setText(mapperName);
		txtvTransportCompany.setText(transportCompany);
		txtvRouteName.setText(routeName);
		txtvStartPoint.setText(startPoint);
		txtvEndPoint.setText(endPoint);
		
		//Set image view transport mode
		TransportModeImageHelper imgHelper = new TransportModeImageHelper(getActivity(), transportMode, ivTransporMode);
		imgHelper.populateBigTransportModeImage();
	}

}
