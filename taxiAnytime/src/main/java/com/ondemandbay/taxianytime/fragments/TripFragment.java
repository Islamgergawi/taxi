package com.ondemandbay.taxianytime.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.ImageOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.ondemandbay.taxianytime.R;
import com.ondemandbay.taxianytime.adapter.PlacesAutoCompleteAdapter;
import com.ondemandbay.taxianytime.component.MyFontEdittextView;
import com.ondemandbay.taxianytime.component.MyFontTextView;
import com.ondemandbay.taxianytime.component.MyFontTextViewMedium;
import com.ondemandbay.taxianytime.models.Driver;
import com.ondemandbay.taxianytime.models.DriverLocation;
import com.ondemandbay.taxianytime.models.Route;
import com.ondemandbay.taxianytime.models.Step;
import com.ondemandbay.taxianytime.parse.HttpRequester;
import com.ondemandbay.taxianytime.parse.ParseContent;
import com.ondemandbay.taxianytime.utils.AndyUtils;
import com.ondemandbay.taxianytime.utils.AppLog;
import com.ondemandbay.taxianytime.utils.Const;
import com.ondemandbay.taxianytime.utils.LocationHelper;
import com.ondemandbay.taxianytime.utils.LocationHelper.OnLocationReceived;
import com.ondemandbay.taxianytime.utils.MathUtils;
import com.ondemandbay.taxianytime.utils.PreferenceHelper;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * @author Elluminati elluminati.in
 */
public class TripFragment extends BaseFragment implements OnLocationReceived {
    private final int LOCATION_SCHEDULE = 5 * 1000;
    ArrayList<LatLng> points;
    WakeLock wakeLock;
    private GoogleMap map;
    private PolylineOptions lineOptions;
    private Route route;
    private TextView /* tvTime , tvDist , */tvDriverName, tvStatus;
    private Driver driver;
    private Marker myMarker, markerDriver;
    private ImageView ivDriverPhoto;
    private LocationHelper locHelper;
    private boolean isContinueStatusRequest;
    private boolean isContinueDriverRequest;
    private Timer timer, timerDriverLocation;
    private String reason;
    private Polyline polyLine;
    private LatLng myLatLng;
    private Location myLocation;
    private boolean isTripStarted = false;
    // private final int DRAW_TIME = 5 * 1000;
    private String lastTime;
    private String lastDistance;
    private WalkerStatusReceiver walkerReceiver;
    private boolean isAllLocationReceived = false;
    private RadioButton r1, r2, r3;
    private PopupWindow notificationWindow, driverStatusWindow;
    private MyFontTextView tvJobAccepted, tvDriverStarted, tvDriverArrvied,
            tvTripStarted, reason1, reason2, reason3; // tvTripCompleted,
    // tvPopupMsg,;
    private ImageView ivJobAccepted, ivDriverStarted, ivDriverArrvied,
            ivTripStarted, imgSelectedCash, imgSelectedCard; // ivTripCompleted,
    // private boolean isNotificationArrievd = false;
    private MyFontTextView tvTaxiModel;
    private MyFontTextView tvTaxiNo;
    private MyFontTextViewMedium tvRateStar;
    private TextView tvEstimatedTime;
    // private View markerLayout;
    private TextView tvDurationUnit;
    // private ProgressBar pBar;
    private FrameLayout layoutCash, layoutCard;
    // private TextView tvCash;
    // private TextView tvCardNo;
    private PreferenceHelper preference;
    // private ImageView ivCard;
    private BroadcastReceiver mReceiver;
    private MapView mMapView;
    private Bundle mBundle;
    private View view;
    private ImageButton btnCancelTrip;
    private AutoCompleteTextView etDestination;
    private PlacesAutoCompleteAdapter adapterDestination;
    private Marker markerDestination;
    private Route routeDest;
    private ArrayList<LatLng> pointsDest;
    private PolylineOptions lineOptionsDest;
    private Polyline polyLineDest;
    private RelativeLayout layout;
    private MyFontEdittextView ibApplyPromo;
    // private Dialog dialog;
    private GoogleApiClient client;
    // private EditText etPromoCode;
    // private ImageView ivPromoResult;
    // private TextView tvPromoResult;
    // private LinearLayout llErrorMsg;
    // private Button btnPromoSubmit, btnPromoSkip;
    private Dialog notificationDialog;
    private RelativeLayout rlPopupWindow;
    private ImageView lvNotificationLine;
    private Boolean isCameraZoom = false;

    // private double distance;
    // private LatLng destLatlng;

