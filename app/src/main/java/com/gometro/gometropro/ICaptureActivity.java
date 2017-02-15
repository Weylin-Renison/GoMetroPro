package com.gometro.gometropro;

import java.util.List;

public interface ICaptureActivity
{
	
	public void onGpsProviderDisabled();
	
	public void updateGpsStatus(String statusMsg);
	
	public void updateGpsStatus(float accuracy);
	
	public void updateDistance(long distTraveled);
	
	public void saveRecordedRoute(List<RoutePoint> routePoints, List<RouteStop> routeStops, boolean serviceUnbinding);
	
	public void logFromGpsService(String msg);

	public void buildTagStopNameDialog();
}
