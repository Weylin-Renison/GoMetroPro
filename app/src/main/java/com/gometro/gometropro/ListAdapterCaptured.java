package com.gometro.gometropro;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.gometro.gometropro.GoMappProtos.Upload;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ListAdapterCaptured extends ArrayAdapter<Upload.Route>
{
	
	private Context context;
	private int layoutResource;
	private List<Upload.Route> items;

	public ListAdapterCaptured(Context context, int layoutResource, List<Upload.Route> items)
	{
		super(context, layoutResource, items);
		this.context = context;
		this.layoutResource = layoutResource;
		this.items = items;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View constructedView = null;
		
		//Inflate layout
		LayoutInflater inflater = LayoutInflater.from(context);
		constructedView = inflater.inflate(layoutResource, null);
		
		TextView txtvRouteName = (TextView) constructedView.findViewById(R.id.txtvRouteNameHeading);
		TextView txtvTransportMode = (TextView) constructedView.findViewById(R.id.txtvTransportMode);
		TextView txtvTransportCompany = (TextView) constructedView.findViewById(R.id.txtvTransportCompany);
		TextView txtvStartPoint = (TextView) constructedView.findViewById(R.id.txtvStartPoint);
		TextView txtvEndPoint = (TextView) constructedView.findViewById(R.id.txtvEndPoint);
		ImageView imgvTransportMode = (ImageView) constructedView.findViewById(R.id.imgvTransportMode);
		TextView txtvTime = (TextView) constructedView.findViewById(R.id.txtvCaptureTime);
		TextView txtvStops = (TextView) constructedView.findViewById(R.id.txtvStops);
		TextView txtvDistance = (TextView) constructedView.findViewById(R.id.txtvDistance);
		
		txtvRouteName.setText(items.get(position).getRouteName());
		txtvTransportMode.setText(items.get(position).getTransportMode());
		txtvTransportCompany.setText(items.get(position).getTransportCompany());
		txtvStartPoint.setText(items.get(position).getStartPoint());
		txtvEndPoint.setText(items.get(position).getEndPoint());
		
		TransportModeImageHelper imgHelper = new TransportModeImageHelper(context, items.get(position).getTransportMode(), imgvTransportMode);
		imgHelper.populateTransportModeImage();
		
		//Convert time to human form
		Date date = new Date(items.get(position).getDuration());
		SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss");
		String duration = dateFormatter.format(date);
		
		txtvTime.setText(duration);
		txtvStops.setText("" + items.get(position).getStopCount());
		txtvDistance.setText(items.get(position).getDistance() + "Km");
		
		return constructedView;
	}

}
