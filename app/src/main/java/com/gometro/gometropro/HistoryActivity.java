package com.gometro.gometropro;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


public class HistoryActivity extends AppCompatActivity implements AdapterView.OnItemClickListener
{
    //Views
    private TextView txtvHistoryCount;
    private CoordinatorLayout cordLayHistory;
    private ListView lstvHistoryRoutes;
    private List<GoMappProtos.Upload.Route> historyRoutes;
    private ListAdapterCaptured lstAdapter;
    private AdapterView.AdapterContextMenuInfo info;

    private final int REQ_PERM_READ_EXTERNAL_STORAGE= 101;

    private final File dirExportedRoutes = new File(Environment.getExternalStorageDirectory() + "/GoMetroPro/exportedRoutes/");

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        txtvHistoryCount = (TextView) findViewById(R.id.txtvHistoryCount);

        cordLayHistory = (CoordinatorLayout) findViewById(R.id.cordLayHistory);

        lstvHistoryRoutes = (ListView) findViewById(R.id.lstvHistoryRoutes);
        updateList();

        txtvHistoryCount.setText(lstAdapter.getCount() + "");

        //Hide status notification bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


    }

    private void updateList()
    {
        //Read data
        historyRoutes = readHistoryRoutes();
        //Toast.makeText(this, "Captured list size: " + capturedRoutes.size(), Toast.LENGTH_LONG).show();

        if(historyRoutes.size() == 0)
            Toast.makeText(this, "No History", Toast.LENGTH_LONG).show();

        //Populate data
        lstAdapter = new ListAdapterCaptured(this, R.layout.captured_route_item, historyRoutes);
        lstvHistoryRoutes.setAdapter(lstAdapter);
        lstvHistoryRoutes.setOnItemClickListener(this);

        registerForContextMenu(lstvHistoryRoutes);

    }

    @Override
    public void onCreateContextMenu(android.view.ContextMenu menu, View v, android.view.ContextMenu.ContextMenuInfo menuInfo)
    {
        if(v.getId() == R.id.lstvHistoryRoutes)
        {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

            LayoutInflater inflater = LayoutInflater.from(this);
            View header = inflater.inflate(R.layout.context_menu_header, null);
            TextView headerTitle = (TextView) header.findViewById(R.id.txtvContextMenuHeaderTitle);

            headerTitle.setText(historyRoutes.get(info.position).getRouteName());
            menu.setHeaderView(header);
//            menu.add(Menu.NONE, 0, 0, "Upload");
            menu.add(Menu.NONE, 0, 0, "View");
            menu.add(Menu.NONE, 1, 1, "Delete");
            menu.add(Menu.NONE, 2, 2, "Export to SD Card");
        }
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item)
    {
        info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int menuItemIndex = item.getItemId();

        switch(menuItemIndex)
        {
            case 0:
                //View
                Intent viewIntent = new Intent(HistoryActivity.this, GoogleMapActivity.class);
                viewIntent.putExtra("routeName", historyRoutes.get(info.position).getRouteName());
                viewIntent.putExtra("fromHistory", true);
                startActivity(viewIntent);
                break;

            case 1:
                //Delete
                buildDeleteConfirmation(info.position);
                break;

            case 2:
                //Export
                // Here, thisActivity is the current activity
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                {

                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE))
                    {

                        // Show an expanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.
                        //TODO: provide explanation
                        final Activity activity = this;
                        android.support.v7.app.AlertDialog.Builder diagBuilder = new android.support.v7.app.AlertDialog.Builder(activity);

                        diagBuilder.setTitle("Permission");
                        diagBuilder.setMessage(R.string.perm_ex_read_external_storage);
                        diagBuilder.setPositiveButton("GOT IT", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQ_PERM_READ_EXTERNAL_STORAGE);
                            }
                        });

                        diagBuilder.show();

                    }
                    else
                    {

                        // No explanation needed, we can request the permission.

                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQ_PERM_READ_EXTERNAL_STORAGE);

                        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                    }
                }
                else
                    exportRoute(info.position, historyRoutes.get(info.position).getRouteName());

                break;
        }

        return true;
    }

    private void exportRoute(int routeIndex, String routeName)
    {
        File dir = new File(getFilesDir() + "/History/");

        File[] allFiles = dir.listFiles();
        List<File> historyFiles = new ArrayList<File>();

        for(int i = 0; i < allFiles.length; i++)
            if(allFiles[i].getName().contains(".pb"))
                historyFiles.add(allFiles[i]);

        if(historyFiles.size() != 0)
        {
            //Export pb file to sd card
            boolean dirReady = true;
            if(dirExportedRoutes == null || !dirExportedRoutes.isDirectory() || !dirExportedRoutes.exists())
                dirReady = dirExportedRoutes.mkdirs();

            if(dirReady)
            {
                try
                {
                    InputStream inStream = new FileInputStream(allFiles[routeIndex]);
                    OutputStream outStream = new FileOutputStream(dirExportedRoutes + "/exportedRoute_" + routeName + ".pb");

                    byte[] buffer = new byte[1024];
                    int len;

                    while((len = inStream.read(buffer)) > 0)
                        outStream.write(buffer, 0, len);

                    inStream.close();
                    outStream.close();

                    //buildExportedExplanation(routeName);
                    snackbarExportSuccess(routeName);
                }
                catch(IOException ioe)
                {
                    Toast.makeText(this, "An error occurred, the could not be exported because: " + ioe.getMessage(), Toast.LENGTH_LONG).show();
                    ioe.printStackTrace();
                }
            }
            else
            {
                Toast.makeText(this, "An error occurred, the route could not be exported", Toast.LENGTH_LONG).show();
                Log.e("HistoryActivity", "Exporting Route directory was not ready");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        switch (requestCode)
        {
            case REQ_PERM_READ_EXTERNAL_STORAGE:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    exportRoute(info.position, historyRoutes.get(info.position).getRouteName());
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

    private void buildExportedExplanation(String routeName)
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("The route " + routeName + " has successfully been exported.\nYou can find it" +
                " here: localstorage/GoMetroPro/exportedRoutes/").setCancelable(false)
                .setPositiveButton("Got it", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Open Folder", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        Uri selectedUri = Uri.parse(dirExportedRoutes.getAbsolutePath());
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(selectedUri, "resource/folder");

                        if (intent.resolveActivityInfo(getPackageManager(), 0) != null)
                        {
                            startActivity(intent);
                        }
                        else
                        {
                            // if you reach this place, it means there is no any file
                            // explorer app installed on your device

                        }
                    }
                });

        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void snackbarExportSuccess(String routeName)
    {
        Snackbar sbExportSuccess = Snackbar.make(cordLayHistory, "Exported " + routeName + " Successfully", Snackbar.LENGTH_LONG)
                .setAction("Open Folder", new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        Uri selectedUri = Uri.parse(dirExportedRoutes.getAbsolutePath());
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(selectedUri, "resource/folder");

                        if (intent.resolveActivityInfo(getPackageManager(), 0) != null)
                        {
                            startActivity(intent);
                        }
                        else
                        {
                            // if you reach this place, it means there is no any file
                            // explorer app installed on your device
                            Snackbar.make(cordLayHistory, "No file manager found", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });

        sbExportSuccess.show();
    }

    private void buildDeleteConfirmation(final int routeIndex)
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this route?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id)
            {
                deleteHistoryRoute(routeIndex);
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
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
    {
        view.showContextMenu();
    }

    private void deleteAllHistory()
    {
        File dir = new File(getFilesDir() + "/History/");

        File[] allHistoryFiles = dir.listFiles();

        for(int i = 0; i < allHistoryFiles.length; i++)
            allHistoryFiles[i].delete();
    }

    private void deleteHistoryRoute(int postition)
    {
        File dir = new File(getFilesDir() + "/History/");

        File[] allFiles = dir.listFiles();
        List<File> historyFiles = new ArrayList<File>();

        for(int i = 0; i < allFiles.length; i++)
            if(allFiles[i].getName().contains(".pb"))
                historyFiles.add(allFiles[i]);

        if(historyFiles.size() != 0)
            historyFiles.get(postition).delete();

        updateList();
    }

    private List<GoMappProtos.Upload.Route> readHistoryRoutes()
    {
        List<GoMappProtos.Upload.Route> historyRoutes = new ArrayList<GoMappProtos.Upload.Route>();

        File dir = new File(getFilesDir() + "/History/");

//		if(!dir.isDirectory())
//			dir.mkdir();

        // Check for history data
        File[] historyRouteFiles = dir.listFiles();

        if(historyRouteFiles != null)
        {
            int noOfFiles = historyRouteFiles.length;
            //        Toast.makeText(this, "No of files: " + noOfFiles, Toast.LENGTH_LONG).show();

            if (noOfFiles == 0)
            {
                Toast.makeText(this, "No history.", Toast.LENGTH_LONG).show();
            }
            else
            {
                FileInputStream is = null;
                GoMappProtos.Upload.Route readHistoryRoute;

                try
                {
                    for (int i = 0; i < noOfFiles; i++)
                    {
//                        Toast.makeText(this, "File Name: " + historyRouteFiles[i].getName(), Toast.LENGTH_LONG).show();

                        if (historyRouteFiles[i].getName().contains(".pb"))
                        {
                            is = new FileInputStream(historyRouteFiles[i]);
                            readHistoryRoute = GoMappProtos.Upload.Route.parseDelimitedFrom(is);
                            historyRoutes.add(readHistoryRoute);
                            is.close();
                        }
                    }
                } catch (IOException ioe)
                {
                    ioe.printStackTrace();
                }
            }

        }

        return historyRoutes;
    }
}
