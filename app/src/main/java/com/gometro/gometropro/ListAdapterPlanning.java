package com.gometro.gometropro;

import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ListAdapterPlanning extends ArrayAdapter<String[]>
{
	int layoutResourceId;
	//ArrayList<CheckBox> cbSelectedRows = new ArrayList<CheckBox>();
	ArrayList<String[]> items;
	Context context;
	
	public ListAdapterPlanning(Context context, int layoutResourceId, ArrayList<String[]> items)
	{
		super(context, layoutResourceId, items);
		this.layoutResourceId = layoutResourceId;
		this.items = items;
		this.context = context;
	}	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View constructedView;
		
		//Inflate layout
		LayoutInflater inflater = LayoutInflater.from(context);
		constructedView = inflater.inflate(layoutResourceId, null);
		
		//populate data
		String routeName = items.get(position)[1];
		String transportMode = items.get(position)[2];
		String transportCompany = items.get(position)[3];
		String startPoint = items.get(position)[4];
		String endPoint = items.get(position)[5];
		
		TextView txtvRouteName = (TextView) constructedView.findViewById(R.id.txtvRouteNameHeading);
		TextView txtvTransportMode = (TextView) constructedView.findViewById(R.id.txtvTransportMode);
		TextView txtvTransportCompany = (TextView) constructedView.findViewById(R.id.txtvTransportCompany);
		TextView txtvStartPoint = (TextView) constructedView.findViewById(R.id.txtvStartPoint);
		TextView txtvEndPoint = (TextView) constructedView.findViewById(R.id.txtvEndPoint);
		ImageView imgvTransportMode = (ImageView) constructedView.findViewById(R.id.imgvTransportMode);
		
		txtvRouteName.setText(routeName);
		txtvTransportMode.setText(transportMode);
		txtvTransportCompany.setText(transportCompany);
		txtvStartPoint.setText(startPoint);
		txtvEndPoint.setText(endPoint);
		
		TransportModeImageHelper imgHelper = new TransportModeImageHelper(context, transportMode, imgvTransportMode);
		imgHelper.populateTransportModeImage();
				
		return constructedView;
	}
	
//	public boolean isChecked(int position)
//	{
//		boolean checkedState = false;		
//		checkedState = cbSelectedRows.get(position).isChecked();		
//		return checkedState;
//	}
	
	public void populateImage(String transportMode ,ImageView imgvTransportMode)
	{
//		String transportMode = items.get(position)[1];
//		String transportCompany = items.get(position)[2];
//		String[] transportCompanies;
//		TypedArray companiesDrawables;
		
		if(transportMode.equalsIgnoreCase("Bus"))
			imgvTransportMode.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_bus));
			
//			//Search for company logo with parallel array set
//			transportCompanies = context.getResources().getStringArray(R.array.transport_bus_companies_array);
//			companiesDrawables = context.getResources().obtainTypedArray(R.array.transport_bus_companies_drawables_array);
//			int searchCompanyIndex = -1;
//			Log.w("populateImages", "Searching for company");
//			for(int i = 0; i < transportCompanies.length; i++)
//			{
//				if(transportCompany.equalsIgnoreCase(transportCompanies[i]))
//					searchCompanyIndex = i-1;
//			}
//			Log.w("populateImages", "Search results: " + searchCompanyIndex);
//			if(searchCompanyIndex != -1)
//				imgvTransportCompany.setImageDrawable(context.getResources().getDrawable(companiesDrawables.getResourceId(searchCompanyIndex, -1)));
//			else
//				imgvTransportCompany.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_broken_image));
//		}
		else if(transportMode.equalsIgnoreCase("train"))
			imgvTransportMode.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_train));
			
//			//Search for company logo with parallel array set
//			transportCompanies = context.getResources().getStringArray(R.array.transport_bus_companies_array);
//			companiesDrawables = context.getResources().obtainTypedArray(R.array.transport_bus_companies_drawables_array);
//			int searchCompanyIndex = -1;
//			
//			for(int i = 0; i < transportCompanies.length; i++)
//			{
//				if(transportCompany.equalsIgnoreCase(transportCompanies[i]))
//					searchCompanyIndex = i;
//			}
//			
//			if(searchCompanyIndex != -1)
//				imgvTransportCompany.setImageDrawable(context.getResources().getDrawable(companiesDrawables.getResourceId(searchCompanyIndex, -1)));
//			else
//				imgvTransportCompany.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_broken_image));
//		}
	}
}
