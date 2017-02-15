package com.gometro.gometropro;

import java.util.ArrayList;

import android.*;
import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SavedRoutesActivity extends AppCompatActivity implements OnItemClickListener
{

	//Contains planned items
	public static Activity selectionActivity;
	private FileHelper fileHlp;
	private ListAdapterPlanning lstAdapter;
	private ArrayList<String[]> plans;
	private CoordinatorLayout cordLayWrapper;
	private ListView lstvPlanItems;
	private Intent captureIntent;

	private final int  REQ_CODE_EDIT = 1;
	private final int REQ_CODE_NEW = 2;
	private final int REQ_CODE_CAPTURE_COMPLETED = 3;
	private final int REQ_PERMISSION_FINE_LOC = 100;

	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_saved_routes);
		
		//selectionActivity = this;
		cordLayWrapper = (CoordinatorLayout) findViewById(R.id.cordLayWrapper);
		lstvPlanItems = (ListView) findViewById(R.id.lstvSavedRoutes);
				
		updateList();
		
		//Hide status notification bar
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}
	
	public void updateList()
	{
		//Read data
		fileHlp = new FileHelper(this);
		plans = fileHlp.readPlan();
				
		//Populate list
		lstAdapter = new ListAdapterPlanning(this, R.layout.route_item, plans);
		lstvPlanItems.setAdapter(lstAdapter);
		lstvPlanItems.setOnItemClickListener(this);
		//lstvPlanItems.setOnItemLongClickListener(this);
		registerForContextMenu(lstvPlanItems);
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu)
//	{
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.planning_selection, menu);
//		return true;
//	}
	
	@Override
	public void onCreateContextMenu(android.view.ContextMenu menu, View v, android.view.ContextMenu.ContextMenuInfo menuInfo) 
	{
		if(v.getId() == R.id.lstvSavedRoutes)
		{
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			
			LayoutInflater inflater = LayoutInflater.from(this);
			View header = inflater.inflate(R.layout.context_menu_header, null);
			TextView headerTitle = (TextView) header.findViewById(R.id.txtvContextMenuHeaderTitle);
			
			headerTitle.setText(plans.get(info.position)[1]);
			menu.setHeaderView(header);
            menu.add(Menu.NONE, 0, 0, "Capture");
			menu.add(Menu.NONE, 1, 1, "Edit");
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
                //Capture
                //Gather data from selected item
				String mapperName = plans.get(info.position)[0];
				String routeName = plans.get(info.position)[1];
                String transportMode = plans.get(info.position)[2];
                String transportCompany = plans.get(info.position)[3];
                String startPoint = plans.get(info.position)[4];
                String endPoint = plans.get(info.position)[5];

                Log.w("SavedRoutes", "Data: " + transportMode + " " + transportCompany + " " +routeName + " " + startPoint + " " + endPoint);

                Bundle bundleRouteData = new Bundle();
                bundleRouteData.putInt("planIndex", info.position);
                bundleRouteData.putString("transportMode", transportMode);
                bundleRouteData.putString("transportCompany", transportCompany);
				bundleRouteData.putString("mapperName", mapperName);
                bundleRouteData.putString("routeName", routeName);
                bundleRouteData.putString("startPoint", startPoint);
                bundleRouteData.putString("endPoint", endPoint);

                captureIntent = new Intent(SavedRoutesActivity.this, CaptureActivity.class);
                captureIntent.putExtra("bundleRouteData", bundleRouteData);
                captureIntent.putExtra("intentFrom", "Saved");

				//Check for run time permissions
				// Here, thisActivity is the current activity
				if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
				{

					// Should we show an explanation?
					if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION))
					{

						// Show an expanation to the user *asynchronously* -- don't block
						// this thread waiting for the user's response! After the user
						// sees the explanation, try again to request the permission.
						//TODO: show explanation
						final Activity activity = this;
						android.support.v7.app.AlertDialog.Builder diagBuilder = new android.support.v7.app.AlertDialog.Builder(activity);

						diagBuilder.setTitle("Permission");
						diagBuilder.setMessage(R.string.perm_ex_access_fine_location);
						diagBuilder.setPositiveButton("GOT IT", new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialogInterface, int i)
							{
								ActivityCompat.requestPermissions(activity,	new String[]{Manifest.permission.ACCESS_FINE_LOCATION},	REQ_PERMISSION_FINE_LOC);
							}
						});

						diagBuilder.show();

					}
					else
					{

						// No explanation needed, we can request the permission.

						ActivityCompat.requestPermissions(this,	new String[]{Manifest.permission.ACCESS_FINE_LOCATION},	REQ_PERMISSION_FINE_LOC);

						// MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
						// app-defined int constant. The callback method gets the
						// result of the request.
					}
				}
				else
					startActivityForResult(captureIntent, REQ_CODE_CAPTURE_COMPLETED);

                break;

			case 1:
				//Edit
				Intent editIntent = new Intent(SavedRoutesActivity.this, NewRouteActivity.class);
				editIntent.putExtra("editIndexId", info.position);
				startActivityForResult(editIntent, REQ_CODE_EDIT);
				break;
				
			case 2:
				buildDeleteConfirmation(info.position);
				break;
		}
		
		return true;		
	}

	private void buildDeleteConfirmation(final int planIndex)
	{
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to delete this route?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener()
		{
			public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id)
			{
				//Delete
				FileHelper fileHlp = new FileHelper(getApplicationContext());
				fileHlp.deletePlan(planIndex, plans);
				updateList();
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
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		switch (requestCode)
		{
			case REQ_PERMISSION_FINE_LOC:
			{
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0	&& grantResults[0] == PackageManager.PERMISSION_GRANTED)
				{

					// permission was granted, yay! Do the
					// contacts-related task you need to do.
					startActivityForResult(captureIntent, REQ_CODE_CAPTURE_COMPLETED);

				}
				else
				{

					// permission denied, boo! Disable the
					// functionality that depends on this permission.

				}
				return;
			}

			// other 'case' lines to check for other
			// permissions this app might request
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		if(requestCode == REQ_CODE_EDIT)
		{
			//catch result for editing a saved route
			if(resultCode == RESULT_OK)
			{
				updateList();
				Snackbar.make(cordLayWrapper, "Changes Saved", Snackbar.LENGTH_LONG).show();
//				Toast.makeText(this, "Changes Saved", Toast.LENGTH_LONG).show();
			}
			else if(resultCode == RESULT_CANCELED)
				Snackbar.make(cordLayWrapper, "Changes Canceled", Snackbar.LENGTH_LONG).show();
//				Toast.makeText(this, "Changes Canceled", Toast.LENGTH_LONG).show();
		}
		else if(requestCode == REQ_CODE_NEW)
		{
			//catch result for new route here
			if(resultCode == RESULT_OK)
			{
				updateList();
				Snackbar.make(cordLayWrapper, "New Route Saved", Snackbar.LENGTH_LONG).show();
//				Toast.makeText(this, "New Route Saved", Toast.LENGTH_LONG).show();
			}
			else if(resultCode == RESULT_CANCELED)
				Snackbar.make(cordLayWrapper, "New Route Canceled", Snackbar.LENGTH_LONG).show();
//				Toast.makeText(this, "New Route Canceled", Toast.LENGTH_LONG).show();
		}
		else if(requestCode == REQ_CODE_CAPTURE_COMPLETED)
		{
			//catch result from capturing activity
			if(resultCode == RESULT_OK)
				this.finish();				
		}
	}
	
	public void onClickNewPlan(View view)
	{
		Intent newPlan = new Intent(SavedRoutesActivity.this, NewRouteActivity.class);
		startActivityForResult(newPlan, REQ_CODE_NEW);
	}
	
//	public void onClickDeletePlan(View view)
//	{
//		ArrayList<Integer> plansToDelete = new ArrayList<Integer>();
//		
//		//Check which index are checked and store in an int array list for deletion
//		for(int i = 0; i < lstvPlanItems.getCount(); i++)
//		{
//			if(lstAdapter.isChecked(i))
//				plansToDelete.add(i);
//		}	
//		
//		fileHlp.deletePlans(plansToDelete, plans);
//		this.recreate();		
//	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3)
	{		
		view.showContextMenu();
	}

//	@Override
//	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) 
//	{
//		Toast.makeText(this, "Item to display context menu for: " + position, Toast.LENGTH_LONG).show();
//		return true;
//	}

}
