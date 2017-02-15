package com.gometro.gometropro;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.gometro.gometropro.GoMappProtos.Upload;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class UploadActivity extends AppCompatActivity implements OnItemClickListener
{
	private CoordinatorLayout cordLayWrapper;
	private TextView txtvUploadCount;
	private ListView lstvCapturedRoutes;
	private ListAdapterCaptured lstAdapter;
	public List<Upload.Route> capturedRoutes;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upload);
		
		//deleteAllCapturedRoutes();		

		cordLayWrapper = (CoordinatorLayout) findViewById(R.id.cordLayUploadWrapper);
		txtvUploadCount = (TextView) findViewById(R.id.txtvUploadCount);

		//Show list of captured routes
		lstvCapturedRoutes = (ListView) findViewById(R.id.lstvUploadRoutes);
		updateList();						
			
//			int routeIndexToUpload = -1;
//			for(int i = 0; i < capturedRoutes.size(); i++)
//			{
//				Toast.makeText(this, "*" + capturedRoutes.get(i).getRouteName() + "*", Toast.LENGTH_LONG).show();
//				if(capturedRoutes.get(i).getRouteName().equalsIgnoreCase("ios-testing"))
//				{					
//					routeIndexToUpload = i;
//				}
//			}
//				
//			Toast.makeText(this, "*" + routeIndexToUpload + "*", Toast.LENGTH_LONG).show();	
//			//Upload first route for now
//			uploadRouteToServer(capturedRoutes.get(3));
		
		//Hide status notification bar
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

    public void onClickHistory(View view)
    {
      Intent historyIntent = new Intent(UploadActivity.this, HistoryActivity.class);
      historyIntent.putExtra("fromUploads", true);
      startActivity(historyIntent);
    }
	
	private void updateList()
	{
		//Read data
		capturedRoutes = readCapturedRoutes();
		//Toast.makeText(this, "Captured list size: " + capturedRoutes.size(), Toast.LENGTH_LONG).show();
		
		if(capturedRoutes.size() == 0)
			Snackbar.make(cordLayWrapper, "There are no captured routes", Snackbar.LENGTH_LONG).show();
//			Toast.makeText(this, "There are no captured routes", Toast.LENGTH_LONG).show();
		
		//Populate data
		lstAdapter = new ListAdapterCaptured(this, R.layout.captured_route_item, capturedRoutes);
		lstvCapturedRoutes.setAdapter(lstAdapter);
		lstvCapturedRoutes.setOnItemClickListener(this);
			
		registerForContextMenu(lstvCapturedRoutes);

		txtvUploadCount.setText(lstAdapter.getCount() + "");
			
	}
	
	private List<Upload.Route> readCapturedRoutes()
	{
		List<Upload.Route> capturedRoutes = new ArrayList<Upload.Route>();
		
		File dir = getFilesDir();
		
//		if(!dir.isDirectory())
//			dir.mkdir();
		
		// Check for upload data
		File[] capturedRouteFiles = dir.listFiles();
		int noOfFiles = capturedRouteFiles.length;
		//Toast.makeText(this, "No of files: " + noOfFiles, Toast.LENGTH_LONG).show();
		
		if (noOfFiles == 0)
		{
			Snackbar.make(cordLayWrapper, "No data to upload", Snackbar.LENGTH_LONG).show();
//			Toast.makeText(this, "No data to upload.", Toast.LENGTH_LONG).show();
		}
		else
		{
			FileInputStream is = null;
			Upload.Route readUploadRoute;
			
			try
			{
				for(int i = 0; i < noOfFiles; i++)
				{
					//Toast.makeText(this, "File Name: " + capturedRouteFiles[i].getName(),  Toast.LENGTH_LONG).show();
					
					if(capturedRouteFiles[i].getName().contains(".pb"))
					{
						is = new FileInputStream(capturedRouteFiles[i]);					
						readUploadRoute = Upload.Route.parseDelimitedFrom(is);										
						capturedRoutes.add(readUploadRoute);
						is.close();
					}
				}					
			}
			catch(IOException ioe)
			{
				ioe.printStackTrace();
			}
		}
		
		return capturedRoutes;
	}
	
	@Override
	public void onCreateContextMenu(android.view.ContextMenu menu, View v, android.view.ContextMenu.ContextMenuInfo menuInfo) 
	{
		if(v.getId() == R.id.lstvUploadRoutes)
		{
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			
			LayoutInflater inflater = LayoutInflater.from(this);
			View header = inflater.inflate(R.layout.context_menu_header, null);
			TextView headerTitle = (TextView) header.findViewById(R.id.txtvContextMenuHeaderTitle);
			
			headerTitle.setText(capturedRoutes.get(info.position).getRouteName());
			menu.setHeaderView(header);
			menu.add(Menu.NONE, 0, 0, "Upload");
			menu.add(Menu.NONE, 1, 1, "View");
			menu.add(Menu.NONE, 2, 2, "Delete");
		}
	}
	
	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) 
	{
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();		
		int menuItemIndex = item.getItemId();
		
		switch(menuItemIndex)
		{
			case 0:
                //Upload
                buildUploadConfirmation(info.position);
				break;
				
			case 1:
                //View
                Intent viewIntent = new Intent(UploadActivity.this, GoogleMapActivity.class);
                viewIntent.putExtra("routeName", capturedRoutes.get(info.position).getRouteName());
                startActivity(viewIntent);
                break;
				
			case 2:
				//Delete
				buildDeleteConfirmation(info.position);
				break;
		}
		
		return true;		
	}



	private void buildUploadConfirmation(final int routeIndex)
	{
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to upload this route?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener()
		{
			public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id)
			{
				FragDiagProjectCode diagFragProjCode = new FragDiagProjectCode();

				Bundle args = new Bundle();
				args.putInt("routeDataIndex", routeIndex);
				diagFragProjCode.setArguments(args);

				diagFragProjCode.show(getSupportFragmentManager(), "diagFragProjCode");
			}
		}).setNegativeButton("No", new DialogInterface.OnClickListener()
		{
			public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id)
			{
				dialog.dismiss();
			}

		});

		final AlertDialog alert = builder.create();
		alert.show();
	}

	private void buildDeleteConfirmation(final int routeIndex)
	{
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to delete this route?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener()
		{
			public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id)
			{
				deleteCapturedRoute(routeIndex);
			}
		}).setNegativeButton("No", new DialogInterface.OnClickListener()
		{
			public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id)
			{
				dialog.dismiss();
			}

		});

		final AlertDialog alert = builder.create();
		alert.show();
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3)
	{		
			view.showContextMenu();
	}
		
	public void uploadRouteToServer(int pos, final FragDiagProjectCode diag)
	{
		Upload.Route routeToUpload = capturedRoutes.get(pos);

		final int position = pos;
		String imei = null;

		// Get imei
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		imei = telephonyManager.getDeviceId();
		
		Upload.Builder uploadBuilder = Upload.newBuilder();
		uploadBuilder.setUnitId(0l);
		uploadBuilder.setUploadId(0);
		
		uploadBuilder.addRoute(routeToUpload);
		
		ByteArrayInputStream dataStream = new ByteArrayInputStream(uploadBuilder.build().toByteArray());

		List<Upload.Route.Stop> stopLst = routeToUpload.getStopList();

		for(int i = 0; i < stopLst.size(); i++)
			Log.d("StopList", "Stop " + i + " Name = " + stopLst.get(i).getStopName());
		
//		int[] arrayOfBytesForByron = new int[1000];
//		int oneByteRead = 0;
//		int counter = 0;
//		String stringOfBytesForByron = "[";
//		
//		while(oneByteRead != -1)
//		{
//			oneByteRead = dataStream.read();
//			arrayOfBytesForByron[counter] = oneByteRead;
//			stringOfBytesForByron += "," + oneByteRead;
//			counter++;
//		}
//		
//		stringOfBytesForByron += "]";
//		
//		Log.i("int of byte array for byron: ", stringOfBytesForByron);
		
		//Send for upload to server
		RequestParams params = new RequestParams();
		params.put("imei", imei);
		params.put("data", dataStream);	
		
		//Get url
		SharedPreferences prefMang = PreferenceManager.getDefaultSharedPreferences(this);
		String URL_BASE = prefMang.getString("URL_BASE", null);
		String API_UPLOAD = prefMang.getString("API_UPLOAD", null);
		
		AsyncHttpClient client = new AsyncHttpClient();
		client.setTimeout(240 * 1000);
		client.setUserAgent("android");
		client.post(URL_BASE + API_UPLOAD, params,		//192.168.0.29 home URL_BASE + API_UPLOAD for server (54.68.55.70)
				new AsyncHttpResponseHandler()
				{					
					@Override
					public void onSuccess(String response)
					{
                        moveRouteToHistory(position);
						diag.resolveUpload(true);
                        //deleteCapturedRoute(position);
						Snackbar.make(cordLayWrapper, "Data uploaded!\nMoved route to history", Snackbar.LENGTH_LONG).show();
//						Toast.makeText(UploadActivity.this, "Data uploaded!\nMoved route to history",Toast.LENGTH_SHORT).show();
					}

					public void onFailure(Throwable error, String content)
					{
						diag.resolveUpload(false);
						Log.e("upload", "Upload failed: " + error + " " + content);
						Snackbar.make(cordLayWrapper, "Unable to upload data, check network connection", Snackbar.LENGTH_LONG).show();
//						Toast.makeText(UploadActivity.this,	"Unable to upload data, check network connection.",	Toast.LENGTH_SHORT).show();
					}
				});

		//Upload stop photos TODO: Link this propperly per stop for now upload all stop photos, will be matched on server vai coords date and time ect

		File photoDir = new File(Environment.getExternalStorageDirectory() + "/GoMetroPro/Photos/");
		File [] photoFiles = photoDir.listFiles();

		if(photoFiles != null)
		{
			for(int i = 0; i < photoFiles.length; i++)
			{
				File photo = photoFiles[i];

				if(photo.getName().contains("cam"))
					if(i == photoFiles.length)
						uploadStopPhoto(photo, true);
					else
						uploadStopPhoto(photo, false);
			}
		}
	}

	private void uploadStopPhoto(final File stopPhoto, final boolean lastUpload)
	{
		//Get url
		//Get url
		SharedPreferences prefMang = PreferenceManager.getDefaultSharedPreferences(this);
		String URL_BASE = prefMang.getString("URL_BASE", null);
		String API_UPLOAD_STOP_PHOTO = prefMang.getString("API_UPLOAD_STOP_PHOTO", null);

		RequestParams params = new RequestParams();

		try
		{
			params.put("stopPhoto", stopPhoto);
		} catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}

		AsyncHttpClient client = new AsyncHttpClient();
		client.setTimeout(240 * 1000);
		client.setUserAgent("android");

		client.post(URL_BASE + API_UPLOAD_STOP_PHOTO, params ,new AsyncHttpResponseHandler()
		{
			@Override
			public void onSuccess(int j, String s)
			{
				Log.i("stopPhotoUpload", "Success");
				stopPhoto.delete();

				if(lastUpload)
				{
					Snackbar.make(cordLayWrapper, "Stop Photos Successfully Uploaded!", Snackbar.LENGTH_LONG).show();
//					Toast.makeText(UploadActivity.this, "Stop Photos Successfully Uploaded!", Toast.LENGTH_LONG).show();
				}
			}

			@Override
			public void onFailure(Throwable throwable, String s)
			{
				Log.i("stopPhotoUpload", "Fail");
			}
		});
	}


    private void moveRouteToHistory(int postition)
    {
        File dir = getFilesDir();

        File[] allFiles = dir.listFiles();
        List<File> capturedRoutes = new ArrayList<File>();

        for(int i = 0; i < allFiles.length; i++)
            if(allFiles[i].getName().contains(".pb"))
                capturedRoutes.add(allFiles[i]);

        if(capturedRoutes.size() != 0)
        {
           String fileName = capturedRoutes.get(postition).getName();

            File dirHistory = new File(getFilesDir() + "/History/");

            if(!dirHistory.isDirectory())
                dirHistory.mkdirs();

            File historyFile = new File(dirHistory, fileName);

            capturedRoutes.get(postition).renameTo(historyFile);
        }

        updateList();

    }

    private void readHistory()
    {
        File[] allFiles = (new File(getFilesDir() + "/History/")).listFiles();

        String fileNames = "File Names: ";
        for(int i = 0; i < allFiles.length; i++)
            fileNames += "\n" +  allFiles[i].getName();

		Snackbar.make(cordLayWrapper, fileNames, Snackbar.LENGTH_LONG).show();
//        Toast.makeText(this, fileNames, Toast.LENGTH_LONG).show();
    }
	
	private void deleteCapturedRoute(int postition)
	{
		File dir = getFilesDir();
		
		File[] allFiles = dir.listFiles();
		List<File> capturedRoutes = new ArrayList<File>();
		
		for(int i = 0; i < allFiles.length; i++)
			if(allFiles[i].getName().contains(".pb"))
				capturedRoutes.add(allFiles[i]);
		
		if(capturedRoutes.size() != 0)
			capturedRoutes.get(postition).delete();
		
		updateList();
		
	}
	
	private void deleteAllCapturedRoutes()
	{
		File dir = getFilesDir();
		
		File[] capturedRoutes = dir.listFiles();
		
		for(int i = 0; i < capturedRoutes.length; i++)
			if(capturedRoutes[i].getName().contains(".pb"))
				capturedRoutes[i].delete();
	}
}
