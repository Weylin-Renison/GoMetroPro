package com.gometro.gometropro;


import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;

public class NewRouteActivity extends AppCompatActivity implements OnItemSelectedListener, TextWatcher, OnFocusChangeListener
{
	private EditText etxtMapperName;
	private EditText etxtRouteName;
	private Spinner transportMode;
	private EditText etxtTransportCompany;
	private EditText etxtTransportModeError;
	private EditText etxtStartPoint;
	private EditText etxtEndPoint;
	private TextView tbtnCancel;
	private TextView tbtnSave;
	private TextView tbtnToCapture;
	private CustomSpinnerAdapter transportModeAdapter;
	private boolean useAsEditActivity = false;
	private int editIndexId = -1;
	private ArrayList<String[]> plans;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_route);

		etxtMapperName = (EditText) findViewById(R.id.etxtMapperName);
		etxtRouteName = (EditText) findViewById(R.id.etxtRouteName);
		etxtRouteName.addTextChangedListener(this);
		etxtRouteName.setOnFocusChangeListener(this);
		etxtRouteName.setOnEditorActionListener(new TextView.OnEditorActionListener()
		{
			@Override
			public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent)
			{
				transportMode.performClick();
				return true;
			}
		});
				
		transportMode = (Spinner) findViewById(R.id.spnrTransportMode);
		transportModeAdapter = new CustomSpinnerAdapter(this, R.id.txtvTransporModeSpinner, R.layout.custom_spinner_row_item_mode, getResources().getStringArray(R.array.transport_mode_array), "mode");
		transportMode.setAdapter(transportModeAdapter);
		transportMode.setOnItemSelectedListener(this);
		etxtTransportModeError = (EditText) findViewById(R.id.txtvTransporModeError);
		

		etxtTransportCompany = (EditText) findViewById(R.id.etxtTransportCompany);
		etxtTransportCompany.setOnFocusChangeListener(this);
		etxtTransportCompany.addTextChangedListener(this);
		
		etxtStartPoint = (EditText) findViewById(R.id.etxtStartPoint);
		etxtStartPoint.addTextChangedListener(this);
		etxtStartPoint.setOnFocusChangeListener(this);
		etxtStartPoint.setOnEditorActionListener(new TextView.OnEditorActionListener()
		{
			@Override
			public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent)
			{
				etxtEndPoint.requestFocus();
				return true;
			}
		});

		etxtEndPoint = (EditText) findViewById(R.id.etxtEndPoint);
		etxtEndPoint.addTextChangedListener(this);
		etxtEndPoint.setOnFocusChangeListener(this);
		etxtEndPoint.setOnEditorActionListener(new TextView.OnEditorActionListener()
		{
			@Override
			public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent)
			{
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
				return true;
			}
		});
		
		tbtnCancel = (TextView)findViewById(R.id.tbtnCancel);
		
		tbtnSave = (TextView)findViewById(R.id.tbtnSave);
		tbtnSave.setOnFocusChangeListener(this);
		
		tbtnToCapture = (TextView)findViewById(R.id.tbtnCaptureRoute);
		tbtnToCapture.setOnFocusChangeListener(this);
		
		//Check if this instance is to be used as a edit route
		editIndexId = getIntent().getIntExtra("editIndexId", -1);
		
		if(editIndexId != -1)
		{
			//Toast.makeText(this, "Activity registered for edit", Toast.LENGTH_LONG).show();
			useAsEditActivity = true;			
			this.setTitle(getResources().getString(R.string.title_activity_edit_route));
			populateEditData(editIndexId);
		}
		
		//Hide status notification bar
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}
	
	public void populateEditData(int editIndexId)
	{
		Log.d("pupulateEditData", "Starting...");

		//Read Plans
		FileHelper fileHlp = new FileHelper(this);
		plans = fileHlp.readPlan();
		
		//Populate data fields
		etxtMapperName.setText(plans.get(editIndexId)[0]);
		etxtRouteName.setText(plans.get(editIndexId)[1]);
		
		//Search for index of transport mode and set selected item in spinner to that index

		String transportModeToSelect = plans.get(editIndexId)[2];
		String [] modeArray = getResources().getStringArray(R.array.transport_mode_array);
		for( int i = 0; i < modeArray.length; i++)
		{
			if(transportModeToSelect.equalsIgnoreCase(modeArray[i]))
			{
				transportMode.setSelection(i);
				Log.d("pupulateEditData", "transportMode selection made: " + i);
				break;
			}
		}
		
		//Check which transport companies should be loaded into array, then search for correct index and set selected item
		String transportCompanyToSelect = plans.get(editIndexId)[3]; //Insert crash here for testing original values is 2
		etxtTransportCompany.setText(transportCompanyToSelect);
		
		etxtStartPoint.setText(plans.get(editIndexId)[4]);
		etxtEndPoint.setText(plans.get(editIndexId)[5]);

		//Project code checked only after loading from server is done
		
		Log.d("pupulateEditData", "Ending...");
	}
	
	public void onClickSave(View view)
	{					
		if(getValidationResult())
		{
			boolean planWrittenSuccess;
			
			Log.w("New Route", "Create File Helper");
			FileHelper fileHlp = new FileHelper(this);

			planWrittenSuccess = fileHlp.writePlan(etxtMapperName.getText().toString(), etxtRouteName.getText().toString(), transportMode.getSelectedItem().toString(), etxtTransportCompany.getText().toString(), etxtStartPoint.getText().toString(), etxtEndPoint.getText().toString(), true);

			if(useAsEditActivity)
			{
				//file is written and read again in saved routes to update
				
				//Delete old record
				fileHlp.deletePlan(editIndexId);				
				this.setResult(RESULT_OK);
			}			
			else if(planWrittenSuccess)
			{
				//Check if new route was created from new route screen or saved routes screen and needs to return result
				if(getCallingActivity() == null)
				{
					Intent savedRoutesIntent = new Intent(NewRouteActivity.this, SavedRoutesActivity.class);
					startActivity(savedRoutesIntent);
				}
				else
					this.setResult(RESULT_OK);
			}
			else
				Toast.makeText(this, "Plan save failed", Toast.LENGTH_LONG).show();
			
			this.finish();
		}
		else
			Toast.makeText(this, "Please correct any errors before proceeding", Toast.LENGTH_LONG).show();
	}
	
	public void onClickCancel(View view)
	{
		if(useAsEditActivity || getCallingActivity() != null)
			this.setResult(RESULT_CANCELED);
			
		this.finish();
	}
	
	public void onClickCaptureRoute(View view)
	{
		if(getValidationResult())
		{
			Intent captureIntent = new Intent(NewRouteActivity.this, CaptureActivity.class);
			
			Bundle bundel = new Bundle();
			bundel.putString("mapperName", etxtMapperName.getText().toString());
			bundel.putString("routeName", etxtRouteName.getText().toString());
			bundel.putString("transportMode", transportMode.getSelectedItem().toString());
			bundel.putString("transportCompany", etxtTransportCompany.getText().toString());
			bundel.putString("startPoint", etxtStartPoint.getText().toString());
			bundel.putString("endPoint", etxtEndPoint.getText().toString());
			captureIntent.putExtra("intentFrom", "New");
			
			captureIntent.putExtra("bundleRouteData", bundel);
			
			startActivity(captureIntent);
		}
		else
			Toast.makeText(this, "Please correct any errors before proceeding", Toast.LENGTH_LONG).show();
	}
	
	public boolean getValidationResult()
	{
		boolean valid = true;
		
		if(!validateSpinners())
			valid = false;
		
		if(!validateEditTexts())
			valid = false;
		
		return valid;
	}
	
	public boolean validateEditTexts()
	{
		boolean valid = true;
		
					
			if(etxtStartPoint.getText().toString() != null && etxtStartPoint.getText().toString().trim().length() == 0)
			{
				etxtStartPoint.setError(getString(R.string.error_msg_cannot_be_empty));
				valid = false;
			}
			
			if(etxtEndPoint.getText().toString() != null && etxtEndPoint.getText().toString().trim().length() == 0)
			{
				etxtEndPoint.setError(getString(R.string.error_msg_cannot_be_empty));
				valid = false;
			}

			if(!etxtStartPoint.getText().toString().trim().equals(etxtEndPoint.getText().toString().trim()))
			{
				etxtStartPoint.setError("License plate does not match");
				etxtEndPoint.setError("License plate does not match");
				valid = false;
			}
			
			if(etxtRouteName.getText().toString() != null && etxtRouteName.getText().toString().trim().length() == 0)
			{
				etxtRouteName.setError(getString(R.string.error_msg_cannot_be_empty));
				etxtRouteName.requestFocus();
				valid = false;
			}

			if(etxtTransportCompany.getText().toString() != null && etxtTransportCompany.getText().toString().trim().length() == 0)
			{
				etxtTransportCompany.setError(getString(R.string.error_msg_cannot_be_empty));
				etxtTransportCompany.requestFocus();
				valid = false;
			}
			
			return valid;
	}
	
	public boolean validateSpinners()
	{
		boolean valid = true;
		
		if(transportMode.getSelectedItemPosition() == 0)
		{
			valid = false;
			etxtTransportModeError.setError("An item must be selected");
			etxtTransportModeError.setVisibility(View.VISIBLE);
			etxtTransportModeError.requestFocus();
		}
		else
		{
			etxtTransportModeError.setError(null);
			etxtTransportModeError.setVisibility(View.GONE);
		}
		
		return valid;
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View view, int selectedIndex, long arg3) 
	{	
		if(view != null)
			Log.e("onItemSelected fired!! ", view.toString());
		else
			Log.e("onItemSelected fired!! ", "view is null");
		//populates transportComapny spinner dynamically depending on what is selected in transportMode
		View parentView = (View) view.getParent();;
		
		switch(parentView.getId())
		{
			case R.id.spnrTransportMode:
				
				if(selectedIndex != 0)
				{
					
					//Center selected view
					LinearLayout linLayTransportModeRoot = (LinearLayout) transportMode.getSelectedView();
//					View spacer = (View) linLayTransportModeRoot.findViewById(R.id.vTranspotModeSpacer);
//					spacer.setVisibility(View.GONE);
//					linLayTransportModeRoot.setGravity(Gravity.CENTER);
					etxtTransportCompany.requestFocus();
				}
				break;

			default:
				break;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) 
	{		
		
	}

	@Override
	public void afterTextChanged(Editable s) 
	{
				
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,	int after)
	{
		
		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count)
	{	
		if(etxtRouteName.hasFocus())
		{
			if(etxtRouteName.getError() != null)
				etxtRouteName.setError(null);
		}
		else if(etxtTransportCompany.hasFocus())
		{
			if(etxtTransportCompany.getError() != null)
				etxtTransportCompany.setError(null);
		}
		else if(etxtStartPoint.hasFocus())
		{
			if(etxtStartPoint.getError() != null)
				etxtStartPoint.setError(null);
		}
		else if(etxtEndPoint.hasFocus())
		{
			if(etxtEndPoint.getError() != null)
				etxtEndPoint.setError(null);
		}
			
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) 
	{
		if(!hasFocus)
		{
			switch(v.getId())
			{
				case R.id.etxtRouteName:
					if(etxtRouteName.getText().toString() != null && etxtRouteName.getText().toString().trim().length() == 0)
						etxtRouteName.setError(getString(R.string.error_msg_cannot_be_empty));
					break;

				case R.id.etxtTransportCompany:
					if(etxtTransportCompany.getText().toString() != null && etxtTransportCompany.getText().toString().trim().length() ==0)
						etxtTransportCompany.setError(getString(R.string.error_msg_cannot_be_empty));
					break;
			
				case R.id.etxtStartPoint:
					if(etxtStartPoint.getText().toString() != null && etxtStartPoint.getText().toString().trim().length() == 0)
						etxtStartPoint.setError(getString(R.string.error_msg_cannot_be_empty));
					break;
					
				case R.id.etxtEndPoint:
					if(etxtEndPoint.getText().toString() != null && etxtEndPoint.getText().toString().trim().length() == 0)
						etxtEndPoint.setError(getString(R.string.error_msg_cannot_be_empty));
					break;
			}
		}
		
	}

	

}
