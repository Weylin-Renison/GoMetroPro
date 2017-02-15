package com.gometro.gometropro;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.Currency;
import java.util.Locale;

import static android.app.Activity.RESULT_CANCELED;

/**
 * Created by wprenison on 2017/01/24.
 */

public class FragDiagTagStop extends AppCompatDialogFragment
{

    private AppCompatDialogFragment thisDialog = this;
    private CaptureActivity activity;
    private ImageView imgvPreview;
    private FloatingActionButton btnAddPhoto;
    private FloatingActionButton btnBoardPass;
    private FloatingActionButton btnAlightPass;
    private TextView txtvTotalPassValue;
    private Button btnClearBoard;
    private Button btnClearAlight;
    private Button btnDone;
    private Button btnMovePrevStop;
    private ImageView imgvBadgeBgMinusPass;
    private TextView txtvPassBoardValue;
    private TextView txtvPassAlightValue;
    private EditText etxtFarePrice;
    private EditText etxtStopName;
    private TextView txtvCurrency;
    private Chronometer chronWaitingTime;
    private int passBoardValue;
    private int passAlightValue;
    private static final int REQ_CODE_CAPTURE_PHOTO = 1001;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Dialog dialog =  super.onCreateDialog(savedInstanceState);

        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View constructedView = inflater.inflate(R.layout.custom_tag_stop_name_dialog, container, false);

        //Get handle on views
        etxtStopName = (EditText) constructedView.findViewById(R.id.etxtStopNameDialogValue);
        etxtFarePrice = (EditText) constructedView.findViewById(R.id.etxtFarePrice);
        txtvCurrency = (TextView) constructedView.findViewById(R.id.txtvFarePriceCurrency);
        chronWaitingTime = (Chronometer) constructedView.findViewById(R.id.chronStopDuration);
        imgvPreview =  (ImageView) constructedView.findViewById(R.id.imgvStopNameDialogImagePreview);
        btnAddPhoto = (FloatingActionButton) constructedView.findViewById(R.id.ibtnStopNameDialogAddPhoto);
        btnBoardPass = (FloatingActionButton) constructedView.findViewById(R.id.ibtnAddPass);
        btnAlightPass = (FloatingActionButton) constructedView.findViewById(R.id.ibtnMinusPass);
        imgvBadgeBgMinusPass = (ImageView) constructedView.findViewById(R.id.ivBadgeBgMinusPass);
        btnClearBoard = (Button) constructedView.findViewById(R.id.btnClearBoard);
        btnClearAlight = (Button) constructedView.findViewById(R.id.btnClearAlight);
        txtvTotalPassValue = (TextView) constructedView.findViewById(R.id.txtvTotalPassValue);
        txtvPassBoardValue = (TextView) constructedView.findViewById(R.id.txtvBoardPassValue);
        txtvPassAlightValue = (TextView) constructedView.findViewById(R.id.txtvAlightPassValue);

        btnDone = (Button) constructedView.findViewById(R.id.btnDone);
        btnMovePrevStop = (Button) constructedView.findViewById(R.id.btnMovePrevStop);