    // private RatingBar ratingBarTrip;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBundle = savedInstanceState;
        PowerManager powerManager = (PowerManager) activity
                .getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                Const.TAG);
        wakeLock.acquire();
        driver = (Driver) getArguments().getParcelable(Const.DRIVER);
        points = new ArrayList<LatLng>();
        route = new Route();
        IntentFilter filter = new IntentFilter(Const.INTENT_WALKER_STATUS);
        walkerReceiver = new WalkerStatusReceiver();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                walkerReceiver, filter);
        isAllLocationReceived = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity.setTitle(getString(R.string.app_name));
        activity.tvTitle.setVisibility(View.GONE);
        activity.layoutDestination.setVisibility(View.VISIBLE);
        activity.setIcon(R.drawable.notification_box);
        view = inflater.inflate(R.layout.fragment_trip_new, container, false);
        try {
            MapsInitializer.initialize(getActivity());
        } catch (Exception e) {
        }
        view.findViewById(R.id.btnCall).setOnClickListener(this);
        // view.findViewById(R.id.btnAddDestination).setOnClickListener(this);
        // view.findViewById(R.id.imgClearDst).setOnClickListener(this);
        // etDestination = (AutoCompleteTextView) view
        // .findViewById(R.id.etEnterSouce);
        // layoutDestination = (LinearLayout) view
        // .findViewById(R.id.layoutDestination);
        etDestination = activity.etEnterSource;
        preference = new PreferenceHelper(activity);
        // tvTime = (TextView) view.findViewById(R.id.tvJobTime);
        // tvDist = (TextView) view.findViewById(R.id.tvJobDistance);
        tvDriverName = (TextView) view.findViewById(R.id.tvDriverName);
        tvTaxiModel = (MyFontTextView) view.findViewById(R.id.tvTaxiModel);
        tvTaxiNo = (MyFontTextView) view.findViewById(R.id.tvTaxiNo);
        tvRateStar = (MyFontTextViewMedium) view.findViewById(R.id.tvRateStar);
        layoutCash = (FrameLayout) view.findViewById(R.id.layoutCash);
        layoutCard = (FrameLayout) view.findViewById(R.id.layoutCard);
        // tvCash = (TextView) view.findViewById(R.id.tvCash);
        // tvCardNo = (TextView) view.findViewById(R.id.tvCardNo);
        // ivCard = (ImageView) view.findViewById(R.id.ivCard);
        ibApplyPromo = (MyFontEdittextView) view
                .findViewById(R.id.ibApplyPromo);
        imgSelectedCash = (ImageView) view.findViewById(R.id.imgSelectedCash);
        imgSelectedCard = (ImageView) view.findViewById(R.id.imgSelectedCard);
        btnCancelTrip = (ImageButton) view.findViewById(R.id.btnCancelTrip);
        btnCancelTrip.setOnClickListener(this);
        layoutCash.setOnClickListener(this);
        layoutCard.setOnClickListener(this);
        ibApplyPromo.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN)
                        && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    applyPromoCode();
                }
                return false;
            }
        });

        tvEstimatedTime = (TextView) view.findViewById(R.id.tvEstimatedTime);
        tvDurationUnit = (TextView) view.findViewById(R.id.tvDurationUnit);
        // pBar = (ProgressBar) view.findViewById(R.id.pBar);

        // ibApplyPromo.setOnClickListener(this);
        // tvDriverPhone = (MyFontTextView)
        // view.findViewById(R.id.tvDriverPhone);
        ivDriverPhoto = (ImageView) view.findViewById(R.id.ivDriverPhoto);
        // tvRate = (TextView) view.findViewById(R.id.tvRate);
        // tvRate.setText(new DecimalFormat("0.0").format(driver.getRating()));
        // ratingBarTrip = (RatingBar) view.findViewById(R.id.ratingBarTrip);
        // ratingBarTrip.setRating((float) driver.getRating());

        // tvDriverPhone.setText(driver.getPhone());
        tvDriverName
                .setText(driver.getFirstName() + " " + driver.getLastName());
        tvTaxiModel.setText(driver.getCarModel());
        tvTaxiNo.setText(driver.getCarNumber());
        tvRateStar.setText(MathUtils.getRound((float) driver.getRating()) + "");

        tvStatus = (TextView) view.findViewById(R.id.tvStatus);
        mMapView = (MapView) view.findViewById(R.id.maptrip);
        mMapView.onCreate(mBundle);
        setUpMap();
        // setDefaultCardDetails();
        if (preference.getPaymentMode() == Const.CASH) {
            imgSelectedCash.setVisibility(View.VISIBLE);
            imgSelectedCard.setVisibility(View.GONE);
            // layoutCash.setSelected(true);
            // layoutCard.setSelected(false);
            // tvCash.setTextColor(getResources().getColor(R.color.white));
            // imgSelectedCash.setVisibility(View.VISIBLE);
        } else {
            imgSelectedCash.setVisibility(View.GONE);
            imgSelectedCard.setVisibility(View.VISIBLE);
            // layoutCash.setSelected(false);
            // layoutCard.setSelected(true);
            // tvCardNo.setTextColor(getResources().getColor(R.color.white));
            // imgSelectedCard.setVisibility(View.VISIBLE);
        }
        if (preference.getIsTripStarted()) {
            btnCancelTrip.setVisibility(View.GONE);
        }
        rlPopupWindow = (RelativeLayout) view.findViewById(R.id.rlPopupWindow);
        lvNotificationLine = (ImageView) view
                .findViewById(R.id.lvNotificationLine);

        if (!TextUtils.isEmpty(preference.getPromoCode())) {
            ibApplyPromo.setText(preference.getPromoCode());
            ibApplyPromo.setEnabled(false);
        }
        if (preference.getClientDestination() != null) {
            getAddressFromLocation(preference.getClientDestination());
        }
        return view;

    }

    @SuppressWarnings("deprecation")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AppLog.Log("TripFragment", "Driver Photo : " + driver.getPicture());
        ImageOptions imageOptions = new ImageOptions();
        imageOptions.fileCache = true;
        imageOptions.memCache = true;
        imageOptions.fallback = R.drawable.default_user;
        new AQuery(activity).id(ivDriverPhoto).progress(R.id.pBar)
                .image(driver.getPicture(), imageOptions);
        locHelper = new LocationHelper(activity);
        locHelper.setLocationReceivedLister(this);
        adapterDestination = new PlacesAutoCompleteAdapter(activity,
                R.layout.autocomplete_list_text);
        etDestination.setText("");
        etDestination.setAdapter(adapterDestination);
        etDestination.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                final String selectedDestPlace = adapterDestination
                        .getItem(arg2);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        preference
                                .putClientDestination(getLocationFromAddress(selectedDestPlace));
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // setMarkerOnRoad(destLatlng, destLatlng);

                                setDestination(
                                        preference.getClientDestination(),
                                        selectedDestPlace);
                                // setDestinationMarker(destLatlng);
                                // if (myMarker != null
                                // && markerDestination != null) {
                                // drawPath(c.getPosition(), destLatlng);
                                // }
                            }
                        });
                    }
                }).start();
            }
        });

        locHelper.onStart();
        // PopUp Window
        LayoutInflater inflate = LayoutInflater.from(activity);
        layout = (RelativeLayout) inflate.inflate(
                R.layout.popup_notification_window, null);
        // tvPopupMsg = (MyFontTextView) layout.findViewById(R.id.tvPopupMsg);
        notificationWindow = new PopupWindow(layout, LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        layout.setOnClickListener(this);
        activity.btnNotification.setOnClickListener(this);

        // Big PopUp Window
        RelativeLayout bigPopupLayout = (RelativeLayout) inflate.inflate(
                R.layout.popup_notification_status_window, null);
        tvJobAccepted = (MyFontTextView) view.findViewById(R.id.tvJobAccepted);
        tvDriverStarted = (MyFontTextView) view
                .findViewById(R.id.tvDriverStarted);
        tvDriverArrvied = (MyFontTextView) view
                .findViewById(R.id.tvDriverArrvied);
        tvTripStarted = (MyFontTextView) view.findViewById(R.id.tvTripStarted);
        // tvTripCompleted = (MyFontTextView) view
        // .findViewById(R.id.tvTripCompleted);
        ivJobAccepted = (ImageView) view.findViewById(R.id.ivJobAccepted);
        ivDriverStarted = (ImageView) view.findViewById(R.id.ivDriverStarted);
        ivDriverArrvied = (ImageView) view.findViewById(R.id.ivDriverArrvied);
        ivTripStarted = (ImageView) view.findViewById(R.id.ivTripStarted);
        // ivTripCompleted = (ImageView)
        // view.findViewById(R.id.ivTripCompleted);
        driverStatusWindow = new PopupWindow(bigPopupLayout,
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        driverStatusWindow.setBackgroundDrawable(new BitmapDrawable());
        // driverStatusWindow.setFocusable(false);
        // driverStatusWindow.setTouchable(true);
        driverStatusWindow.setOutsideTouchable(true);
        // showNotificationPopUp(getString(R.string.text_job_accepted));
        // showNotificationDialog(getString(R.string.text_job_accepted));
    }

    private void getAddressFromLocation(final LatLng latlng) {
        etDestination.setText("Waiting for Address");
        etDestination.setTextColor(Color.GRAY);
        new Thread(new Runnable() {
            private Address address;
            private String strAddress;

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
                                etDestination.setFocusable(false);
                                etDestination.setFocusableInTouchMode(false);
                                etDestination.setText(strAddress);
                                etDestination.setTextColor(getResources()
                                        .getColor(android.R.color.black));
                                etDestination.setFocusable(true);
                                etDestination.setFocusableInTouchMode(true);
                            } else {
                                etDestination.setText("");
                                etDestination.setTextColor(getResources()
                                        .getColor(android.R.color.black));
                            }
                            etDestination.setEnabled(true);
                        }
                    });
                } catch (Exception exc) {
                    exc.printStackTrace();
                }
            }
        }).start();
    }

    private LatLng getLocationFromAddress(final String place) {
        AppLog.Log("Address", "" + place);
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

    // private void setMarkerOnRoad(LatLng source, LatLng destination) {
    // String msg = null;
    // if (source == null) {
    // msg = "Unable to get source location, please try again";
    // } else if (destination == null) {
    // msg = "Unable to get destination location, please try again";
    // }
    // if (msg != null) {
    // Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    // return;
    // }
    // HashMap<String, String> map = new HashMap<String, String>();
    // map.put(Const.URL,
    // "http://maps.googleapis.com/maps/api/directions/json?origin="
    // + source.latitude + "," + source.longitude
    // + "&destination=" + destination.latitude + ","
    // + destination.longitude + "&sensor=false");
    //
    // new HttpRequester(activity, map, Const.ServiceCode.DRAW_PATH_ROAD,
    // true, this);
    // }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCall:
                if (driver != null) {
                    String number = driver.getPhone();
                    if (!TextUtils.isEmpty(number)) {
                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                        callIntent.setData(Uri.parse("tel:" + number));
                        startActivity(callIntent);
                    }
                }
                break;
            // case R.id.rlPopupWindow:
            // notificationWindow.dismiss();
            // activity.setIcon(R.drawable.notification_box);
            // break;
            case R.id.btnActionNotification:
                if (rlPopupWindow.getVisibility() == View.VISIBLE) {
                    lvNotificationLine.setVisibility(View.GONE);
                    rlPopupWindow.setVisibility(View.GONE);
                    activity.setIcon(R.drawable.notification_box);
                } else {
                    lvNotificationLine.setVisibility(View.VISIBLE);
                    rlPopupWindow.setVisibility(View.VISIBLE);
                    activity.setIcon(R.drawable.notification_box_arrived);
                }
                // showDriverStatusNotification();
                break;
            case R.id.layoutCash:
                setPaymentMode(Const.CASH);
                break;
            case R.id.layoutCard:
                setPaymentMode(Const.CREDIT);
                break;
            case R.id.btnCancelTrip:
                showCancellationtDialog();
                // cancleRequest();
                break;
            // case R.id.btnAddDestination:
            // layoutDestination.setVisibility(View.VISIBLE);
            // layout.setVisibility(View.GONE);
            // break;
            case R.id.imgClearDst:
                etDestination.setText("");
                break;
            // case R.id.ibApplyPromo:
            // // showPromoDialog();
            // break;
            // case R.id.btnPromoSkip:
            // if (dialog != null)
            // dialog.dismiss();
            // break;
            // case R.id.btnPromoSubmit:
            // applyPromoCode();
            // break;

            case R.id.cancelPopup:
                activity.setIcon(R.drawable.notification_box);
                notificationDialog.dismiss();
                break;
            default:
                // if(driverStatusWindow.isShowing())
                // driverStatusWindow.dismiss();
                break;
        }
    }

    private void applyPromoCode() {
        if (!AndyUtils.isNetworkAvailable(activity)) {
            AndyUtils.showToast(getResources().getString(R.string.no_internet),
                    activity);
            return;
        }
        AndyUtils.showCustomProgressRequestDialog(activity,
                getString(R.string.text_apply_promo), true, null);
        HashMap<String, String> map = new HashMap<String, String>();

        map.put(Const.URL, Const.ServiceType.APPLY_PROMO);
        map.put(Const.Params.TOKEN, activity.pHelper.getSessionToken());
        map.put(Const.Params.ID, activity.pHelper.getUserId());
        map.put(Const.Params.PROMO_CODE, ibApplyPromo.getText().toString()
                .trim());
        new HttpRequester(activity, map, Const.ServiceCode.APPLY_PROMO, this);
        // requestQueue.add(new VolleyHttpRequest(Method.POST, map,
        // Const.ServiceCode.APPLY_PROMO, this, this));
    }

    public void showDriverStatusNotification() {
        activity.setIcon(R.drawable.notification_box);
        if (driverStatusWindow.isShowing())
            driverStatusWindow.dismiss();
        else {
            if (notificationWindow.isShowing())
                notificationWindow.dismiss();
            else
                driverStatusWindow.showAsDropDown(activity.btnNotification);
        }
    }

    // public void showNotificationPopUp(String text) {
    // tvPopupMsg.setText(text);
    // if (!driverStatusWindow.isShowing()) {
    // if (!notificationWindow.isShowing()) {
    // activity.setIcon(R.drawable.notification_box_arrived);
    // notificationWindow.showAsDropDown(activity.btnNotification);
    // }
    // }
    // }
    private void showCancellationtDialog() {
        final Dialog cancellationDialog = new Dialog(activity);
        cancellationDialog.setTitle("Tell us why");
        cancellationDialog.setContentView(R.layout.dilog_cancel_future_request);
        Button ok = (Button) cancellationDialog.findViewById(R.id.btnOk);

        // r1 = (RadioButton) cancellationDialog.findViewById(R.id.r1);
        // r2 = (RadioButton) cancellationDialog.findViewById(R.id.r2);
        // r3 = (RadioButton) cancellationDialog.findViewById(R.id.r3);
        final List<RadioButton> radioButtons = new ArrayList<RadioButton>();

        radioButtons
                .add((RadioButton) cancellationDialog.findViewById(R.id.r1));
        radioButtons
                .add((RadioButton) cancellationDialog.findViewById(R.id.r2));
        radioButtons
                .add((RadioButton) cancellationDialog.findViewById(R.id.r3));
        for (RadioButton button : radioButtons) {

            button.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                                             boolean isChecked) {
                    if (isChecked) {
                        if (buttonView.getId() == R.id.r1) {
                            Log.d("", "r1 selected");
                            processRadioButtonClick(buttonView);
                            reason = activity.getResources().getString(
                                    R.string.reason1);
                        } else if (buttonView.getId() == R.id.r2) {
                            Log.d("", "r2 selected");
                            processRadioButtonClick(buttonView);
                            reason = activity.getResources().getString(
                                    R.string.reason2);
                        } else if (buttonView.getId() == R.id.r3) {
                            Log.d("", "r3 selected");
                            processRadioButtonClick(buttonView);
                            reason = activity.getResources().getString(
                                    R.string.reason3);
                        } else {
                            reason = "";
                        }
                    }
//					processRadioButtonClick(buttonView);
                }

                private void processRadioButtonClick(CompoundButton buttonView) {
                    for (RadioButton button : radioButtons) {

                        if (button != buttonView)
                            button.setChecked(false);
                    }

                }
            });

        }
        ok.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                cancellationDialog.dismiss();
                cancleRequest(reason);
            }
        });
        cancellationDialog.show();
    }

    private void showNotificationDialog(String text) {
        TextView tvPopupMsg;
        if (notificationDialog != null) {
            notificationDialog.dismiss();
        }
        notificationDialog = new Dialog(getActivity(), R.style.MyDialog);
        notificationDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        notificationDialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        notificationDialog.setContentView(R.layout.popup_notification_window);

        Window window = notificationDialog.getWindow();
        window.setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);

        notificationDialog.setCancelable(true);
        notificationDialog.setCanceledOnTouchOutside(true);
        tvPopupMsg = (TextView) notificationDialog
                .findViewById(R.id.tvPopupMsg);
        tvPopupMsg.setText(text);
        notificationDialog.findViewById(R.id.cancelPopup).setOnClickListener(
                this);
        activity.setIcon(R.drawable.notification_box_arrived);
        notificationDialog.show();

    }

    @Override
    public void onResume() {
        super.onResume();
        // if (activity.pHelper.getRequestTime() == Const.NO_TIME)
        // setRequestTime(SystemClock.e);
        mMapView.onResume();
        activity.btnNotification.setVisibility(View.VISIBLE);
        startUpdateDriverLocation();
        startCheckingStatusUpdate();
        registerCardReceiver();
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_UP
                        && keyCode == KeyEvent.KEYCODE_BACK) {
                    MapFragment mf = MapFragment.newInstance();
                    activity.setIconMenu(R.drawable.menu);
                    activity.addFragment(mf, false, Const.FRAGMENT_MAP);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onPause() {
        stopUpdateDriverLoaction();
        stopCheckingStatusUpdate();

        super.onPause();
        mMapView.onPause();
    }

    // private void setUpMap() {
    // // Do a null check to confirm that we have not already instantiated the
    // // map.
    // if (map == null) {
    // // map = ((SupportMapFragment) getActivity()
    // // .getSupportFragmentManager().findFragmentById(R.id.maptrip))
    // // .getMap();
    // map = ((MapView) view.findViewById(R.id.maptrip)).getMap();
    // // map.setOnMyLocationChangeListener(new
    // // OnMyLocationChangeListener() {
    // //
    // // @Override
    // // public void onMyLocationChange(Location arg0) {
    // // drawTrip(new LatLng(arg0.getLatitude(), arg0.getLongitude()));
    // // }
    // // });
    // // map.setInfoWindowAdapter(new InfoWindowAdapter() {
    // // @Override
    // // public View getInfoWindow(Marker marker) {
    // // View v = activity.getLayoutInflater().inflate(
    // // R.layout.info_window_layout, null);
    // // ((MyFontTextView) v).setText(marker.getTitle());
    // // return v;
    // // }
    //
    // // Defines the contents of the InfoWindow
    // // @Override
    // // public View getInfoContents(Marker marker) {
    // // return null;
    // // }
    // // });
    // //
    // // map.setOnMarkerClickListener(new OnMarkerClickListener() {
    // // @Override
    // // public boolean onMarkerClick(Marker marker) {
    // // marker.showInfoWindow();
    // // return true;
    // // }
    // // });
    // }
    // initPreviousDrawPath();
    //
    // }

    private void setUpMap() {
        // Do a null check to confirm that we have not already instantiated the
        // map.
        if (map == null) {
            // map = ((SupportMapFragment) getActivity()
            // .getSupportFragmentManager().findFragmentById(R.id.maptrip))
            // .getMap();
            map = ((MapView) view.findViewById(R.id.maptrip)).getMap();
            // map.setOnMyLocationChangeListener(new
            // OnMyLocationChangeListener() {
            //
            // @Override
            // public void onMyLocationChange(Location arg0) {
            // drawTrip(new LatLng(arg0.getLatitude(), arg0.getLongitude()));
            // }
            // });
            // map.setInfoWindowAdapter(new InfoWindowAdapter() {
            // @Override
            // public View getInfoWindow(Marker marker) {
            // View v = activity.getLayoutInflater().inflate(
            // R.layout.info_window_layout, null);
            // ((MyFontTextView) v).setText(marker.getTitle());
            // return v;
            // }
            //
            // // Defines the contents of the InfoWindow
            // @Override
            // public View getInfoContents(Marker marker) {
            // return null;
            // }
            // });

            map.setOnMarkerClickListener(new OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    marker.showInfoWindow();
                    return true;
                }
            });
        }
        initPreviousDrawPath();

