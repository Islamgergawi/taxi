package com.ondemandbay.taxianytime.fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.ondemandbay.taxianytime.R;
import com.ondemandbay.taxianytime.adapter.FancyCoverAdapter;
import com.ondemandbay.taxianytime.adapter.PlacesAutoCompleteAdapter;
import com.ondemandbay.taxianytime.component.CustomEventMapView;
import com.ondemandbay.taxianytime.interfaces.OnProgressCancelListener;
import com.ondemandbay.taxianytime.models.Booking;
import com.ondemandbay.taxianytime.models.Driver;
import com.ondemandbay.taxianytime.models.Route;
import com.ondemandbay.taxianytime.models.Step;
import com.ondemandbay.taxianytime.models.VehicalType;
import com.ondemandbay.taxianytime.parse.HttpRequester;
import com.ondemandbay.taxianytime.parse.ParseContent;
import com.ondemandbay.taxianytime.utils.AndyUtils;
import com.ondemandbay.taxianytime.utils.AppLog;
import com.ondemandbay.taxianytime.utils.Const;
import com.ondemandbay.taxianytime.utils.LocationHelper;
import com.ondemandbay.taxianytime.utils.LocationHelper.OnLocationReceived;
import com.ondemandbay.taxianytime.utils.PreferenceHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import at.technikum.mti.fancycoverflow.FancyCoverFlow;

