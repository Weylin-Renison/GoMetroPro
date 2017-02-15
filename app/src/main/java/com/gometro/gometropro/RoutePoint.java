package com.gometro.gometropro;

import android.location.Location;

public class RoutePoint
{

	public Location	location;
	public long		time;
	
	public RoutePoint()
	{
		
	}
	
	public RoutePoint(Location location, long time)
	{
		this.location = location;
		this.time = time;
	}
	
	public Location getLocation()
	{
		return location;
	}
	
	public long getTime()
	{
		return time;
	}
}