//        client = new GoogleApiClient(activity, new GoogleApiClient.ConnectionCallbacks() {
//            @Override
//            public void onConnected(Bundle connectionHint) {
//                Location loc = LocationServices.FusedLocationApi.getLastLocation(client);
//                if (loc != null) {
//                    myLocation = loc;
//                    myLatLng = new LatLng(loc.getLatitude(), loc.getLongitude());
//                    setMarkers(myLatLng);
//                }
//            }
//
//            @Override
//            public void onConnectionSuspended(int i) {
//
//            }
//        }, new GoogleApiClient.OnConnectionFailedListener() {
//
//            @Override
//            public void onConnectionFailed(ConnectionResult result) {
//            }
//        });
        client = new GoogleApiClient() {
            @Override
            public boolean hasConnectedApi(Api<?> api) {
                return false;
            }

            @NonNull
            @Override
            public ConnectionResult getConnectionResult(Api<?> api) {
                return null;
            }

            @Override
            public void connect() {
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
//                Location loc = LocationServices.FusedLocationApi.getLastLocation(client);
//                if (loc != null) {
//                    myLocation = loc;
//                    myLatLng = new LatLng(loc.getLatitude(), loc.getLongitude());
//                    setMarkers(myLatLng);
//                }
            }


            @Override
            public ConnectionResult blockingConnect() {
                return null;
            }

            @Override
            public ConnectionResult blockingConnect(long l, TimeUnit timeUnit) {
                return null;
            }

            @Override
            public void disconnect() {

            }

            @Override
            public void reconnect() {

            }

            @Override
            public PendingResult<Status> clearDefaultAccountAndReconnect() {
                return null;
            }

            @Override
            public void stopAutoManage(FragmentActivity fragmentActivity) {

            }

            @Override
            public boolean isConnected() {
                return false;
            }

            @Override
            public boolean isConnecting() {
                return false;
            }

            @Override
            public void registerConnectionCallbacks(ConnectionCallbacks connectionCallbacks) {

            }

            @Override
            public boolean isConnectionCallbacksRegistered(ConnectionCallbacks connectionCallbacks) {
                return false;
            }

            @Override
            public void unregisterConnectionCallbacks(ConnectionCallbacks connectionCallbacks) {

            }

            @Override
            public void registerConnectionFailedListener(OnConnectionFailedListener onConnectionFailedListener) {

            }

            @Override
            public boolean isConnectionFailedListenerRegistered(OnConnectionFailedListener onConnectionFailedListener) {
                return false;
            }

            @Override
            public void unregisterConnectionFailedListener(OnConnectionFailedListener onConnectionFailedListener) {

            }

            @Override
            public void dump(String s, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strings) {

            }
        };
        client.connect();
    }

    private void setMarkers(LatLng latLang) {
        LatLng latLngDriver = new LatLng(driver.getLatitude(),
                driver.getLongitude());
        setMarker(latLngDriver);
        setDriverMarker(latLngDriver, driver.getBearing());
        animateCameraToMarkerWithZoom(latLngDriver);
        // boundLatLang();

        map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {

            @Override
            public void onMapLoaded() {
                boundLatLang();

            }
        });

        // showDirection(latLang, latLngDriver);
        // Location locDriver = new Location("");
        // locDriver.setLatitude(driver.getLatitude());
        // locDriver.setLongitude(driver.getLongitude());
        // strDistance = convertMilesFromMeters(loc
        // .distanceTo(locDriver));
        // animateCameraToMarker(latLang);
    }

    // private void showPromoDialog() {
    // dialog = new Dialog(getActivity());
    // dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    // dialog.setContentView(R.layout.dialog_promo);
    // ivPromoResult = (ImageView) dialog.findViewById(R.id.ivPromoResult);
    // tvPromoResult = (TextView) dialog.findViewById(R.id.tvPromoResult);
    // etPromoCode = (EditText) dialog.findViewById(R.id.etPromoCode);
    // llErrorMsg = (LinearLayout) dialog.findViewById(R.id.llErrorMsg);
    // btnPromoSubmit = (Button) dialog.findViewById(R.id.btnPromoSubmit);
    // btnPromoSubmit.setOnClickListener(this);
    // btnPromoSkip = (Button) dialog.findViewById(R.id.btnPromoSkip);
    // btnPromoSkip.setOnClickListener(this);
    // if (!TextUtils.isEmpty(activity.pHelper.getPromoCode())) {
    // etPromoCode.setText(activity.pHelper.getPromoCode());
    // btnPromoSkip.setText(getString(R.string.text_done));
    // btnPromoSubmit.setEnabled(false);
    // etPromoCode.setEnabled(false);
    // }
    // dialog.show();
    // }

    // private void showDirection(LatLng source, LatLng destination) {
    //
    // Map<String, String> hashMap = new HashMap<String, String>();
    //
    // final String url =
    // "http://maps.googleapis.com/maps/api/directions/json?origin="
    // + source.latitude
    // + ","
    // + source.longitude
    // + "&destination="
    // + destination.latitude
    // + ","
    // + destination.longitude
    // + "&sensor=false";
    // new HttpRequester(activity, hashMap, Const.ServiceCode.GET_ROUTE, true,
    // this);
    // AndyUtils.showCustomProgressDialog(activity,
    // getString(R.string.text_getting_direction), false, null);
    // }

    private void drawPath(LatLng source, LatLng destination) {
        if (source == null || destination == null) {
            return;
        }
        if (destination.latitude != 0) {
            setDestinationMarker(destination);
            boundLatLang();

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
    }

    private void boundLatLang() {

        try {
            if (myMarker != null && markerDriver != null
                    && markerDestination != null) {
                LatLngBounds.Builder bld = new LatLngBounds.Builder();
                bld.include(new LatLng(myMarker.getPosition().latitude,
                        myMarker.getPosition().longitude));
                bld.include(new LatLng(markerDriver.getPosition().latitude,
                        markerDriver.getPosition().longitude));
                bld.include(new LatLng(
                        markerDestination.getPosition().latitude,
                        markerDestination.getPosition().longitude));
                LatLngBounds latLngBounds = bld.build();

                map.animateCamera(CameraUpdateFactory.newLatLngBounds(
                        latLngBounds, 18));
            } else if (myMarker != null && markerDriver != null) {
                LatLngBounds.Builder bld = new LatLngBounds.Builder();
                bld.include(new LatLng(myMarker.getPosition().latitude,
                        myMarker.getPosition().longitude));
                bld.include(new LatLng(markerDriver.getPosition().latitude,
                        markerDriver.getPosition().longitude));
                LatLngBounds latLngBounds = bld.build();

                map.animateCamera(CameraUpdateFactory.newLatLngBounds(
                        latLngBounds, 18));
            }
        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    public void onDestroyView() {
        wakeLock.release();
        SupportMapFragment f = (SupportMapFragment) getFragmentManager()
                .findFragmentById(R.id.maptrip);
        if (f != null) {
            try {
                getFragmentManager().beginTransaction().remove(f).commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        map = null;
        activity.layoutDestination.setVisibility(View.GONE);
        super.onDestroyView();
    }

    @SuppressLint("NewApi")
    @Override
    public void onTaskCompleted(final String response, int serviceCode) {
        if (!this.isVisible())
            return;
        switch (serviceCode) {
            case Const.ServiceCode.GET_ROUTE:
                AndyUtils.removeCustomProgressDialog();
                if (!TextUtils.isEmpty(response)) {
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            new ParseContent(activity).parseRoute(response, route);

                            final ArrayList<Step> step = route.getListStep();
                            points = new ArrayList<LatLng>();
                            lineOptions = new PolylineOptions();
                            lineOptions.geodesic(true);

                            for (int i = 0; i < step.size(); i++) {

                                List<LatLng> path = step.get(i).getListPoints();
                                // System.out.println("step =====> " + i + " and "
                                // + path.size());
                                points.addAll(path);
                            }
                            activity.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    if (polyLine != null)
                                        polyLine.remove();
                                    lineOptions.addAll(points);
                                    lineOptions.width(15);
                                    lineOptions.geodesic(true);
                                    lineOptions.color(getResources().getColor(
                                            R.color.skyblue));
                                    polyLine = map.addPolyline(lineOptions);
                                    LatLngBounds.Builder bld = new LatLngBounds.Builder();
                                    bld.include(myMarker.getPosition());
                                    bld.include(markerDriver.getPosition());
                                    LatLngBounds latLngBounds = bld.build();
                                    map.moveCamera(CameraUpdateFactory
                                            .newLatLngBounds(latLngBounds, 50));
                                    // tvDist.setText(route.getDistanceText());
                                    // tvTime.setText(route.getDurationText());
                                    // tvDist.setText(0 + " KM");
                                    // tvTime.setText(0 + " MINS");
                                }
                            });
                        }
                    }).start();
                }
            case Const.ServiceCode.DRAW_PATH_ROAD:
                if (!TextUtils.isEmpty(response)) {
                    routeDest = new Route();
                    activity.pContent.parseRoute(response, routeDest);

                    final ArrayList<Step> step = routeDest.getListStep();
                    System.out.println("step size=====> " + step.size());
                    pointsDest = new ArrayList<LatLng>();
                    lineOptionsDest = new PolylineOptions();
                    lineOptionsDest.geodesic(true);

                    for (int i = 0; i < step.size(); i++) {
                        List<LatLng> path = step.get(i).getListPoints();
                        System.out.println("step =====> " + i + " and "
                                + path.size());
                        pointsDest.addAll(path);
                    }
                    if (pointsDest != null && pointsDest.size() > 0) {
                        drawPath(myMarker.getPosition(),
                                preference.getClientDestination());
                    }
                }
                break;
            case Const.ServiceCode.DRAW_PATH:
                if (!TextUtils.isEmpty(response)) {
                    routeDest = new Route();
                    activity.pContent.parseRoute(response, routeDest);

                    final ArrayList<Step> step = routeDest.getListStep();
                    System.out.println("step size=====> " + step.size());
                    pointsDest = new ArrayList<LatLng>();
                    lineOptionsDest = new PolylineOptions();
                    lineOptionsDest.geodesic(true);

                    for (int i = 0; i < step.size(); i++) {
                        List<LatLng> path = step.get(i).getListPoints();
                        System.out.println("step =====> " + i + " and "
                                + path.size());
                        pointsDest.addAll(path);
                    }
                    if (polyLineDest != null)
                        polyLineDest.remove();
                    lineOptionsDest.addAll(pointsDest);
                    lineOptionsDest.width(15);
                    lineOptionsDest.color(getResources().getColor(
                            R.color.color_path)); // #00008B rgb(0,0,139)
                    try {
                        if (lineOptionsDest != null && map != null) {
                            polyLineDest = map.addPolyline(lineOptionsDest);
                            boundLatLang();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case Const.ServiceCode.GET_REQUEST_LOCATION:
                if (activity.pContent.isSuccess(response)) {
                    DriverLocation driverLocation = activity.pContent
                            .getDriverLocation(response);
                    if (driverLocation == null || !this.isVisible()
                            || driverLocation.getLatLng() == null)
                        return;
                    setDriverMarker(driverLocation.getLatLng(),
                            driverLocation.getBearing());

                    if (isTripStarted) {

                        drawTrip(driverLocation.getLatLng());

                        long startTime = Const.NO_TIME;
                        if (activity.pHelper.getRequestTime() == Const.NO_TIME) {
                            startTime = System.currentTimeMillis();
                            activity.pHelper.putRequestTime(startTime);
                        } else {
                            startTime = activity.pHelper.getRequestTime();
                        }

                        try {
                            // distance = Double.parseDouble(driverLocation
                            // .getDistance());
                        } catch (NumberFormatException e) {
                            // DecimalFormat decimalFormat=new
                            // DecimalFormat("#.##");
                            // DecimalFormatSymbols symbol=new
                            // DecimalFormatSymbols();
                            // symbol.setDecimalSeparator('.');
                            // decimalFormat.setDecimalFormatSymbols(symbol);
                            // distance=Double.parseDouble(decimalFormat.format(driverLocation
                            // .getDistance()));
                            // distance =
                            // Double.parseDouble(AndyUtils.comaReplaceWithDot(driverLocation.getDistance()));
                        }
                        // distance = distance / 1625;
                        // tvDist.setText(new DecimalFormat("0.00").format(distance)
                        // + " " + driverLocation.getUnit());
                        // tvDist.setText(new DecimalFormat("0.00").format(distance)
                        // + " " + driverLocation.getUnit());
                        long elapsedTime = System.currentTimeMillis() - startTime;
                        lastTime = elapsedTime / (1000 * 60) + " "
                                + getResources().getString(R.string.text_mins);
                        // tvTime.setText(lastTime);
                        // tvTime.setText("0" + " MINS");
                        // tvDist.setText("0" + " KM");
                    } else {
                        // tvDist.setText(0 + " " + driverLocation.getUnit());
                    }
                }
                isContinueDriverRequest = true;
                // setMarker(latLng);
                break;
            case Const.ServiceCode.GET_REQUEST_STATUS:
                if (activity.pContent.isSuccess(response)) {
                    switch (activity.pContent.checkRequestStatus(response)) {
                        case Const.IS_WALK_STARTED:
                            tvStatus.setText(Html
                                    .fromHtml(getString(R.string.text_driver_arrvied)));
                            // showNotificationPopUp(getString(R.string.text_driver_arrvied));
                            changeNotificationPopUpUI(3);
                            isContinueStatusRequest = true;
                            isTripStarted = false;
                            break;
                        case Const.IS_COMPLETED:
                            btnCancelTrip.setVisibility(View.GONE);
                            tvStatus.setText(Html
                                    .fromHtml(getString(R.string.text_trip_started)));
                            // showNotificationPopUp(getString(R.string.text_trip_started));
                            changeNotificationPopUpUI(4);
                            if (!isAllLocationReceived) {
                                isAllLocationReceived = true;
                                getPath(String.valueOf(activity.pHelper.getRequestId()));
                            }
                            isContinueStatusRequest = true;
                            isTripStarted = true;
                            preference.putIsTripStarted(true);
                            break;
                        case Const.IS_WALKER_ARRIVED:
                            tvStatus.setText(Html
                                    .fromHtml(getString(R.string.text_driver_started)));
                            // showNotificationPopUp(getString(R.string.text_driver_started));
                            changeNotificationPopUpUI(2);
                            isContinueStatusRequest = true;
                            break;
                        case Const.IS_WALKER_STARTED:
                            tvStatus.setText(Html
                                    .fromHtml(getString(R.string.text_job_accepted)));
                            // showNotificationPopUp(getString(R.string.text_job_accepted));
                            changeNotificationPopUpUI(1);
                            isContinueStatusRequest = true;
                            break;
                        case Const.IS_WALKER_RATED:
                            stopCheckingStatusUpdate();
                            isTripStarted = false;
                            if (notificationWindow.isShowing())
                                notificationWindow.dismiss();
                            if (driverStatusWindow.isShowing())
                                driverStatusWindow.dismiss();
                            driver = activity.pContent.getDriverDetail(response);
                            driver.setLastDistance(lastDistance);
                            driver.setLastTime(lastTime);
                            activity.gotoRateFragment(driver);
                            break;

                        default:

                            break;
                    }

                } else {
                    isContinueStatusRequest = true;
                }
                break;
            case Const.ServiceCode.GET_PATH:
                AndyUtils.removeCustomProgressDialog();
                activity.pContent.parsePathRequest(response, points);
                initPreviousDrawPath();
                // AppLog.Log(Const.TAG, "Path====>" + response + "");
                break;
            case Const.ServiceCode.GET_DURATION:
                // pBar.setVisibility(View.GONE);
                // AppLog.Log("UberTripFragment", "Duration Response : " +
                // response);

                if (!TextUtils.isEmpty(response)) {
                    try {
                        String[] durationArr = activity.pContent
                                .parseNearestDriverDurationString(response).split(
                                        " ");
                        if (durationArr != null) {
                            tvEstimatedTime.setText(durationArr[0]);
                            tvDurationUnit.setText(durationArr[1]);
                            myMarker.setIcon(BitmapDescriptorFactory
                                    .fromResource(R.drawable.pin_client_org));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // tvDurationUnit.setText(durationArr[1]);
                }
                break;
            case Const.ServiceCode.PAYMENT_TYPE:
                AndyUtils.removeCustomProgressDialog();
                // AppLog.Log("UberTripFragment", "Payment type reponse : " +
                // response);
                if (activity.pContent.isSuccess(response)) {
                    if (preference.getPaymentMode() == Const.CASH) {
                        imgSelectedCash.setVisibility(View.GONE);
                        imgSelectedCard.setVisibility(View.VISIBLE);
                        preference.putPaymentMode(Const.CREDIT);
                    } else {
                        imgSelectedCash.setVisibility(View.VISIBLE);
                        imgSelectedCard.setVisibility(View.GONE);
                        preference.putPaymentMode(Const.CASH);
                    }

                }
                break;
            case Const.ServiceCode.CANCEL_REQUEST:
                Log.d("", "cancel request response: " + response);
                if (activity.pContent.isSuccess(response)) {

                }
                activity.pHelper.clearRequestData();
                stopCheckingStatusUpdate();
                stopUpdateDriverLoaction();
                AndyUtils.removeCustomProgressDialog();
                activity.gotoMapFragment();
                break;
            case Const.ServiceCode.SET_DESTINATION:
                AndyUtils.removeCustomProgressDialog();
                AppLog.Log("Trip", "Destination Response : " + response);
                if (activity.pContent.isSuccess(response)) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // setMarkerOnRoad(destLatlng, destLatlng);
                            drawPath(myMarker.getPosition(),
                                    preference.getClientDestination());
                        }
                    });
                }
                break;
            case Const.ServiceCode.APPLY_PROMO:
                // llErrorMsg.setVisibility(View.VISIBLE);
                AndyUtils.removeCustomProgressDialog();
                if (activity.pContent.isSuccess(response)) {
                    activity.pHelper
                            .putPromoCode(ibApplyPromo.getText().toString());
                    // tvPromoResult.setText(activity.pContent.getMessage(response));
                    // ivPromoResult.setSelected(true);
                    // tvPromoResult.setSelected(true);
                    // btnPromoSkip.setText(getString(R.string.text_done));
                    // btnPromoSubmit.setEnabled(false);
                    ibApplyPromo.setEnabled(false);

                    // } else {
                    // //
                    // tvPromoResult.setText(activity.pContent.getMessage(response));
                    // // ivPromoResult.setSelected(false);
                    // // tvPromoResult.setSelected(false);
                } else {
                    ibApplyPromo.setText("");
                    ibApplyPromo.setEnabled(true);
                }
                break;
        }
    }

    private void changeNotificationPopUpUI(int i) {
        switch (i) {
            case 1:
                ivJobAccepted.setImageResource(R.drawable.checkbox);
                tvJobAccepted.setTextColor(getResources().getColor(R.color.black));
                break;
            case 2:
                ivJobAccepted.setImageResource(R.drawable.checkbox);
                tvJobAccepted.setTextColor(getResources().getColor(R.color.black));
                ivDriverStarted.setImageResource(R.drawable.checkbox);
                tvDriverStarted
                        .setTextColor(getResources().getColor(R.color.black));
                break;
            case 3:
                ivJobAccepted.setImageResource(R.drawable.checkbox);
                tvJobAccepted.setTextColor(getResources().getColor(R.color.black));
                ivDriverStarted.setImageResource(R.drawable.checkbox);
                tvDriverStarted
                        .setTextColor(getResources().getColor(R.color.black));
                ivDriverArrvied.setImageResource(R.drawable.checkbox);
                tvDriverArrvied
                        .setTextColor(getResources().getColor(R.color.black));
                break;
            case 4:
                ivJobAccepted.setImageResource(R.drawable.checkbox);
                tvJobAccepted.setTextColor(getResources().getColor(R.color.black));
                ivDriverStarted.setImageResource(R.drawable.checkbox);
                tvDriverStarted
                        .setTextColor(getResources().getColor(R.color.black));
                ivDriverArrvied.setImageResource(R.drawable.checkbox);
                tvDriverArrvied
                        .setTextColor(getResources().getColor(R.color.black));
                ivTripStarted.setImageResource(R.drawable.checkbox);
                tvTripStarted.setTextColor(getResources().getColor(R.color.black));
                break;

            default:
                break;
        }
    }

    @Override
    protected boolean isValidate() {
        return false;
    }

    private void getDriverLocation() {

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL,
                Const.ServiceType.GET_REQUEST_LOCATION + Const.Params.ID + "="
                        + new PreferenceHelper(activity).getUserId() + "&"
                        + Const.Params.TOKEN + "="
                        + new PreferenceHelper(activity).getSessionToken()
                        + "&" + Const.Params.REQUEST_ID + "="
                        + new PreferenceHelper(activity).getRequestId());
        AppLog.Log("TAG",
                Const.ServiceType.GET_REQUEST_LOCATION + Const.Params.ID + "="
                        + new PreferenceHelper(activity).getUserId() + "&"
                        + Const.Params.TOKEN + "="
                        + new PreferenceHelper(activity).getSessionToken()
                        + "&" + Const.Params.REQUEST_ID + "="
                        + new PreferenceHelper(activity).getRequestId());
        new HttpRequester(activity, map,
                Const.ServiceCode.GET_REQUEST_LOCATION, true, this);
        // requestQueue.add(new VolleyHttpRequest(Method.GET, map,
        // Const.ServiceCode.GET_REQUEST_LOCATION, this, this));
    }

    private void setMarker(LatLng latLng) {
        if (latLng != null) {
            if (map != null && this.isVisible()) {
                if (myMarker == null) {
                    // markerLayout = ((LayoutInflater) getActivity()
                    // .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    // .inflate(R.layout.custom_marker_layout, null);
                    // tvEstimatedTime = (TextView) markerLayout
                    // .findViewById(R.id.num_txt);
                    // tvDurationUnit = (TextView) markerLayout
                    // .findViewById(R.id.tvDurationUnit);
                    // pBar = (ProgressBar)
                    // markerLayout.findViewById(R.id.pBar);

                    MarkerOptions opt = new MarkerOptions();
                    opt.position(latLng);
                    opt.icon(BitmapDescriptorFactory
                            .fromResource(R.drawable.pin_client_org));
                    opt.title(getString(R.string.text_my_location));
                    myMarker = map.addMarker(opt);
                    // animateCameraToMarkerWithZoom(latLng);
                } else {
                    myMarker.setPosition(latLng);
                }
                drawPath(myMarker.getPosition(),
                        preference.getClientDestination());
            }
        }
    }

    private void setDriverMarker(LatLng latLng, double bearing) {
        if (latLng != null) {
            if (map != null && this.isVisible()) {
                if (markerDriver == null) {
                    MarkerOptions opt = new MarkerOptions();
                    opt.flat(true);
                    opt.anchor(0.5f, 0.5f);
                    opt.position(latLng);
                    opt.icon(BitmapDescriptorFactory
                            .fromResource(R.drawable.pin_driver));
                    opt.title(getString(R.string.text_drive_location));
                    markerDriver = map.addMarker(opt);
                } else {
                    Location driverLocation = new Location("");
                    driverLocation.setLatitude(latLng.latitude);
                    driverLocation.setLongitude(latLng.longitude);
                    driverLocation.setBearing((float) bearing);
                    animateMarker(markerDriver, latLng, driverLocation, false);
                    if (isCameraZoom) {
                        animateCameraToMarker(latLng);
                    }
                }
                if (myMarker != null && myMarker.getPosition() != null)
                    getDirectionsUrl(latLng, myMarker.getPosition());
            }
        }
    }

    private void setDestinationMarker(LatLng latLng) {
        if (latLng != null) {
            if (map != null && this.isVisible()) {
                if (markerDestination == null) {
                    MarkerOptions opt = new MarkerOptions();
                    opt.position(latLng);
                    opt.icon(BitmapDescriptorFactory
                            .fromResource(R.drawable.destination_pin));
                    opt.title(getString(R.string.text_destination_pin_title));
                    markerDestination = map.addMarker(opt);
                } else {
                    markerDestination.setPosition(latLng);
                }
            }
        }
    }

    private void startUpdateDriverLocation() {
        isContinueDriverRequest = true;
        timerDriverLocation = new Timer();
        timerDriverLocation.scheduleAtFixedRate(new TrackLocation(), 0,
                LOCATION_SCHEDULE);
    }

    private void stopUpdateDriverLoaction() {
        isContinueDriverRequest = false;
        if (timerDriverLocation != null) {
            timerDriverLocation.cancel();
            timerDriverLocation = null;
        }
    }

    private void animateCameraToMarkerWithZoom(LatLng latLng) {
        CameraUpdate cameraUpdate = null;
        cameraUpdate = CameraUpdateFactory
                .newLatLngZoom(latLng, Const.MAP_ZOOM);
        map.animateCamera(cameraUpdate);
        if (map != null) {
            map.moveCamera(cameraUpdate);
            isCameraZoom = true;
        }
    }

    private void animateCameraToMarker(LatLng latLng) {
        CameraUpdate cameraUpdate = null;
        cameraUpdate = CameraUpdateFactory.newLatLng(latLng);
        map.animateCamera(cameraUpdate);
    }

    private void startCheckingStatusUpdate() {
        stopCheckingStatusUpdate();
        if (activity.pHelper.getRequestId() != Const.NO_REQUEST) {
            isContinueStatusRequest = true;
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerRequestStatus(), Const.DELAY,
                    Const.TIME_SCHEDULE);
        }
    }

    // private String convertKmFromMeters(float disatanceInMeters) {
    // return new DecimalFormat("0.0").format(0.001f * disatanceInMeters);
    // }

    private void stopCheckingStatusUpdate() {
        isContinueStatusRequest = false;
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
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

    private void getPath(String requestId) {
        AndyUtils.showCustomProgressDialog(activity,
                getString(R.string.progress_loading), false, null);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL,
                Const.ServiceType.GET_PATH + Const.Params.ID + "="
                        + new PreferenceHelper(activity).getUserId() + "&"
                        + Const.Params.TOKEN + "="
                        + new PreferenceHelper(activity).getSessionToken()
                        + "&" + Const.Params.REQUEST_ID + "=" + requestId);
        new HttpRequester(activity, map, Const.ServiceCode.GET_PATH, true, this);
        // requestQueue.add(new VolleyHttpRequest(Method.GET, map,
        // Const.ServiceCode.GET_PATH, this, this));
    }

    private void drawTrip(LatLng latlng) {
        if (map != null && this.isVisible()) {
            points.add(latlng);
            lineOptions = new PolylineOptions();
            lineOptions.addAll(points);
            lineOptions.width(15);
            lineOptions.geodesic(true);
            lineOptions.color(getResources().getColor(R.color.skyblue));
            map.addPolyline(lineOptions);
        }
    }

    private void initPreviousDrawPath() {
        lineOptions = new PolylineOptions();
        lineOptions.addAll(points);
        lineOptions.width(15);
        lineOptions.geodesic(true);
        lineOptions.color(getResources().getColor(R.color.skyblue));
        if (map != null && this.isVisible())
            map.addPolyline(lineOptions);
        points.clear();
    }

    // private void setRequestTime(long time) {
    // activity.pHelper.putRequestTime(time);
    // }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();

        if (notificationDialog != null) {
            notificationDialog.dismiss();
        }
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(
                walkerReceiver);
        if (notificationWindow.isShowing())
            notificationWindow.dismiss();
        if (driverStatusWindow.isShowing())
            driverStatusWindow.dismiss();
        ubregisterCardReceiver();
    }

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

    private void getDirectionsUrl(LatLng origin, LatLng destination) {
        if (!AndyUtils.isNetworkAvailable(activity)) {
            AndyUtils.showToast(getResources().getString(R.string.no_internet),
                    activity);
            return;
        } else if (origin == null) {
            return;
        }
        String str_origin = "origin=" + origin.latitude + ","
                + origin.longitude;
        String str_dest = "destination=" + destination.latitude + ","
                + destination.longitude;
        String sensor = "sensor=false";
        String parameters = str_origin + "&" + str_dest + "&" + sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + parameters;

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL, url);
        new HttpRequester(activity, map, Const.ServiceCode.GET_DURATION, this);
        // requestQueue.add(new VolleyHttpRequest(Method.POST, map,
        // Const.ServiceCode.GET_DURATION, this, this));
    }

    private void setPaymentMode(int type) {
        if (!AndyUtils.isNetworkAvailable(activity)) {
            AndyUtils.showToast(getResources().getString(R.string.no_internet),
                    activity);
            return;
        }
        AndyUtils.showCustomProgressDialog(activity,
                getString(R.string.text_changing_payment_mode), true, null);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL, Const.ServiceType.PAYMENT_TYPE);
        map.put(Const.Params.ID, String.valueOf(activity.pHelper.getUserId()));
        map.put(Const.Params.TOKEN,
                String.valueOf(activity.pHelper.getSessionToken()));
        map.put(Const.Params.REQUEST_ID,
                String.valueOf(activity.pHelper.getRequestId()));
        map.put(Const.Params.CASH_OR_CARD, String.valueOf(type));

        new HttpRequester(activity, map, Const.ServiceCode.PAYMENT_TYPE, this);
        // requestQueue.add(new VolleyHttpRequest(Method.POST, map,
        // Const.ServiceCode.PAYMENT_TYPE, this, this));
    }

    // perfect function...
    // private void animateMarker(final Marker marker, final LatLng toPosition,
    // final Location toLocation, final boolean hideMarker) {
    // if (map == null || !this.isVisible()) {
    // return;
    // }
    // final Handler handler = new Handler();
    // final long start = SystemClock.uptimeMillis();
    // Projection proj = map.getProjection();
    // Point startPoint = proj.toScreenLocation(marker.getPosition());
    // final LatLng startLatLng = proj.fromScreenLocation(startPoint);
    // final double startRotation = marker.getRotation();
    // final long duration = 500;
    //
    // final Interpolator interpolator = new LinearInterpolator();
    // handler.post(new Runnable() {
    // @Override
    // public void run() {
    // long elapsed = SystemClock.uptimeMillis() - start;
    // float t = interpolator.getInterpolation((float) elapsed
    // / duration);
    // double lng = t * toPosition.longitude + (1 - t)
    // * startLatLng.longitude;
    // double lat = t * toPosition.latitude + (1 - t)
    // * startLatLng.latitude;
    // marker.setPosition(new LatLng(lat, lng));
    // float rotation = (float) (t * toLocation.getBearing() + (1 - t)
    // * startRotation);
    // if (rotation != 0) {
    // marker.setRotation(rotation);
    // }
    // if (t < 1.0) {
    // // Post again 16ms later.
    // handler.postDelayed(this, 16);
    // } else {
    // if (hideMarker) {
    // marker.setVisible(false);
    // } else {
    // marker.setVisible(true);
    // }
    // }
    // }
    // });
    // }

    private void registerCardReceiver() {
        IntentFilter intentFilter = new IntentFilter("card_change_receiver");
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // setDefaultCardDetails();
                AppLog.Log("TripFragment", "Card change Receiver");
            }
        };
        getActivity().registerReceiver(mReceiver, intentFilter);
    }

    private void ubregisterCardReceiver() {
        if (mReceiver != null) {
            getActivity().unregisterReceiver(mReceiver);
        }
    }

    private void cancleRequest(String reason4) {
        if (!AndyUtils.isNetworkAvailable(activity)) {
            AndyUtils.showToast(getResources().getString(R.string.no_internet),
                    activity);
            return;
        }
        // AppLog.Log("UberTripFragment",
        // "Request ID : " + activity.pHelper.getRequestId());
        AndyUtils.showCustomProgressDialog(activity,
                getString(R.string.text_canceling_request), true, null);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL, Const.ServiceType.CANCEL_REQUEST);
        map.put(Const.Params.ID, String.valueOf(activity.pHelper.getUserId()));
        map.put(Const.Params.TOKEN,
                String.valueOf(activity.pHelper.getSessionToken()));
        map.put(Const.Params.REQUEST_ID,
                String.valueOf(activity.pHelper.getRequestId()));
        map.put(Const.Params.CANCEL_REASON, reason4);
        new HttpRequester(activity, map, Const.ServiceCode.CANCEL_REQUEST, this);
        // requestQueue.add(new VolleyHttpRequest(Method.POST, map,
        // Const.ServiceCode.CANCEL_REQUEST, this, this));
    }

    // private void setDefaultCardDetails() {
    // if (preference.getDefaultCard() == 0) {
    // layoutCard.setVisibility(View.INVISIBLE);
    // } else {
    // layoutCard.setVisibility(View.VISIBLE);
    // tvCardNo.setText("*****" + preference.getDefaultCardNo());
    // String type = preference.getDefaultCardType();
    // if (type.equalsIgnoreCase(Const.VISA)) {
    // ivCard.setImageResource(R.drawable.ub__creditcard_visa);
    // } else if (type.equalsIgnoreCase(Const.MASTERCARD)) {
    // ivCard.setImageResource(R.drawable.ub__creditcard_mastercard);
    // } else if (type.equalsIgnoreCase(Const.AMERICAN_EXPRESS)) {
    // ivCard.setImageResource(R.drawable.ub__creditcard_amex);
    // } else if (type.equalsIgnoreCase(Const.DISCOVER)) {
    // ivCard.setImageResource(R.drawable.ub__creditcard_discover);
    // } else if (type.equalsIgnoreCase(Const.DINERS_CLUB)) {
    // ivCard.setImageResource(R.drawable.ub__creditcard_discover);
    // } else {
    // ivCard.setImageResource(R.drawable.ub__nav_payment);
    // }
    // }
    // }

    private void setDestination(LatLng destination, String destAddrs) {
        if (!AndyUtils.isNetworkAvailable(activity)) {
            AndyUtils.showToast(getResources().getString(R.string.no_internet),
                    activity);
            return;
        }
        if (destination != null) {
            AndyUtils.showCustomProgressDialog(activity,
                    getString(R.string.text_adding_dest), true, null);
            getAddressFromLocation(preference.getClientDestination());
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(Const.URL, Const.ServiceType.SET_DESTINATION);
            map.put(Const.Params.ID,
                    String.valueOf(activity.pHelper.getUserId()));
            map.put(Const.Params.TOKEN,
                    String.valueOf(activity.pHelper.getSessionToken()));
            map.put(Const.Params.REQUEST_ID,
                    String.valueOf(activity.pHelper.getRequestId()));
            map.put(Const.Params.DEST_LAT, String.valueOf(destination.latitude));
            map.put(Const.Params.DEST_LNG,
                    String.valueOf(destination.longitude));
            map.put(Const.Params.DEST_ADDRESS, destAddrs);
            new HttpRequester(activity, map, Const.ServiceCode.SET_DESTINATION,
                    this);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.automated.taxinow.utils.LocationHelper.OnLocationReceived#
     * onLocationReceived(com.google.android.gms.maps.model.LatLng)
     */
    @Override
    public void onLocationReceived(LatLng latlong) {
        if (isTripStarted && isAllLocationReceived) {
            // drawTrip(latlong);
            myLocation.setLatitude(latlong.latitude);
            myLocation.setLongitude(latlong.longitude);
            if (!isTripStarted)
                setMarker(latlong);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.automated.taxinow.utils.LocationHelper.OnLocationReceived#
     * onLocationReceived(android.location.Location)
     */
    @Override
    public void onLocationReceived(Location location) {

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
            myLatLng = new LatLng(location.getLatitude(),
                    location.getLongitude());
            if (TripFragment.this.isVisible())
                setMarkers(myLatLng);
        }
    }

    class TrackLocation extends TimerTask {
        public void run() {
            if (isContinueDriverRequest) {
                isContinueDriverRequest = false;
                getDriverLocation();
            }
        }
    }

    private class TimerRequestStatus extends TimerTask {
        @Override
        public void run() {
            if (isContinueStatusRequest) {
                isContinueStatusRequest = false;
                getRequestStatus(String
                        .valueOf(activity.pHelper.getRequestId()));
            }
        }
    }

    class WalkerStatusReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            String response = intent.getStringExtra(Const.EXTRA_WALKER_STATUS);
            AppLog.Log("Response ---- Trip", response);
            if (TextUtils.isEmpty(response))
                return;
            stopCheckingStatusUpdate();

            if (activity.pContent.isSuccess(response)) {
                switch (activity.pContent.checkRequestStatus(response)) {
                    case Const.IS_WALK_STARTED:
                        tvStatus.setText(Html
                                .fromHtml(getString(R.string.text_driver_arrvied)));
                        showNotificationDialog(getString(R.string.text_driver_arrvied));
                        changeNotificationPopUpUI(3);
                        isContinueStatusRequest = true;
                        isTripStarted = false;
                        break;
                    case Const.IS_COMPLETED:
                        tvStatus.setText(Html
                                .fromHtml(getString(R.string.text_trip_started)));
                        showNotificationDialog(getString(R.string.text_trip_started));
                        changeNotificationPopUpUI(4);
                        if (!isAllLocationReceived) {
                            isAllLocationReceived = true;
                            getPath(String.valueOf(activity.pHelper.getRequestId()));
                        }
                        btnCancelTrip.setVisibility(View.GONE);
                        isContinueStatusRequest = true;
                        isTripStarted = true;
                        preference.putIsTripStarted(true);
                        break;
                    case Const.IS_WALKER_ARRIVED:
                        tvStatus.setText(Html
                                .fromHtml(getString(R.string.text_driver_started)));
                        showNotificationDialog(getString(R.string.text_driver_started));
                        changeNotificationPopUpUI(2);
                        isContinueStatusRequest = true;
                        break;
                    case Const.IS_WALKER_STARTED:
                        tvStatus.setText(Html
                                .fromHtml(getString(R.string.text_job_accepted)));
                        showNotificationDialog(getString(R.string.text_job_accepted));
                        changeNotificationPopUpUI(1);
                        isContinueStatusRequest = true;
                        break;
                    case Const.IS_WALKER_RATED:
                        stopCheckingStatusUpdate();
                        isTripStarted = false;
                        if (notificationWindow.isShowing())
                            notificationWindow.dismiss();
                        if (driverStatusWindow.isShowing())
                            driverStatusWindow.dismiss();
                        driver = activity.pContent.getDriverDetail(response);
                        driver.setLastDistance(lastDistance);
                        driver.setLastTime(lastTime);
                        activity.gotoRateFragment(driver);
                        break;
                    default:
                        break;
                }
            } else {
                isContinueStatusRequest = true;
            }
            startCheckingStatusUpdate();
        }
    }
}
