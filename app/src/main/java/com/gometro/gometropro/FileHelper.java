package com.gometro.gometropro;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.util.Log;

public class FileHelper
{
	private final String TAG = "FileHelper";
	Context context;
	String PATH_DIR;
	File FILE_PLANS;
		
	public FileHelper(Context context)
	{
		this.context = context;
		
		Log.w("FileHelper", "Initializing");
		
		//Create Plan file if it does not already exist
		PATH_DIR = context.getFilesDir().toString();
		FILE_PLANS = new File(PATH_DIR, "Plans");
		
		if(!FILE_PLANS.isFile())
		{
			try
			{
				FILE_PLANS.createNewFile();
			}
			catch(IOException ioe)
			{
				Log.w("FileHelper", "Failed to create file");
				ioe.printStackTrace();
			}
		}
	}
	
	//Writes a csv planned route to the plans file
	public boolean writePlan(String mapperName, String routeName, String transportMode, String transportCompany, String startPoint, String endPoint, boolean append)
	{
		boolean success = false;

		String plan =  mapperName.trim() + " ," + routeName.trim() + "," + transportMode.trim() + "," + transportCompany.trim() + "," + startPoint.trim() + "," + endPoint.trim() + ","+ "\n";
		FileOutputStream fileOut;

		try
		{
			fileOut = new FileOutputStream(FILE_PLANS, append);
			fileOut.write(plan.getBytes());
			fileOut.flush();
			fileOut.close();
			success = true;
			Log.w("FileHelper", "File written");
		}
		catch(Exception e)
		{
			Log.w("FileHelper", "File write failed");
			e.printStackTrace();
		}

		return success;

	}
	
	public void writePlans(ArrayList<String[]> plans, boolean append)
	{
		String plansDataToWrite = "";
		
		//Construct data to write
		for(int i = 0; i < plans.size(); i++)
			plansDataToWrite += plans.get(i)[0].trim() + "," + plans.get(i)[1].trim() + "," + plans.get(i)[2].trim() + "," + plans.get(i)[3].trim() + "," + plans.get(i)[4].trim() + "," + plans.get(i)[5].trim() + "\n";
		
		FileOutputStream fileOut;
		
		try
		{
			fileOut = new FileOutputStream(FILE_PLANS, append);
			fileOut.write(plansDataToWrite.getBytes());
			fileOut.flush();
			fileOut.close();
			
			Log.w("FileHelper", "File written");
		}
		catch(Exception e)
		{
			Log.w("FileHelper", "File write failed");
			e.printStackTrace();
		}
	}
	
	//Reads csv file with all planned routes into an 2d array list
	public ArrayList<String[]> readPlan()
	{
		ArrayList<String[]> plans = new ArrayList<String[]>();
		
		try
		{
			String line;
			String[] plan = new String[6];
			BufferedReader reader = new BufferedReader(new FileReader(FILE_PLANS));
			
			while((line = reader.readLine()) != null)
			{
				plan = line.split(",");
				plans.add(plan);
			}
			
			reader.close();
			Log.w("FileHelper", "File read successfully");
									
		}		
		catch(IOException ioe)
		{
			Log.w("FileHelper", "Failed to read file");
			ioe.printStackTrace();
		}
		
		return plans;
	}
	
	//Deletes a plan according to its index returning updated data set
	public void deletePlan(int planToDeleteIndex, ArrayList<String[]> plans)
	{
		plans.remove(planToDeleteIndex);		
		writePlans(plans, false);
		
		Log.w("deletePlans", "Plan deleted");
	}
	
	//Deletes a single plan without returning updated dataset
	public void deletePlan(int planToDeleteIndex)
	{
		ArrayList<String[]> plans = readPlan();
		
		plans.remove(planToDeleteIndex);
		writePlans(plans, false);
		
		Log.w("deletePlans", "Plan deleted");
	}
	
	//Deletes plans according to their index
	public void deletePlans(ArrayList<Integer> plansToDelete, ArrayList<String[]> plans)
	{
		Log.w("deletePlans", "Amount of plans to delete: " + plansToDelete.size() );
		
		//Remove plansToDelete and rewrite file
		for(int i = 0; i < plansToDelete.size(); i++)
		{
			plans.remove(plansToDelete.get(i).intValue());
			Log.w("deletePlans", "Plan deleted: " + i );
		}
		
		for(int i = 0; i < plans.size(); i++)
		{
			Log.w("plans to rewrite", "Plan: " + i );
		}
		
		writePlans(plans, false);
	}

	public GoMappProtos.Upload.Route readRouteFile(String routeFileName)
	{
        final String M_TAG = "readRouteFile:";  //Method tag
		GoMappProtos.Upload.Route routeData = null;

		//Read route file
		String routeFileDir = context.getFilesDir() + routeFileName;
		Log.i(TAG, M_TAG + "File Directory: " + routeFileDir);

        try
        {
            FileInputStream fis = new FileInputStream(new File(routeFileDir));
            routeData = GoMappProtos.Upload.Route.parseDelimitedFrom(fis);
            fis.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            Log.e(TAG, M_TAG + "Could not read file because of file not found exception: " + e.getMessage());
        }
        catch (IOException e)
        {
            e.printStackTrace();
            Log.e(TAG, M_TAG + "Could not read file because of io exception: " + e.getMessage());
        }

        return routeData;
	}

	public void writeRouteFile(GoMappProtos.Upload.Route route, String routeFileName)
	{
        final String M_TAG = "writeRouteFile:";
        String finalFilePath = context.getFilesDir() + routeFileName;

        File routeFile = new File(finalFilePath);

        try
        {
            FileOutputStream fos = new FileOutputStream(routeFile);
            route.writeDelimitedTo(fos);
            fos.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            Log.e(TAG, M_TAG + "Could not write file because of file not found exception: " + e.getMessage());
        }
        catch (IOException e)
        {
            e.printStackTrace();
            Log.e(TAG, M_TAG + "Could not write file because of io exception: " + e.getMessage());
        }
    }
}