        return constructedView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        activity = (CaptureActivity) getActivity();
        init();
    }

    private void init()
    {
        //disable alight button if total pass in vehicle is 0
        if(activity.passRuningTotal == 0)
            btnAlightPass.setEnabled(false);

        //Set currency
        Locale defaultLocale = Locale.getDefault();
        Currency defaultCurrency = Currency.getInstance(defaultLocale);

        String currencySymbol = defaultCurrency.getSymbol();

        if(currencySymbol != null || !currencySymbol.isEmpty())
            txtvCurrency.setText(defaultCurrency.getSymbol());

        Log.d("currency", txtvCurrency.getText().toString());

        etxtStopName.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent)
            {
                etxtFarePrice.requestFocus();
                return true;
            }
        });

        etxtStopName.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View view, boolean focused)
            {
                if(focused)
                    thisDialog.getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        });

        etxtFarePrice.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent)
            {
                hideKeyboardImplicit();
                return true;
            }
        });

        btnAddPhoto.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                activity.stopPhotoFile = CamHelper.intentCamCapturePhoto(thisDialog, REQ_CODE_CAPTURE_PHOTO, Environment.getExternalStorageDirectory() + "/GoMetroPro/Photos/", null);
            }
        });

        imgvPreview.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                activity.stopPhotoFile = CamHelper.intentCamCapturePhoto(thisDialog, REQ_CODE_CAPTURE_PHOTO, Environment.getExternalStorageDirectory() + "/GoMetroPro/Photos/", null);
            }
        });

        txtvTotalPassValue.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
                if(activity.passRuningTotal == 0)
                {
                    btnAlightPass.setEnabled(false);
                    btnAlightPass.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_minus_inactive));
                    imgvBadgeBgMinusPass.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.bg_badge_inactive));
                }
                else
                {
                    btnAlightPass.setEnabled(true);
                    btnAlightPass.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_minus));
                    imgvBadgeBgMinusPass.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.bg_badge));
                }
            }

            @Override
            public void afterTextChanged(Editable editable)
            {

            }
        });

        btnBoardPass.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                passBoardValue++;
                txtvPassBoardValue.setText(passBoardValue + "");
            }
        });

        //Disable button if total pass in vehicle is 0
        if(activity.passRuningTotal == 0)
        {
            btnAlightPass.setEnabled(false);
            btnAlightPass.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_minus_inactive));
            imgvBadgeBgMinusPass.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.bg_badge_inactive));
        }

        btnAlightPass.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                passAlightValue++;
                txtvPassAlightValue.setText(passAlightValue + "");

                activity.passRuningTotal--;
                txtvTotalPassValue.setText(activity.passRuningTotal + "");
            }
        });

        btnClearBoard.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                passBoardValue = 0;
                txtvPassBoardValue.setText(passBoardValue + "");
            }
        });

        btnClearAlight.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                activity.passRuningTotal += passAlightValue;
                txtvTotalPassValue.setText(activity.passRuningTotal + "");

                passAlightValue = 0;
                txtvPassAlightValue.setText(passAlightValue + "");
            }
        });

        this.setCancelable(false);

        btnDone.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                chronWaitingTime.stop();
                RouteStop stop = GpsGlobalVars.getInstance().GpsService.routeStops.get(GpsGlobalVars.getInstance().GpsService.routeStops.size() - 1);

                if (etxtStopName.getText().toString().isEmpty())
                {
                    stop.stopName = "Unknown";
                } else
                {
                    Log.d("StopName", "Setting stop name to: " + etxtStopName.getText().toString());
                    stop.stopName = etxtStopName.getText().toString().trim();
                }

                //set stop alight and boarding figures
                stop.alight = passAlightValue;
                stop.board = passBoardValue;
                activity.passRuningTotal += passBoardValue;

                Log.d("valuesTest", "alighted: " + passAlightValue + " boarded: " + passBoardValue + " runningTotal: " + activity.passRuningTotal);

                if(!etxtFarePrice.getText().toString().isEmpty())
                    stop.stopFarePrice = Float.parseFloat(etxtFarePrice.getText().toString());
                else
                    stop.stopFarePrice = 0f;


                stop.departureTime = getCurrentTime();
                stop.currency = txtvCurrency.getText().toString().trim() + "";

                passAlightValue = 0;
                passBoardValue = 0;

                activity.updateStopCountStatus();

                hideKeyboardImplicit();
                Log.d("StopName", "Stop name was set to: " + GpsGlobalVars.getInstance().GpsService.routeStops.get(GpsGlobalVars.getInstance().GpsService.routeStops.size() - 1).stopName);

                thisDialog.dismiss();
            }
        });

        //Only display this if at least one stop has been tagged, keep in mind stop is added as soon
        // as the button is pressed so stop count is always inflated by 1
        if(GpsGlobalVars.getInstance().GpsService.routeStops.size() > 1)
        {
            btnMovePrevStop.setVisibility(View.VISIBLE);

            btnMovePrevStop.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    AlertDialog.Builder confirmDiagBuilder = new AlertDialog.Builder(activity);
                    confirmDiagBuilder.setMessage("Are you sure you want to move the previous stop location?");

                    confirmDiagBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogconfirmInterface, int i)
                        {
                            dialogconfirmInterface.dismiss();
                            thisDialog.dismiss();

                            //untag premature stop
                            if (GpsGlobalVars.getInstance().GpsService.untagStop())
                            {
                                if (GpsGlobalVars.getInstance().GpsService.moveStop(getCurrentTime(), etxtStopName.getText().toString().trim()))
                                {
                                    Snackbar.make(activity.relLayMainCapture, "Stop Moved", Snackbar.LENGTH_LONG).show();
//									Toast.makeText(thisActivity, "Stop Moved", Toast.LENGTH_LONG).show();
                                }
                            } else
                                Snackbar.make(activity.relLayMainCapture, "Failed to move stop", Snackbar.LENGTH_LONG).show();
//								Toast.makeText(thisActivity, "Failed to move stop", Toast.LENGTH_LONG).show();


                            activity.updateStopCountStatus();

                            hideKeyboardImplicit();
                        }
                    });

                    confirmDiagBuilder.setNegativeButton("No", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogconfirmInterface, int i)
                        {
                            dialogconfirmInterface.dismiss();
                            GpsGlobalVars.getInstance().GpsService.untagStop();
                            activity.updateStopCountStatus();

                            hideKeyboardImplicit();
                        }
                    });

                    confirmDiagBuilder.show();
                }
            });
        }

        chronWaitingTime.setBase(SystemClock.elapsedRealtime());
        chronWaitingTime.start();

    }

    public long getCurrentTime(){return System.currentTimeMillis();}

    private void hideKeyboardImplicit()
    {
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
//        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQ_CODE_CAPTURE_PHOTO)
        {
            if(activity.stopPhotoFile != null)
            {
                imgvPreview.setImageURI(Uri.fromFile(activity.stopPhotoFile));
                imgvPreview.setVisibility(View.VISIBLE);
                btnAddPhoto.setVisibility(View.GONE);

                CamHelper.geoTagWithGpsLocation(thisDialog, activity.stopPhotoFile);

                GpsGlobalVars.getInstance().GpsService.routeStops.get(GpsGlobalVars.getInstance().GpsService.routeStops.size() - 1).photo = activity.stopPhotoFile;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode)
        {
            case CamHelper.REQ_PERM_CREATE_PHOTO_FILE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    activity.stopPhotoFile = CamHelper.intentCamCapturePhoto(activity, REQ_CODE_CAPTURE_PHOTO, Environment.getExternalStorageDirectory() + "/GoMetroPro/Photos/", null);
                }
                else
                {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Snackbar.make(activity.relLayMainCapture, "Photos can't be captured without permission", Snackbar.LENGTH_LONG).show();
                }
                break;
        }
    }
}
