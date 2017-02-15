package com.gometro.gometropro;

import android.location.Location;

import java.io.File;
import java.util.List;

public class RouteStop
{

	public Location	location;
	public long		arrivalTime;
	public long		departureTime;
	public int		board;
	public int		alight;
	public String   stopName;
	public float	stopFarePrice;
	public String	currency;
	public File photo;
	public List<String> stopTimes;

}