package com.gometro.gometropro;

import android.content.Context;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CustomSpinnerAdapter extends ArrayAdapter<String>
{
	Context context;
	int LayoutResourceId;
	String[] data;
	String dataType;
	int screenWidth;
	int spacerWidth;

	public CustomSpinnerAdapter(Context context, int resource, int resourceLayoutId, String[] data, String dataType) 
	{
		super(context, resource, data);
		this.context = context;
		this.LayoutResourceId = resourceLayoutId;
		this.data = data;
		this.dataType = dataType;

		//Calculate spacer width on from screen width
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		screenWidth = size.x;
		spacerWidth = screenWidth / 6;
	}
	
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) 
	{
		View customView = null;
		
		if(dataType.equalsIgnoreCase("mode"))
			customView = getCustomViewTransporMode(position, convertView, parent);
		else
			Log.e("CustomSpinnerAdapter", "No data type defined");
		
		return customView;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		View customView = null;
		
		if(dataType.equalsIgnoreCase("mode"))
			customView = getCustomViewTransporMode(position, convertView, parent);
		else
			Log.e("CustomSpinnerAdapter", "No data type defined");
		
		return customView;
	}
	
	public View getCustomViewTransporMode(int position, View convertView, ViewGroup parent)
	{
		View constructedView;
		
		//Inflate layout
		LayoutInflater inflater = LayoutInflater.from(context);
		constructedView = inflater.inflate(LayoutResourceId, null);
		
		//Get handle on views
		ImageView ivTransportMode = (ImageView) constructedView.findViewById(R.id.ivTransportModeSpinner);
		TextView txtvTransporMode = (TextView) constructedView.findViewById(R.id.txtvTransporModeSpinner);
//		View spacer = (View) constructedView.findViewById(R.id.vTranspotModeSpacer);

//		spacer.setLayoutParams(new LinearLayout.LayoutParams(spacerWidth, 1));

		txtvTransporMode.setText(data[position]);
		
		TransportModeImageHelper imgHelper = new TransportModeImageHelper(context, data[position], ivTransportMode);
		imgHelper.populateTransportModeImage();
		
		return constructedView;
	}

}
