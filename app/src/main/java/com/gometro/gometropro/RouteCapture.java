package com.gometro.gometropro;

import java.util.ArrayList;

import java.util.List;

import com.gometro.gometropro.GoMappProtos.Upload;
import com.gometro.gometropro.GoMappProtos.Upload.Route.Point;
import com.gometro.gometropro.GoMappProtos.Upload.Route.Stop;

import android.location.Location;

public class RouteCapture
{

	public Integer		id;
	
	//App requires
	public String		routeName;
	public String		mapperName;
	public String		projectCode;
	public String		transportMode;
	public String		transportCompany;
	public String		startPoint;	//Descriptor
	public String		endPoint;	//Descriptor
	
	//App optionals
	public boolean		oneWay;	//if the route is only one way
	public String		commonReasonForTrip;	//Passengers common reason for trip
	public float		ratingDrivingStyle;
	public float		ratingSaftey;
	public float		ratingComfort;
	public String		routeComments;

	//public long			startMs;
	public long			startTime;
	public long			duration;

	public Integer		totalPassengerCount	= 0;
	public Integer		alightCount			= 0;
	public Integer		boardCount			= 0;

	public Long			distance			= 0l;

	List<RoutePoint>	points				= new ArrayList<RoutePoint>();

	List<RouteStop>		stops				= new ArrayList<RouteStop>();

	public void setRouteName(String routeName)
	{

		if (routeName.equals(""))
			this.routeName = "Route " + id;
		else
			this.routeName = routeName;
	}

	public static RouteCapture deseralize(Upload.Route r)
	{

		RouteCapture route = new RouteCapture();

		route.routeName = r.getRouteName();
		route.projectCode = r.getProjectCode();
		route.mapperName = r.getMapperName();
		route.transportMode = r.getTransportMode();
		route.transportCompany = r.getTransportCompany();
		route.startPoint = r.getStartPoint();
		route.endPoint = r.getEndPoint();
		route.routeComments = r.getRouteComments();

		route.startTime = r.getStartTime();

		long lastTimepoint = route.startTime;

		for (Point p : r.getPointList())
		{
			RoutePoint rp = new RoutePoint();
			rp.location = new Location("GPS");
			rp.location.setLatitude(p.getLat());
			rp.location.setLongitude(p.getLon());
			route.points.add(rp);
		}

//		lastTimepoint = route.startTime;

		for (Stop s : r.getStopList())
		{
			RouteStop rs = new RouteStop();
			rs.location = new Location("GPS");
			rs.location.setLatitude(s.getLat());
			rs.location.setLongitude(s.getLon());
			rs.arrivalTime = (s.getArrivalTime());
			rs.stopName = s.getStopName();

//			lastTimepoint = rs.arrivalTime;

			rs.board = s.getBoard();
			rs.alight = s.getAlight();

			route.stops.add(rs);
		}

		return route;

	}

	public Upload.Route seralize()
	{

		Upload.Route.Builder route = Upload.Route.newBuilder();
		route.setRouteName(routeName);
		route.setMapperName(mapperName);
		route.setTransportMode(transportMode);
		route.setTransportCompany(transportCompany);
		route.setStartPoint(startPoint);
		route.setEndPoint(endPoint);
		route.setStartTime(startTime);
		
		route.setDuration(duration);
		route.setDistance(distance);
		
		route.setOneWay(oneWay);
		route.setCommonReasonForTrip(commonReasonForTrip);
		route.setRatingDrivingStyle(ratingDrivingStyle);
		route.setRatingComfort(ratingComfort);
		route.setRatingSaftey(ratingSaftey);
		route.setRouteComments(routeComments);
		

		//long lastTimepoint = startMs;

		for (RoutePoint rp : points)
		{
			Upload.Route.Point.Builder point = Upload.Route.Point.newBuilder();
			point.setLat((float) rp.location.getLatitude());
			point.setLon((float) rp.location.getLongitude());
			//point.setTimeoffset((int) ((rp.time - lastTimepoint) / 1000));
			//lastTimepoint = rp.time;

			route.addPoint(point);
		}

		//lastTimepoint = startMs;

		for (RouteStop rs : stops)
		{

			Upload.Route.Stop.Builder stop = Upload.Route.Stop.newBuilder();

			stop.setLat((float) rs.location.getLatitude());
			stop.setLon((float) rs.location.getLongitude());
			stop.setArrivalTime(rs.arrivalTime);
			stop.setDepartureTime(rs.departureTime);
			stop.setStopName(rs.stopName);
			//stop.setDepartureTimeoffset((int) ((rs.departureTime - lastTimepoint) / 1000));
			stop.setFareProce(rs.stopFarePrice);
			stop.setCurrency(rs.currency);
			stop.setAlight(rs.alight);
			stop.setBoard(rs.board);
			//lastTimepoint = rs.arrivalTime;

			route.addStop(stop);
		}

		return route.build();
	}
}
