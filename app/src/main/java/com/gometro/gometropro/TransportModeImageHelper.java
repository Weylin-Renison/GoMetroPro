package com.gometro.gometropro;

import android.content.Context;
import android.widget.ImageView;

public class TransportModeImageHelper
{
	String transportMode;
	ImageView imgvTransportMode;
	Context context;
	
	public TransportModeImageHelper(Context context ,String transportMode, ImageView imgvTransportMode)
	{
		this.context = context;
		this.transportMode = transportMode;
		this.imgvTransportMode = imgvTransportMode;
	}
	
	public void populateTransportModeImage()
	{
		if(transportMode.equalsIgnoreCase("Bus"))
			imgvTransportMode.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_bus));
		else if(transportMode.equalsIgnoreCase("Rail"))
			imgvTransportMode.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_train));
		else if(transportMode.equalsIgnoreCase("Minibus Taxi"))
			imgvTransportMode.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_minibus));
		else if(transportMode.equalsIgnoreCase("Tram / Light Rail"))
			imgvTransportMode.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_tram));
		else if(transportMode.equalsIgnoreCase("Ferry"))
			imgvTransportMode.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_ferry));
		else if(transportMode.equalsIgnoreCase("Subway / Metro"))
			imgvTransportMode.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_subway));
		else if(transportMode.equalsIgnoreCase("Cable Car"))
			imgvTransportMode.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_cable_car));
		else if(transportMode.equalsIgnoreCase("Funicular"))
			imgvTransportMode.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_funicular));
		else if(transportMode.equalsIgnoreCase("other"))
			imgvTransportMode.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_other));
		
	}

	public void populateBigTransportModeImage()
	{
		if(transportMode.equalsIgnoreCase("Bus"))
			imgvTransportMode.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_bus_big));
		else if(transportMode.equalsIgnoreCase("Rail"))
			imgvTransportMode.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_train_big));
		else if(transportMode.equalsIgnoreCase("Minibus Taxi"))
			imgvTransportMode.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_minibus_big));
		else if(transportMode.equalsIgnoreCase("Tram / Light Rail"))
			imgvTransportMode.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_tram_big));
		else if(transportMode.equalsIgnoreCase("Ferry"))
			imgvTransportMode.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_ferry_big));
		else if(transportMode.equalsIgnoreCase("Subway / Metro"))
			imgvTransportMode.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_subway_big));
		else if(transportMode.equalsIgnoreCase("Cable Car"))
			imgvTransportMode.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_cable_car_big));
		else if(transportMode.equalsIgnoreCase("Funicular"))
			imgvTransportMode.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_funicular_big));
		else if(transportMode.equalsIgnoreCase("other"))
			imgvTransportMode.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_other));

	}
}