public class MapFragment extends BaseFragment implements
        OnProgressCancelListener, OnItemClickListener,
        OnLocationReceived, OnItemSelectedListener {
    private Handler locationHandler,statusHandler;
    public static boolean isMapTouched = false;
    private final int LOCATION_SCHEDULE = 5 * 1000;
    public Driver driverInfoTemp = new Driver();
    public Booking booking;
    boolean isLocationFound;
    int start;
    int pointer;
    private PlacesAutoCompleteAdapter adapter;
    private float currentZoom = -1;
    private GoogleMap map;
    private DatePicker datePicker;
    private TimePicker timePicker;
    private LatLng curretLatLng;
    private String strAddress = null, selectedDate = "", selectedTime = "",
            startTime = "", appliedPromoCode = "";
    private boolean isContinueRequest;
  //  private Timer timer;
    private WalkerStatusReceiver walkerReceiver;
    private ImageButton btnMyLocation;
    private FrameLayout mapFrameLayout;
    private ArrayList<VehicalType> listType;
    private int selectedPostion = -1, selectedHour, selectedMinute, reqId;
    private boolean isGettingVehicalType = true;
    private LinearLayout layoutDestination;
    private int paymentMode;
    private TextView tvEstimatedTime, tvTripPickupAddress, tvCash, tvCard;
    private PlacesAutoCompleteAdapter adapterDestination;
    private Marker markerDestination, markerSource;
    private Route route;
    private ArrayList<LatLng> points;
    private PolylineOptions lineOptions;
    private boolean isSource, isAddDestination;
    private Polyline polyLine;
    private View layoutBubble;
    private PreferenceHelper preference;
    private LinearLayout layoutCardDetails;
    private Dialog quoteDialog, confirmFutureRequest, dialog, referralDialog,
            cancellationDialog;
    private View view;
    private CustomEventMapView mMapView;
    private Bundle mBundle;
    private TextView tvMaxSize, tvMinFare, tvETA, tvGetFareEst, tvTotalFare,
            tvHomeAddress, tvWorkAddress, tvRateVehicleTypeName,
            tvRateBasePrice, tvRateDistanceCost, tvRateTimeCost, tvVehicleType;
    private ImageView cancelVehicleDetail, selectedCard, selectedCash;
    private String estimatedTimeTxt;
    private AutoCompleteTextView etSource, etDestination, etPopupDestination,
            etHomeAddress, etWorkAddress;
    private PlacesAutoCompleteAdapter adapterPopUpDestination,
            adapterHomeAddress, adapterWorkAddress;
    private Dialog destinationDialog, scheduleDialog;
    private ProgressBar pbMinFare;
    private LinearLayout layoutHomeText, layoutHomeEdit, layoutWorkText,
            layoutWorkEdit, sendReqLayout, linearPickupAddress, vehicleLayout,
            llErrorMsg;
    private ListView nearByList;
    private ArrayAdapter<String> nearByAd;
    private Address address;
    private ProgressBar pbNearby, pbar;
    private ArrayList<Driver> listDriver = new ArrayList<Driver>();
    private HashMap<Integer, Marker> nearDriverMarker;
   // private Timer timerProvidersLocation;
    private ArrayList<Driver> listUpdatedDriver;
    private EditText etRefCode;
    private int is_skip = 0;
    private LocationHelper locHelper;
    private Location myLocation;
    private FancyCoverFlow fancyCoverFlow;
    private Button btnRideNow, btnRideLater, scheduleBtn, cancel, ok;
    private EditText editPromo;
    private String pickupAddrs;

    public static MapFragment newInstance() {
        MapFragment mapFragment = new MapFragment();
        return mapFragment;
    }
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
      /* do what you need to do */
            updateAllProviders(curretLatLng);
      /* and here comes the "trick" */
            locationHandler.postDelayed(this, LOCATION_SCHEDULE);
        }
    };
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_map, container, false);
        try {
            MapsInitializer.initialize(getActivity());
        } catch (Exception e) {
        }
        locationHandler = new Handler();
        statusHandler = new Handler();
        isLocationFound = false;
        inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view.findViewById(R.id.markerBubblePickMeUp).setOnClickListener(this);
        layoutBubble = view.findViewById(R.id.layoutBubble);
        tvEstimatedTime = (TextView) view.findViewById(R.id.tvEstimatedTime);
        layoutCardDetails = (LinearLayout) view
                .findViewById(R.id.layoutCardDetails);
        layoutCardDetails.setOnClickListener(this);
        tvTripPickupAddress = (TextView) view
                .findViewById(R.id.tvTripPickupAddress);
        tvVehicleType = (TextView) view.findViewById(R.id.tvVehicleType);
        layoutDestination = (LinearLayout) view
                .findViewById(R.id.layoutDestination);
        sendReqLayout = (LinearLayout) view.findViewById(R.id.sendReqLayout);
        vehicleLayout = (LinearLayout) view.findViewById(R.id.vehicleLayout);
        linearPickupAddress = (LinearLayout) view
                .findViewById(R.id.linearLayoutPickup);
        tvCash = (TextView) view.findViewById(R.id.tvCash);
        tvCard = (TextView) view.findViewById(R.id.tvCardNo);
        tvCash.setOnClickListener(this);
        tvCard.setOnClickListener(this);
        selectedCard = (ImageView) view.findViewById(R.id.imgSelectedCard);
        selectedCash = (ImageView) view.findViewById(R.id.imgSelectedCash);
        editPromo = (EditText) view.findViewById(R.id.ibApplyPromo);
        editPromo.setOnClickListener(this);
        btnRideNow = (Button) view.findViewById(R.id.btnRideNow);
        btnRideLater = (Button) view.findViewById(R.id.btnRideLater);
        btnRideNow.setOnClickListener(this);
        btnRideLater.setOnClickListener(this);

        if (activity.pHelper.getReferee() == 0) {
            showReferralDialog();
        }
        etSource = (AutoCompleteTextView) view.findViewById(R.id.etEnterSouce);
        etDestination = (AutoCompleteTextView) view
                .findViewById(R.id.etEnterDestination);
        selectedPostion = 0;
        mapFrameLayout = (FrameLayout) view.findViewById(R.id.mapFrameLayout);

        mapFrameLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN | MotionEvent.ACTION_MOVE:
                        MapFragment.isMapTouched = true;

                        break;

                    case MotionEvent.ACTION_UP:
                        MapFragment.isMapTouched = false;

                        break;
                }
                return true;
            }
        });

        btnMyLocation = (ImageButton) view.findViewById(R.id.btnMyLocation);
        mMapView = (CustomEventMapView) view.findViewById(R.id.map);
        mMapView.onCreate(mBundle);
        try{

            setUpMapIfNeeded();
        }catch(NullPointerException e)
        {


            e.printStackTrace();


        }
        preference = new PreferenceHelper(activity);
        tvVehicleType = (TextView) view.findViewById(R.id.tvVehicleType);
        datePicker = new DatePicker(activity);
        timePicker = new TimePicker(activity);
        listType = new ArrayList<VehicalType>();

        fancyCoverFlow = (FancyCoverFlow) view
                .findViewById(R.id.fancyCoverFlow);
        // fancyCoverFlow.setUnselectedAlpha(1);
        fancyCoverFlow.setUnselectedSaturation(0f);
        fancyCoverFlow.setUnselectedScale(0.4f);
        fancyCoverFlow.setOnItemSelectedListener(this);
        editPromo.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN)
                        && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    if ((!(editPromo.getText().toString().equals(getResources()
                            .getString(R.string.text_enter_promo_code))))
                            && (!(editPromo.getText().toString().equals("")))) {
                        appliedPromoCode = editPromo.getText().toString();
                        new PreferenceHelper(activity)
                                .putPromoCode(appliedPromoCode);
                    }
                }
                // Returning false allows other listeners to react to the press.

                if (event.getAction() == KeyEvent.ACTION_UP
                        && keyCode == KeyEvent.KEYCODE_BACK) {
                    if (sendReqLayout.getVisibility() == View.VISIBLE) {
                        cancelConfirmation();
                    } else {
                        return false;
                    }
                    return true;
                }
                return false;
            }
        });
        etDestination.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP
                /* && keyCode == KeyEvent.KEYCODE_BACK */) {
                    if (sendReqLayout.getVisibility() == View.VISIBLE) {
                        cancelConfirmation();
                    } else {
                        return false;
                    }
                    return true;
                }
                return false;
            }
        });

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBundle = savedInstanceState;
        IntentFilter filter = new IntentFilter(Const.INTENT_WALKER_STATUS);
        walkerReceiver = new WalkerStatusReceiver();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                walkerReceiver, filter);

        paymentMode = Const.CASH;
        new PreferenceHelper(getActivity()).putPaymentMode(paymentMode);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity.btnNotification.setVisibility(View.VISIBLE);
        activity.tvTitle.setVisibility(View.VISIBLE);
        activity.setIcon(R.drawable.fare_info);
        activity.setTitle(getString(R.string.text_make_request));
        activity.btnNotification.setOnClickListener(this);
        adapter = new PlacesAutoCompleteAdapter(activity,
                R.layout.autocomplete_list_text);
        adapterDestination = new PlacesAutoCompleteAdapter(activity,
                R.layout.autocomplete_list_text);
        etSource.setAdapter(adapter);
        locHelper = new LocationHelper(activity);
        locHelper.setLocationReceivedLister(this);

        etDestination.setAdapter(adapterDestination);
        etSource.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                etSource.setText("");
            }
        });
        etSource.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                final String selectedDestPlace = adapter.getItem(arg2);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final LatLng latlng = getLocationFromAddress(selectedDestPlace);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                isMapTouched = true;
                                curretLatLng = latlng;
                                isSource = true;
                                setMarker(curretLatLng, isSource);
                                setMarkerOnRoad(curretLatLng, curretLatLng);
                                animateCameraToMarker(curretLatLng, true);
                                stopUpdateProvidersLoaction();
                                getAllProviders(curretLatLng);
                            }
                        });
                    }
                }).start();
            }
        });


        locHelper.onStart();

        etDestination.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                final String selectedDestPlace = adapterDestination
                        .getItem(arg2);
                etDestination.setText(selectedDestPlace);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final LatLng latlng = getLocationFromAddress(selectedDestPlace);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                isSource = false;
                                setMarker(latlng, isSource);
                                setMarkerOnRoad(latlng, latlng);
                            }
                        });
                    }
                }).start();
            }
        });

        getVehicalTypes();
    }

    @Override
    public void onResume() {
        super.onResume();
        activity.tvTitle.setText(getString(R.string.text_make_request));
        mMapView.onResume();
        startCheckingStatusUpdate();
        startUpdateProvidersLocation();
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_UP
                        && keyCode == KeyEvent.KEYCODE_BACK) {
                    if (sendReqLayout.getVisibility() == View.VISIBLE) {
                        cancelConfirmation();
                    } else {
                        return false;
                    }
                    return true;
                }
                return false;
            }
        });
    }

    private void cancelConfirmation() {
        if (markerSource != null) {
            markerSource.remove();
            markerSource = null;
        }
        if (markerDestination != null) {
            markerDestination.remove();
            markerDestination = null;
        }
        if (polyLine != null)
            polyLine.remove();

        appliedPromoCode = "";
        new PreferenceHelper(activity).putPromoCode(appliedPromoCode);
        isAddDestination = false;
        isSource = true;
        etDestination.setText("");
        vehicleLayout.setVisibility(View.VISIBLE);
        tvVehicleType.setVisibility(View.VISIBLE);
        layoutDestination.setVisibility(View.GONE);
        sendReqLayout.setVisibility(View.GONE);
        // confirmLayout.setVisibility(View.GONE);
        layoutBubble.setVisibility(View.VISIBLE);
        vehicleLayout.setVisibility(View.VISIBLE);
        linearPickupAddress.setVisibility(View.VISIBLE);
        btnMyLocation.setVisibility(View.VISIBLE);
    }

    private void showReferralDialog() {
        referralDialog = new Dialog(getActivity(), R.style.MyDialog);
        referralDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        referralDialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        referralDialog.setContentView(R.layout.dialog_referral);
        referralDialog.setCancelable(false);
        etRefCode = (EditText) referralDialog.findViewById(R.id.etRefCode);
        llErrorMsg = (LinearLayout) referralDialog
                .findViewById(R.id.llErrorMsg);
        referralDialog.findViewById(R.id.btnRefSubmit).setOnClickListener(this);
        referralDialog.findViewById(R.id.btnSkip).setOnClickListener(this);
        referralDialog.show();
    }

    private void applyReffralCode(boolean isShowLoader) {
        if (isShowLoader)
            AndyUtils.showCustomProgressDialog(activity,
                    getString(R.string.progress_loading), false, null);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL, Const.ServiceType.APPLY_REFFRAL_CODE);
        map.put(Const.Params.REFERRAL_CODE, etRefCode.getText().toString());
        map.put(Const.Params.ID, activity.pHelper.getUserId());
        map.put(Const.Params.TOKEN, activity.pHelper.getSessionToken());
        map.put(Const.Params.IS_SKIP, String.valueOf(is_skip));
        new HttpRequester(activity, map, Const.ServiceCode.APPLY_REFFRAL_CODE,
                this);
        // requestQueue.add(new VolleyHttpRequest(Method.POST, map,
        // Const.ServiceCode.APPLY_REFFRAL_CODE, this, this));
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the
        // map.
        if (map == null) {
            // map = ((SupportMapFragment) activity.getSupportFragmentManager()
            // .findFragmentById(R.id.map)).getMap();
            map = ((CustomEventMapView) view.findViewById(R.id.map)).getMap();
            mMapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }


                    map.setMyLocationEnabled(false);
                    map.getUiSettings().setMyLocationButtonEnabled(false);
                    map.getUiSettings().setZoomControlsEnabled(false);

                    btnMyLocation.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            // Location loc = map.getMyLocation();
                            if (myLocation != null) {
                                LatLng latLang = new LatLng(myLocation.getLatitude(),
                                        myLocation.getLongitude());
                                animateCameraToMarker(latLang, true);
                            }
                        }
                    });

                    map.setOnCameraChangeListener(new OnCameraChangeListener() {

                        public void onCameraChange(CameraPosition camPos) {
                            if (currentZoom == -1) {
                                currentZoom = camPos.zoom;
                            } else if (camPos.zoom != currentZoom) {
                                currentZoom = camPos.zoom;
                                return;
                            }

                            if (!isMapTouched) {
                                curretLatLng = camPos.target;
                                if (!isAddDestination) {
                                    // layoutMarker.setVisibility(LinearLayout.VISIBLE);
                                    if (listType.size() > 0) {
                                        stopUpdateProvidersLoaction();
                                        getAllProviders(curretLatLng);
                                    }
                                    getAddressFromLocation(camPos.target, etSource);
                                }
                            }
                            isMapTouched = false;
                            // setMarker(camPos.target);
                        }

                    });
                    if (map != null) {
                        // Log.i("Map", "Map Fragment");
                    }

                }
            });

        }
    }

    @Override
    public void onPause() {
        stopCheckingStatusUpdate();
        stopUpdateProvidersLoaction();
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroyView() {

        SupportMapFragment f = (SupportMapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        if (f != null) {
            try {
                getFragmentManager().beginTransaction().remove(f).commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        map = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(
                walkerReceiver);
        etSource.setText("");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.etEnterSouce:
                etSource.setText(" ");
                break;

            case R.id.markerBubblePickMeUp:
                if (isValidate()) {
                    requestConfirmation(null);
                }
                break;
            case R.id.btnRideNow:
                pickMeUp();
                break;
            case R.id.btnRideLater:
                showDateTimePicker();
                break;
            case R.id.btnActionNotification:
                showVehicleDetails();
                break;
            case R.id.tvCardNo:
                if (preference.getDefaultCard() == 0) {
                    AndyUtils.showToast(getResources().getString(R.string.no_card),
                            activity);
                    return;
                }
                if (selectedCash.getVisibility() == View.VISIBLE)
                    selectedCash.setVisibility(View.GONE);

                selectedCard.setVisibility(View.VISIBLE);
                tvCard.setSelected(true);
                tvCash.setSelected(false);
                paymentMode = Const.CREDIT;
                new PreferenceHelper(getActivity()).putPaymentMode(paymentMode);
                break;

            case R.id.tvCash:
                if (selectedCard.getVisibility() == View.VISIBLE) {
                    selectedCard.setVisibility(View.GONE);
                }
                selectedCash.setVisibility(View.VISIBLE);
                tvCard.setSelected(false);
                tvCash.setSelected(true);
                paymentMode = Const.CASH;
                new PreferenceHelper(getActivity()).putPaymentMode(paymentMode);
                break;
            case R.id.btnOKFareQuote:
                quoteDialog.dismiss();
                break;
            case R.id.tvGetFareEst:
                if (address != null) {
                    showDestinationPopup();
                } else {
                    AndyUtils.showToast(
                            getString(R.string.text_enter_pickup_location),
                            activity);
                }
                break;
            case R.id.btnEditHome:
                layoutHomeEdit.setVisibility(LinearLayout.VISIBLE);
                layoutHomeText.setVisibility(LinearLayout.GONE);
                break;
            case R.id.btnEditWork:
                layoutWorkEdit.setVisibility(LinearLayout.VISIBLE);
                layoutWorkText.setVisibility(LinearLayout.GONE);
                break;
            case R.id.layoutHomeText:
                if (preference.getWorkAddress() != null)
                    sendQuoteRequest(preference.getHomeAddress());
                break;
            case R.id.layoutWorkText:
                if (preference.getWorkAddress() != null)
                    sendQuoteRequest(preference.getWorkAddress());
                break;
            case R.id.imgClearDest:
                destinationDialog.dismiss();
                break;
            case R.id.imgClearHome:
                etHomeAddress.setText("");
                break;
            case R.id.imgClearWork:
                etWorkAddress.setText("");
                break;
            case R.id.btnRefSubmit:
                if (etRefCode.getText().length() == 0) {
                    AndyUtils.showToast(
                            getResources().getString(R.string.text_blank_ref_code),
                            activity);
                    return;
                } else {
                    if (!AndyUtils.isNetworkAvailable(activity)) {
                        AndyUtils
                                .showToast(
                                        getResources().getString(
                                                R.string.dialog_no_inter_message),
                                        activity);
                        return;
                    }
                    is_skip = 0;
                    applyReffralCode(true);
                }
                break;
            case R.id.btnSkip:
                is_skip = 1;
                applyReffralCode(false);
                activity.onBackPressed();
                break;
            case R.id.cancelVehicleDetail:
                dialog.dismiss();
                break;
            case R.id.btn_cancel_request:
                showCancellationtDialog();
                break;
            case R.id.btnOk:
                confirmFutureRequest.dismiss();
                AndyUtils.showToast(getString(R.string.text_request_sumitted), activity);
                break;
            default:
                break;
        }
    }

    private void showCancellationtDialog() {
        cancellationDialog = new Dialog(activity);
        cancellationDialog.setTitle(getString(R.string.text_tell_us_why));
        cancellationDialog.setContentView(R.layout.dilog_cancel_future_request);
        ok = (Button) cancellationDialog.findViewById(R.id.btnOk);
        ok.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                cancellationDialog.dismiss();
                AndyUtils.showCustomProgressDialog(activity, getResources()
                                .getString(R.string.progress_canceling_trip), false,
                        null);

                HashMap<String, String> hmap = new HashMap<String, String>();
                hmap.put(Const.URL, Const.ServiceType.DELETE_FUTURE_REQUEST);
                hmap.put(Const.Params.TOKEN,
                        new PreferenceHelper(activity).getSessionToken());
                hmap.put(Const.Params.ID,
                        new PreferenceHelper(activity).getUserId());
                hmap.put(Const.Params.REQUEST_ID, String.valueOf(reqId));

                new HttpRequester(activity, hmap,
                        Const.ServiceCode.DELETE_FUTURE_REQUEST,
                        MapFragment.this);
                // requestQueue.add(new VolleyHttpRequest(Method.POST, hmap,
                // Const.ServiceCode.DELETE_FUTURE_REQUEST,
                // MapFragment.this, MapFragment.this));
            }
        });
        cancellationDialog.show();
    }

    private void requestConfirmation(String destAddr) {
        isAddDestination = true;
        isSource = true;
        tvTripPickupAddress.setText(etSource.getText().toString());
        setMarker(curretLatLng, isSource);
        tvVehicleType.setVisibility(View.GONE);
        layoutDestination.setVisibility(View.VISIBLE);
        sendReqLayout.setVisibility(View.VISIBLE);
        // confirmLay out.setVisibility(View.VISIBLE);
        layoutBubble.setVisibility(View.GONE);
        vehicleLayout.setVisibility(View.GONE);
        linearPickupAddress.setVisibility(View.GONE);
        btnMyLocation.setVisibility(View.GONE);
        if (preference.getPaymentMode() == Const.CREDIT)
            selectedCard.setVisibility(View.VISIBLE);
        else
            selectedCash.setVisibility(View.VISIBLE);
        if (destAddr != null) {
            LatLng destLatLng = getLocationFromAddress(destAddr);
            addDestination(destAddr, destLatLng);
        }
    }

    private void addDestination(String destAddr, LatLng destLatLng) {
        isSource = false;
        setMarker(destLatLng, isSource);
        setMarkerOnRoad(destLatLng, destLatLng);
        etDestination.setText(destAddr);
        etDestination.dismissDropDown();
    }

    private void showDateTimePicker() {

        final Calendar c = Calendar.getInstance();
        final int year = c.get(Calendar.YEAR);
        final int month = c.get(Calendar.MONTH);
        final int date = c.get(Calendar.DAY_OF_MONTH);
        final int hour = c.get(Calendar.HOUR_OF_DAY);
        final int minute = c.get(Calendar.MINUTE);

        scheduleDialog = new Dialog(activity);
        scheduleDialog.setContentView(R.layout.picker_dialog);
        scheduleDialog.setTitle(getResources().getString(
                R.string.text_schedule_trip));
        datePicker = (DatePicker) scheduleDialog.findViewById(R.id.date_picker);
        timePicker = (TimePicker) scheduleDialog.findViewById(R.id.time_picker);
        scheduleBtn = (Button) scheduleDialog
                .findViewById(R.id.confirm_schedule);
        datePicker.setMinDate(System.currentTimeMillis() - 1000);
        datePicker.setMaxDate((System.currentTimeMillis())
                + (2 * 24 * 60 * 60 * 1000));

        selectedDate = String.valueOf(year) + "-" + String.valueOf(month + 1)
                + "-" + String.valueOf(date);

        datePicker.init(year, month, date, new OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker v, int selectedYear,
                                      int selectedMonth, int selectedDay) {

                selectedDate = String.valueOf(datePicker.getYear()) + "-"
                        + String.valueOf((datePicker.getMonth()) + 1) + "-"
                        + String.valueOf(datePicker.getDayOfMonth());

            }

        });
        timePicker.setIs24HourView(true);
        timePicker.setCurrentHour(hour);
        timePicker.setCurrentMinute(minute);
        selectedTime = String.valueOf(hour) + ":" + String.valueOf(minute);
        selectedHour = hour;
        selectedMinute = minute;

        timePicker.setOnTimeChangedListener(new OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int min) {
                selectedHour = hourOfDay;
                selectedMinute = min;
                selectedTime = String.valueOf(hourOfDay) + ":"
                        + String.valueOf(min);
            }
        });

        scheduleBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                if ((datePicker.getDayOfMonth() == c.get(Calendar.DAY_OF_MONTH))
                        && (selectedHour == c.get(Calendar.HOUR_OF_DAY))
                        && (((selectedMinute - c.get(Calendar.MINUTE)) >= 0) && (((selectedMinute - c
                        .get(Calendar.MINUTE)) <= 30)))) {

                    AndyUtils.showCustomProgressDialog(activity,
                            getString(R.string.text_contacting), false, null);
                    HashMap<String, String> map = new HashMap<String, String>();

                    map.put(Const.URL, Const.ServiceType.CREATE_REQUEST);
                    map.put(Const.Params.TOKEN,
                            new PreferenceHelper(activity).getSessionToken());
                    map.put(Const.Params.ID,
                            new PreferenceHelper(activity).getUserId());
                    map.put(Const.Params.LATITUDE,
                            String.valueOf(curretLatLng.latitude));
                    map.put(Const.Params.LONGITUDE,
                            String.valueOf(curretLatLng.longitude));
                    map.put(Const.Params.PAYMENT_OPT, String
                            .valueOf(new PreferenceHelper(activity)
                                    .getPaymentMode()));
                    map.put(Const.Params.TYPE, String.valueOf(listType.get(
                            selectedPostion).getId()));
                    map.put(Const.Params.CARD_ID, String
                            .valueOf(new PreferenceHelper(activity)
                                    .getDefaultCard()));
                    map.put(Const.Params.DISTANCE, "1");
                    map.put(Const.Params.PROMO_CODE, appliedPromoCode);
                    map.put(Const.Params.SOURCE_ADDRESS, tvTripPickupAddress
                            .getText().toString());
                    if (markerDestination != null) {
                        final LatLng dest = markerDestination.getPosition();

                        map.put(Const.Params.DESTI_LATITUDE,
                                String.valueOf(dest.latitude));
                        map.put(Const.Params.DESTI_LONGITUDE,
                                String.valueOf(dest.longitude));
                    }

                    new HttpRequester(activity, map,
                            Const.ServiceCode.CREATE_REQUEST, MapFragment.this);
                } else if ((datePicker.getDayOfMonth() == c
                        .get(Calendar.DAY_OF_MONTH))
                        && (selectedHour == c.get(Calendar.HOUR_OF_DAY))
                        && ((selectedMinute - c.get(Calendar.MINUTE)) < 0)) {
                    AndyUtils.showToast(
                            getString(R.string.text_trip_onwards),
                            activity);
                } else if ((datePicker.getDayOfMonth() == c
                        .get(Calendar.DAY_OF_MONTH))
                        && ((selectedHour - c.get(Calendar.HOUR_OF_DAY)) < 0)) {
                    AndyUtils.showToast(
                            getString(R.string.text_trip_onwards),
                            activity);
                } else if ((datePicker.getDayOfMonth() == c
                        .get(Calendar.DAY_OF_MONTH))
                        && ((selectedHour - c.get(Calendar.HOUR_OF_DAY)) < 0)) {
                    AndyUtils.showToast(
                            getString(R.string.text_trip_onwards),
                            activity);
                } else if ((datePicker.getDayOfMonth() == ((c
                        .get(Calendar.DAY_OF_MONTH)) + 2))
                        && ((selectedHour > c.get(Calendar.HOUR_OF_DAY)))) {
                    AndyUtils.showToast(
                            getString(R.string.text_trip_48_hours),
                            activity);
                } else if ((datePicker.getDayOfMonth() == ((c
                        .get(Calendar.DAY_OF_MONTH)) + 2))
                        && ((selectedHour == c.get(Calendar.HOUR_OF_DAY)))
                        && ((selectedMinute - c.get(Calendar.MINUTE)) > 0)) {

                    // if((selectedMinute - c.get(Calendar.MINUTE)) > 0)
                    AndyUtils.showToast(
                            getString(R.string.text_trip_48_hours),
                            activity);
                } else if ((datePicker.getMonth() == ((c.get(Calendar.MONTH)) + 1))
                        && (datePicker.getDayOfMonth() == 2)
                        && (selectedHour > c.get(Calendar.HOUR_OF_DAY))) {

                    AndyUtils.showToast(
                            getString(R.string.text_trip_48_hours),
                            activity);
                } else if ((datePicker.getMonth() == ((c.get(Calendar.MONTH)) + 1))
                        && (datePicker.getDayOfMonth() == 2)
                        && (selectedHour == c.get(Calendar.HOUR_OF_DAY))
                        && ((selectedMinute - c.get(Calendar.MINUTE)) > 0)) {

                    AndyUtils.showToast(
                            getString(R.string.text_trip_48_hours),
                            activity);
                } else {
                    startTime = selectedDate + " " + selectedTime;
                    activity.pHelper.putStartTime(startTime);
                    Calendar cal = Calendar.getInstance();
                    TimeZone timeZone = cal.getTimeZone();
                    activity.pHelper.putTimeZone(timeZone.getID());
                    scheduleTrip();
                }
            }
        });
        scheduleDialog.show();
    }

    private void scheduleTrip() {
        if (!AndyUtils.isNetworkAvailable(activity)) {
            AndyUtils.showToast(
                    getResources().getString(R.string.dialog_no_inter_message),
                    activity);
            return;
        }
        AndyUtils.showCustomProgressRequestDialog(activity,
                getString(R.string.text_creating_request), true, null);
        HashMap<String, String> map = new HashMap<String, String>();

        map.put(Const.URL, Const.ServiceType.CREATE_FUTURE_REQUEST);
        map.put(Const.Params.TOKEN,
                new PreferenceHelper(activity).getSessionToken());
        map.put(Const.Params.ID, new PreferenceHelper(activity).getUserId());
        map.put(Const.Params.LATITUDE, String.valueOf(curretLatLng.latitude));
        map.put(Const.Params.LONGITUDE, String.valueOf(curretLatLng.longitude));
        map.put(Const.Params.PAYMENT_MODE,
                String.valueOf(new PreferenceHelper(activity).getPaymentMode()));
        map.put(Const.Params.TYPE,
                String.valueOf(listType.get(selectedPostion).getId()));
        map.put(Const.Params.PROMO_CODE, editPromo.getText().toString().trim());
        map.put(Const.Params.SRC_ADDRESS, pickupAddrs);

        map.put(Const.Params.TIME_ZONE,
                new PreferenceHelper(activity).getTimeZone());
        map.put(Const.Params.START_TIME,
                new PreferenceHelper(activity).getStartTime());
        if (markerDestination != null) {
            final LatLng dest = markerDestination.getPosition();
            map.put(Const.Params.DEST_ADDRESS, etDestination.getText()
                    .toString().trim());
            map.put(Const.Params.D_LATITUDE, String.valueOf(dest.latitude));
            map.put(Const.Params.D_LONGITUDE, String.valueOf(dest.longitude));
        }
        new HttpRequester(activity, map,
                Const.ServiceCode.CREATE_FUTURE_REQUEST, MapFragment.this);
        // requestQueue.add(new VolleyHttpRequest(Method.POST, map,
        // Const.ServiceCode.CREATE_FUTURE_REQUEST, this, this));
    }

    private void getAddressFromLocation(final LatLng latlng, final EditText et) {
        et.setText("Waiting for Address");
        et.setTextColor(Color.GRAY);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Geocoder gCoder = new Geocoder(getActivity());
                try {
                    final List<Address> list = gCoder.getFromLocation(
                            latlng.latitude, latlng.longitude, 1);
                    if (list != null && list.size() > 0) {
                        address = list.get(0);
                        StringBuilder sb = new StringBuilder();
                        if (address.getAddressLine(0) != null) {
                            if (address.getMaxAddressLineIndex() > 0) {
                                for (int i = 0; i < address
                                        .getMaxAddressLineIndex(); i++) {
                                    sb.append(address.getAddressLine(i))
                                            .append("\n");
                                }
                                sb.append(",");
                                sb.append(address.getCountryName());
                            } else {
                                sb.append(address.getAddressLine(0));
                            }
                        }

                        strAddress = sb.toString();
                        strAddress = strAddress.replace(",null", "");
                        strAddress = strAddress.replace("null", "");
                        strAddress = strAddress.replace("Unnamed", "");
                    }

                    if (getActivity() == null)
                        return;

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!TextUtils.isEmpty(strAddress)) {
                                et.setFocusable(false);
                                et.setFocusableInTouchMode(false);
                                et.setText(strAddress);
                                et.setTextColor(getColor(getActivity(), android.R.color.black));
                                et.setFocusable(true);
                                et.setFocusableInTouchMode(true);
                            } else {
                                et.setText("");



                                    et.setTextColor(getColor(getActivity(),android.R.color.black));


                            }
                            etSource.setEnabled(true);
                        }
                    });
                }catch(IllegalStateException e)
                {
                    e.printStackTrace();
                }
                catch (Exception exc) {
                    exc.printStackTrace();
                }
            }
        }).start();
    }

    private void animateCameraToMarker(LatLng latLng, boolean isAnimate) {
        try {
            etSource.setFocusable(false);
            etSource.setFocusableInTouchMode(false);
            CameraUpdate cameraUpdate = null;

            cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16);
            if (cameraUpdate != null && map != null) {
                if (isAnimate)
                    map.animateCamera(cameraUpdate);
                else
                    map.moveCamera(cameraUpdate);
            }
            etSource.setFocusable(true);
            etSource.setFocusableInTouchMode(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // perfect function...
    private void animateMarker(final Marker marker, final LatLng toPosition,
                               final Location toLocation, final boolean hideMarker) {
        if (map == null || !this.isVisible() || marker == null
                || marker.getPosition() == null) {
            return;
        }
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = map.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final double startRotation = marker.getRotation();
        final long duration = 500;

        final Interpolator interpolator = new LinearInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));
                float rotation = (float) (t * toLocation.getBearing() + (1 - t)
                        * startRotation);
                if (rotation != 0) {
                    marker.setRotation(rotation);
                }
                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }

    private LatLng getLocationFromAddress(final String place) {
        LatLng loc = null;
        Geocoder gCoder = new Geocoder(getActivity());
        try {
            final List<Address> list = gCoder.getFromLocationName(place, 1);
            if (list != null && list.size() > 0) {
                loc = new LatLng(list.get(0).getLatitude(), list.get(0)
                        .getLongitude());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return loc;
    }

    @Override
    public void onProgressCancel() {
        stopCheckingStatusUpdate();
        cancleRequest();

    }

    @Override
    protected boolean isValidate() {
        String msg = null;
        if (curretLatLng == null) {
            msg = getString(R.string.text_location_not_found);
        } else if (selectedPostion == -1) {
            msg = getString(R.string.text_select_type);
        } else if (TextUtils.isEmpty(etSource.getText().toString())
                || etSource.getText().toString()
                .equalsIgnoreCase("Waiting for Address")) {
            msg = getString(R.string.text_waiting_for_address);
        }
        pickupAddrs = etSource.getText().toString().trim();
        if (msg == null)
            return true;
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
        return false;
    }

    private void pickMeUp() {
        if (!AndyUtils.isNetworkAvailable(activity)) {
            AndyUtils.showToast(getResources().getString(R.string.no_internet),
                    activity);
            return;
        } else if (preference.getDefaultCard() == 0
                && paymentMode == Const.CREDIT) {
            AndyUtils.showToast(getResources().getString(R.string.no_card),
                    activity);
            return;
        }
        AndyUtils.showCustomProgressRequestDialog(activity,
                getString(R.string.text_creating_request), true, null);
        HashMap<String, String> map = new HashMap<String, String>();

        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d hh:mm a");
        String currentDate = sdf.format(c.getTime());
        TimeZone timezone = TimeZone.getDefault();

        map.put(Const.URL, Const.ServiceType.CREATE_REQUEST);
        map.put(Const.Params.TOKEN,
                new PreferenceHelper(activity).getSessionToken());
        map.put(Const.Params.ID, new PreferenceHelper(activity).getUserId());
        map.put(Const.Params.LATITUDE, String.valueOf(curretLatLng.latitude));
        map.put(Const.Params.LONGITUDE, String.valueOf(curretLatLng.longitude));
        map.put(Const.Params.PAYMENT_MODE,
                String.valueOf(new PreferenceHelper(activity).getPaymentMode()));
        map.put(Const.Params.TYPE,
                String.valueOf(listType.get(selectedPostion).getId()));
        map.put(Const.Params.PROMO_CODE, appliedPromoCode);
        map.put(Const.Params.SRC_ADDRESS, pickupAddrs);
        map.put(Const.Params.CURRENT_DATE, currentDate);
        map.put(Const.Params.TIME_ZONE, timezone.getID());
        if (markerDestination != null) {
            final LatLng dest = markerDestination.getPosition();
            map.put(Const.Params.DEST_ADDRESS, etDestination.getText()
                    .toString());
            map.put(Const.Params.D_LATITUDE, String.valueOf(dest.latitude));
            map.put(Const.Params.D_LONGITUDE, String.valueOf(dest.longitude));
        }
        new HttpRequester(activity, map, Const.ServiceCode.CREATE_REQUEST, this);
        // requestQueue.add(new VolleyHttpRequest(Method.POST, map,
        // Const.ServiceCode.CREATE_REQUEST, this, this));
    }


    @Override
    public void onTaskCompleted(final String response, int serviceCode) {
        super.onTaskCompleted(response, serviceCode);
        switch (serviceCode) {
            case Const.ServiceCode.CREATE_REQUEST:
                // AppLog.Log(Const.TAG, "Create Request Response::::" + response);
                AndyUtils.removeCustomProgressRequestDialog();
                if (activity.pContent.isSuccess(response)) {
                    activity.pHelper.putRequestLocation(curretLatLng);
                    activity.pHelper.putRequestId(activity.pContent
                            .getRequestId(response));
                    Driver driverInfo = activity.pContent.getDriverDetail(response);

                    AndyUtils.showDriverDetailDialog(activity, this, driverInfo);

                    stopUpdateProvidersLoaction();
                    startCheckingStatusUpdate();
                } else {
                    cancelConfirmation();
                }

                break;

            case Const.ServiceCode.CREATE_FUTURE_REQUEST:
                // AppLog.Log("UberMapFragment", "Create future request Response : "
                // + response);
                if (activity.pContent.isSuccess(response)) {
                    activity.pHelper.putRequestLocation(curretLatLng);
                    scheduleDialog.dismiss();
                    if (!(TextUtils.isEmpty(response))) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray jArray = jsonObject
                                    .getJSONArray("all_scheduled_requests");
                            if (jsonObject.getBoolean("success")) {
                                JSONObject jObj = jArray.getJSONObject(0);
                                reqId = jObj.getInt(Const.Params.REQ_ID);
                                cancelConfirmation();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    AndyUtils.removeCustomProgressRequestDialog();
                    showConfirmationDialog();
                } else {
                    AndyUtils.removeCustomProgressRequestDialog();
                    AndyUtils.showToast(getString(R.string.text_error), activity);
                }
                break;

            case Const.ServiceCode.GET_REQUEST_STATUS:
                // AppLog.Log(Const.TAG, "Get Request Response:::::::::" +
                // response);
                if (activity.pContent.isSuccess(response)) {
                    switch (activity.pContent.checkRequestStatus(response)) {
                        case Const.IS_WALK_STARTED:
                        case Const.IS_WALKER_ARRIVED:
                        case Const.IS_COMPLETED:
                        case Const.IS_WALKER_STARTED:
                            AndyUtils.removeCustomProgressRequestDialog();
                            //stopCheckingStatusUpdate();
                            stopUpdateProvidersLoaction();

                            Driver driver = activity.pContent.getDriverDetail(response);

                            if (this.isVisible())
                                removeThisFragment();
                            activity.gotoTripFragment(driver);
                            tvVehicleType.setVisibility(View.VISIBLE);
                            layoutDestination.setVisibility(View.GONE);
                            sendReqLayout.setVisibility(View.GONE);
                            layoutBubble.setVisibility(View.VISIBLE);
                            vehicleLayout.setVisibility(View.VISIBLE);
                            linearPickupAddress.setVisibility(View.VISIBLE);
                            break;
                        case Const.IS_WALKER_RATED:
                            stopCheckingStatusUpdate();
                            if (this.isVisible())
                                removeThisFragment();
                            activity.gotoRateFragment(activity.pContent
                                    .getDriverDetail(response));
                            break;

                        case Const.IS_REQEUST_CREATED:
                            if (activity.pHelper.getRequestId() != Const.NO_REQUEST) {
                                Driver driverInfo = activity.pContent
                                        .getDriverDetail(response);
                                AndyUtils.showCustomProgressDialog(activity,
                                        getString(R.string.text_contacting), false,
                                        this, driverInfo);

                                stopUpdateProvidersLoaction();
                            }
                            isContinueRequest = true;
                            break;
                        case Const.NO_REQUEST:
                            if (!isGettingVehicalType) {
                                AndyUtils.removeDriverDetailDialog();
                                startUpdateProvidersLocation();
                            }
                          //  stopCheckingStatusUpdate();
                            break;
                        default:
                            isContinueRequest = false;
                            break;
                    }

                } else if (activity.pContent.getErrorCode(response) == Const.REQUEST_ID_NOT_FOUND) {
                    AndyUtils.removeCustomProgressDialog();
                    activity.pHelper.clearRequestData();
                    isContinueRequest = false;
                } else if (activity.pContent.getErrorCode(response) == Const.INVALID_TOKEN) {
                    if (activity.pHelper.getLoginBy()
                            .equalsIgnoreCase(Const.MANUAL))
                        login();
                    else
                        loginSocial(activity.pHelper.getUserId(),
                                activity.pHelper.getLoginBy());
                } else {
                    isContinueRequest = true;
                }
                break;
            case Const.ServiceCode.CANCEL_REQUEST:
                Log.d("", "cancel request response: " + response);
                if (activity.pContent.isSuccess(response)) {
                    activity.pHelper.clearRequestData();
                    AndyUtils.removeDriverDetailDialog();
                    AndyUtils
                            .showToast(getString(R.string.text_request_cancelled), activity);
                }

                break;
            case Const.ServiceCode.GET_VEHICAL_TYPES:
                AppLog.Log(Const.TAG, "Vehical Response:::::::::" +
                        response);
                if (activity.pContent.isSuccess(response)) {
                    listType.clear();
                    activity.pContent.parseTypes(response, listType);
                    pointer = listType.size();

                    fancyCoverFlow.setAdapter(new FancyCoverAdapter(activity,
                            listType));

                    if (listType.size() > 2) {
                        selectedPostion = 1;
                        fancyCoverFlow.setSelection(1);
                    } else {
                        selectedPostion = 0;
                    }

                    isGettingVehicalType = false;


                }
                AndyUtils.removeCustomProgressDialog();
                break;
            case Const.ServiceCode.GET_PROVIDERS:
                try {
                    map.getUiSettings().setScrollGesturesEnabled(true);
                    // AppLog.Log("", "Provider Response : " + response);
                    if (new JSONObject(response).getBoolean("success")) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                listDriver = new ArrayList<Driver>();
                                listDriver = activity.pContent
                                        .parseNearestDrivers(response);
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (isVisible())
                                            setProvirderOnMap();
                                    }
                                });
                            }
                        }).start();
                    } else {
                        // Log.d("clear map", "GET_PROVIDERS");
                        map.clear();
                    }
                } catch (Exception e) {
                }
                break;
            case Const.ServiceCode.GET_DURATION:

                if (!TextUtils.isEmpty(response)) {
                    try {
                        estimatedTimeTxt = activity.pContent
                                .parseNearestDriverDurationString(response);
                        if (estimatedTimeTxt != null) {

                            tvEstimatedTime.setText(estimatedTimeTxt);
                        } else {
                            tvEstimatedTime.setText(getString(R.string.text_n_a));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;

            case Const.ServiceCode.DRAW_PATH_ROAD:
                if (!TextUtils.isEmpty(response)) {
                    route = new Route();
                    activity.pContent.parseRoute(response, route);

                    final ArrayList<Step> step = route.getListStep();
                    System.out.println("step size=====> " + step.size());
                    points = new ArrayList<LatLng>();
                    lineOptions = new PolylineOptions();

                    for (int i = 0; i < step.size(); i++) {
                        List<LatLng> path = step.get(i).getListPoints();
                        System.out.println("step =====> " + i + " and "
                                + path.size());
                        points.addAll(path);
                    }
                    if (points != null && points.size() > 0) {
                        setMarker(new LatLng(points.get(0).latitude,
                                points.get(0).longitude), isSource);
                        if (isSource) {
                            getAddressFromLocation(
                                    new LatLng(points.get(0).latitude,
                                            points.get(0).longitude), etSource);
                        }

                        if (markerSource != null && markerDestination != null) {
                            showDirection(markerSource.getPosition(),
                                    markerDestination.getPosition());
                        }
                    }
                }
                break;
            case Const.ServiceCode.DRAW_PATH:
                if (!TextUtils.isEmpty(response)) {
                    route = new Route();
                    activity.pContent.parseRoute(response, route);

                    final ArrayList<Step> step = route.getListStep();
                    System.out.println("step size=====> " + step.size());
                    points = new ArrayList<LatLng>();
                    lineOptions = new PolylineOptions();

                    for (int i = 0; i < step.size(); i++) {

                        List<LatLng> path = step.get(i).getListPoints();
                        System.out.println("step =====> " + i + " and "
                                + path.size());
                        points.addAll(path);

                    }

                    if (polyLine != null)
                        polyLine.remove();
                    lineOptions.addAll(points);
                    lineOptions.width(15);
                    lineOptions.color(getResources().getColor(R.color.color_path)); // #00008B
                    // rgb(0,0,139)

                    if (lineOptions != null && map != null) {
                        polyLine = map.addPolyline(lineOptions);

                        LatLngBounds.Builder bld = new LatLngBounds.Builder();

                        bld.include(markerSource.getPosition());
                        bld.include(markerDestination.getPosition());
                        LatLngBounds latLngBounds = bld.build();
                        map.moveCamera(CameraUpdateFactory.newLatLngBounds(
                                latLngBounds, 30));
                    }
                }
                break;

            case Const.ServiceCode.GET_FARE_QUOTE:
                if (!TextUtils.isEmpty(response)) {
                    try {
                        JSONArray jsonArray = new JSONObject(response)
                                .getJSONArray("routes");
                        JSONArray jArrSub = jsonArray.getJSONObject(0)
                                .getJSONArray("legs");
                        JSONObject legObj = jArrSub.getJSONObject(0);

                        JSONObject durationObj = legObj.getJSONObject("duration");
                        JSONObject distanceObj = legObj.getJSONObject("distance");

                        double minute = durationObj.getDouble("value") / 60;
                        double kms = distanceObj.getDouble("value") / 1000;

                        tvETA.setText(new DecimalFormat("##.##").format(minute)
                                + " "+getString(R.string.text_min));
                        pbMinFare.setVisibility(View.GONE);
                        tvTotalFare.setVisibility(View.VISIBLE);
                        tvTotalFare.setText(getString(R.string.payment_unit)
                                + getFareCalculation(kms));
                    } catch (Exception e) {
                        AppLog.Log("UberMapFragment=====",
                                "GET_FARE_QUOTE Response: " + e);
                    }
                }
                break;
            case Const.ServiceCode.GET_NEAR_BY:
                // AppLog.Log("TAG", "Near by : " + response);
                pbNearby.setVisibility(View.GONE);
                nearByList.setVisibility(View.VISIBLE);
                ArrayList<String> resultList = new ArrayList<String>();
                activity.pContent.parseNearByPlaces(response, resultList);
                nearByAd = new ArrayAdapter<String>(getActivity(),
                        R.layout.autocomplete_list_text, R.id.tvPlace, resultList);
                nearByList.setAdapter(nearByAd);
                break;
            case Const.ServiceCode.UPDATE_PROVIDERS:
                // AppLog.Log("Mapfragment", "UPDATE_PROVIDERS : " + response);
                try {
                    if (new JSONObject(response).getBoolean("success")) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                listUpdatedDriver = new ArrayList<Driver>();
                                listUpdatedDriver = activity.pContent
                                        .parseNearestDrivers(response);
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        updateProviderOnMap();
                                    }
                                });
                            }
                        }).start();
                    } else {
                        Log.d("clear map", "UPDATE_PROVIDERS");
                        if (sendReqLayout.getVisibility() != View.VISIBLE) {
                            map.clear();
                        }
                        // if (etDestination.getText().toString().length() != 0) {
                        // markerSource = map
                        // .addMarker(new MarkerOptions()
                        // .position(
                        // new LatLng(
                        // curretLatLng.latitude,
                        // curretLatLng.longitude))
                        // .title(getResources().getString(
                        // R.string.text_source_pin_title))
                        // .icon(BitmapDescriptorFactory
                        // .fromResource(R.drawable.pin_client_org)));
                        // markerSource.setDraggable(false);
                        // showDirection(markerSource.getPosition(),
                        // markerDestination.getPosition());
                        // }

                    }
                } catch (Exception e) {
                }
                break;
            case Const.ServiceCode.APPLY_REFFRAL_CODE:
                AndyUtils.removeCustomProgressDialog();
                // AppLog.Log(Const.TAG, "Referral Response: " + response);
                if (new ParseContent(activity).isSuccess(response)) {
                    new PreferenceHelper(activity).putReferee(1);
                    referralDialog.dismiss();
                    // activity.startActivity(new Intent(activity,
                    // MainDrawerActivity.class));
                } else {
                    llErrorMsg.setVisibility(View.VISIBLE);
                    etRefCode.requestFocus();
                }
                break;

            case Const.ServiceCode.DELETE_FUTURE_REQUEST:
                AppLog.Log("DELETE_FUTURE_REQUEST response", response);
                if (activity.pContent.isSuccess(response)) {

                    confirmFutureRequest.dismiss();
                    AndyUtils.showToast(getString(R.string.text_request_cancelled),
                            activity);
                    AndyUtils.removeCustomProgressDialog();
                    appliedPromoCode = "";
                    new PreferenceHelper(activity).putPromoCode(appliedPromoCode);
                    editPromo.setText(getString(R.string.text_enter_promo_code));
                    isAddDestination = false;
                    isSource = true;
                    etDestination.setText("");
                    tvVehicleType.setVisibility(View.VISIBLE);
                    layoutDestination.setVisibility(View.GONE);
                    sendReqLayout.setVisibility(View.GONE);
                    // confirmLayout.setVisibility(View.GONE);
                    layoutBubble.setVisibility(View.VISIBLE);
                    vehicleLayout.setVisibility(View.VISIBLE);
                    linearPickupAddress.setVisibility(View.VISIBLE);

                } else {
                    AndyUtils.removeCustomProgressDialog();
                    AndyUtils.showToast("Error", activity);
                }
                break;
        }
    }

    private void showConfirmationDialog() {
        confirmFutureRequest = new Dialog(activity);
        confirmFutureRequest.requestWindowFeature(Window.FEATURE_NO_TITLE);
        confirmFutureRequest
                .setContentView(R.layout.dialog_confirm_future_request);
        cancel = (Button) confirmFutureRequest
                .findViewById(R.id.btn_cancel_request);
        ok = (Button) confirmFutureRequest.findViewById(R.id.btnOk);
        cancel.setOnClickListener(this);
        ok.setOnClickListener(this);
        confirmFutureRequest.show();
    }

    private void getRequestStatus(String requestId) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL,
                Const.ServiceType.GET_REQUEST_STATUS + Const.Params.ID + "="
                        + new PreferenceHelper(activity).getUserId() + "&"
                        + Const.Params.TOKEN + "="
                        + new PreferenceHelper(activity).getSessionToken()
                        + "&" + Const.Params.REQUEST_ID + "=" + requestId);

        new HttpRequester(activity, map, Const.ServiceCode.GET_REQUEST_STATUS,
                true, this);
        // requestQueue.add(new VolleyHttpRequest(Method.GET, map,
        // Const.ServiceCode.GET_REQUEST_STATUS, this, this));
    }

    private void startCheckingStatusUpdate() {
        stopCheckingStatusUpdate();
        if (activity.pHelper.getRequestId() != Const.NO_REQUEST) {
            isContinueRequest = true;
          statusHandler.postDelayed(timerRequestStatusRunnable, LOCATION_SCHEDULE);

           /* timer = new Timer();
            timer.scheduleAtFixedRate(new TimerRequestStatus(), Const.DELAY,
                    Const.TIME_SCHEDULE);
*/

        }
    }

    private void stopCheckingStatusUpdate() {
        isContinueRequest = false;
        if (statusHandler != null) {
            statusHandler.removeCallbacks(timerRequestStatusRunnable);

        }
    }

    private void cancleRequest() {
        if (!AndyUtils.isNetworkAvailable(activity)) {
            AndyUtils.showToast(getResources().getString(R.string.no_internet),
                    activity);
            return;
        }
        AndyUtils.removeCustomProgressRequestDialog();
        AndyUtils.showCustomProgressRequestDialog(activity,
                getString(R.string.text_canceling_request), true, null);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL, Const.ServiceType.CANCEL_REQUEST);
        map.put(Const.Params.ID, String.valueOf(activity.pHelper.getUserId()));
        map.put(Const.Params.TOKEN,
                String.valueOf(activity.pHelper.getSessionToken()));
        map.put(Const.Params.REQUEST_ID,
                String.valueOf(activity.pHelper.getRequestId()));
        new HttpRequester(activity, map, Const.ServiceCode.CANCEL_REQUEST, this);
        // requestQueue.add(new VolleyHttpRequest(Method.POST, map,
        // Const.ServiceCode.CANCEL_REQUEST, this, this));
    }

    private void removeThisFragment() {
        try {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .remove(this).commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getVehicalTypes() {
        AndyUtils.showCustomProgressDialog(activity, getString(R.string.progress_loading), false, null);
        isGettingVehicalType = true;
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL, Const.ServiceType.GET_VEHICAL_TYPES);
        // AppLog.Log(Const.TAG, Const.URL);
        new HttpRequester(activity, map, Const.ServiceCode.GET_VEHICAL_TYPES,
                true, this);
        // requestQueue.add(new VolleyHttpRequest(Method.GET, map,
        // Const.ServiceCode.GET_VEHICAL_TYPES, this, this));
    }


    private void getAllProviders(LatLng latlang) {
        try {
            estimatedTimeTxt = "";
            if (!AndyUtils.isNetworkAvailable(activity)) {
                AndyUtils.showToast(
                        getResources().getString(R.string.no_internet),
                        activity);
                return;
            } else if (latlang == null) {
                Toast.makeText(activity,
                        getString(R.string.text_location_not_found),
                        Toast.LENGTH_SHORT).show();
                return;
            }
            // AppLog.Log("TAG", "Provider lat : " + latlang.latitude +
            // " Long :"
            // + latlang.longitude);
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(Const.URL, Const.ServiceType.GET_PROVIDERS);
            map.put(Const.Params.ID,
                    String.valueOf(activity.pHelper.getUserId()));
            map.put(Const.Params.TOKEN,
                    String.valueOf(activity.pHelper.getSessionToken()));
            map.put(Const.Params.USER_LATITUDE,
                    String.valueOf(latlang.latitude));
            map.put(Const.Params.USER_LONGITUDE,
                    String.valueOf(latlang.longitude));
            new HttpRequester(activity, map, Const.ServiceCode.GET_PROVIDERS,
                    this);
            // requestQueue.add(new VolleyHttpRequest(Method.POST, map,
            // Const.ServiceCode.GET_PROVIDERS, this, this));
        } catch (Exception e) {
            AppLog.Log("TAG", "getAllProviderException : " + e);
        }
    }

    private void updateAllProviders(LatLng latlang) {
        try {
            if (!AndyUtils.isNetworkAvailable(activity)) {
                return;
            } else if (latlang == null) {
                return;
            }
            // AppLog.Log("TAG", "Update Provider lat : " + latlang.latitude
            // + " Long :" + latlang.longitude);
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(Const.URL, Const.ServiceType.GET_PROVIDERS);
            map.put(Const.Params.ID,
                    String.valueOf(activity.pHelper.getUserId()));
            map.put(Const.Params.TOKEN,
                    String.valueOf(activity.pHelper.getSessionToken()));
            map.put(Const.Params.USER_LATITUDE,
                    String.valueOf(latlang.latitude));
            map.put(Const.Params.USER_LONGITUDE,
                    String.valueOf(latlang.longitude));
            new HttpRequester(activity, map,
                    Const.ServiceCode.UPDATE_PROVIDERS, this);
            // requestQueue.add(new VolleyHttpRequest(Method.POST, map,
            // Const.ServiceCode.UPDATE_PROVIDERS, this, this));
        } catch (Exception e) {
            // AppLog.Log("TAG", "updateAllProviderException : " + e);
        }
    }

    private void getDuration(LatLng origin, String lat, String lng) {
        if (!AndyUtils.isNetworkAvailable(activity)) {
            AndyUtils.showToast(getResources().getString(R.string.no_internet),
                    activity);
            return;
        } else if (origin == null) {
            return;
        }
        String str_origin = "origin=" + origin.latitude + ","
                + origin.longitude;
        String str_dest = "destination=" + lat + "," + lng;
        String sensor = "sensor=false";
        String parameters = str_origin + "&" + str_dest + "&" + sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + parameters + "&key="
                + Const.PLACES_AUTOCOMPLETE_API_KEY;
        // AppLog.Log("MapFragment", "Url : " + url);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL, url);
        new HttpRequester(activity, map, Const.ServiceCode.GET_DURATION, true,
                this);
        // requestQueue.add(new VolleyHttpRequest(Method.GET, map,
        // Const.ServiceCode.GET_DURATION, this, this));
    }

    private void getFareQuote(LatLng origin, String destination) {
        if (!AndyUtils.isNetworkAvailable(activity)) {
            AndyUtils.showToast(getResources().getString(R.string.no_internet),
                    activity);
        } else {
            try {
                LatLng dest = getLocationFromAddress(destination);

                String str_origin = "origin=" + origin.latitude + ","
                        + origin.longitude;
                String str_dest = "destination=" + dest.latitude + ","
                        + dest.longitude;
                String sensor = "sensor=false";
                String parameters = str_origin + "&" + str_dest + "&" + sensor;
                String output = "json";
                String url = "https://maps.googleapis.com/maps/api/directions/"
                        + output + "?" + parameters + "&key="
                        + Const.PLACES_AUTOCOMPLETE_API_KEY;
                 AppLog.Log("MapFragment-----------------------", "Url : " + url);
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(Const.URL, url);
                new HttpRequester(activity, map,
                        Const.ServiceCode.GET_FARE_QUOTE, this);
                // requestQueue.add(new VolleyHttpRequest(Method.POST, map,
                // Const.ServiceCode.GET_FARE_QUOTE, this, this));
            } catch (Exception e) {

            }
        }
    }

    private void setMarker(LatLng latLng, boolean isSource) {
        if (!MapFragment.this.isVisible())
            return;
        if (getActivity() != null && getActivity().getCurrentFocus() != null) {
            // inputMethodManager.hideSoftInputFromWindow(getActivity()
            // .getCurrentFocus().getWindowToken(), 0);
            activity.hideKeyboard();
        }

        if (latLng != null && map != null) {
            if (isSource) {
                if (markerSource == null) {
                    markerSource = map.addMarker(new MarkerOptions()
                            .position(
                                    new LatLng(latLng.latitude,
                                            latLng.longitude))
                            .title(getResources().getString(
                                    R.string.text_source_pin_title))
                            .icon(BitmapDescriptorFactory
                                    .fromResource(R.drawable.pin_client_org)));
                    // markerSource.setDraggable(true);
                } else {
                    markerSource.setPosition(latLng);
                }
                CameraUpdateFactory.newLatLng(latLng);

            } else {
                if (markerDestination == null) {
                    MarkerOptions opt = new MarkerOptions();
                    opt.position(latLng);
                    opt.title(getResources().getString(
                            R.string.text_destination_pin_title));
                    opt.icon(BitmapDescriptorFactory
                            .fromResource(R.drawable.destination_pin));
                    markerDestination = map.addMarker(opt);

                    markerDestination.setDraggable(true);

                    if (markerSource != null) {
                        LatLngBounds.Builder bld = new LatLngBounds.Builder();

                        bld.include(new LatLng(
                                markerSource.getPosition().latitude,
                                markerSource.getPosition().longitude));
                        bld.include(new LatLng(
                                markerDestination.getPosition().latitude,
                                markerDestination.getPosition().longitude));
                        LatLngBounds latLngBounds = bld.build();
                        map.moveCamera(CameraUpdateFactory.newLatLngBounds(
                                latLngBounds, 30));
                    } else {
                        CameraUpdateFactory.newLatLng(latLng);
                    }

                } else {
                    markerDestination.setPosition(latLng);
                }
            }

            getAddressFromLocation(markerSource.getPosition(), etSource);
        } else {
            Toast.makeText(getActivity(), getString(R.string.error_unable_location),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void setMarkerOnRoad(LatLng source, LatLng destination) {
        String msg = null;
        if (source == null) {
            msg = getString(R.string.error_unable_source_location);
        } else if (destination == null) {
            msg = getString(R.string.error_unable_destination_location);
        }
        if (msg != null) {
            Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
            return;
        }
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL,
                "http://maps.googleapis.com/maps/api/directions/json?origin="
                        + source.latitude + "," + source.longitude
                        + "&destination=" + destination.latitude + ","
                        + destination.longitude + "&sensor=false");

        new HttpRequester(activity, map, Const.ServiceCode.DRAW_PATH_ROAD,
                true, this);
        // requestQueue.add(new VolleyHttpRequest(Method.GET, map,
        // Const.ServiceCode.DRAW_PATH_ROAD, this, this));
    }

    private void showDirection(LatLng source, LatLng destination) {
        if (source == null || destination == null) {
            return;
        }

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL,
                "http://maps.googleapis.com/maps/api/directions/json?origin="
                        + source.latitude + "," + source.longitude
                        + "&destination=" + destination.latitude + ","
                        + destination.longitude + "&sensor=false");
        new HttpRequester(activity, map, Const.ServiceCode.DRAW_PATH, true,
                this);
        // requestQueue.add(new VolleyHttpRequest(Method.GET, map,
        // Const.ServiceCode.DRAW_PATH, this, this));
    }

    private void setProvirderOnMap() {
        VehicalType vehicle = null;

        if (listType != null && listType.size() > selectedPostion) {
            vehicle = listType.get(selectedPostion);
        }
        if (vehicle == null) {
            return;
        }
        if (map != null) {
            Log.d("clear map", "setProvirderOnMap");
            map.clear();
        }

        nearDriverMarker = new HashMap<Integer, Marker>();
        for (int i = 0; i < listDriver.size(); i++) {
            Driver driver = listDriver.get(i);
            if (vehicle.getId() == driver.getVehicleTypeId()) {
                MarkerOptions mo = new MarkerOptions();
                mo.flat(true);
                mo.anchor(0.5f, 0.5f);
                mo.icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.pin_driver));
                mo.title(getString(R.string.text_drive_location));
                mo.position(new LatLng(driver.getLatitude(), driver
                        .getLongitude()));

                nearDriverMarker.put(driver.getDriverId(), map.addMarker(mo));
            }
        }

        boolean isGetProvider = false;
        for (int i = 0; i < listDriver.size(); i++) {
            Driver driver = listDriver.get(i);
            if (vehicle.getId() == driver.getVehicleTypeId()) {
                isGetProvider = true;
                getDuration(curretLatLng, String.valueOf(driver.getLatitude()),
                        String.valueOf(driver.getLongitude()));
                break;
            }
        }

        startUpdateProvidersLocation();
    }

    private void updateProviderOnMap() {
        try {
            VehicalType vehicle = listType.get(selectedPostion);
            for (int i = 0; i < listDriver.size(); i++) {
                Driver driver = listDriver.get(i);
                if (vehicle.getId() == driver.getVehicleTypeId()) {
                    for (int j = 0; j < listUpdatedDriver.size(); j++) {
                        Driver updatedDriver = listUpdatedDriver.get(j);
                        if (driver.getDriverId() == updatedDriver.getDriverId()) {
                            Location driverLocation = new Location("");
                            driverLocation.setLatitude(updatedDriver
                                    .getLatitude());
                            driverLocation.setLongitude(updatedDriver
                                    .getLongitude());
                            driverLocation.setBearing((float) updatedDriver
                                    .getBearing());
                            animateMarker(nearDriverMarker.get(i),
                                    new LatLng(updatedDriver.getLatitude(),
                                            updatedDriver.getLongitude()),
                                    driverLocation, false);
                            break;
                        }
                    }
                }
            }

            boolean isGetProvider = false;
            for (int i = 0; i < listUpdatedDriver.size(); i++) {
                Driver driver = listUpdatedDriver.get(i);
                if (vehicle.getId() == driver.getVehicleTypeId()) {
                    isGetProvider = true;
                    getDuration(curretLatLng,
                            String.valueOf(driver.getLatitude()),
                            String.valueOf(driver.getLongitude()));
                    break;
                }
            }
            listDriver.clear();
            listDriver.addAll(listUpdatedDriver);

        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showVehicleDetails() {
        if (listType != null && listType.size() > 0) {
            VehicalType vehicle = (VehicalType) listType.get(selectedPostion);
            // AppLog.Log("", "MAX:" + vehicle.getMaxSize());
            dialog = new Dialog(getActivity());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.vehicle_details);
            dialog.getWindow().setLayout(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT);

            tvMaxSize = (TextView) dialog.findViewById(R.id.tvMaxSize);
            tvMinFare = (TextView) dialog.findViewById(R.id.tvMinFare);
            tvETA = (TextView) dialog.findViewById(R.id.tvETA);
            pbar = (ProgressBar) dialog.findViewById(R.id.pbar);
            tvGetFareEst = (TextView) dialog.findViewById(R.id.tvGetFareEst);
            pbMinFare = (ProgressBar) dialog.findViewById(R.id.pbMinFare);
            tvTotalFare = (TextView) dialog.findViewById(R.id.tvTotalFare);
            cancelVehicleDetail = (ImageView) dialog
                    .findViewById(R.id.cancelVehicleDetail);

            tvRateVehicleTypeName = (TextView) dialog
                    .findViewById(R.id.tvRateVehicleTypeName);
            tvRateBasePrice = (TextView) dialog
                    .findViewById(R.id.tvRateBasePrice);
            tvRateDistanceCost = (TextView) dialog
                    .findViewById(R.id.tvRateDistanceCost);
            tvRateTimeCost = (TextView) dialog
                    .findViewById(R.id.tvRateTimeCost);

            tvGetFareEst.setOnClickListener(this);
            tvETA.setText(estimatedTimeTxt);
            tvETA.setVisibility(View.VISIBLE);
            pbar.setVisibility(View.GONE);
            tvMaxSize.setText(vehicle.getMaxSize() + " "+getString(R.string.text_person));
            tvMinFare.setText(activity.getString(R.string.payment_unit)
                    + vehicle.getMinFare());

            tvRateVehicleTypeName.setText(vehicle.getName());

            tvRateBasePrice.setText(getString(R.string.payment_unit)
                    + vehicle.getBasePrice() + " "
                    + getString(R.string.text_for) + " "
                    + vehicle.getBaseDistance() + vehicle.getUnit());
            tvRateDistanceCost.setText(getString(R.string.payment_unit)
                    + vehicle.getPricePerUnitDistance()
                    + getString(R.string.text_per) + vehicle.getUnit());
            tvRateTimeCost.setText(getString(R.string.payment_unit)
                    + vehicle.getPricePerUnitTime()
                    + getString(R.string.text_per)
                    + getString(R.string.text_min));

            cancelVehicleDetail.setOnClickListener(this);
            dialog.show();
        }
    }

    private void showDestinationPopup() {
        destinationDialog = new Dialog(getActivity());
        destinationDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        destinationDialog.setContentView(R.layout.destination_popup);
        etPopupDestination = (AutoCompleteTextView) destinationDialog
                .findViewById(R.id.etPopupDestination);
        etHomeAddress = (AutoCompleteTextView) destinationDialog
                .findViewById(R.id.etHomeAddress);
        etWorkAddress = (AutoCompleteTextView) destinationDialog
                .findViewById(R.id.etWorkAddress);
        tvHomeAddress = (TextView) destinationDialog
                .findViewById(R.id.tvHomeAddress);
        tvWorkAddress = (TextView) destinationDialog
                .findViewById(R.id.tvWorkAddress);
        tvHomeAddress.setText(preference.getHomeAddress());
        tvWorkAddress.setText(preference.getWorkAddress());
        etHomeAddress.setText(preference.getHomeAddress());
        etWorkAddress.setText(preference.getWorkAddress());

        layoutHomeText = (LinearLayout) destinationDialog
                .findViewById(R.id.layoutHomeText);
        layoutHomeEdit = (LinearLayout) destinationDialog
                .findViewById(R.id.layoutHomeEdit);
        layoutWorkText = (LinearLayout) destinationDialog
                .findViewById(R.id.layoutWorkText);
        layoutWorkEdit = (LinearLayout) destinationDialog
                .findViewById(R.id.layoutWorkEdit);
        layoutHomeText.setOnClickListener(this);
        layoutWorkText.setOnClickListener(this);
        destinationDialog.findViewById(R.id.imgClearDest).setOnClickListener(
                this);
        destinationDialog.findViewById(R.id.imgClearHome).setOnClickListener(
                this);
        destinationDialog.findViewById(R.id.imgClearWork).setOnClickListener(
                this);

        destinationDialog.findViewById(R.id.btnEditHome).setOnClickListener(
                this);
        destinationDialog.findViewById(R.id.btnEditWork).setOnClickListener(
                this);
        nearByList = (ListView) destinationDialog.findViewById(R.id.nearByList);
        pbNearby = (ProgressBar) destinationDialog.findViewById(R.id.pbNearby);

        adapterPopUpDestination = new PlacesAutoCompleteAdapter(activity,
                R.layout.autocomplete_list_text);
        etPopupDestination.setAdapter(adapterPopUpDestination);
        adapterHomeAddress = new PlacesAutoCompleteAdapter(activity,
                R.layout.autocomplete_list_text);
        etHomeAddress.setAdapter(adapterHomeAddress);
        adapterWorkAddress = new PlacesAutoCompleteAdapter(activity,
                R.layout.autocomplete_list_text);
        etWorkAddress.setAdapter(adapterWorkAddress);
        destinationDialog.getWindow().setLayout(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        etPopupDestination.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                sendQuoteRequest(adapterPopUpDestination.getItem(arg2));
            }
        });
        etHomeAddress.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                String selectedPlace = adapterHomeAddress.getItem(arg2);
                preference.putHomeAddress(selectedPlace);
                tvHomeAddress.setText(selectedPlace);
                layoutHomeEdit.setVisibility(LinearLayout.GONE);
                layoutHomeText.setVisibility(LinearLayout.VISIBLE);
            }
        });
        etWorkAddress.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                String selectedPlace = adapterWorkAddress.getItem(arg2);
                preference.putWorkAddress(selectedPlace);
                tvWorkAddress.setText(selectedPlace);
                layoutWorkEdit.setVisibility(LinearLayout.GONE);
                layoutWorkText.setVisibility(LinearLayout.VISIBLE);
            }
        });
        nearByLocations();
        nearByList.setOnItemClickListener(this);
        destinationDialog.show();
    }

    private void sendQuoteRequest(String destination) {
        pbMinFare.setVisibility(View.VISIBLE);
        tvMinFare.setVisibility(View.GONE);
        tvTotalFare.setVisibility(View.GONE);
        tvGetFareEst.setText(destination);
        getFareQuote(curretLatLng, destination);
        destinationDialog.dismiss();
    }

    private void nearByLocations() {
        StringBuilder sb = new StringBuilder(Const.PLACES_API_BASE
                + Const.TYPE_NEAR_BY + Const.OUT_JSON);
        sb.append("?sensor=true&key=" + Const.PLACES_AUTOCOMPLETE_API_KEY);
        sb.append("&location=" + curretLatLng.latitude + ","
                + curretLatLng.longitude);
        sb.append("&radius=500");
        // AppLog.Log("", "Near location Url : " + sb.toString());
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL, sb.toString());
        new HttpRequester(activity, map, Const.ServiceCode.GET_NEAR_BY, true,
                this);
        // requestQueue.add(new VolleyHttpRequest(Method.GET, map,
        // Const.ServiceCode.GET_NEAR_BY, this, this));
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        if (address != null) {
            sendQuoteRequest(nearByAd.getItem(arg2) + ", "
                    + address.getLocality() + ", " + address.getAdminArea()
                    + ", " + address.getCountryName());
        }

    }

    private void startUpdateProvidersLocation() {
    /*    timerProvidersLocation = new Timer();
        timerProvidersLocation.scheduleAtFixedRate(new TrackLocation(), 0,
                LOCATION_SCHEDULE);
*/
        locationHandler.postDelayed(runnable, LOCATION_SCHEDULE);
    }

    private void stopUpdateProvidersLoaction() {

        if(locationHandler!=null)
            locationHandler.removeCallbacks(runnable);
        /*
        if (timerProvidersLocation != null) {
            timerProvidersLocation.cancel();
            timerProvidersLocation = null;
        }*/
    }

    /*
     * (non-Javadoc)
     *
     * @see com.automated.taxinow.utils.LocationHelper.OnLocationReceived#
     * onLocationReceived(com.google.android.gms.maps.model.LatLng)
     */
    @Override
    public void onLocationReceived(LatLng latlong) {

    }

    /*
     * (non-Javadoc) onLocationReceived(android.location.Location)
     */
    @Override
    public void onLocationReceived(Location location) {

        if (location != null) {
            // drawTrip(latlong);
            myLocation = location;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.automated.taxinow.utils.LocationHelper.OnLocationReceived#onConntected
     * (android.os.Bundle)
     */
    @Override
    public void onConntected(Bundle bundle) {

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.automated.taxinow.utils.LocationHelper.OnLocationReceived#onConntected
     * (android.location.Location)
     */
    @Override
    public void onConntected(Location location) {

        if (location != null) {
            myLocation = location;
            LatLng latLang = new LatLng(location.getLatitude(),
                    location.getLongitude());
            curretLatLng = latLang;
            getAllProviders(latLang);
            animateCameraToMarker(latLang, false);
        } else {
            activity.showLocationOffDialog();
        }
    }

    private String getFareCalculation(double distanceInKm) {
        VehicalType vehicle = (VehicalType) listType.get(selectedPostion);
        double basePrice = vehicle.getBasePrice();
        int baseDistance = vehicle.getBaseDistance();
        double distanceCost = vehicle.getPricePerUnitDistance();
        double fare = (distanceInKm - baseDistance) * distanceCost + basePrice;
        DecimalFormat format = new DecimalFormat("0.00");
        String finalFare = format.format(fare);

        return finalFare;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
                               long id) {
        selectedPostion = position;
        tvVehicleType.setText(listType.get(position).getName());
        if (listDriver.size() > 0) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setProvirderOnMap();
                }
            });
        } else {
            // pBar.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {

    }
    private Runnable timerRequestStatusRunnable = new Runnable() {
        @Override
        public void run() {
      /* do what you need to do */
            if (isContinueRequest) {
                isContinueRequest = false;
                getRequestStatus(String
                        .valueOf(activity.pHelper.getRequestId()));
            }
      /* and here comes the "trick" */
            statusHandler.postDelayed(this, LOCATION_SCHEDULE);
        }
    };
    private class TimerRequestStatus extends TimerTask {
        @Override
        public void run() {
            if (isContinueRequest) {
                isContinueRequest = false;
                getRequestStatus(String
                        .valueOf(activity.pHelper.getRequestId()));
            }
        }
    }

    class WalkerStatusReceiver extends BroadcastReceiver implements
            OnProgressCancelListener {

        @Override
        public void onReceive(Context context, Intent intent) {
            String response = intent.getStringExtra(Const.EXTRA_WALKER_STATUS);
            if (TextUtils.isEmpty(response))
                return;
            stopCheckingStatusUpdate();

            if (activity.pContent.isSuccess(response)) {

                Driver driverInfo = activity.pContent.getDriverInfo(response);
                AndyUtils.removeDriverDetailDialog();
                AndyUtils.showDriverDetailDialog(activity, this, driverInfo);

                switch (activity.pContent.checkRequestStatus(response)) {
                    case Const.IS_WALK_STARTED:
                    case Const.IS_WALKER_ARRIVED:
                    case Const.IS_COMPLETED:
                    case Const.IS_WALKER_STARTED:
                        AndyUtils.removeDriverDetailDialog();
                        Driver driver = activity.pContent.getDriverDetail(response);
                        if (MapFragment.this.isVisible())
                            activity.gotoTripFragment(driver);
                        removeThisFragment();
                        view.findViewById(R.id.markerBubblePickMeUp).setVisibility(
                                View.GONE);
                        AndyUtils.removeCustomProgressRequestDialog();
                        break;
                    case Const.IS_WALKER_RATED:
                        stopCheckingStatusUpdate();
                        if (MapFragment.this.isVisible())
                            activity.gotoRateFragment(activity.pContent
                                    .getDriverDetail(response));
                        AndyUtils.removeDriverDetailDialog();
                        break;

                    case Const.IS_REQEUST_CREATED:
                        view.findViewById(R.id.markerBubblePickMeUp).setVisibility(
                                View.GONE);
                        Driver driverData = activity.pContent
                                .getDriverDetail(response);
                        AndyUtils.showCustomProgressDialog(activity,
                                getString(R.string.text_contacting), false,
                                MapFragment.this, driverData);
                        startCheckingStatusUpdate();
                        isContinueRequest = true;
                        break;
                    default:
                        isContinueRequest = false;
                        break;
                }

            } else if (activity.pContent.getErrorCode(response) == Const.REQUEST_ID_NOT_FOUND) {
                AndyUtils.removeDriverDetailDialog();
                activity.pHelper.clearRequestData();
                isContinueRequest = false;
            } else if (activity.pContent.getErrorCode(response) == Const.INVALID_TOKEN) {
                if (activity.pHelper.getLoginBy()
                        .equalsIgnoreCase(Const.MANUAL))

                    login();
                else
                    loginSocial(activity.pHelper.getUserId(),
                            activity.pHelper.getLoginBy());
            } else {
                isContinueRequest = true;
                startCheckingStatusUpdate();
            }
        }

        @Override
        public void onProgressCancel() {
            stopCheckingStatusUpdate();
            cancleRequest();
        }
    }

    class TrackLocation extends TimerTask {
        public void run() {
            updateAllProviders(curretLatLng);
        }
    }

    public static final int getColor(Context context, int id) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            return ContextCompat.getColor(context, id);
        } else {
            return context.getResources().getColor(id);
        }
    }


}